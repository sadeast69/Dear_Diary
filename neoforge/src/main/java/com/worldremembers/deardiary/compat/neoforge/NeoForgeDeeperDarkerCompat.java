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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * NeoForge-only runtime hooks for Deeper and Darker compatibility.
 */
public final class NeoForgeDeeperDarkerCompat {
    static final String MOD_ID = "deeperdarker";
    private static final int ITEM_SCAN_INTERVAL_TICKS = 40;
    private static final int STRUCTURE_SCAN_INTERVAL_TICKS = 20 * 5;

    private static final ResourceLocation OTHERSIDE_DIMENSION =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "otherside");
    private static final ResourceLocation STALKER_ENTITY =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "stalker");
    private static final ResourceLocation SONOROUS_STAFF_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "sonorous_staff");
    private static final ResourceLocation SCULK_TRANSMITTER_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "sculk_transmitter");
    private static final ResourceLocation REINFORCED_ECHO_SHARD_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "reinforced_echo_shard");
    private static final ResourceLocation SOUL_ELYTRA_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "soul_elytra");

    private static final String FIRST_OTHERSIDE_ENTRY = "deeperdarker:first_otherside_entry";
    private static final String FIRST_STALKER_DEFEATED = "deeperdarker:first_stalker_defeated";
    private static final String FIRST_SONOROUS_STAFF_ACQUIRED =
            "deeperdarker:first_sonorous_staff_acquired";
    private static final String FIRST_ANCIENT_TEMPLE_VISIT =
            "deeperdarker:first_ancient_temple_visit";
    private static final String FIRST_SCULK_TRANSMITTER = "deeperdarker:first_sculk_transmitter";
    private static final String FIRST_REINFORCED_ECHO_SHARD =
            "deeperdarker:first_reinforced_echo_shard";
    private static final String FIRST_SOUL_ELYTRA = "deeperdarker:first_soul_elytra";

    private static final Map<ResourceLocation, String> ITEM_ACQUISITION_MEMORIES = Map.of(
            SONOROUS_STAFF_ITEM, FIRST_SONOROUS_STAFF_ACQUIRED,
            SCULK_TRANSMITTER_ITEM, FIRST_SCULK_TRANSMITTER,
            REINFORCED_ECHO_SHARD_ITEM, FIRST_REINFORCED_ECHO_SHARD,
            SOUL_ELYTRA_ITEM, FIRST_SOUL_ELYTRA
    );

    private static final List<String> ITEM_MEMORY_EVENTS = List.of(
            FIRST_SONOROUS_STAFF_ACQUIRED,
            FIRST_SCULK_TRANSMITTER,
            FIRST_REINFORCED_ECHO_SHARD,
            FIRST_SOUL_ELYTRA
    );

    private static final List<StructureVisitMemory> STRUCTURE_VISIT_MEMORIES = List.of(
            new StructureVisitMemory(FIRST_ANCIENT_TEMPLE_VISIT, structureKey("ancient_temple"))
    );

    private static boolean registered;
    private static int itemScanTicker;
    private static int structureScanTicker;

    private NeoForgeDeeperDarkerCompat() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NeoForgeDeeperDarkerCompat::onPlayerChangedDimension);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NeoForgeDeeperDarkerCompat::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(NeoForgeDeeperDarkerCompat::onServerTick);
        DearDiaryMod.LOGGER.info("Dear Diary Deeper and Darker gameplay callbacks registered for NeoForge");
    }

    private static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) {
            return;
        }

        if (event.getTo().location().equals(OTHERSIDE_DIMENSION)) {
            triggerAndSync(player, FIRST_OTHERSIDE_ENTRY);
        }
    }

    private static void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled()) {
            return;
        }

        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel)) {
            return;
        }

        if (!BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).equals(STALKER_ENTITY)) {
            return;
        }

        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof ServerPlayer player) || player.isSpectator()) {
            return;
        }

        triggerAndSync(player, FIRST_STALKER_DEFEATED);
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();

        itemScanTicker++;
        if (itemScanTicker >= ITEM_SCAN_INTERVAL_TICKS) {
            itemScanTicker = 0;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                scanItemMemories(player);
            }
        }

        structureScanTicker++;
        if (structureScanTicker < STRUCTURE_SCAN_INTERVAL_TICKS) {
            return;
        }

        structureScanTicker = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            scanStructureVisits(player);
        }
    }

    private static void scanItemMemories(ServerPlayer player) {
        if (player.isSpectator()) {
            return;
        }

        AutomaticEventState state = DearDiaryApi.getDiary(player).automaticEventState();
        if (!needsAnyItemMemory(state)) {
            return;
        }

        Set<String> triggeredThisScan = new HashSet<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            String eventId = ITEM_ACQUISITION_MEMORIES.get(BuiltInRegistries.ITEM.getKey(stack.getItem()));
            if (eventId != null
                    && !triggeredThisScan.contains(eventId)
                    && needsAutomaticMemory(state, eventId)) {
                triggerAndSync(player, eventId);
                triggeredThisScan.add(eventId);
            }
        }
    }

    private static boolean needsAnyItemMemory(AutomaticEventState state) {
        for (String eventId : ITEM_MEMORY_EVENTS) {
            if (needsAutomaticMemory(state, eventId)) {
                return true;
            }
        }

        return false;
    }

    private static void scanStructureVisits(ServerPlayer player) {
        if (player.isSpectator()) {
            return;
        }

        ServerLevel world = player.serverLevel();
        if (!world.dimension().location().equals(OTHERSIDE_DIMENSION)) {
            return;
        }

        AutomaticEventState state = DearDiaryApi.getDiary(player).automaticEventState();
        if (!needsAnyStructureVisitMemory(state)) {
            return;
        }

        BlockPos pos = player.blockPosition();
        for (StructureVisitMemory memory : STRUCTURE_VISIT_MEMORIES) {
            if (needsAutomaticMemory(state, memory.eventId())
                    && isInsideStructure(world, pos, memory.structureKey())) {
                triggerAndSync(player, memory.eventId());
            }
        }
    }

    private static boolean needsAnyStructureVisitMemory(AutomaticEventState state) {
        for (StructureVisitMemory memory : STRUCTURE_VISIT_MEMORIES) {
            if (needsAutomaticMemory(state, memory.eventId())) {
                return true;
            }
        }

        return false;
    }

    private static boolean needsAutomaticMemory(AutomaticEventState state, String eventId) {
        if (state.isTriggered(eventId)) {
            return false;
        }

        return DearDiaryEventRegistry.get(eventId)
                .map(NeoForgeDeeperDarkerCompat::isAllowed)
                .orElse(false);
    }

    private static boolean isInsideStructure(
            ServerLevel world,
            BlockPos pos,
            ResourceKey<Structure> structureKey
    ) {
        Structure structure = world.registryAccess().registryOrThrow(Registries.STRUCTURE).get(structureKey);
        if (structure == null) {
            return false;
        }

        StructureStart structureStart = world.structureManager().getStructureWithPieceAt(pos, structure);
        return structureStart != null && structureStart.isValid();
    }

    private static ResourceKey<Structure> structureKey(String path) {
        return ResourceKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(MOD_ID, path));
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

    private record StructureVisitMemory(String eventId, ResourceKey<Structure> structureKey) {
    }
}
