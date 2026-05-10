package com.worldremembers.deardiary.compat.neoforge;

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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * NeoForge runtime hooks for Farmer's Delight compatibility.
 */
public final class NeoForgeFarmersDelightCompat {
    static final String MOD_ID = "farmersdelight";

    private static final String COUNTER_MEALS_EATEN = "farmersdelight_meals_eaten";
    private static final String COUNTER_FEASTS_SERVED = "farmersdelight_feasts_served";
    private static final String FIRST_STOVE_PLACED = "farmersdelight:first_stove_placed";
    private static final String FIRST_PANTRY_CABINET = "farmersdelight:first_pantry_cabinet";

    private static final ResourceLocation STOVE_BLOCK_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "stove");
    private static final TagKey<Item> MEALS_TAG =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "meals"));
    private static final TagKey<Block> FEASTS_TAG =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "feasts"));
    private static final TagKey<Block> CABINETS_TAG =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "cabinets"));

    private static final Map<ResourceLocation, String> ADVANCEMENT_MEMORIES = Map.of(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "main/craft_knife"),
            "farmersdelight:first_knife_acquired",
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "main/use_cutting_board"),
            "farmersdelight:first_cutting_board_used",
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "main/place_cooking_pot"),
            "farmersdelight:first_cooking_pot_placed",
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "main/use_skillet"),
            "farmersdelight:first_skillet_cooked",
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "main/eat_nourishing_food"),
            "farmersdelight:first_nourishing_meal",
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "main/place_feast"),
            "farmersdelight:first_feast_served",
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "main/get_rich_soil"),
            "farmersdelight:first_rich_soil_created"
    );

    private static boolean registered;

    private NeoForgeFarmersDelightCompat() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NeoForgeFarmersDelightCompat::onAdvancementEarned);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NeoForgeFarmersDelightCompat::onItemUseFinish);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NeoForgeFarmersDelightCompat::onBlockPlace);
        DearDiaryMod.LOGGER.info("Dear Diary Farmer's Delight gameplay callbacks registered for NeoForge");
    }

    private static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) {
            return;
        }

        String eventId = ADVANCEMENT_MEMORIES.get(event.getAdvancement().id());
        if (eventId == null) {
            return;
        }

        AutomaticEventState state = DearDiaryApi.getDiary(player).automaticEventState();
        if (needsAutomaticMemory(state, eventId)) {
            triggerAndSync(player, eventId);
        }
    }

    private static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) {
            return;
        }

        ItemStack stack = event.getItem();
        if (!stack.isEmpty() && stack.is(MEALS_TAG)) {
            incrementCounterAndSync(player, COUNTER_MEALS_EATEN, 1);
        }
    }

    private static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.isCanceled()
                || event.getLevel().isClientSide()
                || !(event.getEntity() instanceof ServerPlayer player)
                || player.isSpectator()) {
            return;
        }

        BlockState placedState = event.getPlacedBlock();
        if (!placedState.isAir() && placedState.is(FEASTS_TAG)) {
            incrementCounterAndSync(player, COUNTER_FEASTS_SERVED, 1);
        }
        if (!placedState.isAir() && isStove(placedState)) {
            triggerAndSync(player, FIRST_STOVE_PLACED);
        }
        if (!placedState.isAir() && placedState.is(CABINETS_TAG)) {
            triggerAndSync(player, FIRST_PANTRY_CABINET);
        }
    }

    private static boolean isStove(BlockState placedState) {
        return STOVE_BLOCK_ID.equals(BuiltInRegistries.BLOCK.getKey(placedState.getBlock()));
    }

    private static boolean needsAutomaticMemory(AutomaticEventState state, String eventId) {
        if (state.isTriggered(eventId)) {
            return false;
        }

        return DearDiaryEventRegistry.get(eventId)
                .map(NeoForgeFarmersDelightCompat::isAllowed)
                .orElse(false);
    }

    private static boolean isAllowed(AutomaticEventDefinition definition) {
        return DearDiaryServices.config().isAutomaticEventAllowed(
                definition.eventId(),
                definition.category(),
                definition.importance()
        );
    }

    private static void triggerAndSync(ServerPlayer player, String eventId) {
        AutomaticDiaryEvents.trigger(player, eventId).ifPresent(created -> {
            DearDiaryNetworking.sendDiarySnapshot(player);
            DearDiaryNetworking.sendAutomaticEntryNotice(player, created);
        });
    }

    private static List<DiaryEntry> incrementCounterAndSync(ServerPlayer player, String counter, int amount) {
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
