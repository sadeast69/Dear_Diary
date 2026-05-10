package com.worldremembers.deardiary.event;

import java.util.Random;
import net.minecraft.server.level.ServerPlayer;

public record AutomaticEventContext(
        ServerPlayer player,
        AutomaticEventDefinition definition,
        boolean force
) {
    public Random random() {
        long seed = player.getUUID().getMostSignificantBits()
                ^ player.getUUID().getLeastSignificantBits()
                ^ definition.eventId().hashCode()
                ^ player.serverLevel().getGameTime();
        return new Random(seed);
    }
}
