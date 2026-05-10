# Localization Guide

Automatic memories should read like personal diary entries, not system logs.

## Key Pattern

Use stable keys that include your own mod id:

```text
entry.mycoolmod.first_airship_flight.title
entry.mycoolmod.first_airship_flight.text.1
entry.mycoolmod.first_airship_flight.text.2
```

The title key can be shared by several text variants. Each text variant should have its own key.

Place the keys in a Dear Diary language resource shipped by your mod:

```text
assets/dear_diary/lang/en_us.json
assets/dear_diary/lang/ru_ru.json
```

Dear Diary merges all matching language resources found on the classpath, so an optional compat mod can provide entries from its own jar.

## Variants

Each `AutomaticEventDefinition` needs at least one variant:

```java
.variant(
        "default",
        "entry.mycoolmod.first_airship_flight.title",
        "entry.mycoolmod.first_airship_flight.text.1",
        "First Flight",
        "The airship finally lifted away from the ground."
)
```

Use several variants for common events so repeated playthroughs do not feel identical.

## Resolved Text

Dear Diary resolves localization keys when the automatic entry is created. It stores:

- `resolvedTitle`
- `resolvedText`

Those saved strings are what the diary UI displays later. Updating language files does not rewrite old diary entries automatically.

This keeps each saved diary entry tied to the moment it was written.

## Style Recommendations

Do:

- write concise, readable memories;
- match the actual event signal;
- use light humor when it fits;
- keep fallback text acceptable in English;
- add localized text for your supported languages.

Avoid:

- dry logs like "Player completed event";
- false claims about how an item was obtained;
- oversized paragraphs;
- modern slang that will age quickly;
- text that requires the player to know internal event names.

For fallback item-acquisition events, phrase carefully. For example, if your hook only knows that the player received a relic, do not claim they excavated it unless that is guaranteed.
