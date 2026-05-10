package com.worldremembers.deardiary.event;

/**
 * One localized title/text option for an automatic diary event.
 *
 * <p>The keys are resolved when the memory is created. Fallback strings are
 * used only when localization is unavailable.</p>
 */
public record DiaryTextVariant(
        String id,
        String titleKey,
        String textKey,
        String fallbackTitle,
        String fallbackText
) {
}
