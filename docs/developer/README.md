# Dear Diary Developer Guide

World Remembers: Dear Diary lets each player keep manual notes and automatic memories in one server-owned diary. This guide explains how another mod can add memories through the public API.

## Architecture

- The server owns diary state.
- Player diary JSON is internal storage, not an integration API.
- Client classes are UI-only and should not be called by compat code.
- Automatic memories are described by `AutomaticEventDefinition`, stored in `DearDiaryEventRegistry`, and fired through `AutomaticDiaryEvents.trigger(...)`.
- Milestone memories can be advanced through `AutomaticDiaryEvents.incrementCounterAndTriggerMilestones(...)`.
- `DearDiaryApi` is the server-side API for manual entries, automatic entry creation, editing, deletion, favorite toggles, sharing, and lookup.

## Public Integration Surface

Use these classes for compatibility work:

- `com.worldremembers.deardiary.api.DearDiaryApi`
- `com.worldremembers.deardiary.api.AutomaticEntryRequest`
- `com.worldremembers.deardiary.event.AutomaticEventDefinition`
- `com.worldremembers.deardiary.event.DearDiaryEventRegistry`
- `com.worldremembers.deardiary.event.AutomaticDiaryEvents`
- `com.worldremembers.deardiary.event.TriggerPolicy`
- `com.worldremembers.deardiary.data.DiaryCategory`
- `com.worldremembers.deardiary.data.DiaryImportance`

`DiaryEntry` and `PlayerDiary` are useful read models, but normal integrations should not mutate them directly.

The public API is server-side. It does not require Dear Diary client GUI classes, networking payload construction, or direct access to storage files.

## Recommended Compat Flow

1. Add Dear Diary as an optional integration target.
2. Check whether `dear_diary` is loaded.
3. Load a small compat class only when Dear Diary is present.
4. Register your automatic event definitions during initialization.
5. Add title and text localization keys in a Dear Diary lang resource shipped by your mod.
6. From your gameplay hook, call `AutomaticDiaryEvents.trigger(player, eventId)`.
7. Let Dear Diary handle config filters, trigger policy, storage, snapshots, and notifications.

Keep loader code isolated. Fabric source must not import NeoForge APIs, and NeoForge source must not import `net.fabricmc.*`.

See:

- [API_OVERVIEW.md](API_OVERVIEW.md)
- [ADDING_AUTOMATIC_EVENTS.md](ADDING_AUTOMATIC_EVENTS.md)
- [FABRIC_COMPAT_EXAMPLE.md](FABRIC_COMPAT_EXAMPLE.md)
- [LOCALIZATION_GUIDE.md](LOCALIZATION_GUIDE.md)
- [DO_AND_DONT.md](DO_AND_DONT.md)
