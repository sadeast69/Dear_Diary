package com.worldremembers.deardiary.client;

import com.google.gson.JsonParseException;
import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.data.DiaryEntry;
import com.worldremembers.deardiary.data.DiaryJson;
import com.worldremembers.deardiary.data.PlayerDiary;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ClientDiaryCache {
    private static PlayerDiary currentDiary;
    private static DiaryEntry lastAutomaticEntry;
    private static final List<Listener> listeners = new CopyOnWriteArrayList<>();

    private ClientDiaryCache() {
    }

    public static void updateFromJson(String diaryJson, UUID fallbackPlayerUuid) {
        try {
            PlayerDiary diary = DiaryJson.GSON.fromJson(diaryJson, PlayerDiary.class);
            if (diary != null) {
                diary.normalize(fallbackPlayerUuid);
                currentDiary = diary;
                notifyListeners();
            }
        } catch (JsonParseException exception) {
            DearDiaryMod.LOGGER.warn("Failed to parse Dear Diary snapshot", exception);
        }
    }

    public static void rememberAutomaticEntry(String entryJson) {
        try {
            DiaryEntry entry = DiaryJson.GSON.fromJson(entryJson, DiaryEntry.class);
            if (entry != null) {
                entry.normalizeForStorage();
                lastAutomaticEntry = entry;
            }
        } catch (JsonParseException exception) {
            DearDiaryMod.LOGGER.warn("Failed to parse Dear Diary automatic entry notice", exception);
        }
    }

    public static Optional<PlayerDiary> currentDiary() {
        return Optional.ofNullable(currentDiary);
    }

    public static Optional<DiaryEntry> lastAutomaticEntry() {
        return Optional.ofNullable(lastAutomaticEntry);
    }

    public static int entryCount() {
        return currentDiary == null ? 0 : currentDiary.entriesView().size();
    }

    public static void addListener(Listener listener) {
        listeners.add(listener);
    }

    public static void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private static void notifyListeners() {
        for (Listener listener : listeners) {
            listener.onDiaryCacheUpdated();
        }
    }

    @FunctionalInterface
    public interface Listener {
        void onDiaryCacheUpdated();
    }
}
