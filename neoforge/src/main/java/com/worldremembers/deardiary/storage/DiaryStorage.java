package com.worldremembers.deardiary.storage;

import com.worldremembers.deardiary.data.DiaryEntry;
import com.worldremembers.deardiary.data.PlayerDiary;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

public interface DiaryStorage {
    PlayerDiary getDiary(UUID playerUuid);

    DiaryEntry addEntry(UUID playerUuid, DiaryEntry entry);

    Optional<DiaryEntry> findEntry(UUID playerUuid, UUID entryId);

    Optional<DiaryEntry> updateEntryText(UUID playerUuid, UUID entryId, String title, String text);

    boolean deleteEntry(UUID playerUuid, UUID entryId);

    Optional<DiaryEntry> setFavorite(UUID playerUuid, UUID entryId, boolean favorite);

    void clearDiary(UUID playerUuid);

    void save(UUID playerUuid);

    void saveAll();

    Path getPlayerFile(UUID playerUuid);
}
