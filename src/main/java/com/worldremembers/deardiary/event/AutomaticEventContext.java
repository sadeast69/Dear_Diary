package com.worldremembers.deardiary.event;

import java.util.Random;
import net.minecraft.server.network.ServerPlayerEntity;

public record AutomaticEventContext(
        ServerPlayerEntity player,
        AutomaticEventDefinition definition,
        boolean force
) {
    public Random random() {
        long seed = player.getUuid().getMostSignificantBits()
                ^ player.getUuid().getLeastSignificantBits()
                ^ definition.eventId().hashCode()
                ^ player.getServerWorld().getTime();
        return new Random(seed);
    }
}
