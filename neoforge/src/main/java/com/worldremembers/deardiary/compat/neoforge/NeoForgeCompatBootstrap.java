package com.worldremembers.deardiary.compat.neoforge;

import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.compat.ContentCompat;

/**
 * NeoForge entrypoint for optional content compatibility scaffolding.
 */
public final class NeoForgeCompatBootstrap {
    private NeoForgeCompatBootstrap() {
    }

    public static void register() {
        NeoForgeCompatEnvironment environment = new NeoForgeCompatEnvironment();
        ContentCompat.register(environment);
        if (environment.isModLoaded(NeoForgeTwilightForestCompat.MOD_ID)) {
            NeoForgeTwilightForestCompat.register();
        }
        if (environment.isModLoaded(NeoForgeAetherCompat.MOD_ID)) {
            NeoForgeAetherCompat.register();
        }
        if (environment.isModLoaded(NeoForgeDeeperDarkerCompat.MOD_ID)) {
            NeoForgeDeeperDarkerCompat.register();
        }
        if (environment.isModLoaded(NeoForgeIronsSpellsCompat.MOD_ID)) {
            NeoForgeIronsSpellsCompat.register();
        }
        if (environment.isModLoaded(NeoForgeCataclysmCompat.MOD_ID)) {
            NeoForgeCataclysmCompat.register();
        }
        if (environment.isModLoaded(NeoForgeCreateCompat.MOD_ID)) {
            NeoForgeCreateCompat.register();
        }
        if (environment.isModLoaded(NeoForgeFarmersDelightCompat.MOD_ID)) {
            NeoForgeFarmersDelightCompat.register();
        }
        if (environment.isModLoaded(NeoForgeWaystonesCompat.MOD_ID)) {
            NeoForgeWaystonesCompat.register();
        }
        if (environment.isModLoaded(NeoForgeSporeCompat.MOD_ID)) {
            NeoForgeSporeCompat.register();
        }
        if (environment.isModLoaded(NeoForgeScorchedGunsCompat.MOD_ID)) {
            NeoForgeScorchedGunsCompat.register();
        }
        DearDiaryMod.LOGGER.debug("Dear Diary content compat scaffold initialized for {}", environment.loaderName());
    }
}
