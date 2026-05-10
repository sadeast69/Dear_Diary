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
import java.util.Optional;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Fabric runtime hooks for Waystones compatibility.
 */
public final class FabricWaystonesCompat {
    public static final String MOD_ID = "waystones";

    private static final int ITEM_SCAN_INTERVAL_TICKS = 40;
    private static final String FIRST_WAYSTONE_ACTIVATED = "waystones:first_waystone_activated";
    private static final String FIRST_WAYSTONE_PLACED = "waystones:first_waystone_placed";
    private static final String FIRST_WARP_STONE_ACQUIRED = "waystones:first_warp_stone_acquired";
    private static final String FIRST_RETURN_SCROLL_USED = "waystones:first_return_scroll_used";
    private static final String COUNTER_WAYSTONES_ACTIVATED = "waystones_activated";

    private static final Identifier WAYSTONE_ACTIVATED_STAT = Identifier.of(MOD_ID, "waystone_activated");
    private static final Identifier WARP_STONE_ITEM = Identifier.of(MOD_ID, "warp_stone");
    private static final Identifier RETURN_SCROLL_ITEM = Identifier.of(MOD_ID, "return_scroll");
    private static final TagKey<Block> WAYSTONES_TAG =
            TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "waystones"));

    private static boolean registered;
    private static int itemScanTicker;

    private FabricWaystonesCompat() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        ServerTickEvents.END_SERVER_TICK.register(FabricWaystonesCompat::onEndServerTick);
        DearDiaryMod.LOGGER.info("Dear Diary Waystones gameplay callbacks registered for Fabric");
    }

    public static void onCustomStatAwarded(ServerPlayerEntity player, Identifier statId) {
        if (!registered || player == null || player.isSpectator() || !WAYSTONE_ACTIVATED_STAT.equals(statId)) {
            return;
        }

        triggerAndSync(player, FIRST_WAYSTONE_ACTIVATED);
        incrementCounterAndSync(player, COUNTER_WAYSTONES_ACTIVATED, 1);
    }

    public static void onBlockPlaced(ServerPlayerEntity player, BlockState placedState) {
        if (!registered || player == null || player.isSpectator() || placedState == null || placedState.isAir()) {
            return;
        }

        if (placedState.isIn(WAYSTONES_TAG)) {
            triggerAndSync(player, FIRST_WAYSTONE_PLACED);
        }
    }

    public static void onItemConsumed(ServerPlayerEntity player, ItemStack stack, int amount) {
        if (!registered
                || player == null
                || player.isSpectator()
                || amount <= 0
                || stack == null
                || stack.isEmpty()) {
            return;
        }

        if (RETURN_SCROLL_ITEM.equals(Registries.ITEM.getId(stack.getItem()))) {
            triggerAndSync(player, FIRST_RETURN_SCROLL_USED);
        }
    }

    private static void onEndServerTick(MinecraftServer server) {
        itemScanTicker++;
        if (itemScanTicker < ITEM_SCAN_INTERVAL_TICKS) {
            return;
        }

        itemScanTicker = 0;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            scanItemMemories(player);
        }
    }

    private static void scanItemMemories(ServerPlayerEntity player) {
        if (player.isSpectator()) {
            return;
        }

        AutomaticEventState state = DearDiaryApi.getDiary(player).automaticEventState();
        if (!needsAutomaticMemory(state, FIRST_WARP_STONE_ACQUIRED)) {
            return;
        }

        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (!stack.isEmpty() && WARP_STONE_ITEM.equals(Registries.ITEM.getId(stack.getItem()))) {
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
                .map(FabricWaystonesCompat::isAllowed)
                .orElse(false);
    }

    private static boolean isAllowed(AutomaticEventDefinition definition) {
        return DearDiaryServices.config().isAutomaticEventAllowed(
                definition.eventId(),
                definition.category(),
                definition.importance()
        );
    }

    private static Optional<DiaryEntry> triggerAndSync(ServerPlayerEntity player, String eventId) {
        Optional<DiaryEntry> entry = AutomaticDiaryEvents.trigger(player, eventId);
        entry.ifPresent(created -> {
            DearDiaryNetworking.sendDiarySnapshot(player);
            DearDiaryNetworking.sendAutomaticEntryNotice(player, created);
        });
        return entry;
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
