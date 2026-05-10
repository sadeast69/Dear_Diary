# Do And Don't

## Do

- Use `DearDiaryEventRegistry.register(...)` for automatic memory definitions.
- Use `AutomaticDiaryEvents.trigger(...)` from server-side gameplay hooks.
- Use `AutomaticDiaryEvents.incrementCounterAndTriggerMilestones(...)` for milestone counters.
- Use stable namespaced event ids from your own mod id.
- Use `DiaryCategory` and `DiaryImportance` honestly so server filters work.
- Add localized title and text keys.
- Treat `DiaryEntry` and `PlayerDiary` as read models in compat code.
- Keep optional integration code isolated behind a Dear Diary loaded check.
- Keep Fabric and NeoForge API imports in their own loader source sets.
- Test on a dedicated server.

## Don't

- Do not write diary JSON files directly.
- Do not mutate `PlayerDiary` or `DiaryEntry` directly for normal gameplay.
- Do not bypass Dear Diary config and anti-spam rules.
- Do not send Dear Diary networking payloads from another mod.
- Do not call client GUI classes from server code.
- Do not import NeoForge APIs from Fabric source.
- Do not import `net.fabricmc.*` from NeoForge source.
- Do not use `DiaryCategory.MANUAL` for automatic memories.
- Do not use reserved trigger policies in released compat code.
- Do not create automatic entries for every small repeated action.
- Do not write dry system logs as diary prose.

## Good Memory Text

```text
The airship rose at last. The ground looked smaller, and so did several of my worries.
```

## Weak Memory Text

```text
Player triggered mycoolmod:first_airship_flight.
```
