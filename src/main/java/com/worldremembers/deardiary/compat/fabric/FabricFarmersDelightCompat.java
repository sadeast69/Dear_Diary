package com.worldremembers.deardiary.compat.fabric;

import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.DearDiaryServices;
import com.worldremembers.deardiary.api.DearDiaryApi;
import com.worldremembers.deardiary.data.AutomaticEventState;
import com.worldremembers.deardiary.data.DiaryEntry;
import com.worldremembers.deardiary.event.AutomaticDiaryEvents;
import com.worldremembers.deardiary.event.AutomaticEventDefinition;
import com.worldremembers.deardiary.event.DearDiaryEventRegistry;
import com.worldremembers.deardiary.network.DearDiaryNetworking;
import java.util.List;
import java.util.Map;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Fabric runtime hooks for Farmer's Delight compatibility.
 */
public final class FabricFarmersDelightCompat {
    public static final String MOD_ID = "farmersdelight";

    private static final String COUNTER_MEALS_EATEN = "farmersdelight_meals_eaten";
    private static final String COUNTER_FEASTS_SERVED = "farmersdelight_feasts_served";
    private static final String FIRST_STOVE_PLACED = "farmersdelight:first_stove_placed";
    private static final String FIRST_PANTRY_CABINET = "farmersdelight:first_pantry_cabinet";

    private static final Identifier STOVE_BLOCK_ID = Identifier.of(MOD_ID, "stove");
    private static final TagKey<Item> MEALS_TAG =
            TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "meals"));
    private static final TagKey<Block> FEASTS_TAG =
            TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "feasts"));
    private static final TagKey<Block> CABINETS_TAG =
            TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "cabinets"));

    private static final Map<Identifier, String> ADVANCEMENT_MEMORIES = Map.of(
            Identifier.of(MOD_ID, "main/craft_knife"), "farmersdelight:first_knife_acquired",
            Identifier.of(MOD_ID, "main/use_cutting_board"), "farmersdelight:first_cutting_board_used",
            Identifier.of(MOD_ID, "main/place_cooking_pot"), "farmersdelight:first_cooking_pot_placed",
            Identifier.of(MOD_ID, "main/use_skillet"), "farmersdelight:first_skillet_cooked",
            Identifier.of(MOD_ID, "main/eat_nourishing_food"), "farmersdelight:first_nourishing_meal",
            Identifier.of(MOD_ID, "main/place_feast"), "farmersdelight:first_feast_served",
            Identifier.of(MOD_ID, "main/get_rich_soil"), "farmersdelight:first_rich_soil_created"
    );

    private static boolean registered;

    private FabricFarmersDelightCompat() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        DearDiaryMod.LOGGER.info("Dear Diary Farmer's Delight gameplay callbacks registered for Fabric");
    }

    public static void onAdvancementCriterionGranted(
            ServerPlayerEntity player,
            AdvancementEntry advancement,
            boolean granted
    ) {
        if (!registered || !granted || player == null || player.isSpectator() || advancement == null) {
            return;
        }

        String eventId = ADVANCEMENT_MEMORIES.get(advancement.id());
        if (eventId == null) {
            return;
        }

        AutomaticEventState state = DearDiaryApi.getDiary(player).automaticEventState();
        if (needsAutomaticMemory(state, eventId)) {
            triggerAndSync(player, eventId);
        }
    }

    public static void onFinishedUsingItem(ServerPlayerEntity player, ItemStack stack) {
        if (!registered || player == null || player.isSpectator() || stack == null || stack.isEmpty()) {
            return;
        }

        if (stack.isIn(MEALS_TAG)) {
            incrementCounterAndSync(player, COUNTER_MEALS_EATEN, 1);
        }
    }

    public static void onBlockPlaced(ServerPlayerEntity player, BlockState placedState) {
        if (!registered || player == null || player.isSpectator() || placedState == null || placedState.isAir()) {
            return;
        }

        if (placedState.isIn(FEASTS_TAG)) {
            incrementCounterAndSync(player, COUNTER_FEASTS_SERVED, 1);
        }
        if (isStove(placedState)) {
            triggerAndSync(player, FIRST_STOVE_PLACED);
        }
        if (placedState.isIn(CABINETS_TAG)) {
            triggerAndSync(player, FIRST_PANTRY_CABINET);
        }
    }

    private static boolean isStove(BlockState placedState) {
        return STOVE_BLOCK_ID.equals(Registries.BLOCK.getId(placedState.getBlock()));
    }

    private static boolean needsAutomaticMemory(AutomaticEventState state, String eventId) {
        if (state.isTriggered(eventId)) {
            return false;
        }

        return DearDiaryEventRegistry.get(eventId)
                .map(FabricFarmersDelightCompat::isAllowed)
                .orElse(false);
    }

    private static boolean isAllowed(AutomaticEventDefinition definition) {
        return DearDiaryServices.config().isAutomaticEventAllowed(
                definition.eventId(),
                definition.category(),
                definition.importance()
        );
    }

    private static void triggerAndSync(ServerPlayerEntity player, String eventId) {
        AutomaticDiaryEvents.trigger(player, eventId).ifPresent(created -> {
            DearDiaryNetworking.sendDiarySnapshot(player);
            DearDiaryNetworking.sendAutomaticEntryNotice(player, created);
        });
    }

    private static List<DiaryEntry> incrementCounterAndSync(ServerPlayerEntity player, String counter, int amount) {
        List<DiaryEntry> entries = AutomaticDiaryEvents.incrementCounterAndTriggerMilestones(player, counter, amount);
        if (!entries.isEmpty()) {
            DearDiaryNetworking.sendDiarySnapshot(player);
            for (DiaryEntry entry : entries) {
                DearDiaryNetworking.sendAutomaticEntryNotice(player, entry);
            }
        }
        return entries;
    }
}
