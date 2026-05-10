# API Overview

## `DearDiaryApi`

`DearDiaryApi` is the server-side API for diary operations.

Common methods:

- `getDiary(ServerPlayerEntity player)` returns the player's diary model.
- `getEntry(ServerPlayerEntity player, UUID entryId)` returns one entry owned by that player.
- `createManualEntry(...)` creates a player-authored note and applies server config length limits.
- `createAutomaticEntry(...)` creates an automatic entry from an `AutomaticEntryRequest`.
- `editEntryText(...)`, `deleteEntry(...)`, `setFavorite(...)`, `toggleFavorite(...)`, and `shareEntryToChat(...)` mutate existing entries through server storage.

For registered gameplay memories, prefer `AutomaticDiaryEvents.trigger(...)` over `DearDiaryApi.createAutomaticEntry(...)`. The trigger service applies event policy state before creating the entry.

## `AutomaticEntryRequest`

`AutomaticEntryRequest` is the low-level payload used to create one automatic entry.

It contains:

- `eventType`: stable namespaced event id.
- `source`: source mod id, such as `minecraft` or `mycoolmod`.
- `category`: broad memory category.
- `importance`: filtering and sorting weight.
- `titleKey` and `textKey`: localization keys.
- `resolvedTitle` and `resolvedText`: fallback text.
- `icon`: namespaced item/icon id metadata.
- `includeLocation`: whether to store the player's current dimension and block position.
- `customData`: optional machine-readable metadata.

Most compat mods should let `AutomaticEventDefinition.createRequest(...)` build this request.

## `AutomaticEventDefinition`

`AutomaticEventDefinition` describes a registered automatic memory:

- event id
- source mod id
- category
- importance
- trigger policy
- icon id
- location/share flags
- cooldown or milestone fields when needed
- one or more localized text variants

Definitions should be registered during initialization, before gameplay hooks can trigger them.

## `DearDiaryEventRegistry`

`DearDiaryEventRegistry` stores automatic event definitions.

Useful methods:

- `register(definition)`
- `get(eventId)` / `getDefinition(eventId)`
- `getAllDefinitions()`
- `isRegistered(eventId)`
- `listByCategory(category)`
- `eventIds()`

Event ids must be unique and namespaced. Use your own mod id for compat events, for example `mycoolmod:first_airship_flight`.

Collection-returning methods provide snapshots. They are safe to iterate for diagnostics, but changing those collections does not mutate Dear Diary's registry.

## `AutomaticDiaryEvents`

`AutomaticDiaryEvents` is the preferred gameplay trigger service.

Use:

```java
AutomaticDiaryEvents.trigger(player, MyDearDiaryCompat.FIRST_AIRSHIP_FLIGHT);
```

For milestone memories, increment a named counter and trigger matching milestone definitions:

```java
AutomaticDiaryEvents.incrementCounterAndTriggerMilestones(player, "mycoolmod_airship_flights", 1);
```

It checks:

- Dear Diary automatic memory config
- disabled event ids
- disabled categories
- minimum importance
- trigger policy state
- cooldown or milestone state

The returned `Optional<DiaryEntry>` is present only when a memory was actually created.

`trigger(player, eventId, true)` is intended for diagnostics. The force flag bypasses anti-spam state, but it does not bypass config disabling.

`incrementCounterAndTriggerMilestones(...)` returns the entries created by milestone definitions reached by that counter update.

## `TriggerPolicy`

Supported for current integrations:

- `ONCE_PER_PLAYER`: one memory per player.
- `COOLDOWN`: repeat only after a cooldown.
- `MILESTONE`: trigger when a named counter reaches a threshold.

Reserved for future runtime support:

- `ONCE_PER_BIOME`
- `ONCE_PER_STRUCTURE_TYPE`

Do not use reserved policies in released compat code yet.

## Categories And Importance

Use `DiaryCategory` for filtering and UI grouping. Do not use `MANUAL` for automatic memories.

Use `DiaryImportance` for filtering and visual sorting:

- `MINOR`
- `NORMAL`
- `MAJOR`
- `LEGENDARY`

## Resolved Text Behavior

Dear Diary resolves `titleKey` and `textKey` at entry creation time and stores the resulting `resolvedTitle` and `resolvedText`.

Compatibility mods can provide keys by shipping `assets/dear_diary/lang/<locale>.json` in their own jar. Dear Diary merges all matching resources on the classpath.

This means existing diary entries do not silently change when language files are updated later. If you update localization, only newly created memories use the new prose unless a developer command relocalizes entries.
