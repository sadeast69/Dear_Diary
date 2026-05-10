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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;

/**
 * Fabric-only runtime hooks for Deeper and Darker compatibility.
 */
public final class FabricDeeperDarkerCompat {
    static final String MOD_ID = "deeperdarker";
    private static final int ITEM_SCAN_INTERVAL_TICKS = 40;
    private static final int STRUCTURE_SCAN_INTERVAL_TICKS = 20 * 5;

    private static final Identifier OTHERSIDE_DIMENSION = Identifier.of(MOD_ID, "otherside");
    private static final Identifier STALKER_ENTITY = Identifier.of(MOD_ID, "stalker");
    private static final Identifier SONOROUS_STAFF_ITEM = Identifier.of(MOD_ID, "sonorous_staff");
    private static final Identifier SCULK_TRANSMITTER_ITEM = Identifier.of(MOD_ID, "sculk_transmitter");
    private static final Identifier REINFORCED_ECHO_SHARD_ITEM = Identifier.of(MOD_ID, "reinforced_echo_shard");
    private static final Identifier SOUL_ELYTRA_ITEM = Identifier.of(MOD_ID, "soul_elytra");

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

    private static final Map<Identifier, String> ITEM_ACQUISITION_MEMORIES = Map.of(
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

    private FabricDeeperDarkerCompat() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(
                FabricDeeperDarkerCompat::onPlayerChangedWorld
        );
        ServerLivingEntityEvents.AFTER_DEATH.register(FabricDeeperDarkerCompat::onLivingDeath);
        ServerTickEvents.END_SERVER_TICK.register(FabricDeeperDarkerCompat::onEndServerTick);
        DearDiaryMod.LOGGER.info("Dear Diary Deeper and Darker gameplay callbacks registered for Fabric");
    }

    private static void onPlayerChangedWorld(
            ServerPlayerEntity player,
            ServerWorld origin,
            ServerWorld destination
    ) {
        if (player.isSpectator()) {
            return;
        }

        if (destination.getRegistryKey().getValue().equals(OTHERSIDE_DIMENSION)) {
            triggerAndSync(player, FIRST_OTHERSIDE_ENTRY);
        }
    }

    private static void onLivingDeath(LivingEntity entity, net.minecraft.entity.damage.DamageSource damageSource) {
        if (!(entity.getWorld() instanceof ServerWorld)) {
            return;
        }

        if (!Registries.ENTITY_TYPE.getId(entity.getType()).equals(STALKER_ENTITY)) {
            return;
        }

        Entity attacker = damageSource.getAttacker();
        if (!(attacker instanceof ServerPlayerEntity player) || player.isSpectator()) {
            return;
        }

        triggerAndSync(player, FIRST_STALKER_DEFEATED);
    }

    private static void onEndServerTick(MinecraftServer server) {
        itemScanTicker++;
        if (itemScanTicker >= ITEM_SCAN_INTERVAL_TICKS) {
            itemScanTicker = 0;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                scanItemMemories(player);
            }
        }

        structureScanTicker++;
        if (structureScanTicker < STRUCTURE_SCAN_INTERVAL_TICKS) {
            return;
        }

        structureScanTicker = 0;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            scanStructureVisits(player);
        }
    }

    private static void scanItemMemories(ServerPlayerEntity player) {
        if (player.isSpectator()) {
            return;
        }

        AutomaticEventState state = DearDiaryApi.getDiary(player).automaticEventState();
        if (!needsAnyItemMemory(state)) {
            return;
        }

        Set<String> triggeredThisScan = new HashSet<>();
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (stack.isEmpty()) {
                continue;
            }

            String eventId = ITEM_ACQUISITION_MEMORIES.get(Registries.ITEM.getId(stack.getItem()));
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

    private static void scanStructureVisits(ServerPlayerEntity player) {
        if (player.isSpectator()) {
            return;
        }

        ServerWorld world = player.getServerWorld();
        if (!world.getRegistryKey().getValue().equals(OTHERSIDE_DIMENSION)) {
            return;
        }

        AutomaticEventState state = DearDiaryApi.getDiary(player).automaticEventState();
        if (!needsAnyStructureVisitMemory(state)) {
            return;
        }

        BlockPos pos = player.getBlockPos();
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
                .map(FabricDeeperDarkerCompat::isAllowed)
                .orElse(false);
    }

    private static boolean isInsideStructure(ServerWorld world, BlockPos pos, RegistryKey<Structure> structureKey) {
        Registry<Structure> structures = world.getRegistryManager().get(RegistryKeys.STRUCTURE);
        Structure structure = structures.get(structureKey);
        if (structure == null) {
            return false;
        }

        StructureStart structureStart = world.getStructureAccessor().getStructureContaining(pos, structure);
        return structureStart != null && structureStart.hasChildren();
    }

    private static RegistryKey<Structure> structureKey(String path) {
        return RegistryKey.of(RegistryKeys.STRUCTURE, Identifier.of(MOD_ID, path));
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

    private record StructureVisitMemory(String eventId, RegistryKey<Structure> structureKey) {
    }
}
