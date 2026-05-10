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
 * Fabric-only runtime hooks for Aether compatibility.
 */
public final class FabricAetherCompat {
    static final String MOD_ID = "aether";
    private static final int ITEM_SCAN_INTERVAL_TICKS = 40;
    private static final int STRUCTURE_SCAN_INTERVAL_TICKS = 20 * 5;

    private static final Identifier AETHER_DIMENSION = Identifier.of(MOD_ID, "the_aether");
    private static final Identifier SLIDER_ENTITY = Identifier.of(MOD_ID, "slider");
    private static final Identifier VALKYRIE_QUEEN_ENTITY = Identifier.of(MOD_ID, "valkyrie_queen");
    private static final Identifier SUN_SPIRIT_ENTITY = Identifier.of(MOD_ID, "sun_spirit");
    private static final Identifier ENCHANTED_GRAVITITE_ITEM = Identifier.of(MOD_ID, "enchanted_gravitite");
    private static final Identifier VALKYRIE_LANCE_ITEM = Identifier.of(MOD_ID, "valkyrie_lance");
    private static final Identifier HAMMER_OF_KINGBDOGZ_ITEM = Identifier.of(MOD_ID, "hammer_of_kingbdogz");
    private static final Identifier REGENERATION_STONE_ITEM = Identifier.of(MOD_ID, "regeneration_stone");
    private static final Identifier PHOENIX_HELMET_ITEM = Identifier.of(MOD_ID, "phoenix_helmet");
    private static final Identifier PHOENIX_CHESTPLATE_ITEM = Identifier.of(MOD_ID, "phoenix_chestplate");
    private static final Identifier PHOENIX_LEGGINGS_ITEM = Identifier.of(MOD_ID, "phoenix_leggings");
    private static final Identifier PHOENIX_BOOTS_ITEM = Identifier.of(MOD_ID, "phoenix_boots");
    private static final Identifier PHOENIX_GLOVES_ITEM = Identifier.of(MOD_ID, "phoenix_gloves");
    private static final Identifier BLUE_MOA_EGG_ITEM = Identifier.of(MOD_ID, "blue_moa_egg");
    private static final Identifier WHITE_MOA_EGG_ITEM = Identifier.of(MOD_ID, "white_moa_egg");
    private static final Identifier BLACK_MOA_EGG_ITEM = Identifier.of(MOD_ID, "black_moa_egg");

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

    private static final Map<Identifier, String> BOSS_DEFEAT_MEMORIES = Map.of(
            SLIDER_ENTITY, FIRST_SLIDER_DEFEATED,
            VALKYRIE_QUEEN_ENTITY, FIRST_VALKYRIE_QUEEN_DEFEATED,
            SUN_SPIRIT_ENTITY, FIRST_SUN_SPIRIT_DEFEATED
    );

    private static final Map<Identifier, String> ITEM_ACQUISITION_MEMORIES = Map.ofEntries(
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

    private FabricAetherCompat() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(FabricAetherCompat::onPlayerChangedWorld);
        ServerLivingEntityEvents.AFTER_DEATH.register(FabricAetherCompat::onLivingDeath);
        ServerTickEvents.END_SERVER_TICK.register(FabricAetherCompat::onEndServerTick);
        DearDiaryMod.LOGGER.info("Dear Diary Aether gameplay callbacks registered for Fabric");
    }

    private static void onPlayerChangedWorld(
            ServerPlayerEntity player,
            ServerWorld origin,
            ServerWorld destination
    ) {
        if (player.isSpectator()) {
            return;
        }

        if (destination.getRegistryKey().getValue().equals(AETHER_DIMENSION)) {
            triggerAndSync(player, FIRST_AETHER_ENTRY);
        }
    }

    private static void onLivingDeath(LivingEntity entity, net.minecraft.entity.damage.DamageSource damageSource) {
        if (!(entity.getWorld() instanceof ServerWorld)) {
            return;
        }

        Identifier entityId = Registries.ENTITY_TYPE.getId(entity.getType());
        String eventId = BOSS_DEFEAT_MEMORIES.get(entityId);
        if (eventId == null) {
            return;
        }

        Entity attacker = damageSource.getAttacker();
        if (!(attacker instanceof ServerPlayerEntity player) || player.isSpectator()) {
            return;
        }

        triggerAndSync(player, eventId);
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
        if (!world.getRegistryKey().getValue().equals(AETHER_DIMENSION)) {
            return;
        }

        AutomaticEventState state = DearDiaryApi.getDiary(player).automaticEventState();
        if (!needsAnyStructureVisitMemory(state)) {
            return;
        }

        BlockPos pos = player.getBlockPos();
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

    private static boolean needsAutomaticMemory(ServerPlayerEntity player, String eventId) {
        return needsAutomaticMemory(DearDiaryApi.getDiary(player).automaticEventState(), eventId);
    }

    private static boolean needsAutomaticMemory(AutomaticEventState state, String eventId) {
        if (state.isTriggered(eventId)) {
            return false;
        }

        return DearDiaryEventRegistry.get(eventId)
                .map(FabricAetherCompat::isAllowed)
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
