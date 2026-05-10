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
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * NeoForge-only runtime hooks for Cataclysm compatibility.
 */
public final class NeoForgeCataclysmCompat {
    static final String MOD_ID = "cataclysm";
    private static final int ITEM_SCAN_INTERVAL_TICKS = 40;
    private static final int STRUCTURE_SCAN_INTERVAL_TICKS = 20 * 5;

    private static final ResourceLocation OVERWORLD_DIMENSION =
            ResourceLocation.fromNamespaceAndPath("minecraft", "overworld");
    private static final ResourceLocation NETHER_DIMENSION =
            ResourceLocation.fromNamespaceAndPath("minecraft", "the_nether");
    private static final ResourceLocation END_DIMENSION =
            ResourceLocation.fromNamespaceAndPath("minecraft", "the_end");
    private static final ResourceLocation ENDER_GUARDIAN_ENTITY =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "ender_guardian");
    private static final ResourceLocation NETHERITE_MONSTROSITY_ENTITY =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "netherite_monstrosity");
    private static final ResourceLocation IGNIS_ENTITY =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "ignis");
    private static final ResourceLocation HARBINGER_ENTITY =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "the_harbinger");
    private static final ResourceLocation LEVIATHAN_ENTITY =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "the_leviathan");
    private static final ResourceLocation ANCIENT_REMNANT_ENTITY =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "ancient_remnant");
    private static final ResourceLocation MALEDICTUS_ENTITY =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "maledictus");
    private static final ResourceLocation SCYLLA_ENTITY =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "scylla");
    private static final ResourceLocation GAUNTLET_OF_GUARD_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "gauntlet_of_guard");

    private static final String FIRST_RUINED_CITADEL_VISIT =
            "cataclysm:first_ruined_citadel_visit";
    private static final String FIRST_ENDER_GUARDIAN_DEFEATED =
            "cataclysm:first_ender_guardian_defeated";
    private static final String FIRST_NETHERITE_MONSTROSITY_DEFEATED =
            "cataclysm:first_netherite_monstrosity_defeated";
    private static final String FIRST_IGNIS_DEFEATED = "cataclysm:first_ignis_defeated";
    private static final String FIRST_GAUNTLET_OF_GUARD_ACQUIRED =
            "cataclysm:first_gauntlet_of_guard_acquired";
    private static final String FIRST_ANCIENT_FACTORY_VISIT =
            "cataclysm:first_ancient_factory_visit";
    private static final String FIRST_SUNKEN_CITY_VISIT = "cataclysm:first_sunken_city_visit";
    private static final String FIRST_CURSED_PYRAMID_VISIT =
            "cataclysm:first_cursed_pyramid_visit";
    private static final String FIRST_HARBINGER_DEFEATED =
            "cataclysm:first_harbinger_defeated";
    private static final String FIRST_LEVIATHAN_DEFEATED =
            "cataclysm:first_leviathan_defeated";
    private static final String FIRST_ANCIENT_REMNANT_DEFEATED =
            "cataclysm:first_ancient_remnant_defeated";
    private static final String FIRST_MALEDICTUS_DEFEATED =
            "cataclysm:first_maledictus_defeated";
    private static final String FIRST_SCYLLA_DEFEATED = "cataclysm:first_scylla_defeated";
    private static final String FIRST_FROSTED_PRISON_VISIT =
            "cataclysm:first_frosted_prison_visit";
    private static final String FIRST_ACROPOLIS_VISIT = "cataclysm:first_acropolis_visit";
    private static final String FIRST_BURNING_ARENA_VISIT =
            "cataclysm:first_burning_arena_visit";
    private static final String FIRST_SOUL_BLACK_SMITH_VISIT =
            "cataclysm:first_soul_black_smith_visit";

    private static final Map<ResourceLocation, String> BOSS_DEFEAT_MEMORIES = Map.of(
            ENDER_GUARDIAN_ENTITY, FIRST_ENDER_GUARDIAN_DEFEATED,
            NETHERITE_MONSTROSITY_ENTITY, FIRST_NETHERITE_MONSTROSITY_DEFEATED,
            IGNIS_ENTITY, FIRST_IGNIS_DEFEATED,
            HARBINGER_ENTITY, FIRST_HARBINGER_DEFEATED,
            LEVIATHAN_ENTITY, FIRST_LEVIATHAN_DEFEATED,
            ANCIENT_REMNANT_ENTITY, FIRST_ANCIENT_REMNANT_DEFEATED,
            MALEDICTUS_ENTITY, FIRST_MALEDICTUS_DEFEATED,
            SCYLLA_ENTITY, FIRST_SCYLLA_DEFEATED
    );

    private static final Map<ResourceLocation, String> ITEM_ACQUISITION_MEMORIES = Map.of(
            GAUNTLET_OF_GUARD_ITEM, FIRST_GAUNTLET_OF_GUARD_ACQUIRED
    );

    private static final List<StructureVisitMemory> STRUCTURE_VISIT_MEMORIES = List.of(
            new StructureVisitMemory(
                    FIRST_RUINED_CITADEL_VISIT,
                    END_DIMENSION,
                    structureKey("ruined_citadel")
            ),
            new StructureVisitMemory(
                    FIRST_ANCIENT_FACTORY_VISIT,
                    OVERWORLD_DIMENSION,
                    structureKey("ancient_factory")
            ),
            new StructureVisitMemory(
                    FIRST_SUNKEN_CITY_VISIT,
                    OVERWORLD_DIMENSION,
                    structureKey("sunken_city")
            ),
            new StructureVisitMemory(
                    FIRST_CURSED_PYRAMID_VISIT,
                    OVERWORLD_DIMENSION,
                    structureKey("cursed_pyramid")
            ),
            new StructureVisitMemory(
                    FIRST_FROSTED_PRISON_VISIT,
                    OVERWORLD_DIMENSION,
                    structureKey("frosted_prison")
            ),
            new StructureVisitMemory(
                    FIRST_ACROPOLIS_VISIT,
                    OVERWORLD_DIMENSION,
                    structureKey("acropolis")
            ),
            new StructureVisitMemory(
                    FIRST_BURNING_ARENA_VISIT,
                    NETHER_DIMENSION,
                    structureKey("burning_arena")
            ),
            new StructureVisitMemory(
                    FIRST_SOUL_BLACK_SMITH_VISIT,
                    NETHER_DIMENSION,
                    structureKey("soul_black_smith")
            )
    );

    private static boolean registered;
    private static int itemScanTicker;
    private static int structureScanTicker;

    private NeoForgeCataclysmCompat() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NeoForgeCataclysmCompat::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(NeoForgeCataclysmCompat::onServerTick);
        DearDiaryMod.LOGGER.info("Dear Diary Cataclysm gameplay callbacks registered for NeoForge");
    }

    private static void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled()) {
            return;
        }

        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel)) {
            return;
        }

        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        String eventId = BOSS_DEFEAT_MEMORIES.get(entityId);
        if (eventId == null) {
            return;
        }

        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof ServerPlayer player) || player.isSpectator()) {
            return;
        }

        triggerAndSync(player, eventId);
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
                    && triggeredThisScan.add(eventId)
                    && needsAutomaticMemory(state, eventId)) {
                triggerAndSync(player, eventId);
            }
        }
    }

    private static boolean needsAnyItemMemory(AutomaticEventState state) {
        for (String eventId : ITEM_ACQUISITION_MEMORIES.values()) {
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
        ResourceLocation dimensionId = world.dimension().location();

        AutomaticEventState state = DearDiaryApi.getDiary(player).automaticEventState();
        if (!needsAnyStructureVisitMemory(state, dimensionId)) {
            return;
        }

        BlockPos pos = player.blockPosition();
        for (StructureVisitMemory memory : STRUCTURE_VISIT_MEMORIES) {
            if (memory.dimensionId().equals(dimensionId)
                    && needsAutomaticMemory(state, memory.eventId())
                    && isInsideStructure(world, pos, memory.structureKey())) {
                triggerAndSync(player, memory.eventId());
            }
        }
    }

    private static boolean needsAnyStructureVisitMemory(
            AutomaticEventState state,
            ResourceLocation dimensionId
    ) {
        for (StructureVisitMemory memory : STRUCTURE_VISIT_MEMORIES) {
            if (memory.dimensionId().equals(dimensionId)
                    && needsAutomaticMemory(state, memory.eventId())) {
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
                .map(NeoForgeCataclysmCompat::isAllowed)
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

    private record StructureVisitMemory(
            String eventId,
            ResourceLocation dimensionId,
            ResourceKey<Structure> structureKey
    ) {
    }
}
