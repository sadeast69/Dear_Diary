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
 * NeoForge-only runtime hooks for Aether compatibility.
 */
public final class NeoForgeAetherCompat {
    static final String MOD_ID = "aether";
    private static final int ITEM_SCAN_INTERVAL_TICKS = 40;
    private static final int STRUCTURE_SCAN_INTERVAL_TICKS = 20 * 5;

    private static final ResourceLocation AETHER_DIMENSION =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "the_aether");
    private static final ResourceLocation SLIDER_ENTITY =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "slider");
    private static final ResourceLocation VALKYRIE_QUEEN_ENTITY =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "valkyrie_queen");
    private static final ResourceLocation SUN_SPIRIT_ENTITY =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "sun_spirit");
    private static final ResourceLocation ENCHANTED_GRAVITITE_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "enchanted_gravitite");
    private static final ResourceLocation VALKYRIE_LANCE_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "valkyrie_lance");
    private static final ResourceLocation HAMMER_OF_KINGBDOGZ_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "hammer_of_kingbdogz");
    private static final ResourceLocation REGENERATION_STONE_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "regeneration_stone");
    private static final ResourceLocation PHOENIX_HELMET_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "phoenix_helmet");
    private static final ResourceLocation PHOENIX_CHESTPLATE_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "phoenix_chestplate");
    private static final ResourceLocation PHOENIX_LEGGINGS_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "phoenix_leggings");
    private static final ResourceLocation PHOENIX_BOOTS_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "phoenix_boots");
    private static final ResourceLocation PHOENIX_GLOVES_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "phoenix_gloves");
    private static final ResourceLocation BLUE_MOA_EGG_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "blue_moa_egg");
    private static final ResourceLocation WHITE_MOA_EGG_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "white_moa_egg");
    private static final ResourceLocation BLACK_MOA_EGG_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "black_moa_egg");

    private static final String FIRST_AETHER_ENTRY = "aether:first_aether_entry";
    private static final String FIRST_SLIDER_DEFEATED = "aether:first_slider_defeated";
    private static final String FIRST_ENCHANTED_GRAVITITE_ACQUIRED =
            "aether:first_enchanted_gravitite_acquired";
    private static final String FIRST_VALKYRIE_QUEEN_DEFEATED =
            "aether:first_valkyrie_queen_defeated";
    private static final String FIRST_SUN_SPIRIT_DEFEATED = "aether:first_sun_spirit_defeated";
    private static final String FIRST_BRONZE_DUNGEON_VISIT = "aether:first_bronze_dungeon_visit";
    private static final String FIRST_SILVER_DUNGEON_VISIT = "aether:first_silver_dungeon_visit";
    private static final String FIRST_GOLD_DUNGEON_VISIT = "aether:first_gold_dungeon_visit";
    private static final String FIRST_VALKYRIE_LANCE = "aether:first_valkyrie_lance";
    private static final String FIRST_HAMMER_OF_KINGBDOGZ = "aether:first_hammer_of_kingbdogz";
    private static final String FIRST_REGENERATION_STONE = "aether:first_regeneration_stone";
    private static final String FIRST_PHOENIX_ARMOR = "aether:first_phoenix_armor";
    private static final String FIRST_MOA_EGG = "aether:first_moa_egg";

    private static final Map<ResourceLocation, String> BOSS_DEFEAT_MEMORIES = Map.of(
            SLIDER_ENTITY, FIRST_SLIDER_DEFEATED,
            VALKYRIE_QUEEN_ENTITY, FIRST_VALKYRIE_QUEEN_DEFEATED,
            SUN_SPIRIT_ENTITY, FIRST_SUN_SPIRIT_DEFEATED
    );

    private static final Map<ResourceLocation, String> ITEM_ACQUISITION_MEMORIES = Map.ofEntries(
            Map.entry(ENCHANTED_GRAVITITE_ITEM, FIRST_ENCHANTED_GRAVITITE_ACQUIRED),
            Map.entry(VALKYRIE_LANCE_ITEM, FIRST_VALKYRIE_LANCE),
            Map.entry(HAMMER_OF_KINGBDOGZ_ITEM, FIRST_HAMMER_OF_KINGBDOGZ),
            Map.entry(REGENERATION_STONE_ITEM, FIRST_REGENERATION_STONE),
            Map.entry(PHOENIX_HELMET_ITEM, FIRST_PHOENIX_ARMOR),
            Map.entry(PHOENIX_CHESTPLATE_ITEM, FIRST_PHOENIX_ARMOR),
            Map.entry(PHOENIX_LEGGINGS_ITEM, FIRST_PHOENIX_ARMOR),
            Map.entry(PHOENIX_BOOTS_ITEM, FIRST_PHOENIX_ARMOR),
            Map.entry(PHOENIX_GLOVES_ITEM, FIRST_PHOENIX_ARMOR),
            Map.entry(BLUE_MOA_EGG_ITEM, FIRST_MOA_EGG),
            Map.entry(WHITE_MOA_EGG_ITEM, FIRST_MOA_EGG),
            Map.entry(BLACK_MOA_EGG_ITEM, FIRST_MOA_EGG)
    );

    private static final List<String> ITEM_MEMORY_EVENTS = List.of(
            FIRST_ENCHANTED_GRAVITITE_ACQUIRED,
            FIRST_VALKYRIE_LANCE,
            FIRST_HAMMER_OF_KINGBDOGZ,
            FIRST_REGENERATION_STONE,
            FIRST_PHOENIX_ARMOR,
            FIRST_MOA_EGG
    );

    private static final List<StructureVisitMemory> STRUCTURE_VISIT_MEMORIES = List.of(
            new StructureVisitMemory(FIRST_BRONZE_DUNGEON_VISIT, structureKey("bronze_dungeon")),
            new StructureVisitMemory(FIRST_SILVER_DUNGEON_VISIT, structureKey("silver_dungeon")),
            new StructureVisitMemory(FIRST_GOLD_DUNGEON_VISIT, structureKey("gold_dungeon"))
    );

    private static boolean registered;
    private static int itemScanTicker;
    private static int structureScanTicker;

    private NeoForgeAetherCompat() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NeoForgeAetherCompat::onPlayerChangedDimension);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NeoForgeAetherCompat::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(NeoForgeAetherCompat::onServerTick);
        DearDiaryMod.LOGGER.info("Dear Diary Aether gameplay callbacks registered for NeoForge");
    }

    private static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) {
            return;
        }

        if (event.getTo().location().equals(AETHER_DIMENSION)) {
            triggerAndSync(player, FIRST_AETHER_ENTRY);
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
        if (!world.dimension().location().equals(AETHER_DIMENSION)) {
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

    private static boolean needsStructureVisitMemory(AutomaticEventState state, String eventId) {
        return needsAutomaticMemory(state, eventId);
    }

    private static boolean needsAutomaticMemory(ServerPlayer player, String eventId) {
        return needsAutomaticMemory(DearDiaryApi.getDiary(player).automaticEventState(), eventId);
    }

    private static boolean needsAutomaticMemory(AutomaticEventState state, String eventId) {
        if (state.isTriggered(eventId)) {
            return false;
        }

        return DearDiaryEventRegistry.get(eventId)
                .map(NeoForgeAetherCompat::isAllowed)
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
