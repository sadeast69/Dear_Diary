package com.worldremembers.deardiary.compat.neoforge;

import com.worldremembers.deardiary.compat.CompatEnvironment;
import net.neoforged.fml.ModList;

/**
 * NeoForge implementation of optional mod detection for Dear Diary compat.
 */
public final class NeoForgeCompatEnvironment implements CompatEnvironment {
    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public String loaderName() {
        return "NeoForge";
    }
}
