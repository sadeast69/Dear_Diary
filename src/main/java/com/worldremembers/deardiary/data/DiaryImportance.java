package com.worldremembers.deardiary.data;

import java.util.Locale;

/**
 * Importance level used for automatic memory filtering and sorting.
 */
public enum DiaryImportance {
    MINOR,
    NORMAL,
    MAJOR,
    LEGENDARY;

    /**
     * Returns whether this level passes a minimum-importance filter.
     */
    public boolean isAtLeast(DiaryImportance minimum) {
        return ordinal() >= minimum.ordinal();
    }

    /**
     * Parses a config or integration value, returning the fallback when the
     * value is blank or unknown.
     */
    public static DiaryImportance parse(String value, DiaryImportance fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        try {
            return DiaryImportance.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}
