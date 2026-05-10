package com.worldremembers.deardiary.storage;

import com.google.gson.JsonParseException;
import com.worldremembers.deardiary.data.DiaryEntry;
import com.worldremembers.deardiary.data.DiaryJson;
import com.worldremembers.deardiary.data.PlayerDiary;
import com.worldremembers.deardiary.event.AutomaticEventStateBackfill;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JsonDiaryStorage implements DiaryStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonDiaryStorage.class);

    private final Path playersDirectory;
    private final Map<UUID, PlayerDiary> loadedDiaries = new HashMap<>();

    public JsonDiaryStorage(Path playersDirectory) {
        this.playersDirectory = playersDirectory;
    }

    public void initialize() {
        try {
            Files.createDirectories(playersDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create diary storage directory: " + playersDirectory, exception);
        }
    }

    @Override
    public synchronized PlayerDiary getDiary(UUID playerUuid) {
        return loadedDiaries.computeIfAbsent(playerUuid, this::loadDiary);
    }

    @Override
    public synchronized DiaryEntry addEntry(UUID playerUuid, DiaryEntry entry) {
        PlayerDiary diary = getDiary(playerUuid);
        diary.addEntry(entry);
        save(playerUuid);
        return entry;
    }

    @Override
    public synchronized Optional<DiaryEntry> findEntry(UUID playerUuid, UUID entryId) {
        return getDiary(playerUuid).findEntry(entryId);
    }

    @Override
    public synchronized Optional<DiaryEntry> updateEntryText(UUID playerUuid, UUID entryId, String title, String text) {
        Optional<DiaryEntry> entry = getDiary(playerUuid).updateEntryText(entryId, title, text);
        entry.ifPresent(ignored -> save(playerUuid));
        return entry;
    }

    @Override
    public synchronized boolean deleteEntry(UUID playerUuid, UUID entryId) {
        boolean removed = getDiary(playerUuid).removeEntry(entryId);
        if (removed) {
            save(playerUuid);
        }

        return removed;
    }

    @Override
    public synchronized Optional<DiaryEntry> setFavorite(UUID playerUuid, UUID entryId, boolean favorite) {
        Optional<DiaryEntry> entry = getDiary(playerUuid).setFavorite(entryId, favorite);
        entry.ifPresent(ignored -> save(playerUuid));
        return entry;
    }

    @Override
    public synchronized void clearDiary(UUID playerUuid) {
        PlayerDiary diary = getDiary(playerUuid);
        diary.clear();
        save(playerUuid);
    }

    @Override
    public synchronized void save(UUID playerUuid) {
        PlayerDiary diary = getDiary(playerUuid);
        try {
            writeDiary(diary, getPlayerFile(playerUuid));
        } catch (IOException exception) {
            LOGGER.error("Failed to save diary for {}", playerUuid, exception);
        }
    }

    @Override
    public synchronized void saveAll() {
        for (UUID playerUuid : loadedDiaries.keySet()) {
            save(playerUuid);
        }
    }

    @Override
    public Path getPlayerFile(UUID playerUuid) {
        return playersDirectory.resolve(playerUuid + ".json");
    }

    private PlayerDiary loadDiary(UUID playerUuid) {
        Path file = getPlayerFile(playerUuid);
        if (Files.notExists(file)) {
            return new PlayerDiary(playerUuid);
        }

        try {
            PlayerDiary diary;
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                diary = DiaryJson.GSON.fromJson(reader, PlayerDiary.class);
            }
            diary = diary == null ? new PlayerDiary(playerUuid) : diary;
            diary.normalize(playerUuid);
            if (AutomaticEventStateBackfill.reconcile(diary)) {
                writeDiary(diary, file);
            }
            return diary;
        } catch (IOException | JsonParseException exception) {
            LOGGER.error("Failed to load diary for {}, using an empty in-memory diary", playerUuid, exception);
            return new PlayerDiary(playerUuid);
        }
    }

    private void writeDiary(PlayerDiary diary, Path file) throws IOException {
        Files.createDirectories(playersDirectory);
        Path temporaryFile = file.resolveSibling(file.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8)) {
            DiaryJson.GSON.toJson(diary, writer);
        }

        try {
            Files.move(
                    temporaryFile,
                    file,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
