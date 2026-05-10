package com.worldremembers.deardiary.event;

/**
 * Per-player anti-spam policy for an automatic diary event.
 */
public enum TriggerPolicy {
    /**
     * Create at most one memory with this event id for each player.
     */
    ONCE_PER_PLAYER,

    /**
     * Allow repeat memories after the definition cooldown has elapsed.
     */
    COOLDOWN,

    /**
     * Create a memory when a named counter reaches the definition threshold.
     */
    MILESTONE,

    /**
     * Reserved for future biome-aware memories. Not implemented by the current
     * runtime.
     */
    ONCE_PER_BIOME,

    /**
     * Reserved for future structure-aware memories. Not implemented by the
     * current runtime.
     */
    ONCE_PER_STRUCTURE_TYPE
}
