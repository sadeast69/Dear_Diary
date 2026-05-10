package com.worldremembers.deardiary;

import com.worldremembers.deardiary.config.DearDiaryConfig;
import com.worldremembers.deardiary.config.DearDiaryConfigManager;
import com.worldremembers.deardiary.storage.DiaryStorage;
import java.nio.file.Path;

public final class DearDiaryServices {
    private static DearDiaryConfigManager configManager;
    private static DiaryStorage storage;

    private DearDiaryServices() {
    }

    public static void setConfigManager(DearDiaryConfigManager configManager) {
        DearDiaryServices.configManager = configManager;
    }

    public static DearDiaryConfig config() {
        if (configManager == null) {
            throw new IllegalStateException("Dear Diary config is not initialized");
        }

        return configManager.config();
    }

    public static Path configPath() {
        if (configManager == null) {
            throw new IllegalStateException("Dear Diary config is not initialized");
        }

        return configManager.configPath();
    }

    public static Path configGuidePath() {
        if (configManager == null) {
            throw new IllegalStateException("Dear Diary config is not initialized");
        }

        return configManager.configGuidePath();
    }

    public static Path eventsGuidePath() {
        if (configManager == null) {
            throw new IllegalStateException("Dear Diary config is not initialized");
        }

        return configManager.eventsGuidePath();
    }

    public static void setStorage(DiaryStorage storage) {
        DearDiaryServices.storage = storage;
    }

    public static DiaryStorage storage() {
        if (storage == null) {
            throw new IllegalStateException("Dear Diary storage is not initialized");
        }

        return storage;
    }

    public static void saveAll() {
        if (storage != null) {
            storage.saveAll();
        }
    }

    public static void clearStorage() {
        storage = null;
    }
}
