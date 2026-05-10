package com.worldremembers.deardiary.compat.neoforge;

import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.DearDiaryServices;
import com.worldremembers.deardiary.api.DearDiaryApi;
import com.worldremembers.deardiary.data.AutomaticEventState;
import com.worldremembers.deardiary.event.AutomaticEventDefinition;
import com.worldremembers.deardiary.event.AutomaticDiaryEvents;
import com.worldremembers.deardiary.event.DearDiaryEventRegistry;
import com.worldremembers.deardiary.network.DearDiaryNetworking;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * NeoForge-only runtime hooks for The Twilight Forest compatibility.
 */
public final class NeoForgeTwilightForestCompat {
    static final String MOD_ID = "twilightforest";
    private static final int ITEM_SCAN_INTERVAL_TICKS = 40;
    private static final int STRUCTURE_SCAN_INTERVAL_TICKS = 20 * 5;

    private static final ResourceLocation TWILIGHT_FOREST_DIMENSION =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "twilight_forest");
    private static final ResourceLocation NAGA_ENTITY =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "naga");
    private static final ResourceLocation LICH_ENTITY =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "lich");
    private static final ResourceLocation MINOSHROOM_ENTITY =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "minoshroom");
    private static final ResourceLocation HYDRA_ENTITY =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "hydra");
    private static final ResourceLocation UR_GHAST_ENTITY =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "ur_ghast");
    private static final ResourceLocation ALPHA_YETI_ENTITY =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "alpha_yeti");
    private static final ResourceLocation SNOW_QUEEN_ENTITY =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "snow_queen");
    private static final ResourceLocation MAGIC_MAP_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_map");
    private static final ResourceLocation FILLED_MAGIC_MAP_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "filled_magic_map");
    private static final ResourceLocation MAZE_MAP_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "maze_map");
    private static final ResourceLocation FILLED_MAZE_MAP_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "filled_maze_map");
    private static final ResourceLocation CHARM_OF_LIFE_1_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "charm_of_life_1");
    private static final ResourceLocation CHARM_OF_LIFE_2_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "charm_of_life_2");
    private static final ResourceLocation CHARM_OF_KEEPING_1_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "charm_of_keeping_1");
    private static final ResourceLocation CHARM_OF_KEEPING_2_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "charm_of_keeping_2");
    private static final ResourceLocation CHARM_OF_KEEPING_3_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "charm_of_keeping_3");
    private static final ResourceLocation MAZEBREAKER_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "mazebreaker_pickaxe");
    private static final ResourceLocation CRUMBLE_HORN_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "crumble_horn");
    private static final ResourceLocation MAGIC_BEANS_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "magic_beans");
    private static final ResourceLocation PROGRESS_KNIGHTS_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "progress_knights");
    private static final ResourceLocation PROGRESS_TROLL_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "progress_troll");

    private static final String FIRST_TWILIGHT_FOREST_ENTRY = "twilightforest:first_twilight_forest_entry";
    private static final String FIRST_NAGA_DEFEATED = "twilightforest:first_naga_defeated";
    private static final String FIRST_LICH_DEFEATED = "twilightforest:first_lich_defeated";
    private static final String FIRST_NAGA_COURTYARD_VISIT = "twilightforest:first_naga_courtyard_visit";
    private static final String FIRST_LICH_TOWER_VISIT = "twilightforest:first_lich_tower_visit";
    private static final String FIRST_LABYRINTH_VISIT = "twilightforest:first_labyrinth_visit";
    private static final String FIRST_DARK_TOWER_VISIT = "twilightforest:first_dark_tower_visit";
    private static final String FIRST_AURORA_PALACE_VISIT = "twilightforest:first_aurora_palace_visit";
    private static final String FIRST_MINOSHROOM_DEFEATED = "twilightforest:first_minoshroom_defeated";
    private static final String FIRST_HYDRA_DEFEATED = "twilightforest:first_hydra_defeated";
    private static final String FIRST_UR_GHAST_DEFEATED = "twilightforest:first_ur_ghast_defeated";
    private static final String FIRST_ALPHA_YETI_DEFEATED = "twilightforest:first_alpha_yeti_defeated";
    private static final String FIRST_SNOW_QUEEN_DEFEATED = "twilightforest:first_snow_queen_defeated";
    private static final String FIRST_MAGIC_MAP = "twilightforest:first_magic_map";
    private static final String FIRST_MAZE_MAP = "twilightforest:first_maze_map";
    private static final String FIRST_CHARM_OF_LIFE = "twilightforest:first_charm_of_life";
    private static final String FIRST_CHARM_OF_KEEPING = "twilightforest:first_charm_of_keeping";
    private static final String FIRST_MAZEBREAKER = "twilightforest:first_mazebreaker";
    private static final String FIRST_CRUMBLE_HORN = "twilightforest:first_crumble_horn";
    private static final String FIRST_KNIGHT_PHANTOM_DEFEATED =
            "twilightforest:first_knight_phantom_defeated";
    private static final String FIRST_MAGIC_BEANS = "twilightforest:first_magic_beans";
    private static final String FIRST_LAMP_OF_CINDERS = "twilightforest:first_lamp_of_cinders";
    private static final String FIRST_FINAL_CASTLE_VISIT = "twilightforest:first_final_castle_visit";

    private static final Map<ResourceLocation, String> BOSS_DEFEAT_MEMORIES = Map.of(
            NAGA_ENTITY, FIRST_NAGA_DEFEATED,
            LICH_ENTITY, FIRST_LICH_DEFEATED,
            MINOSHROOM_ENTITY, FIRST_MINOSHROOM_DEFEATED,
            HYDRA_ENTITY, FIRST_HYDRA_DEFEATED,
            UR_GHAST_ENTITY, FIRST_UR_GHAST_DEFEATED,
            ALPHA_YETI_ENTITY, FIRST_ALPHA_YETI_DEFEATED,
            SNOW_QUEEN_ENTITY, FIRST_SNOW_QUEEN_DEFEATED
    );

    private static final Map<ResourceLocation, String> ITEM_ACQUISITION_MEMORIES = Map.ofEntries(
            Map.entry(MAGIC_MAP_ITEM, FIRST_MAGIC_MAP),
            Map.entry(FILLED_MAGIC_MAP_ITEM, FIRST_MAGIC_MAP),
            Map.entry(MAZE_MAP_ITEM, FIRST_MAZE_MAP),
            Map.entry(FILLED_MAZE_MAP_ITEM, FIRST_MAZE_MAP),
            Map.entry(CHARM_OF_LIFE_1_ITEM, FIRST_CHARM_OF_LIFE),
            Map.entry(CHARM_OF_LIFE_2_ITEM, FIRST_CHARM_OF_LIFE),
            Map.entry(CHARM_OF_KEEPING_1_ITEM, FIRST_CHARM_OF_KEEPING),
            Map.entry(CHARM_OF_KEEPING_2_ITEM, FIRST_CHARM_OF_KEEPING),
            Map.entry(CHARM_OF_KEEPING_3_ITEM, FIRST_CHARM_OF_KEEPING),
            Map.entry(MAZEBREAKER_ITEM, FIRST_MAZEBREAKER),
            Map.entry(CRUMBLE_HORN_ITEM, FIRST_CRUMBLE_HORN),
            Map.entry(MAGIC_BEANS_ITEM, FIRST_MAGIC_BEANS)
    );

    private static final List<StructureVisitMemory> STRUCTURE_VISIT_MEMORIES = List.of(
            new StructureVisitMemory(FIRST_NAGA_COURTYARD_VISIT, structureKey("naga_courtyard")),
            new StructureVisitMemory(FIRST_LICH_TOWER_VISIT, structureKey("lich_tower")),
            new StructureVisitMemory(FIRST_LABYRINTH_VISIT, structureKey("labyrinth")),
            new StructureVisitMemory(FIRST_DARK_TOWER_VISIT, structureKey("dark_tower")),
            new StructureVisitMemory(FIRST_AURORA_PALACE_VISIT, structureKey("aurora_palace")),
            new StructureVisitMemory(FIRST_FINAL_CASTLE_VISIT, structureKey("final_castle"))
    );

    private static final Map<ResourceLocation, String> ADVANCEMENT_MEMORIES = Map.of(
            PROGRESS_KNIGHTS_ADVANCEMENT, FIRST_KNIGHT_PHANTOM_DEFEATED,
            PROGRESS_TROLL_ADVANCEMENT, FIRST_LAMP_OF_CINDERS
    );

    private static boolean registered;
    private static int itemScanTicker;
    private static int structureScanTicker;

    private NeoForgeTwilightForestCompat() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NeoForgeTwilightForestCompat::onPlayerChangedDimension);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NeoForgeTwilightForestCompat::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NeoForgeTwilightForestCompat::onAdvancementEarned);
        NeoForge.EVENT_BUS.addListener(NeoForgeTwilightForestCompat::onServerTick);
        DearDiaryMod.LOGGER.info("Dear Diary Twilight Forest gameplay callbacks registered for NeoForge");
    }

    private static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) {
            return;
        }

        if (event.getTo().location().equals(TWILIGHT_FOREST_DIMENSION)) {
            triggerAndSync(player, FIRST_TWILIGHT_FOREST_ENTRY);
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

    private static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.isSpectator()) {
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

    private static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();

        itemScanTicker++;
        if (itemScanTicker >= ITEM_SCAN_INTERVAL_TICKS) {
            itemScanTicker = 0;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                scanItemAcquisitions(player);
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

    private static void scanItemAcquisitions(ServerPlayer player) {
        if (player.isSpectator()) {
            return;
        }

        AutomaticEventState state = DearDiaryApi.getDiary(player).automaticEventState();
        if (!needsAnyItemAcquisitionMemory(state)) {
            return;
        }

        Set<String> checkedEvents = new HashSet<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            String eventId = ITEM_ACQUISITION_MEMORIES.get(itemId);
            if (eventId != null
                    && checkedEvents.add(eventId)
                    && needsItemAcquisitionMemory(state, eventId)) {
                triggerAndSync(player, eventId);
            }
        }
    }

    private static void scanStructureVisits(ServerPlayer player) {
        if (player.isSpectator()) {
            return;
        }

        ServerLevel world = player.serverLevel();
        if (!world.dimension().location().equals(TWILIGHT_FOREST_DIMENSION)) {
            return;
        }

        AutomaticEventState state = DearDiaryApi.getDiary(player).automaticEventState();
        if (!needsAnyStructureVisitMemory(state)) {
            return;
        }

        BlockPos pos = player.blockPosition();
        for (StructureVisitMemory memory : STRUCTURE_VISIT_MEMORIES) {
            if (needsStructureVisitMemory(state, memory.eventId())
                    && isInsideStructure(world, pos, memory.structureKey())) {
                triggerAndSync(player, memory.eventId());
            }
        }
    }

    private static boolean needsAnyStructureVisitMemory(AutomaticEventState state) {
        for (StructureVisitMemory memory : STRUCTURE_VISIT_MEMORIES) {
            if (needsStructureVisitMemory(state, memory.eventId())) {
                return true;
            }
        }

        return false;
    }

    private static boolean needsAnyItemAcquisitionMemory(AutomaticEventState state) {
        for (String eventId : ITEM_ACQUISITION_MEMORIES.values()) {
            if (needsItemAcquisitionMemory(state, eventId)) {
                return true;
            }
        }

        return false;
    }

    private static boolean needsItemAcquisitionMemory(AutomaticEventState state, String eventId) {
        return needsAutomaticMemory(state, eventId);
    }

    private static boolean needsStructureVisitMemory(AutomaticEventState state, String eventId) {
        return needsAutomaticMemory(state, eventId);
    }

    private static boolean needsAutomaticMemory(AutomaticEventState state, String eventId) {
        if (state.isTriggered(eventId)) {
            return false;
        }

        return DearDiaryEventRegistry.get(eventId)
                .map(NeoForgeTwilightForestCompat::isAllowed)
                .orElse(false);
    }

    private static boolean isAllowed(AutomaticEventDefinition definition) {
        return DearDiaryServices.config().isAutomaticEventAllowed(
                definition.eventId(),
                definition.category(),
                definition.importance()
        );
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

    private static void triggerAndSync(ServerPlayer player, String eventId) {
        AutomaticDiaryEvents.trigger(player, eventId).ifPresent(created -> {
            DearDiaryNetworking.sendDiarySnapshot(player);
            DearDiaryNetworking.sendAutomaticEntryNotice(player, created);
        });
    }

    private record StructureVisitMemory(String eventId, ResourceKey<Structure> structureKey) {
    }
}
