package com.worldremembers.deardiary.event;

import com.worldremembers.deardiary.DearDiaryServices;
import com.worldremembers.deardiary.api.DearDiaryApi;
import com.worldremembers.deardiary.config.DearDiaryConfig;
import com.worldremembers.deardiary.data.AutomaticEventState;
import com.worldremembers.deardiary.data.DiaryEntry;
import com.worldremembers.deardiary.data.PlayerDiary;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;

/**
 * Trigger service for registered automatic diary events.
 *
 * <p>This is the preferred runtime entrypoint for compatibility mods. It
 * resolves the definition, applies server config filters and trigger policy
 * state, creates the entry through the API, and returns the created memory
 * only when one was actually written.</p>
 */
public final class AutomaticDiaryEvents {
    private AutomaticDiaryEvents() {
    }

    /**
     * Triggers a registered event with normal anti-spam policy checks.
     */
    public static Optional<DiaryEntry> trigger(ServerPlayer player, String eventId) {
        return trigger(player, eventId, false);
    }

    /**
     * Triggers a registered event.
     *
     * <p>{@code force} bypasses per-player anti-spam state for diagnostics, but
     * it does not bypass server config disabling.</p>
     */
    public static Optional<DiaryEntry> trigger(ServerPlayer player, String eventId, boolean force) {
        return DearDiaryEventRegistry.get(eventId).flatMap(definition -> trigger(player, definition, force));
    }

    /**
     * Triggers a definition directly. Useful when the caller already has the
     * definition object from the registry.
     */
    public static Optional<DiaryEntry> trigger(ServerPlayer player, AutomaticEventDefinition definition, boolean force) {
        DearDiaryConfig config = DearDiaryServices.config();
        if (!config.isAutomaticEventAllowed(definition.eventId(), definition.category(), definition.importance())) {
            return Optional.empty();
        }

        PlayerDiary diary = DearDiaryApi.getDiary(player);
        AutomaticEventState state = diary.automaticEventState();
        long nowMillis = System.currentTimeMillis();
        if (!state.canTrigger(definition, nowMillis, force)) {
            return Optional.empty();
        }

        Optional<DiaryEntry> entry = DearDiaryApi.createAutomaticEntry(
                player,
                definition.createRequest(new AutomaticEventContext(player, definition, force))
        );
        entry.ifPresent(created -> {
            state.markTriggered(definition, nowMillis);
            DearDiaryServices.storage().save(player.getUUID());
        });
        return entry;
    }

    /**
     * Adds to an automatic event counter and saves the player's diary state.
     */
    public static int incrementCounter(ServerPlayer player, String eventId, int amount) {
        PlayerDiary diary = DearDiaryApi.getDiary(player);
        int value = diary.automaticEventState().incrementCounter(eventId, amount);
        DearDiaryServices.storage().save(player.getUUID());
        return value;
    }

    /**
     * Adds to a counter and triggers any registered milestone definitions that
     * reached their threshold.
     */
    public static List<DiaryEntry> incrementCounterAndTriggerMilestones(ServerPlayer player, String counter, int amount) {
        if (counter == null || counter.isBlank() || amount <= 0) {
            return List.of();
        }

        PlayerDiary diary = DearDiaryApi.getDiary(player);
        int value = diary.automaticEventState().incrementCounter(counter, amount);
        DearDiaryServices.storage().save(player.getUUID());

        List<DiaryEntry> createdEntries = new ArrayList<>();
        for (AutomaticEventDefinition definition : DearDiaryEventRegistry.all()) {
            if (definition.triggerPolicy() != TriggerPolicy.MILESTONE) {
                continue;
            }

            if (!counter.equals(definition.milestoneCounter()) || value < definition.milestoneThreshold()) {
                continue;
            }

            trigger(player, definition, false).ifPresent(createdEntries::add);
        }
        return createdEntries;
    }
}
