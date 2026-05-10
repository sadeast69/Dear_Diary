package com.worldremembers.deardiary.compat.fabric;

import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.compat.ContentCompat;

/**
 * Fabric entrypoint for optional content compatibility scaffolding.
 */
public final class FabricCompatBootstrap {
    private FabricCompatBootstrap() {
    }

    public static void register() {
        FabricCompatEnvironment environment = new FabricCompatEnvironment();
        ContentCompat.register(environment);
        if (environment.isModLoaded(FabricAetherCompat.MOD_ID)) {
            FabricAetherCompat.register();
        }
        if (environment.isModLoaded(FabricDeeperDarkerCompat.MOD_ID)) {
            FabricDeeperDarkerCompat.register();
        }
        if (environment.isModLoaded(FabricFarmersDelightCompat.MOD_ID)) {
            FabricFarmersDelightCompat.register();
        }
        if (environment.isModLoaded(FabricWaystonesCompat.MOD_ID)) {
            FabricWaystonesCompat.register();
        }
        DearDiaryMod.LOGGER.debug("Dear Diary content compat scaffold initialized for {}", environment.loaderName());
    }
}
