package com.worldremembers.deardiary.storage;

import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.DearDiaryServices;
import com.worldremembers.deardiary.data.PlayerDiary;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class DiaryBackupManager {
    private static final int MAX_BACKUPS_PER_PLAYER = 10;
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final Pattern BACKUP_FILE_PATTERN = Pattern.compile("^(\\d{8}-\\d{6})-diary(?:-\\d+)?\\.json$");

    private DiaryBackupManager() {
    }

    public static void backupPlayerDiaryOnLogout(UUID playerUuid) {
        if (playerUuid == null) {
            return;
        }

        try {
            backupPlayerDiary(playerUuid);
        } catch (IllegalStateException exception) {
            DearDiaryMod.LOGGER.debug("Skipping Dear Diary backup for {} because storage is not initialized", playerUuid, exception);
        } catch (IOException exception) {
            DearDiaryMod.LOGGER.warn("Failed to back up Dear Diary file for {}", playerUuid, exception);
        } catch (RuntimeException exception) {
            DearDiaryMod.LOGGER.warn("Unexpected failure while backing up Dear Diary file for {}", playerUuid, exception);
        }
    }

    private static void backupPlayerDiary(UUID playerUuid) throws IOException {
        DiaryStorage storage = DearDiaryServices.storage();
        PlayerDiary diary = storage.getDiary(playerUuid);
        if (diary.entriesView().isEmpty()) {
            DearDiaryMod.LOGGER.debug("Skipping Dear Diary backup for {} because the diary has no entries", playerUuid);
            return;
        }

        storage.save(playerUuid);
        Path source = storage.getPlayerFile(playerUuid).toAbsolutePath().normalize();
        if (Files.notExists(source) || !Files.isRegularFile(source)) {
            DearDiaryMod.LOGGER.warn("Skipping Dear Diary backup for {} because the saved diary file is missing: {}", playerUuid, source);
            return;
        }

        Path playerBackupDirectory = playerBackupDirectory(source, playerUuid);
        Files.createDirectories(playerBackupDirectory);
        Path target = uniqueBackupTarget(playerBackupDirectory);
        Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
        DearDiaryMod.LOGGER.debug("Backed up Dear Diary file for {} to {}", playerUuid, relativeBackupPath(target, playerUuid));
        rotateBackups(playerBackupDirectory);
    }

    private static Path playerBackupDirectory(Path source, UUID playerUuid) throws IOException {
        Path playersDirectory = requireParent(source, "player diary file");
        Path diaryRoot = requireParent(playersDirectory, "players directory");
        Path backupsRoot = diaryRoot.resolve("backups").toAbsolutePath().normalize();
        Path playerBackupDirectory = backupsRoot.resolve(playerUuid.toString()).normalize();
        if (!playerBackupDirectory.startsWith(backupsRoot)) {
            throw new IOException("Refusing to write outside Dear Diary backup directory: " + playerBackupDirectory);
        }
        return playerBackupDirectory;
    }

    private static Path requireParent(Path path, String label) throws IOException {
        Path parent = path.getParent();
        if (parent == null) {
            throw new IOException("Cannot resolve parent for " + label + ": " + path);
        }
        return parent;
    }

    private static Path uniqueBackupTarget(Path playerBackupDirectory) throws IOException {
        String timestamp = FILE_TIMESTAMP.format(LocalDateTime.now());
        for (int attempt = 1; attempt <= 9999; attempt++) {
            String suffix = attempt == 1 ? "" : "-" + attempt;
            Path candidate = playerBackupDirectory.resolve(timestamp + "-diary" + suffix + ".json").normalize();
            if (!candidate.startsWith(playerBackupDirectory)) {
                throw new IOException("Refusing unsafe backup path: " + candidate);
            }
            if (Files.notExists(candidate)) {
                return candidate;
            }
        }

        throw new IOException("Could not find a free backup filename in " + playerBackupDirectory);
    }

    private static void rotateBackups(Path playerBackupDirectory) {
        List<BackupCandidate> backups = backupCandidates(playerBackupDirectory);
        backups.sort(Comparator
                .comparing(BackupCandidate::sortInstant)
                .reversed()
                .thenComparing(candidate -> candidate.path().getFileName().toString(), Comparator.reverseOrder()));

        for (int index = MAX_BACKUPS_PER_PLAYER; index < backups.size(); index++) {
            deleteOldBackup(backups.get(index).path(), playerBackupDirectory);
        }
    }

    private static List<BackupCandidate> backupCandidates(Path playerBackupDirectory) {
        List<BackupCandidate> backups = new ArrayList<>();
        try (Stream<Path> stream = Files.list(playerBackupDirectory)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .map(DiaryBackupManager::backupCandidate)
                    .forEach(backups::add);
        } catch (IOException exception) {
            DearDiaryMod.LOGGER.warn("Failed to list Dear Diary backups in {}", playerBackupDirectory, exception);
        }
        return backups;
    }

    private static BackupCandidate backupCandidate(Path path) {
        String fileName = path.getFileName().toString();
        Matcher matcher = BACKUP_FILE_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            try {
                LocalDateTime timestamp = LocalDateTime.parse(matcher.group(1), FILE_TIMESTAMP);
                return new BackupCandidate(path, timestamp.atZone(ZoneId.systemDefault()).toInstant());
            } catch (DateTimeParseException ignored) {
                // Fall through to last-modified time for malformed legacy names.
            }
        }

        try {
            return new BackupCandidate(path, Files.getLastModifiedTime(path).toInstant());
        } catch (IOException exception) {
            DearDiaryMod.LOGGER.warn("Failed to read Dear Diary backup timestamp for {}", path, exception);
            return new BackupCandidate(path, Instant.EPOCH);
        }
    }

    private static void deleteOldBackup(Path backup, Path playerBackupDirectory) {
        Path safeDirectory = playerBackupDirectory.toAbsolutePath().normalize();
        Path safeBackup = backup.toAbsolutePath().normalize();
        if (!safeBackup.startsWith(safeDirectory)) {
            DearDiaryMod.LOGGER.warn("Refusing to delete Dear Diary backup outside expected directory: {}", safeBackup);
            return;
        }

        try {
            Files.deleteIfExists(safeBackup);
        } catch (IOException exception) {
            DearDiaryMod.LOGGER.warn("Failed to delete old Dear Diary backup {}", safeBackup, exception);
        }
    }

    private static String relativeBackupPath(Path target, UUID playerUuid) {
        return "data/dear_diary/backups/" + playerUuid + "/" + target.getFileName();
    }

    private record BackupCandidate(Path path, Instant sortInstant) {
    }
}
