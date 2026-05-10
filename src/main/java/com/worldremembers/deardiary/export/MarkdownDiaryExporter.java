package com.worldremembers.deardiary.export;

import com.worldremembers.deardiary.DearDiaryServices;
import com.worldremembers.deardiary.data.DiaryEntry;
import com.worldremembers.deardiary.data.DiaryEntryKind;
import com.worldremembers.deardiary.data.DiaryEntryMarkers;
import com.worldremembers.deardiary.data.PlayerDiary;
import com.worldremembers.deardiary.localization.DiaryLocalization;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.server.network.ServerPlayerEntity;

public final class MarkdownDiaryExporter {
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z");
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String UNTITLED_FALLBACK = "Untitled note";

    private MarkdownDiaryExporter() {
    }

    public static ExportResult export(ServerPlayerEntity player, PlayerDiary diary) throws IOException {
        ZoneId zoneId = DearDiaryServices.config().sharedMemoryZoneId();
        List<DiaryEntry> entries = diary.entriesView().stream()
                .sorted(Comparator.comparing(DiaryEntry::getCreatedAt))
                .toList();
        Path output = uniqueExportPath(player, zoneId);
        String markdown = buildMarkdown(player, entries, zoneId);
        Files.writeString(output, markdown, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        return new ExportResult(output, relativeExportPath(output, player.getUuidAsString()), entries.size());
    }

    private static Path uniqueExportPath(ServerPlayerEntity player, ZoneId zoneId) throws IOException {
        Path playerFile = DearDiaryServices.storage().getPlayerFile(player.getUuid()).toAbsolutePath().normalize();
        Path playersDirectory = requireParent(playerFile, "player file");
        Path diaryRoot = requireParent(playersDirectory, "players directory");
        Path exportsRoot = diaryRoot.resolve("exports").toAbsolutePath().normalize();
        Path playerExports = exportsRoot.resolve(player.getUuidAsString()).normalize();
        if (!playerExports.startsWith(exportsRoot)) {
            throw new IOException("Refusing to write outside Dear Diary export directory: " + playerExports);
        }

        Files.createDirectories(playerExports);
        String timestamp = FILE_TIMESTAMP.withZone(zoneId).format(Instant.now());
        for (int attempt = 1; attempt <= 9999; attempt++) {
            String suffix = attempt == 1 ? "" : "-" + attempt;
            Path candidate = playerExports.resolve(timestamp + "-diary" + suffix + ".md").normalize();
            if (!candidate.startsWith(playerExports)) {
                throw new IOException("Refusing unsafe export path: " + candidate);
            }
            if (Files.notExists(candidate)) {
                return candidate;
            }
        }

        throw new IOException("Could not find a free export filename in " + playerExports);
    }

    private static Path requireParent(Path path, String label) throws IOException {
        Path parent = path.getParent();
        if (parent == null) {
            throw new IOException("Cannot resolve parent for " + label + ": " + path);
        }
        return parent;
    }

    private static String relativeExportPath(Path output, String playerUuid) {
        return "data/dear_diary/exports/" + playerUuid + "/" + output.getFileName();
    }

    private static String buildMarkdown(ServerPlayerEntity player, List<DiaryEntry> entries, ZoneId zoneId) {
        String chapterLabel = DiaryLocalization.resolveFor(player, "export.dear_diary.chapter", "Chapter");
        StringBuilder builder = new StringBuilder(Math.max(512, entries.size() * 256));
        builder.append("# Dear Diary Export\n\n");
        builder.append("Player: ").append(escapeInline(player.getName().getString())).append("\n");
        builder.append("Exported: ").append(DISPLAY_DATE_TIME.withZone(zoneId).format(Instant.now())).append("\n");
        builder.append("Entries: ").append(entries.size()).append("\n\n");

        LocalDate currentDate = null;
        for (DiaryEntry entry : entries) {
            LocalDate entryDate = LocalDate.ofInstant(entry.getCreatedAt(), zoneId);
            if (!entryDate.equals(currentDate)) {
                currentDate = entryDate;
                builder.append("## ").append(DISPLAY_DATE.format(entryDate)).append("\n\n");
            }

            appendEntry(builder, entry, zoneId, chapterLabel);
        }

        return builder.toString();
    }

    private static void appendEntry(StringBuilder builder, DiaryEntry entry, ZoneId zoneId, String chapterLabel) {
        if (DiaryEntryMarkers.isChapterEntry(entry)) {
            appendChapter(builder, entry, chapterLabel);
            return;
        }

        builder.append("### ").append(escapeHeading(safeTitle(entry))).append("\n\n");
        builder.append("- Date: ").append(DISPLAY_DATE_TIME.withZone(zoneId).format(entry.getCreatedAt())).append("\n");
        builder.append("- Kind: ").append(entry.getEntryKind() == DiaryEntryKind.MANUAL ? "Manual" : "Automatic").append("\n");
        builder.append("- Source: ").append(escapeInline(sourceName(entry.getSource()))).append("\n");
        builder.append("- Category: ").append(titleCaseEnum(entry.getCategory().name())).append("\n");
        builder.append("- Importance: ").append(titleCaseEnum(entry.getImportance().name())).append("\n");
        if (entry.isFavorite()) {
            builder.append("- Favorite: yes\n");
        }
        if (entry.hasLocation()) {
            builder.append("- Location: ")
                    .append(escapeInline(dimensionName(entry.getDimension())))
                    .append(", ")
                    .append(entry.getX())
                    .append(" ")
                    .append(entry.getY())
                    .append(" ")
                    .append(entry.getZ())
                    .append("\n");
        }
        builder.append("\n");

        String text = safeText(entry).strip();
        if (!text.isEmpty()) {
            String[] lines = text.split("\\R", -1);
            for (String line : lines) {
                builder.append(escapeMarkdownLine(line)).append("\n");
            }
        }
        builder.append("\n");
    }

    private static void appendChapter(StringBuilder builder, DiaryEntry entry, String chapterLabel) {
        builder.append("### ")
                .append(escapeHeading(chapterLabel))
                .append(": ")
                .append(escapeHeading(safeTitle(entry)))
                .append("\n\n");

        String text = safeText(entry).strip();
        if (!text.isEmpty()) {
            String[] lines = text.split("\\R", -1);
            for (String line : lines) {
                builder.append(escapeMarkdownLine(line)).append("\n");
            }
            builder.append("\n");
        }
    }

    private static String safeTitle(DiaryEntry entry) {
        String title = entry.getResolvedTitle();
        return title == null || title.isBlank() ? UNTITLED_FALLBACK : title.strip();
    }

    private static String safeText(DiaryEntry entry) {
        String text = entry.getResolvedText();
        return text == null ? "" : text;
    }

    private static String sourceName(String source) {
        if (source == null || source.isBlank()) {
            return "Unknown";
        }

        return switch (source) {
            case "minecraft" -> "Vanilla";
            case "dear_diary" -> "Dear Diary";
            case "twilightforest" -> "Twilight Forest";
            case "aether" -> "Aether";
            case "deeperdarker" -> "Deeper and Darker";
            case "irons_spellbooks" -> "Iron's Spells";
            case "cataclysm" -> "Cataclysm";
            case "create" -> "Create";
            case "farmersdelight" -> "Farmer's Delight";
            case "waystones" -> "Waystones";
            default -> source;
        };
    }

    private static String dimensionName(String dimension) {
        if (dimension == null || dimension.isBlank()) {
            return "";
        }

        return switch (dimension) {
            case "minecraft:overworld", "overworld" -> "Overworld";
            case "minecraft:the_nether", "the_nether" -> "The Nether";
            case "minecraft:the_end", "the_end" -> "The End";
            default -> titleCaseName(dimension.startsWith("minecraft:") ? dimension.substring("minecraft:".length()) : dimension);
        };
    }

    private static String titleCaseEnum(String value) {
        return titleCaseName(value == null ? "" : value.toLowerCase(Locale.ROOT));
    }

    private static String titleCaseName(String value) {
        String normalized = (value == null ? "" : value).toLowerCase(Locale.ROOT).replace('_', ' ').replace(':', ' ');
        if (normalized.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder(normalized.length());
        boolean capitalizeNext = true;
        for (int index = 0; index < normalized.length(); index++) {
            char character = normalized.charAt(index);
            if (Character.isWhitespace(character)) {
                builder.append(character);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                builder.append(Character.toUpperCase(character));
                capitalizeNext = false;
            } else {
                builder.append(character);
            }
        }
        return builder.toString();
    }

    private static String escapeHeading(String value) {
        return escapeInline(value).replace("\r", " ").replace("\n", " ");
    }

    private static String escapeInline(String value) {
        return escapeMarkdownLine(value == null ? "" : value)
                .replace("\r", " ")
                .replace("\n", " ");
    }

    private static String escapeMarkdownLine(String line) {
        String escaped = (line == null ? "" : line)
                .replace("\\", "\\\\")
                .replace("`", "\\`")
                .replace("[", "\\[")
                .replace("]", "\\]");
        int index = firstNonWhitespaceIndex(escaped);
        if (index >= 0 && isMarkdownLinePrefix(escaped.charAt(index))) {
            escaped = escaped.substring(0, index) + "\\" + escaped.substring(index);
        }
        return escaped;
    }

    private static int firstNonWhitespaceIndex(String value) {
        for (int index = 0; index < value.length(); index++) {
            if (!Character.isWhitespace(value.charAt(index))) {
                return index;
            }
        }
        return -1;
    }

    private static boolean isMarkdownLinePrefix(char character) {
        return character == '#' || character == '-' || character == '+' || character == '*' || character == '>';
    }

    public record ExportResult(Path absolutePath, String relativePath, int entryCount) {
    }
}
