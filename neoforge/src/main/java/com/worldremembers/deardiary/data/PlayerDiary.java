package com.worldremembers.deardiary.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Per-player diary container loaded from server storage.
 *
 * <p>Compatibility code should treat this as a read model and write through
 * the public API or automatic trigger service.</p>
 */
public final class PlayerDiary {
    public static final int CURRENT_DATA_VERSION = 2;

    private int dataVersion = CURRENT_DATA_VERSION;
    private UUID playerUuid;
    private List<DiaryEntry> entries = new ArrayList<>();
    private AutomaticEventState automaticEventState = new AutomaticEventState();

    public PlayerDiary() {
    }

    public PlayerDiary(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    public void normalize(UUID fallbackPlayerUuid) {
        dataVersion = dataVersion <= 0 ? CURRENT_DATA_VERSION : Math.max(dataVersion, CURRENT_DATA_VERSION);
        playerUuid = fallbackPlayerUuid == null ? playerUuid : fallbackPlayerUuid;
        entries = entries == null ? new ArrayList<>() : entries;
        automaticEventState = automaticEventState == null ? new AutomaticEventState() : automaticEventState;
        automaticEventState.normalize();
        entries.forEach(DiaryEntry::normalizeForStorage);
        sortChronologically();
    }

    public void addEntry(DiaryEntry entry) {
        entry.normalizeForStorage();
        entries.add(entry);
        sortChronologically();
    }

    public void clear() {
        entries.clear();
        automaticEventState.clear();
    }

    public Optional<DiaryEntry> findEntry(UUID entryId) {
        if (entryId == null) {
            return Optional.empty();
        }

        return entries.stream()
                .filter(entry -> entryId.equals(entry.getId()))
                .findFirst();
    }

    public Optional<DiaryEntry> findLastEntry() {
        if (entries.isEmpty()) {
            return Optional.empty();
        }

        sortChronologically();
        return Optional.of(entries.get(entries.size() - 1));
    }

    public boolean hasEntryWithEventType(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            return false;
        }

        return entries.stream().anyMatch(entry -> eventType.equals(entry.getEventType()));
    }

    public boolean removeEntry(UUID entryId) {
        return entries.removeIf(entry -> entryId != null && entryId.equals(entry.getId()));
    }

    public Optional<DiaryEntry> updateEntryText(UUID entryId, String title, String text) {
        Optional<DiaryEntry> entry = findEntry(entryId);
        entry.ifPresent(found -> found.updateResolvedText(title, text));
        return entry;
    }

    public Optional<DiaryEntry> setFavorite(UUID entryId, boolean favorite) {
        Optional<DiaryEntry> entry = findEntry(entryId);
        entry.ifPresent(found -> found.setFavorite(favorite));
        return entry;
    }

    public int getDataVersion() {
        return dataVersion;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public List<DiaryEntry> entriesView() {
        return List.copyOf(entries);
    }

    public AutomaticEventState automaticEventState() {
        automaticEventState = automaticEventState == null ? new AutomaticEventState() : automaticEventState;
        automaticEventState.normalize();
        return automaticEventState;
    }

    private void sortChronologically() {
        entries.sort(Comparator.comparing(DiaryEntry::getCreatedAt));
    }
}
