package com.worldremembers.deardiary.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.worldremembers.deardiary.data.DiaryCategory;
import com.worldremembers.deardiary.data.DiaryImportance;
import com.worldremembers.deardiary.data.DiaryJson;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DearDiaryConfig {
    public static final int CURRENT_VERSION = 2;

    private static final Logger LOGGER = LoggerFactory.getLogger(DearDiaryConfig.class);

    private int version = CURRENT_VERSION;
    private LanguageConfig language = new LanguageConfig();
    private ManualEntriesConfig manualEntries = new ManualEntriesConfig();
    private AutomaticMemoriesConfig automaticMemories = new AutomaticMemoriesConfig();
    private transient List<String> invalidAutomaticCategories = new ArrayList<>();

    public static DearDiaryConfig fromJson(JsonObject root) {
        if (root == null) {
            DearDiaryConfig config = new DearDiaryConfig();
            config.normalize();
            return config;
        }

        DearDiaryConfig config = isLegacyFormat(root)
                ? fromLegacyJson(root)
                : DiaryJson.GSON.fromJson(root, DearDiaryConfig.class);
        if (config == null) {
            config = new DearDiaryConfig();
        }
        config.normalize();
        return config;
    }

    public static boolean isLegacyFormat(JsonObject root) {
        if (root == null) {
            return false;
        }
        if (root.has("automaticMemories") || root.has("manualEntries") || root.has("language") || root.has("version")) {
            return false;
        }

        return root.has("enableAutomaticEntries")
                || root.has("enableOriginEntry")
                || root.has("maxSharedTextLength")
                || root.has("sharedMemoryTimeZone")
                || root.has("maxManualTitleLength")
                || root.has("maxManualTextLength")
                || root.has("minAutomaticImportance")
                || root.has("defaultDiaryLanguage")
                || root.has("enabledAutomaticCategories")
                || root.has("disabledAutomaticCategories")
                || root.has("disabledAutomaticEvents")
                || root.has("_help")
                || root.has("_comment");
    }

    private static DearDiaryConfig fromLegacyJson(JsonObject root) {
        DearDiaryConfig config = new DearDiaryConfig();
        config.version = CURRENT_VERSION;
        config.language.defaultDiaryLanguage = readString(root, "defaultDiaryLanguage", config.language.defaultDiaryLanguage);
        config.manualEntries.maxSharedTextLength = readInt(root, "maxSharedTextLength", config.manualEntries.maxSharedTextLength);
        config.manualEntries.sharedMemoryTimeZone = readString(root, "sharedMemoryTimeZone", config.manualEntries.sharedMemoryTimeZone);
        config.manualEntries.maxTitleLength = readInt(root, "maxManualTitleLength", config.manualEntries.maxTitleLength);
        config.manualEntries.maxTextLength = readInt(root, "maxManualTextLength", config.manualEntries.maxTextLength);
        config.automaticMemories.enabled = readBoolean(root, "enableAutomaticEntries", config.automaticMemories.enabled);
        config.automaticMemories.createOriginEntry = readBoolean(root, "enableOriginEntry", config.automaticMemories.createOriginEntry);
        config.automaticMemories.minimumImportance = readString(root, "minAutomaticImportance", config.automaticMemories.minimumImportance);
        config.automaticMemories.categories = defaultCategoryMap();

        List<String> enabledCategories = readStringList(root, "enabledAutomaticCategories");
        if (!enabledCategories.isEmpty()) {
            config.automaticMemories.categories.replaceAll((key, value) -> false);
            for (String category : enabledCategories) {
                String categoryKey = normalizeCategoryKey(category);
                if (categoryKey != null) {
                    config.automaticMemories.categories.put(categoryKey, true);
                }
            }
        }

        for (String category : readStringList(root, "disabledAutomaticCategories")) {
            String categoryKey = normalizeCategoryKey(category);
            if (categoryKey != null) {
                config.automaticMemories.categories.put(categoryKey, false);
            }
        }
        config.automaticMemories.disabledEvents = readStringList(root, "disabledAutomaticEvents");
        return config;
    }

    public void normalize() {
        version = CURRENT_VERSION;
        if (language == null) {
            language = new LanguageConfig();
        }
        if (manualEntries == null) {
            manualEntries = new ManualEntriesConfig();
        }
        if (automaticMemories == null) {
            automaticMemories = new AutomaticMemoriesConfig();
        }

        manualEntries.maxSharedTextLength = Math.max(1, manualEntries.maxSharedTextLength);
        manualEntries.maxTitleLength = Math.max(1, manualEntries.maxTitleLength);
        manualEntries.maxTextLength = Math.max(1, manualEntries.maxTextLength);
        manualEntries.sharedMemoryTimeZone = normalizeSharedMemoryTimeZone(manualEntries.sharedMemoryTimeZone);
        language.defaultDiaryLanguage = normalizeLanguage(language.defaultDiaryLanguage);
        String configuredImportance = automaticMemories.minimumImportance;
        DiaryImportance parsedImportance = DiaryImportance.parse(
                automaticMemories.minimumImportance,
                DiaryImportance.MINOR
        );
        if (configuredImportance != null
                && !configuredImportance.isBlank()
                && !parsedImportance.name().equals(configuredImportance.strip().toUpperCase(Locale.ROOT))) {
            LOGGER.warn(
                    "Ignoring unknown Dear Diary automatic minimum importance '{}', using {}",
                    configuredImportance,
                    parsedImportance.name()
            );
        }
        automaticMemories.minimumImportance = parsedImportance.name();
        invalidAutomaticCategories = new ArrayList<>();
        automaticMemories.categories = normalizeCategoryMap(automaticMemories.categories);
        automaticMemories.disabledEvents = normalizeEventIds(automaticMemories.disabledEvents);
    }

    public int version() {
        return version;
    }

    public boolean enableAutomaticEntries() {
        return automaticMemories.enabled;
    }

    public boolean enableOriginEntry() {
        return automaticMemories.createOriginEntry;
    }

    public boolean shouldCreateOriginEntry() {
        return automaticMemories.enabled && automaticMemories.createOriginEntry;
    }

    public int maxSharedTextLength() {
        return manualEntries.maxSharedTextLength;
    }

    public int maxManualTitleLength() {
        return manualEntries.maxTitleLength;
    }

    public int maxManualTextLength() {
        return manualEntries.maxTextLength;
    }

    public String sharedMemoryTimeZone() {
        return manualEntries.sharedMemoryTimeZone;
    }

    public ZoneId sharedMemoryZoneId() {
        String configured = manualEntries.sharedMemoryTimeZone;
        if ("SERVER".equalsIgnoreCase(configured)) {
            return ZoneId.systemDefault();
        }
        if ("UTC".equalsIgnoreCase(configured)) {
            return ZoneId.of("UTC");
        }

        try {
            return ZoneId.of(configured);
        } catch (DateTimeException exception) {
            LOGGER.warn("Ignoring invalid Dear Diary shared memory timezone '{}', using SERVER", configured);
            return ZoneId.systemDefault();
        }
    }

    public DiaryImportance minAutomaticImportance() {
        return DiaryImportance.parse(automaticMemories.minimumImportance, DiaryImportance.MINOR);
    }

    public String defaultDiaryLanguage() {
        return language.defaultDiaryLanguage;
    }

    public Map<String, Boolean> automaticCategorySettings() {
        return Map.copyOf(automaticMemories.categories);
    }

    public List<String> enabledAutomaticCategories() {
        return categoryNamesByState(true);
    }

    public List<String> disabledAutomaticCategories() {
        return categoryNamesByState(false);
    }

    public List<String> disabledAutomaticEvents() {
        return List.copyOf(automaticMemories.disabledEvents);
    }

    public List<String> invalidAutomaticCategories() {
        return List.copyOf(invalidAutomaticCategories);
    }

    public List<String> invalidEnabledAutomaticCategories() {
        return invalidAutomaticCategories();
    }

    public List<String> invalidDisabledAutomaticCategories() {
        return List.of();
    }

    public boolean isAutomaticCategoryEnabled(DiaryCategory category) {
        if (category == null || category == DiaryCategory.MANUAL) {
            return true;
        }

        return automaticMemories.categories.getOrDefault(categoryKey(category), true);
    }

    public boolean isAutomaticCategoryDisabled(DiaryCategory category) {
        return !isAutomaticCategoryEnabled(category);
    }

    public boolean isAutomaticEventDisabled(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return false;
        }

        return automaticMemories.disabledEvents.contains(normalizeEventId(eventId));
    }

    public boolean isAutomaticEventAllowed(String eventId, DiaryCategory category, DiaryImportance importance) {
        return automaticMemories.enabled
                && !isAutomaticEventDisabled(eventId)
                && isAutomaticCategoryEnabled(category)
                && importance.isAtLeast(minAutomaticImportance());
    }

    public static List<String> categoryConfigKeys() {
        return defaultCategoryMap().keySet().stream().toList();
    }

    private List<String> categoryNamesByState(boolean enabled) {
        List<String> values = new ArrayList<>();
        for (DiaryCategory category : DiaryCategory.values()) {
            if (category == DiaryCategory.MANUAL) {
                continue;
            }
            if (isAutomaticCategoryEnabled(category) == enabled) {
                values.add(category.name());
            }
        }
        return values;
    }

    private static String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) {
            return "en_us";
        }

        String normalized = language.strip().replace('-', '_').toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "ru" -> "ru_ru";
            case "en" -> "en_us";
            default -> normalized;
        };
    }

    private static String normalizeSharedMemoryTimeZone(String timeZone) {
        if (timeZone == null || timeZone.isBlank()) {
            return "SERVER";
        }

        String normalized = timeZone.strip();
        if ("SERVER".equalsIgnoreCase(normalized)) {
            return "SERVER";
        }
        if ("UTC".equalsIgnoreCase(normalized)) {
            return "UTC";
        }

        try {
            return ZoneId.of(normalized).getId();
        } catch (DateTimeException exception) {
            LOGGER.warn("Ignoring invalid Dear Diary shared memory timezone '{}', using SERVER", timeZone);
            return "SERVER";
        }
    }

    private Map<String, Boolean> normalizeCategoryMap(Map<String, Boolean> categories) {
        Map<String, Boolean> normalized = defaultCategoryMap();
        if (categories == null || categories.isEmpty()) {
            return normalized;
        }

        for (Map.Entry<String, Boolean> entry : categories.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                continue;
            }

            String categoryKey = normalizeCategoryKey(entry.getKey());
            if (categoryKey == null) {
                invalidAutomaticCategories.add(entry.getKey().strip());
                LOGGER.warn("Ignoring unknown Dear Diary automatic category key '{}'", entry.getKey());
                continue;
            }
            if (entry.getValue() != null) {
                normalized.put(categoryKey, entry.getValue());
            }
        }
        return normalized;
    }

    private static String normalizeCategoryKey(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }

        String candidate = category.strip().replace('-', '_').toUpperCase(Locale.ROOT);
        try {
            DiaryCategory parsed = DiaryCategory.valueOf(candidate);
            if (parsed == DiaryCategory.MANUAL) {
                return null;
            }
            return categoryKey(parsed);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static String categoryKey(DiaryCategory category) {
        return category.name().toLowerCase(Locale.ROOT);
    }

    private static Map<String, Boolean> defaultCategoryMap() {
        Map<String, Boolean> categories = new LinkedHashMap<>();
        for (DiaryCategory category : DiaryCategory.values()) {
            if (category != DiaryCategory.MANUAL) {
                categories.put(categoryKey(category), true);
            }
        }
        return categories;
    }

    private static List<String> normalizeEventIds(List<String> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String eventId : eventIds) {
            if (eventId == null || eventId.isBlank()) {
                continue;
            }

            normalized.add(normalizeEventId(eventId));
        }
        return new ArrayList<>(normalized);
    }

    private static String normalizeEventId(String eventId) {
        return eventId.strip().toLowerCase(Locale.ROOT);
    }

    private static boolean readBoolean(JsonObject root, String key, boolean fallback) {
        JsonElement element = root.get(key);
        if (element == null || element.isJsonNull()) {
            return fallback;
        }

        try {
            return element.getAsBoolean();
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static int readInt(JsonObject root, String key, int fallback) {
        JsonElement element = root.get(key);
        if (element == null || element.isJsonNull()) {
            return fallback;
        }

        try {
            return element.getAsInt();
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static String readString(JsonObject root, String key, String fallback) {
        JsonElement element = root.get(key);
        if (element == null || element.isJsonNull()) {
            return fallback;
        }

        try {
            String value = element.getAsString();
            return value == null || value.isBlank() ? fallback : value;
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static List<String> readStringList(JsonObject root, String key) {
        JsonElement element = root.get(key);
        if (element == null || !element.isJsonArray()) {
            return new ArrayList<>();
        }

        List<String> values = new ArrayList<>();
        JsonArray array = element.getAsJsonArray();
        for (JsonElement item : array) {
            if (item == null || item.isJsonNull()) {
                continue;
            }
            try {
                String value = item.getAsString();
                if (value != null && !value.isBlank()) {
                    values.add(value.strip());
                }
            } catch (RuntimeException ignored) {
                // Ignore malformed legacy list values.
            }
        }
        return values;
    }

    private static final class LanguageConfig {
        private String defaultDiaryLanguage = "en_us";
    }

    private static final class ManualEntriesConfig {
        private int maxTitleLength = 80;
        private int maxTextLength = 2000;
        private int maxSharedTextLength = 500;
        private String sharedMemoryTimeZone = "SERVER";
    }

    private static final class AutomaticMemoriesConfig {
        private boolean enabled = true;
        private boolean createOriginEntry = true;
        private String minimumImportance = "MINOR";
        private Map<String, Boolean> categories = defaultCategoryMap();
        private List<String> disabledEvents = new ArrayList<>();
    }
}
