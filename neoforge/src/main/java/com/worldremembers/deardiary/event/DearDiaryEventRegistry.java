package com.worldremembers.deardiary.event;

import com.worldremembers.deardiary.data.DiaryCategory;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Server-side registry of automatic diary event definitions.
 *
 * <p>Compatibility mods may register their own definitions during
 * initialization. Register before gameplay hooks can fire, and use stable
 * namespaced event ids.</p>
 */
public final class DearDiaryEventRegistry {
    private static final Map<String, AutomaticEventDefinition> EVENTS = new LinkedHashMap<>();

    private DearDiaryEventRegistry() {
    }

    /**
     * Registers one automatic event definition.
     *
     * @throws IllegalStateException if another definition already uses the
     * same event id
     */
    public static void register(AutomaticEventDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        if (!isNamespaced(definition.eventId())) {
            throw new IllegalArgumentException("Automatic diary event id must be namespaced: " + definition.eventId());
        }
        if (EVENTS.containsKey(definition.eventId())) {
            throw new IllegalStateException("Duplicate automatic diary event id: " + definition.eventId());
        }

        EVENTS.put(definition.eventId(), definition);
    }

    /**
     * Finds a definition by event id.
     */
    public static Optional<AutomaticEventDefinition> get(String eventId) {
        return Optional.ofNullable(EVENTS.get(eventId));
    }

    /**
     * Alias for {@link #get(String)} used by diagnostics and integrations.
     */
    public static Optional<AutomaticEventDefinition> getDefinition(String eventId) {
        return get(eventId);
    }

    /**
     * Returns registered definitions in registration order as an immutable
     * snapshot.
     */
    public static Collection<AutomaticEventDefinition> all() {
        return List.copyOf(EVENTS.values());
    }

    /**
     * Alias for {@link #all()} used by diagnostics and integrations.
     */
    public static Collection<AutomaticEventDefinition> getAllDefinitions() {
        return all();
    }

    /**
     * Returns whether an event id is registered.
     */
    public static boolean isRegistered(String eventId) {
        return EVENTS.containsKey(eventId);
    }

    /**
     * Lists definitions in one category, sorted by event id.
     */
    public static List<AutomaticEventDefinition> listByCategory(DiaryCategory category) {
        return EVENTS.values().stream()
                .filter(definition -> definition.category() == category)
                .sorted(Comparator.comparing(AutomaticEventDefinition::eventId))
                .toList();
    }

    /**
     * Returns a copy of registered event ids.
     */
    public static Set<String> eventIds() {
        return Set.copyOf(EVENTS.keySet());
    }

    private static boolean isNamespaced(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return false;
        }

        int separator = eventId.indexOf(':');
        return separator > 0 && separator < eventId.length() - 1;
    }
}
