package com.worldremembers.deardiary.client.gui;

import com.worldremembers.deardiary.data.DiaryEntry;
import com.worldremembers.deardiary.data.DiaryEntryKind;
import com.worldremembers.deardiary.data.DiaryEntryMarkers;
import net.minecraft.network.chat.Component;

enum DiaryFilter {
    ALL("screen.dear_diary.filter.all", "screen.dear_diary.filter.all"),
    MANUAL("screen.dear_diary.filter.manual", "screen.dear_diary.filter.manual.short"),
    AUTOMATIC("screen.dear_diary.filter.automatic", "screen.dear_diary.filter.automatic.short"),
    FAVORITES("screen.dear_diary.filter.favorites", "screen.dear_diary.filter.favorites.short");

    private final String translationKey;
    private final String tabTranslationKey;

    DiaryFilter(String translationKey, String tabTranslationKey) {
        this.translationKey = translationKey;
        this.tabTranslationKey = tabTranslationKey;
    }

    Component text() {
        return Component.translatable(translationKey);
    }

    Component tabText() {
        return Component.translatable(tabTranslationKey);
    }

    boolean matches(DiaryEntry entry) {
        return switch (this) {
            case ALL -> true;
            case MANUAL -> entry.getEntryKind() == DiaryEntryKind.MANUAL;
            case AUTOMATIC -> entry.getEntryKind() == DiaryEntryKind.AUTOMATIC;
            case FAVORITES -> entry.isFavorite() && !DiaryEntryMarkers.isChapterEntry(entry);
        };
    }
}
