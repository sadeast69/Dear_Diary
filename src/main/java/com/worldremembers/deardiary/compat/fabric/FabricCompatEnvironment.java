package com.worldremembers.deardiary.compat.fabric;

import com.worldremembers.deardiary.compat.CompatEnvironment;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Fabric implementation of optional mod detection for Dear Diary compat.
 */
public final class FabricCompatEnvironment implements CompatEnvironment {
    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public String loaderName() {
        return "Fabric";
    }
}
