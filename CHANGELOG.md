# Changelog

## 1.0.1 - Optional compatibility refresh

### Added

- Added optional Fungal Infection: Spore compatibility with infection, laboratory, scanner, infected enemy, and infected kill milestone memories.
- Added optional Scorched Guns compatibility with firearm workbench, weapon, ammunition, blueprint, raid flare, enemy defeat, and bullet-kill memories.

### Changed

- Refreshed Fabric and NeoForge release artifacts for the accepted optional compatibility set.

## 1.0.0 - Public release

### Added

- Added diary search inside the book screen.
- Added Markdown export through `/deardiary export markdown`.
- Added automatic diary JSON backups when players leave a world or server.
- Added journey chapters through command and GUI creation.
- Added click-to-copy coordinates for diary entries with locations.
- Added PvP diary memories for the first duel won and PvP milestones.

### Changed

- Polished diary text across vanilla memories and optional compatibility.
- Polished generated config and event help text.
- Refreshed public documentation for the completed Fabric and NeoForge release.
- Completed the optional compatibility set for Twilight Forest, Aether, Deeper and Darker, Iron's Spells, Cataclysm, Create, Farmer's Delight, and Waystones.

## 0.10.0 - Waystones compatibility and text polish

### Added

- Added optional Waystones compatibility with 5 automatic diary memories.
- Added memories for first activated waystone, first placed waystone, first warp stone, first return scroll, and an activated-waystones milestone.
- Added localized `en_us` and `ru_ru` diary text for Waystones memories.

### Changed

- Polished many diary texts across vanilla and optional compatibility memories.
- Improved the generated `CONFIG_GUIDE.md` and `EVENTS.md` help text.
- Kept Waystones and Balm optional. Dear Diary stays safe when those mods are absent.

## 0.9.0 - Farmer's Delight compatibility

### Added

- Added optional Farmer's Delight compatibility with 12 automatic diary memories.
- Added memories for kitchen tools, meals, feasts, pantry setup, stove placement, rich soil, and repeated meal and feast milestones.
- Added localized `en_us` and `ru_ru` diary text for Farmer's Delight memories.

### Changed

- Kept Farmer's Delight support optional. Dear Diary stays safe when the mod is absent.

## 0.8.0 - Create compatibility

### Added

- Added optional Create compatibility with 19 automatic diary memories.
- Added memories for workshop power, machines, processing, fluids, logistics, trains, and factory control.
- Added localized `en_us` and `ru_ru` diary text for Create memories.

### Changed

- Kept Create support optional. Runtime Create gameplay hooks are available on NeoForge.

## 0.7.0 - Cataclysm compatibility

### Added

- Added optional L_Ender's Cataclysm compatibility with 17 automatic diary memories.
- Added memories for major structures, bosses, and a signature artifact.
- Added localized `en_us` and `ru_ru` diary text for Cataclysm memories.

### Changed

- Kept Cataclysm support optional. Runtime Cataclysm gameplay hooks are available on NeoForge.

## 0.1.0 - Fabric MVP

### Added

- Book-style diary GUI with inline view, create, and edit modes.
- Per-player server-side diary storage.
- Manual diary entries.
- Automatic memory system with vanilla event coverage.
- Favorites, edit, delete, and share actions.
- Inventory diary button and client hotkey.
- Notification overlay for newly created automatic memories.
- `en_us` and `ru_ru` localization.
- Server config v2.
- Runtime config guide and automatic event id guide files.
- Developer API and documentation for optional integrations.

### Changed

- Automatic entries store resolved title and text when created, so older memories keep their wording after language updates.
- Chat sharing uses a localized diary quote format.
- Developer and test commands require operator permission level 2.

### Known Limits

- This first public milestone supported Fabric `1.21.1`.
- NeoForge support was added in later versions.
- Some discovery memories were definition-only until reliable hooks were added.
- There was no Mod Menu config screen.
- Entry icons inside the diary were postponed.
