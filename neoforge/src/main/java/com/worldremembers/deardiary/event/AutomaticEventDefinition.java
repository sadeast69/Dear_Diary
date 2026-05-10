package com.worldremembers.deardiary.event;

import com.worldremembers.deardiary.api.AutomaticEntryRequest;
import com.worldremembers.deardiary.data.DiaryCategory;
import com.worldremembers.deardiary.data.DiaryImportance;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Registry definition for one automatic diary memory.
 *
 * <p>Definitions describe the public identity, filtering metadata,
 * localization variants, and trigger policy for an event. They do not create
 * entries by themselves; gameplay code should register definitions during mod
 * initialization and fire them through {@link AutomaticDiaryEvents#trigger}.</p>
 */
public final class AutomaticEventDefinition {
    private final String eventId;
    private final String source;
    private final DiaryCategory category;
    private final DiaryImportance importance;
    private final TriggerPolicy triggerPolicy;
    private final String icon;
    private final boolean includeLocation;
    private final boolean shareable;
    private final long cooldownMillis;
    private final String milestoneCounter;
    private final int milestoneThreshold;
    private final List<DiaryTextVariant> variants;

    private AutomaticEventDefinition(Builder builder) {
        this.eventId = Objects.requireNonNull(builder.eventId, "eventId");
        this.source = builder.source == null || builder.source.isBlank() ? "minecraft" : builder.source;
        this.category = builder.category == null ? DiaryCategory.OTHER : builder.category;
        this.importance = builder.importance == null ? DiaryImportance.NORMAL : builder.importance;
        this.triggerPolicy = builder.triggerPolicy == null ? TriggerPolicy.ONCE_PER_PLAYER : builder.triggerPolicy;
        this.icon = builder.icon == null || builder.icon.isBlank() ? "minecraft:writable_book" : builder.icon;
        this.includeLocation = builder.includeLocation;
        this.shareable = builder.shareable;
        this.cooldownMillis = Math.max(0L, builder.cooldownMillis);
        this.milestoneCounter = builder.milestoneCounter == null || builder.milestoneCounter.isBlank()
                ? this.eventId
                : builder.milestoneCounter;
        this.milestoneThreshold = Math.max(0, builder.milestoneThreshold);
        this.variants = List.copyOf(builder.variants);
        if (variants.isEmpty()) {
            throw new IllegalStateException("Automatic event must have at least one text variant: " + eventId);
        }
    }

    /**
     * Starts a definition for a namespaced event id such as
     * {@code mymod:first_airship_flight}.
     */
    public static Builder builder(String eventId) {
        return new Builder(eventId);
    }

    /**
     * Creates the automatic entry request for one selected text variant.
     */
    public AutomaticEntryRequest createRequest(AutomaticEventContext context) {
        DiaryTextVariant variant = variants.get(context.random().nextInt(variants.size()));
        return AutomaticEntryRequest.builder(eventId, source)
                .category(category)
                .importance(importance)
                .titleKey(variant.titleKey())
                .textKey(variant.textKey())
                .resolvedTitle(variant.fallbackTitle())
                .resolvedText(variant.fallbackText())
                .icon(icon)
                .includeLocation(includeLocation)
                .shareable(shareable)
                .build();
    }

    public String eventId() {
        return eventId;
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

    public TriggerPolicy triggerPolicy() {
        return triggerPolicy;
    }

    public String icon() {
        return icon;
    }

    public boolean includeLocation() {
        return includeLocation;
    }

    public long cooldownMillis() {
        return cooldownMillis;
    }

    public int milestoneThreshold() {
        return milestoneThreshold;
    }

    public String milestoneCounter() {
        return milestoneCounter;
    }

    /**
     * Returns all text variants as an immutable list.
     */
    public List<DiaryTextVariant> variants() {
        return variants;
    }

    /**
     * Builder for registering an automatic event definition.
     */
    public static final class Builder {
        private final String eventId;
        private String source = "minecraft";
        private DiaryCategory category = DiaryCategory.OTHER;
        private DiaryImportance importance = DiaryImportance.NORMAL;
        private TriggerPolicy triggerPolicy = TriggerPolicy.ONCE_PER_PLAYER;
        private String icon = "minecraft:writable_book";
        private boolean includeLocation = true;
        private boolean shareable = true;
        private long cooldownMillis;
        private String milestoneCounter;
        private int milestoneThreshold;
        private final List<DiaryTextVariant> variants = new ArrayList<>();

        private Builder(String eventId) {
            this.eventId = eventId;
        }

        public Builder source(String source) {
            this.source = source;
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

        public Builder triggerPolicy(TriggerPolicy triggerPolicy) {
            this.triggerPolicy = triggerPolicy;
            return this;
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder includeLocation(boolean includeLocation) {
            this.includeLocation = includeLocation;
            return this;
        }

        public Builder shareable(boolean shareable) {
            this.shareable = shareable;
            return this;
        }

        public Builder cooldownMillis(long cooldownMillis) {
            this.cooldownMillis = cooldownMillis;
            return this;
        }

        public Builder milestoneCounter(String milestoneCounter) {
            this.milestoneCounter = milestoneCounter;
            return this;
        }

        public Builder milestoneThreshold(int milestoneThreshold) {
            this.milestoneThreshold = milestoneThreshold;
            return this;
        }

        /**
         * Adds one localized title/text variant.
         *
         * <p>The fallback strings are used if localization is unavailable.
         * Variants should read like diary memories, not system messages.</p>
         */
        public Builder variant(String id, String titleKey, String textKey, String fallbackTitle, String fallbackText) {
            variants.add(new DiaryTextVariant(id, titleKey, textKey, fallbackTitle, fallbackText));
            return this;
        }

        /**
         * Builds the definition. At least one text variant is required.
         */
        public AutomaticEventDefinition build() {
            return new AutomaticEventDefinition(this);
        }
    }
}
