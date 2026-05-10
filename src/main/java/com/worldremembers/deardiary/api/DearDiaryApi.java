package com.worldremembers.deardiary.api;

import com.google.gson.JsonElement;
import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.DearDiaryServices;
import com.worldremembers.deardiary.config.DearDiaryConfig;
import com.worldremembers.deardiary.data.DiaryCategory;
import com.worldremembers.deardiary.data.DiaryEntry;
import com.worldremembers.deardiary.data.DiaryEntryKind;
import com.worldremembers.deardiary.data.DiaryEntryMarkers;
import com.worldremembers.deardiary.data.DiaryImportance;
import com.worldremembers.deardiary.data.PlayerDiary;
import com.worldremembers.deardiary.localization.DiaryLocalization;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

/**
 * Server-side entrypoint for reading and mutating Dear Diary data.
 *
 * <p>Compatibility mods should use this class only from logical server code.
 * Registered automatic memories should normally be fired through
 * {@link com.worldremembers.deardiary.event.AutomaticDiaryEvents#trigger(ServerPlayerEntity, String)}
 * so Dear Diary can apply config filters and anti-spam state consistently.</p>
 */
public final class DearDiaryApi {
    private static final String UNTITLED_NOTE_FALLBACK = "Untitled note";
    private static final Locale RUSSIAN_LOCALE = Locale.forLanguageTag("ru-RU");
    private static final Locale ENGLISH_LOCALE = Locale.ENGLISH;

    private DearDiaryApi() {
    }

    /**
     * Adds an already-built entry to the player's diary.
     *
     * <p>This is a low-level method. It does not apply automatic event policy
     * checks; prefer {@link #createManualEntry(ServerPlayerEntity, String, String)}
     * for manual notes and {@code AutomaticDiaryEvents.trigger(...)} for
     * registered automatic events.</p>
     */
    public static DiaryEntry addEntry(ServerPlayerEntity player, DiaryEntry entry) {
        return DearDiaryServices.storage().addEntry(player.getUuid(), entry);
    }

    /**
     * Returns the player's diary model from server storage.
     *
     * <p>Treat the returned model as read-only unless you are part of Dear
     * Diary internals. Use API methods for writes so storage remains
     * consistent.</p>
     */
    public static PlayerDiary getDiary(ServerPlayerEntity player) {
        return DearDiaryServices.storage().getDiary(player.getUuid());
    }

    /**
     * Looks up one entry that belongs to the given player.
     */
    public static Optional<DiaryEntry> getEntry(ServerPlayerEntity player, UUID entryId) {
        return DearDiaryServices.storage().findEntry(player.getUuid(), entryId);
    }

    /**
     * Creates a player-authored diary note with the player's current location.
     */
    public static DiaryEntry createManualEntry(ServerPlayerEntity player, String title, String text) {
        return createManualEntry(player, title, text, true);
    }

    /**
     * Creates a player-authored diary note and applies server-side length
     * limits from the Dear Diary config.
     */
    public static DiaryEntry createManualEntry(ServerPlayerEntity player, String title, String text, boolean includeLocation) {
        DearDiaryConfig config = DearDiaryServices.config();
        String resolvedTitle = limit(title, config.maxManualTitleLength(), UNTITLED_NOTE_FALLBACK);
        if (isBlank(text)) {
            throw new IllegalArgumentException("Manual diary text cannot be blank");
        }

        String resolvedText = limit(text, config.maxManualTextLength(), "");

        DiaryEntry.Builder builder = DiaryEntry.builder(DiaryEntryKind.MANUAL, "manual", DearDiaryMod.MOD_ID)
                .category(DiaryCategory.MANUAL)
                .importance(DiaryImportance.NORMAL)
                .resolvedTitle(resolvedTitle)
                .resolvedText(resolvedText)
                .icon("minecraft:writable_book")
                .editable(true)
                .shareable(true);

        if (includeLocation) {
            applyPlayerLocation(builder, player);
        }

        return addEntry(player, builder.build());
    }

    /**
     * Creates a player-authored chapter marker that behaves like a manual
     * timeline divider without adding new storage fields.
     */
    public static DiaryEntry createChapterEntry(ServerPlayerEntity player, String title) {
        return createChapterEntry(player, title, "", true);
    }

    /**
     * Creates a player-authored chapter marker with optional note text.
     */
    public static DiaryEntry createChapterEntry(ServerPlayerEntity player, String title, String text, boolean includeLocation) {
        DearDiaryConfig config = DearDiaryServices.config();
        if (isBlank(title)) {
            throw new IllegalArgumentException("Chapter title cannot be blank");
        }
        if (title.strip().length() > config.maxManualTitleLength()) {
            throw new IllegalArgumentException("Chapter title is too long");
        }

        String resolvedTitle = limit(title, config.maxManualTitleLength(), UNTITLED_NOTE_FALLBACK);
        String resolvedText = limitAllowBlank(text, config.maxManualTextLength());

        DiaryEntry.Builder builder = DiaryEntry.builder(DiaryEntryKind.MANUAL, DiaryEntryMarkers.CHAPTER_EVENT_TYPE, DearDiaryMod.MOD_ID)
                .category(DiaryCategory.OTHER)
                .importance(DiaryImportance.NORMAL)
                .resolvedTitle(resolvedTitle)
                .resolvedText(resolvedText)
                .icon("minecraft:writable_book")
                .editable(true)
                .shareable(false);

        if (includeLocation) {
            applyPlayerLocation(builder, player);
        }
        return addEntry(player, builder.build());
    }

    public static boolean isChapterEntry(DiaryEntry entry) {
        return DiaryEntryMarkers.isChapterEntry(entry);
    }

    /**
     * Creates an automatic diary entry from a prepared request.
     *
     * <p>This method applies server config filters for automatic events and
     * resolves localization keys into stored text. It does not update
     * per-player trigger policy state. Third-party integrations should
     * register an {@code AutomaticEventDefinition} and call
     * {@code AutomaticDiaryEvents.trigger(...)} for normal gameplay events.</p>
     */
    public static Optional<DiaryEntry> createAutomaticEntry(ServerPlayerEntity player, AutomaticEntryRequest request) {
        DearDiaryConfig config = DearDiaryServices.config();
        if (!config.isAutomaticEventAllowed(request.eventType(), request.category(), request.importance())) {
            return Optional.empty();
        }

        String resolvedTitle = DiaryLocalization.resolveFor(player, request.titleKey(), request.resolvedTitle());
        String resolvedText = DiaryLocalization.resolveFor(player, request.textKey(), request.resolvedText());
        DiaryEntry.Builder builder = DiaryEntry.builder(DiaryEntryKind.AUTOMATIC, request.eventType(), request.source())
                .category(request.category())
                .importance(request.importance())
                .titleKey(request.titleKey())
                .textKey(request.textKey())
                .resolvedTitle(resolvedTitle)
                .resolvedText(resolvedText)
                .icon(request.icon())
                .editable(true)
                .shareable(request.shareable());

        for (Map.Entry<String, JsonElement> customEntry : request.customData().entrySet()) {
            builder.customData(customEntry.getKey(), customEntry.getValue());
        }

        if (request.includeLocation()) {
            applyPlayerLocation(builder, player);
        }

        return Optional.of(addEntry(player, builder.build()));
    }

    public static void clearDiary(ServerPlayerEntity player) {
        DearDiaryServices.storage().clearDiary(player.getUuid());
    }

    /**
     * Updates the resolved title and text for one editable entry owned by the
     * player. Automatic metadata such as event id, category, and location is
     * not changed.
     */
    public static boolean editEntryText(ServerPlayerEntity player, UUID entryId, String newTitle, String newText) {
        Optional<DiaryEntry> entry = getEntry(player, entryId);
        if (entry.isEmpty() || !entry.get().isEditable()) {
            return false;
        }

        if (entry.get().getEntryKind() == DiaryEntryKind.MANUAL
                && !isChapterEntry(entry.get())
                && isBlank(newText)) {
            return false;
        }

        DearDiaryConfig config = DearDiaryServices.config();
        String resolvedTitle = limit(newTitle, config.maxManualTitleLength(), UNTITLED_NOTE_FALLBACK);
        String resolvedText = limitAllowBlank(newText, config.maxManualTextLength());
        return DearDiaryServices.storage()
                .updateEntryText(player.getUuid(), entryId, resolvedTitle, resolvedText)
                .isPresent();
    }

    /**
     * Deletes one entry owned by the player.
     */
    public static boolean deleteEntry(ServerPlayerEntity player, UUID entryId) {
        return DearDiaryServices.storage().deleteEntry(player.getUuid(), entryId);
    }

    /**
     * Sets the favorite flag for one entry owned by the player.
     */
    public static boolean setFavorite(ServerPlayerEntity player, UUID entryId, boolean favorite) {
        Optional<DiaryEntry> entry = getEntry(player, entryId);
        if (entry.isEmpty() || isChapterEntry(entry.get())) {
            return false;
        }

        return DearDiaryServices.storage().setFavorite(player.getUuid(), entryId, favorite).isPresent();
    }

    /**
     * Toggles the favorite flag for one entry owned by the player.
     */
    public static boolean toggleFavorite(ServerPlayerEntity player, UUID entryId) {
        Optional<DiaryEntry> entry = getEntry(player, entryId);
        return entry.isPresent() && setFavorite(player, entryId, !entry.get().isFavorite());
    }

    /**
     * Broadcasts a shareable diary entry to server chat using the localized
     * quote-style share format.
     */
    public static boolean shareEntryToChat(ServerPlayerEntity player, UUID entryId) {
        Optional<DiaryEntry> optionalEntry = getEntry(player, entryId);
        if (optionalEntry.isEmpty() || !optionalEntry.get().isShareable()) {
            return false;
        }

        DiaryEntry entry = optionalEntry.get();
        DearDiaryConfig config = DearDiaryServices.config();
        String title = limit(entry.getResolvedTitle(), config.maxManualTitleLength(), UNTITLED_NOTE_FALLBACK);
        String text = limitSharedText(entry.getResolvedText(), config.maxSharedTextLength());
        String formattedDate = formatSharedDate(player, entry, config);

        player.getServer().getPlayerManager().broadcast(
                Text.translatable(
                        "chat.dear_diary.share.header",
                        Text.translatable("chat.dear_diary.share.prefix").formatted(Formatting.GOLD),
                        player.getDisplayName()
                ),
                false
        );
        player.getServer().getPlayerManager().broadcast(
                Text.translatable("chat.dear_diary.share.title", title).formatted(Formatting.GOLD),
                false
        );
        if (entry.hasLocation()) {
            player.getServer().getPlayerManager().broadcast(
                    Text.translatable(
                            "chat.dear_diary.share.meta.location",
                            formattedDate,
                            formatDimension(entry.getDimension()),
                            entry.getX(),
                            entry.getY(),
                            entry.getZ()
                    ).formatted(Formatting.GRAY),
                    false
            );
        } else {
            player.getServer().getPlayerManager().broadcast(
                    Text.translatable("chat.dear_diary.share.meta.no_location", formattedDate).formatted(Formatting.GRAY),
                    false
            );
        }

        if (!text.isBlank()) {
            player.getServer().getPlayerManager().broadcast(Text.literal(""), false);
            for (String line : text.split("\\R", -1)) {
                player.getServer().getPlayerManager().broadcast(Text.literal(line), false);
            }
        }

        return true;
    }

    private static String formatSharedDate(ServerPlayerEntity player, DiaryEntry entry, DearDiaryConfig config) {
        String locale = DiaryLocalization.localeFor(player);
        ZoneId zoneId = config.sharedMemoryZoneId();
        DateTimeFormatter formatter = locale.startsWith("ru")
                ? DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", RUSSIAN_LOCALE)
                : DateTimeFormatter.ofPattern("MMMM d, yyyy, HH:mm", ENGLISH_LOCALE);
        return formatter.withZone(zoneId).format(entry.getCreatedAt());
    }

    private static Text formatDimension(String dimension) {
        if (dimension == null || dimension.isBlank()) {
            return Text.literal("");
        }

        return switch (dimension) {
            case "minecraft:overworld", "overworld" -> Text.translatable("chat.dear_diary.dimension.overworld");
            case "minecraft:the_nether", "the_nether" -> Text.translatable("chat.dear_diary.dimension.nether");
            case "minecraft:the_end", "the_end" -> Text.translatable("chat.dear_diary.dimension.end");
            default -> Text.literal(titleCaseDimension(dimension));
        };
    }

    private static String titleCaseDimension(String dimension) {
        String readable = dimension.startsWith("minecraft:")
                ? dimension.substring("minecraft:".length())
                : dimension;
        String normalized = readable.toLowerCase(Locale.ROOT).replace('_', ' ').replace(':', ' ');
        if (normalized.isEmpty()) {
            return dimension;
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

    private static void applyPlayerLocation(DiaryEntry.Builder builder, ServerPlayerEntity player) {
        BlockPos position = player.getBlockPos();
        String dimension = player.getServerWorld().getRegistryKey().getValue().toString();
        builder.location(dimension, position.getX(), position.getY(), position.getZ());
    }

    private static String limit(String value, int maxLength, String fallback) {
        String normalized = value == null ? "" : value.strip();
        if (normalized.isEmpty()) {
            normalized = fallback;
        }

        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength);
    }

    private static String limitAllowBlank(String value, int maxLength) {
        String normalized = value == null ? "" : value.strip();
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength);
    }

    private static String limitSharedText(String value, int maxLength) {
        String normalized = value == null ? "" : value.strip();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        if (maxLength <= 3) {
            return "...".substring(0, maxLength);
        }
        return normalized.substring(0, maxLength - 3).stripTrailing() + "...";
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
