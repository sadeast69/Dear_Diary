package com.worldremembers.deardiary.api;

import com.google.gson.JsonElement;
import com.worldremembers.deardiary.data.DiaryCategory;
import com.worldremembers.deardiary.data.DiaryImportance;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable payload used to create one automatic diary memory.
 *
 * <p>Most integrations should not build this directly during gameplay. Define
 * an {@code AutomaticEventDefinition}, register it, and let
 * {@code AutomaticDiaryEvents.trigger(...)} create the request after config and
 * anti-spam checks pass.</p>
 */
public final class AutomaticEntryRequest {
    private final String eventType;
    private final String source;
    private final DiaryCategory category;
    private final DiaryImportance importance;
    private final String titleKey;
    private final String textKey;
    private final String resolvedTitle;
    private final String resolvedText;
    private final String icon;
    private final boolean shareable;
    private final boolean includeLocation;
    private final Map<String, JsonElement> customData;

    private AutomaticEntryRequest(Builder builder) {
        this.eventType = builder.eventType;
        this.source = builder.source;
        this.category = builder.category == null ? DiaryCategory.OTHER : builder.category;
        this.importance = builder.importance == null ? DiaryImportance.NORMAL : builder.importance;
        this.titleKey = builder.titleKey;
        this.textKey = builder.textKey;
        this.resolvedTitle = builder.resolvedTitle == null ? "" : builder.resolvedTitle;
        this.resolvedText = builder.resolvedText == null ? "" : builder.resolvedText;
        this.icon = builder.icon == null || builder.icon.isBlank() ? "minecraft:book" : builder.icon;
        this.shareable = builder.shareable;
        this.includeLocation = builder.includeLocation;
        this.customData = Map.copyOf(builder.customData);
    }

    /**
     * Starts a request for a namespaced event id and source mod id.
     */
    public static Builder builder(String eventType, String source) {
        return new Builder(eventType, source);
    }

    public String eventType() {
        return eventType;
    }

    public String source() {
        return source;
    }

    public DiaryCategory category() {
        return category;
    }

    public DiaryImportance importance() {
        return importance;
    }

    public String titleKey() {
        return titleKey;
    }

    public String textKey() {
        return textKey;
    }

    public String resolvedTitle() {
        return resolvedTitle;
    }

    public String resolvedText() {
        return resolvedText;
    }

    public String icon() {
        return icon;
    }

    public boolean shareable() {
        return shareable;
    }

    public boolean includeLocation() {
        return includeLocation;
    }

    /**
     * Returns custom metadata as an immutable view.
     */
    public Map<String, JsonElement> customData() {
        return customData;
    }

    /**
     * Builder for an automatic entry request.
     */
    public static final class Builder {
        private final String eventType;
        private final String source;
        private DiaryCategory category = DiaryCategory.OTHER;
        private DiaryImportance importance = DiaryImportance.NORMAL;
        private String titleKey;
        private String textKey;
        private String resolvedTitle = "";
        private String resolvedText = "";
        private String icon = "minecraft:book";
        private boolean shareable = true;
        private boolean includeLocation = true;
        private final Map<String, JsonElement> customData = new LinkedHashMap<>();

        private Builder(String eventType, String source) {
            this.eventType = eventType;
            this.source = source;
        }

        public Builder category(DiaryCategory category) {
            this.category = category;
            return this;
        }

        public Builder importance(DiaryImportance importance) {
            this.importance = importance;
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

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder shareable(boolean shareable) {
            this.shareable = shareable;
            return this;
        }

        public Builder includeLocation(boolean includeLocation) {
            this.includeLocation = includeLocation;
            return this;
        }

        /**
         * Adds optional machine-readable metadata for other integrations.
         * Values are stored with the entry, but Dear Diary does not interpret
         * them for normal UI behavior.
         */
        public Builder customData(String key, JsonElement value) {
            if (key != null && !key.isBlank() && value != null) {
                customData.put(key, value);
            }
            return this;
        }

        /**
         * Builds an immutable request.
         */
        public AutomaticEntryRequest build() {
            return new AutomaticEntryRequest(this);
        }
    }
}
