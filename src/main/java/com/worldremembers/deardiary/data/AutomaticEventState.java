package com.worldremembers.deardiary.data;

import com.worldremembers.deardiary.event.AutomaticEventDefinition;
import com.worldremembers.deardiary.event.TriggerPolicy;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class AutomaticEventState {
    private Set<String> triggeredEvents = new LinkedHashSet<>();
    private Map<String, Long> cooldowns = new LinkedHashMap<>();
    private Map<String, Integer> counters = new LinkedHashMap<>();

    public void normalize() {
        triggeredEvents = triggeredEvents == null ? new LinkedHashSet<>() : new LinkedHashSet<>(triggeredEvents);
        cooldowns = cooldowns == null ? new LinkedHashMap<>() : new LinkedHashMap<>(cooldowns);
        counters = counters == null ? new LinkedHashMap<>() : new LinkedHashMap<>(counters);
        triggeredEvents.removeIf(value -> value == null || value.isBlank());
        cooldowns.entrySet().removeIf(entry -> entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null);
        counters.entrySet().removeIf(entry -> entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null);
    }

    public boolean canTrigger(AutomaticEventDefinition definition, long nowMillis, boolean force) {
        normalize();
        if (force) {
            return true;
        }

        return switch (definition.triggerPolicy()) {
            case ONCE_PER_PLAYER -> !triggeredEvents.contains(definition.eventId());
            case COOLDOWN -> cooldowns.getOrDefault(definition.eventId(), 0L) <= nowMillis;
            case MILESTONE -> canTriggerMilestone(definition);
            case ONCE_PER_BIOME, ONCE_PER_STRUCTURE_TYPE -> !triggeredEvents.contains(definition.eventId());
        };
    }

    public void markTriggered(AutomaticEventDefinition definition, long nowMillis) {
        normalize();
        if (definition.triggerPolicy() == TriggerPolicy.MILESTONE) {
            triggeredEvents.add(milestoneKey(definition.eventId(), definition.milestoneThreshold()));
            return;
        }

        triggeredEvents.add(definition.eventId());
        if (definition.triggerPolicy() == TriggerPolicy.COOLDOWN) {
            cooldowns.put(definition.eventId(), nowMillis + definition.cooldownMillis());
        }
    }

    public boolean markTriggeredEvent(String eventId) {
        normalize();
        if (eventId == null || eventId.isBlank()) {
            return false;
        }

        return triggeredEvents.add(eventId);
    }

    public int incrementCounter(String counter, int amount) {
        normalize();
        if (counter == null || counter.isBlank() || amount <= 0) {
            return 0;
        }

        int value = counters.getOrDefault(counter, 0) + amount;
        counters.put(counter, value);
        return value;
    }

    public boolean isTriggered(String eventId) {
        normalize();
        return triggeredEvents.contains(eventId);
    }

    public int triggeredEventCount() {
        normalize();
        return triggeredEvents.size();
    }

    public int activeCooldownCount(long nowMillis) {
        normalize();
        int count = 0;
        for (long cooldownUntil : cooldowns.values()) {
            if (cooldownUntil > nowMillis) {
                count++;
            }
        }
        return count;
    }

    public int counterCount() {
        normalize();
        return counters.size();
    }

    public Set<String> triggeredEventsView() {
        normalize();
        return Set.copyOf(triggeredEvents);
    }

    public Map<String, Long> cooldownsView() {
        normalize();
        return Map.copyOf(cooldowns);
    }

    public Map<String, Integer> countersView() {
        normalize();
        return Map.copyOf(counters);
    }

    public void clear() {
        triggeredEvents.clear();
        cooldowns.clear();
        counters.clear();
    }

    private boolean canTriggerMilestone(AutomaticEventDefinition definition) {
        int threshold = definition.milestoneThreshold();
        if (threshold <= 0) {
            return false;
        }

        return counters.getOrDefault(definition.milestoneCounter(), 0) >= threshold
                && !triggeredEvents.contains(milestoneKey(definition.eventId(), threshold));
    }

    private static String milestoneKey(String eventId, int threshold) {
        return eventId + "#" + threshold;
    }
}
