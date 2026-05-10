# Adding Automatic Events

This page describes the normal workflow for adding a memory from another mod.

## 1. Choose A Stable Event Id

Use your own namespace:

```text
mycoolmod:first_airship_flight
```

Do not use the `minecraft` or `dear_diary` namespace for your own mod's events.

## 2. Choose Category And Importance

Pick a category that matches the memory:

```java
DiaryCategory.EXPLORATION
```

Pick an importance level:

```java
DiaryImportance.MAJOR
```

Avoid marking ordinary events as `LEGENDARY`; server admins can filter automatic memories by minimum importance.

## 3. Choose A Trigger Policy

Most first-time memories should use:

```java
TriggerPolicy.ONCE_PER_PLAYER
```

Use `COOLDOWN` for repeatable danger or travel memories. Use `MILESTONE` only when you have a real counter.

Milestone definitions need a stable counter id and threshold. Advance that counter from gameplay code with `AutomaticDiaryEvents.incrementCounterAndTriggerMilestones(...)`.

## 4. Register The Definition

Register during initialization, before your gameplay hook can fire.

```java
public static final String FIRST_AIRSHIP_FLIGHT = "mycoolmod:first_airship_flight";

public static void registerDearDiaryEvents() {
    DearDiaryEventRegistry.register(AutomaticEventDefinition.builder(FIRST_AIRSHIP_FLIGHT)
            .source("mycoolmod")
            .category(DiaryCategory.EXPLORATION)
            .importance(DiaryImportance.MAJOR)
            .triggerPolicy(TriggerPolicy.ONCE_PER_PLAYER)
            .icon("mycoolmod:airship")
            .variant(
                    "default",
                    "entry.mycoolmod.first_airship_flight.title",
                    "entry.mycoolmod.first_airship_flight.text.1",
                    "First Flight",
                    "The airship finally lifted away from the ground. I kept one hand on the rail and pretended that counted as confidence."
            )
            .variant(
                    "wind",
                    "entry.mycoolmod.first_airship_flight.title",
                    "entry.mycoolmod.first_airship_flight.text.2",
                    "First Flight",
                    "For a while the world was below me instead of around me. That is a strange place to put the horizon."
            )
            .build());
}
```

Fallback strings should be readable, but localization files should provide the final text.

## 5. Add Localization

Add keys to a Dear Diary language resource in your mod jar:

```text
assets/dear_diary/lang/en_us.json
```

Dear Diary reads all matching `assets/dear_diary/lang/<locale>.json` files on the classpath and merges the keys. The keys themselves can still use your mod id:

```json
{
  "entry.mycoolmod.first_airship_flight.title": "First Flight",
  "entry.mycoolmod.first_airship_flight.text.1": "The airship finally lifted away from the ground. I kept one hand on the rail and pretended that counted as confidence.",
  "entry.mycoolmod.first_airship_flight.text.2": "For a while the world was below me instead of around me. That is a strange place to put the horizon."
}
```

Dear Diary resolves the keys when the entry is created.

## 6. Trigger From Gameplay Code

Call the trigger service from a server-side gameplay hook:

```java
AutomaticDiaryEvents.trigger(player, FIRST_AIRSHIP_FLIGHT);
```

If the event is disabled by config, filtered by category or importance, or blocked by policy state, no entry is created and no notification is sent.

For milestone events, update the counter instead:

```java
AutomaticDiaryEvents.incrementCounterAndTriggerMilestones(player, "mycoolmod_airship_flights", 1);
```

## 7. Test In Game

For your own integration, test:

- event registration happened once;
- localization keys exist;
- the memory triggers from the real gameplay hook;
- `AutomaticDiaryEvents.trigger(...)` returns empty when config disables the event;
- duplicate first-time memories do not repeat;
- milestone counters trigger at the intended threshold;
- the server does not load client-only classes.

For optional compatibility, keep loader-specific code in loader-specific source sets. Fabric code should not import NeoForge APIs, and NeoForge code should not import `net.fabricmc.*`.
