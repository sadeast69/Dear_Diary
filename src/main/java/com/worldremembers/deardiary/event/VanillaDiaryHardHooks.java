package com.worldremembers.deardiary.event;

import com.worldremembers.deardiary.compat.fabric.FabricFarmersDelightCompat;
import java.util.Map;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class VanillaDiaryHardHooks {
    private static final Map<Identifier, String> ADVANCEMENT_EVENTS = Map.of(
            Identifier.of("minecraft", "nether/find_fortress"), VanillaDiaryEventDefinitions.FIRST_NETHER_FORTRESS,
            Identifier.of("minecraft", "nether/find_bastion"), VanillaDiaryEventDefinitions.FIRST_BASTION,
            Identifier.of("minecraft", "end/find_end_city"), VanillaDiaryEventDefinitions.FIRST_END_CITY,
            Identifier.of("minecraft", "adventure/minecraft_trials_edition"), VanillaDiaryEventDefinitions.FIRST_TRIAL_CHAMBER,
            Identifier.of("minecraft", "husbandry/breed_an_animal"), VanillaDiaryEventDefinitions.FIRST_ANIMAL_BREED
    );

    private VanillaDiaryHardHooks() {
    }

    public static void onAdvancementCriterionGranted(ServerPlayerEntity player, AdvancementEntry advancement, boolean granted) {
        if (!granted || player == null || player.isSpectator() || advancement == null) {
            return;
        }

        String eventId = ADVANCEMENT_EVENTS.get(advancement.id());
        if (eventId != null) {
            VanillaDiaryEvents.triggerAndSync(player, eventId);
        }

        FabricFarmersDelightCompat.onAdvancementCriterionGranted(player, advancement, true);
    }

    public static void onHorseTamed(ServerPlayerEntity player) {
        if (player == null || player.isSpectator()) {
            return;
        }

        VanillaDiaryEvents.triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_TAMED_HORSE);
    }

    public static void onRaidStarted(ServerPlayerEntity player) {
        if (player == null || player.isSpectator()) {
            return;
        }

        VanillaDiaryEvents.triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_RAID_STARTED);
    }
}
