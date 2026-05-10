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
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * NeoForge runtime hooks for Waystones compatibility.
 */
public final class NeoForgeWaystonesCompat {
    static final String MOD_ID = "waystones";

    private static final int ITEM_SCAN_INTERVAL_TICKS = 40;
    private static final String FIRST_WAYSTONE_ACTIVATED = "waystones:first_waystone_activated";
    private static final String FIRST_WAYSTONE_PLACED = "waystones:first_waystone_placed";
    private static final String FIRST_WARP_STONE_ACQUIRED = "waystones:first_warp_stone_acquired";
    private static final String FIRST_RETURN_SCROLL_USED = "waystones:first_return_scroll_used";
    private static final String COUNTER_WAYSTONES_ACTIVATED = "waystones_activated";

    private static final ResourceLocation WAYSTONE_ACTIVATED_STAT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "waystone_activated");
    private static final ResourceLocation WARP_STONE_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "warp_stone");
    private static final ResourceLocation RETURN_SCROLL_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "return_scroll");
    private static final TagKey<Block> WAYSTONES_TAG =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "waystones"));

    private static boolean registered;
    private static int itemScanTicker;

    private NeoForgeWaystonesCompat() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NeoForgeWaystonesCompat::onBlockPlace);
        NeoForge.EVENT_BUS.addListener(NeoForgeWaystonesCompat::onServerTick);
        DearDiaryMod.LOGGER.info("Dear Diary Waystones gameplay callbacks registered for NeoForge");
    }

    public static void onCustomStatAwarded(ServerPlayer player, ResourceLocation statId) {
        if (!registered || player == null || player.isSpectator() || !WAYSTONE_ACTIVATED_STAT.equals(statId)) {
            return;
        }

        triggerAndSync(player, FIRST_WAYSTONE_ACTIVATED);
        incrementCounterAndSync(player, COUNTER_WAYSTONES_ACTIVATED, 1);
    }

    public static void onItemConsumed(ServerPlayer player, ItemStack stack, int amount) {
        if (!registered
                || player == null
                || player.isSpectator()
                || amount <= 0
                || stack == null
                || stack.isEmpty()) {
            return;
        }

        if (RETURN_SCROLL_ITEM.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
            triggerAndSync(player, FIRST_RETURN_SCROLL_USED);
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
        if (!placedState.isAir() && placedState.is(WAYSTONES_TAG)) {
            triggerAndSync(player, FIRST_WAYSTONE_PLACED);
        }
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();

        itemScanTicker++;
        if (itemScanTicker < ITEM_SCAN_INTERVAL_TICKS) {
            return;
        }

        itemScanTicker = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            scanItemMemories(player);
        }
    }

    private static void scanItemMemories(ServerPlayer player) {
        if (player.isSpectator()) {
            return;
        }

        AutomaticEventState state = DearDiaryApi.getDiary(player).automaticEventState();
        if (!needsAutomaticMemory(state, FIRST_WARP_STONE_ACQUIRED)) {
            return;
        }

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && WARP_STONE_ITEM.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
                triggerAndSync(player, FIRST_WARP_STONE_ACQUIRED);
                return;
            }
        }
    }

    private static boolean needsAutomaticMemory(AutomaticEventState state, String eventId) {
        if (state.isTriggered(eventId)) {
            return false;
        }

        return DearDiaryEventRegistry.get(eventId)
                .map(NeoForgeWaystonesCompat::isAllowed)
                .orElse(false);
    }

    private static boolean isAllowed(AutomaticEventDefinition definition) {
        return DearDiaryServices.config().isAutomaticEventAllowed(
                definition.eventId(),
                definition.category(),
                definition.importance()
        );
    }

    private static Optional<DiaryEntry> triggerAndSync(ServerPlayer player, String eventId) {
        Optional<DiaryEntry> entry = AutomaticDiaryEvents.trigger(player, eventId);
        entry.ifPresent(created -> {
            DearDiaryNetworking.sendDiarySnapshot(player);
            DearDiaryNetworking.sendAutomaticEntryNotice(player, created);
        });
        return entry;
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
