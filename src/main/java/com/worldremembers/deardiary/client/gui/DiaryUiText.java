package com.worldremembers.deardiary.client.gui;

import com.worldremembers.deardiary.data.DiaryEntry;
import com.worldremembers.deardiary.data.DiaryEntryKind;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

final class DiaryUiText {
    private static final ZoneId DISPLAY_ZONE = ZoneId.systemDefault();
    private static final DateTimeFormatter RU_DATE = DateTimeFormatter
            .ofPattern("dd.MM.yyyy, HH:mm")
            .withZone(DISPLAY_ZONE);
    private static final DateTimeFormatter EN_DATE = DateTimeFormatter
            .ofPattern("MMMM d, yyyy, HH:mm", Locale.ENGLISH)
            .withZone(DISPLAY_ZONE);
    private static final DateTimeFormatter RU_SHORT_DATE = DateTimeFormatter
            .ofPattern("dd.MM HH:mm")
            .withZone(DISPLAY_ZONE);
    private static final DateTimeFormatter EN_SHORT_DATE = DateTimeFormatter
            .ofPattern("MM/dd HH:mm")
            .withZone(DISPLAY_ZONE);

    private DiaryUiText() {
    }

    static String formatDate(DiaryEntry entry) {
        return isRussian() ? RU_DATE.format(entry.getCreatedAt()) : EN_DATE.format(entry.getCreatedAt());
    }

    static String formatShortDate(DiaryEntry entry) {
        return isRussian() ? RU_SHORT_DATE.format(entry.getCreatedAt()) : EN_SHORT_DATE.format(entry.getCreatedAt());
    }

    static Text entryKind(DiaryEntry entry) {
        return entry.getEntryKind() == DiaryEntryKind.MANUAL
                ? Text.translatable("screen.dear_diary.entry_kind.manual")
                : Text.translatable("screen.dear_diary.entry_kind.automatic");
    }

    static Text entryKindShort(DiaryEntry entry) {
        return entry.getEntryKind() == DiaryEntryKind.MANUAL
                ? Text.translatable("screen.dear_diary.entry_kind.manual.short")
                : Text.translatable("screen.dear_diary.entry_kind.automatic.short");
    }

    static Text category(DiaryEntry entry) {
        return Text.translatable("screen.dear_diary.category." + entry.getCategory().name().toLowerCase(Locale.ROOT));
    }

    static Text categoryShort(DiaryEntry entry) {
        return Text.translatable("screen.dear_diary.category." + entry.getCategory().name().toLowerCase(Locale.ROOT) + ".short");
    }

    static Text importance(DiaryEntry entry) {
        return Text.translatable("screen.dear_diary.importance." + entry.getImportance().name().toLowerCase(Locale.ROOT));
    }

    static Text importanceShort(DiaryEntry entry) {
        return Text.translatable("screen.dear_diary.importance." + entry.getImportance().name().toLowerCase(Locale.ROOT) + ".short");
    }

    static String location(DiaryEntry entry) {
        if (!entry.hasLocation()) {
            return "";
        }

        return dimension(entry.getDimension()) + " \u2022 " + entry.getX() + ", " + entry.getY() + ", " + entry.getZ();
    }

    static String currentLocation(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) {
            return Text.translatable("screen.dear_diary.location_preview.unavailable").getString();
        }

        String dimension = dimension(client.world.getRegistryKey().getValue().toString());
        return dimension + " \u2022 " + client.player.getBlockX() + ", " + client.player.getBlockY() + ", " + client.player.getBlockZ();
    }

    static String dimension(String dimension) {
        if (dimension == null || dimension.isBlank()) {
            return "";
        }

        return switch (dimension) {
            case "minecraft:overworld", "overworld" -> Text.translatable("screen.dear_diary.dimension.overworld").getString();
            case "minecraft:the_nether", "the_nether" -> Text.translatable("screen.dear_diary.dimension.nether").getString();
            case "minecraft:the_end", "the_end" -> Text.translatable("screen.dear_diary.dimension.end").getString();
            default -> titleCaseName(dimension.startsWith("minecraft:") ? dimension.substring("minecraft:".length()) : dimension);
        };
    }

    private static boolean isRussian() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && "ru_ru".equals(client.getLanguageManager().getLanguage());
    }

    private static String titleCaseName(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).replace('_', ' ').replace(':', ' ');
        if (normalized.isEmpty()) {
            return value;
        }

        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }
}
