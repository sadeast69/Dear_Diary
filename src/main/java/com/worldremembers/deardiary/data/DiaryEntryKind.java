package com.worldremembers.deardiary.data;

import java.util.Locale;

/**
 * Stored entry source kind. Automatic entries come from registered events;
 * manual entries are authored by the player.
 */
public enum DiaryEntryKind {
    AUTOMATIC("automatic"),
    MANUAL("manual");

    private final String serializedName;

    DiaryEntryKind(String serializedName) {
        this.serializedName = serializedName;
    }

    /**
     * Stable lowercase value written to diary JSON.
     */
    public String serializedName() {
        return serializedName;
    }

    /**
     * Parses the stored value, falling back to manual for old or malformed
     * data.
     */
    public static DiaryEntryKind fromSerializedName(String value) {
        if (value == null || value.isBlank()) {
            return MANUAL;
        }

        String normalized = value.toLowerCase(Locale.ROOT);
        for (DiaryEntryKind kind : values()) {
            if (kind.serializedName.equals(normalized) || kind.name().equalsIgnoreCase(value)) {
                return kind;
            }
        }

        return MANUAL;
    }
}
