# Fabric Optional Integration Example

This example shows how a Fabric mod can integrate with Dear Diary while keeping the integration optional.

## `fabric.mod.json`

Use `suggests` for an optional integration:

```json
{
  "suggests": {
    "dear_diary": "*"
  }
}
```

Use `depends` only if your mod requires Dear Diary to run.

Compile against Dear Diary as a compile-time-only integration target if your build setup supports it. The exact Gradle notation depends on how you consume the jar, but the intent is:

```gradle
modCompileOnly "com.worldremembers:dear-diary:<version>"
```

Do not bundle Dear Diary inside your mod jar.

## Main Initializer

Keep Dear Diary imports out of classes that must load when Dear Diary is absent.

```java
package com.example.mycoolmod;

import com.example.mycoolmod.compat.MyCoolModDearDiaryCompat;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class MyCoolMod implements ModInitializer {
    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().isModLoaded("dear_diary")) {
            MyCoolModDearDiaryCompat.register();
        }

        MyCoolModEvents.register();
    }
}
```

The main initializer imports only your own compat bridge class. Load that bridge only after the `isModLoaded("dear_diary")` check passes.

## Isolated Compat Class

This class imports Dear Diary API types. Only load it after checking that Dear Diary is present.

```java
package com.example.mycoolmod.compat;

import com.worldremembers.deardiary.data.DiaryCategory;
import com.worldremembers.deardiary.data.DiaryImportance;
import com.worldremembers.deardiary.event.AutomaticDiaryEvents;
import com.worldremembers.deardiary.event.AutomaticEventDefinition;
import com.worldremembers.deardiary.event.DearDiaryEventRegistry;
import com.worldremembers.deardiary.event.TriggerPolicy;
import net.minecraft.server.network.ServerPlayerEntity;

public final class MyCoolModDearDiaryCompat {
    public static final String FIRST_AIRSHIP_FLIGHT = "mycoolmod:first_airship_flight";

    private MyCoolModDearDiaryCompat() {
    }

    public static void register() {
        if (!DearDiaryEventRegistry.isRegistered(FIRST_AIRSHIP_FLIGHT)) {
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
                            "The airship rose at last. The ground looked smaller, and so did several of my worries."
                    )
                    .build());
        }
    }

    public static void onFirstAirshipFlight(ServerPlayerEntity player) {
        AutomaticDiaryEvents.trigger(player, FIRST_AIRSHIP_FLIGHT);
    }
}
```

## Gameplay Hook

Call your compat bridge only from server-side code and only after the same loaded check:

```java
if (FabricLoader.getInstance().isModLoaded("dear_diary")) {
    MyCoolModDearDiaryCompat.onFirstAirshipFlight(player);
}
```

Avoid calling this from client-only code. Dear Diary diary data and automatic memories are server-owned.

## Notes

- Do not write to `world/data/dear_diary/players/*.json`.
- Do not send Dear Diary networking payloads yourself.
- Do not load Dear Diary API imports unless Dear Diary is installed.
- Let `AutomaticDiaryEvents.trigger(...)` handle config and trigger policy.
- Keep Fabric-specific detection in Fabric source. NeoForge source should use NeoForge APIs and should not import `net.fabricmc.*`.
