# World Remembers: Dear Diary

World Remembers: Dear Diary is a Minecraft mod that gives each player a personal diary. Players can write their own notes, and the server can add automatic memories for important survival, travel, combat, building, and progression moments.

The diary opens as an old journal with reading, writing, editing, filtering, and sharing built into the screen. Diary data is stored on the server, so multiplayer worlds keep one shared history for each player.

## Features

- Personal diary for each player.
- Manual entries with inline create and edit modes.
- Automatic memories for vanilla gameplay.
- Favorites, delete, edit, and share actions.
- Search inside the diary.
- Filters for all, manual, automatic, and favorite entries.
- Sorting by date or importance.
- Journey chapters for marking important parts of a playthrough.
- Markdown export for the current player's diary.
- Automatic diary JSON backups on logout.
- Click-to-copy coordinates for entries with locations.
- Inventory diary button.
- Hotkey support for opening a new entry.
- Notification overlay for new automatic memories.
- Chat sharing for diary memories.
- Localized `en_us` and `ru_ru` text.
- Server-side config for automatic memories and sharing limits.

## Current Version

Version `1.0.0` targets Minecraft `1.21.1` and Java `21`.

Fabric is the root project. NeoForge is supported through the nested `neoforge/` project.

## Optional Compatibility

Dear Diary works without any optional content mods. Compatibility activates only when the optional mod is installed on a supported loader.

| Source | Coverage | Notes |
| --- | --- | --- |
| Vanilla | automatic memories | Built in. |
| Twilight Forest | 23 events | Optional. |
| Aether | 13 events | Optional. |
| Deeper and Darker | 7 events | Optional. |
| Iron's Spells | 9 events | Optional. |
| Cataclysm | 17 events | NeoForge gameplay hooks. Fabric stays safe when the mod or hooks are absent. |
| Create | 19 events | NeoForge gameplay hooks. Fabric stays safe when the mod or hooks are absent. |
| Farmer's Delight | 12 events | Fabric and NeoForge. Includes meal and feast milestones. |
| Waystones | 5 events | Fabric and NeoForge. Includes an activated-waystones milestone. |

## Requirements

- Minecraft `1.21.1`
- Java `21`
- Fabric Loader with Fabric API, or NeoForge

## Installation

1. Install Fabric Loader with Fabric API, or install NeoForge, for Minecraft `1.21.1`.
2. Put the matching Dear Diary jar into your `mods` folder.
3. Start the game or dedicated server.

Install only the optional content mods you want. Dear Diary skips optional compatibility when those mods are absent.

## Basic Usage

- Open the diary with `/deardiary open`.
- Open directly into a new note with `/deardiary open new`.
- List recent entries with `/deardiary list`.
- Add a journey chapter with `/deardiary chapter <title>`.
- Export your diary with `/deardiary export markdown`.
- Use the inventory diary button when it is enabled.
- Use the configured hotkey to start a new entry from the client.
- Select entries on the left page and read, edit, favorite, delete, or share them on the right page.
- Search diary entries from the left page.
- Click a displayed coordinate line to copy `x y z`.

## Configuration

Main server config:

```text
config/world_remembers/dear_diary/dear_diary.json
```

Client config:

```text
config/world_remembers/dear_diary/client.json
```

At runtime Dear Diary also writes helper files next to the server config:

- `CONFIG_GUIDE.md`
- `EVENTS.md`

The server config controls automatic memories, origin entry creation, category filters, disabled event ids, minimum importance, manual entry limits, and chat-share timezone display.

## Commands

Common commands:

- `/deardiary open`
- `/deardiary open new`
- `/deardiary list`

Operator commands are also available for config checks, event lists, validation, counters, relocalization, and test triggers:

- `/deardiary config_status`
- `/deardiary config_help`
- `/deardiary events_list`
- `/deardiary validate_events`

## Developer Integration

Other mods can add automatic memories through the server-side API. Keep integrations optional, register `AutomaticEventDefinition` instances, ship localization keys, and trigger memories through `AutomaticDiaryEvents.trigger(...)`.

Do not write directly to Dear Diary JSON files.

Developer docs:

- [Developer Guide](docs/developer/README.md)
- [API Overview](docs/developer/API_OVERVIEW.md)
- [Adding Automatic Events](docs/developer/ADDING_AUTOMATIC_EVENTS.md)
- [Fabric Optional Integration Example](docs/developer/FABRIC_COMPAT_EXAMPLE.md)
- [Localization Guide](docs/developer/LOCALIZATION_GUIDE.md)
- [Do And Don't](docs/developer/DO_AND_DONT.md)

## Current Limits

- The server config is edited through JSON. There is no Mod Menu config screen.
- Entry icons inside the diary are postponed.
- Automatic text is resolved and stored when an entry is created, so old diary entries keep their original wording after language updates.

## License

MIT License. See [LICENSE](LICENSE).
