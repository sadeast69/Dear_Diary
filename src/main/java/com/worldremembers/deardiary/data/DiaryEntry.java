package com.worldremembers.deardiary.data;

import com.google.gson.JsonElement;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Stored diary entry model.
 *
 * <p>External integrations may read entries returned by the API, but should
 * not construct or mutate stored entries directly for normal gameplay. Use
 * {@code DearDiaryApi} and automatic event definitions instead.</p>
 */
public final class DiaryEntry {
    private UUID id = UUID.randomUUID();
    private String eventType = "manual";
    private String source = "dear_diary";
    private DiaryEntryKind entryKind = DiaryEntryKind.MANUAL;
    private DiaryCategory category = DiaryCategory.OTHER;
    private DiaryImportance importance = DiaryImportance.NORMAL;
    private Instant createdAt = Instant.now();
    private String titleKey;
    private String textKey;
    private String resolvedTitle = "";
    private String resolvedText = "";
    private String dimension;
    private Integer x;
    private Integer y;
    private Integer z;
    private String icon = "minecraft:writable_book";
    private boolean favorite;
    private boolean editable = true;
    private boolean shareable = true;
    private Map<String, JsonElement> customData = new LinkedHashMap<>();

    public DiaryEntry() {
    }

    private DiaryEntry(Builder builder) {
        this.id = builder.id;
        this.eventType = builder.eventType;
        this.source = builder.source;
        this.entryKind = builder.entryKind;
        this.category = builder.category;
        this.importance = builder.importance;
        this.createdAt = builder.createdAt;
        this.titleKey = builder.titleKey;
        this.textKey = builder.textKey;
        this.resolvedTitle = builder.resolvedTitle;
        this.resolvedText = builder.resolvedText;
        this.dimension = builder.dimension;
        this.x = builder.x;
        this.y = builder.y;
        this.z = builder.z;
        this.icon = builder.icon;
        this.favorite = builder.favorite;
        this.editable = builder.editable;
        this.shareable = builder.shareable;
        this.customData = new LinkedHashMap<>(builder.customData);
        normalizeForStorage();
    }

    public static Builder builder(DiaryEntryKind entryKind, String eventType, String source) {
        return new Builder(entryKind, eventType, source);
    }

    public void normalizeForStorage() {
        id = id == null ? UUID.randomUUID() : id;
        eventType = normalizeText(eventType, "manual");
        source = normalizeText(source, "dear_diary");
        entryKind = entryKind == null ? DiaryEntryKind.MANUAL : entryKind;
        category = category == null ? DiaryCategory.OTHER : category;
        importance = importance == null ? DiaryImportance.NORMAL : importance;
        createdAt = createdAt == null ? Instant.now() : createdAt;
        resolvedTitle = resolvedTitle == null ? "" : resolvedTitle;
        resolvedText = resolvedText == null ? "" : resolvedText;
        icon = normalizeText(icon, "minecraft:writable_book");
        customData = customData == null ? new LinkedHashMap<>() : customData;

        if (dimension == null || x == null || y == null || z == null) {
            dimension = null;
            x = null;
            y = null;
            z = null;
        }
    }

    private static String normalizeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    public UUID getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public String getSource() {
        return source;
    }

    public DiaryEntryKind getEntryKind() {
        return entryKind;
    }

    public DiaryCategory getCategory() {
        return category;
    }

    public DiaryImportance getImportance() {
        return importance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public String getTextKey() {
        return textKey;
    }

    public String getResolvedTitle() {
        return resolvedTitle;
    }

    public String getResolvedText() {
        return resolvedText;
    }

    public String getDimension() {
        return dimension;
    }

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    public Integer getZ() {
        return z;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isShareable() {
        return shareable;
    }

    public Map<String, JsonElement> getCustomData() {
        return Map.copyOf(customData);
    }

    public boolean hasLocation() {
        return dimension != null && x != null && y != null && z != null;
    }

    public void updateResolvedText(String title, String text) {
        if (!editable) {
            throw new IllegalStateException("Diary entry is not editable");
        }

        resolvedTitle = title == null ? "" : title;
        resolvedText = text == null ? "" : text;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public static final class Builder {
        private UUID id = UUID.randomUUID();
        private String eventType;
        private String source;
        private DiaryEntryKind entryKind;
        private DiaryCategory category = DiaryCategory.OTHER;
        private DiaryImportance importance = DiaryImportance.NORMAL;
        private Instant createdAt = Instant.now();
        private String titleKey;
        private String textKey;
        private String resolvedTitle = "";
        private String resolvedText = "";
        private String dimension;
        private Integer x;
        private Integer y;
        private Integer z;
        private String icon = "minecraft:writable_book";
        private boolean favorite;
        private boolean editable = true;
        private boolean shareable = true;
        private final Map<String, JsonElement> customData = new LinkedHashMap<>();

        private Builder(DiaryEntryKind entryKind, String eventType, String source) {
            this.entryKind = Objects.requireNonNull(entryKind, "entryKind");
            this.eventType = eventType;
            this.source = source;
            if (entryKind == DiaryEntryKind.MANUAL) {
                this.category = DiaryCategory.MANUAL;
            }
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder category(DiaryCategory category) {
            this.category = category;
            return this;
        }

        public Builder importance(DiaryImportance importance) {
            this.importance = importance;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder titleKey(String titleKey) {
            this.titleKey = titleKey;
            return this;
        }

        public Builder textKey(String textKey) {
            this.textKey = textKey;
            return this;
        }

        public Builder resolvedTitle(String resolvedTitle) {
            this.resolvedTitle = resolvedTitle;
            return this;
        }

        public Builder resolvedText(String resolvedText) {
            this.resolvedText = resolvedText;
            return this;
        }

        public Builder location(String dimension, int x, int y, int z) {
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder favorite(boolean favorite) {
            this.favorite = favorite;
            return this;
        }

        public Builder editable(boolean editable) {
            this.editable = editable;
            return this;
        }

        public Builder shareable(boolean shareable) {
            this.shareable = shareable;
            return this;
        }

        public Builder customData(String key, JsonElement value) {
            if (key != null && !key.isBlank() && value != null) {
                customData.put(key, value);
            }
            return this;
        }

        public DiaryEntry build() {
            return new DiaryEntry(this);
        }
    }
}
