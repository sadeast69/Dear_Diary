package com.worldremembers.deardiary.data;

import com.worldremembers.deardiary.DearDiaryMod;

public final class DiaryEntryMarkers {
    public static final String CHAPTER_EVENT_TYPE = DearDiaryMod.MOD_ID + ":chapter";

    private DiaryEntryMarkers() {
    }

    public static boolean isChapterEntry(DiaryEntry entry) {
        return entry != null && CHAPTER_EVENT_TYPE.equals(entry.getEventType());
    }
}
