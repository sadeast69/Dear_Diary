package com.worldremembers.deardiary.compat;

/**
 * Loader-neutral view of the runtime environment for optional integrations.
 */
public interface CompatEnvironment {
    /**
     * Returns whether an optional mod is present on the active loader.
     */
    boolean isModLoaded(String modId);

    /**
     * Human-readable loader name for diagnostics.
     */
    String loaderName();
}
