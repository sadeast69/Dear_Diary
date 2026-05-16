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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class NeoForgeScorchedGunsCompat {
    public static final String MOD_ID = "scguns";

    private static final int ITEM_SCAN_INTERVAL_TICKS = 40;

    private static final String FIRST_GUN_BENCH_BUILT = "scguns:first_gun_bench_built";
    private static final String FIRST_FIREARM_ACQUIRED = "scguns:first_firearm_acquired";
    private static final String FIRST_AMMO_ACQUIRED = "scguns:first_ammo_acquired";
    private static final String FIRST_BLUEPRINT_ACQUIRED = "scguns:first_blueprint_acquired";
    private static final String FIRST_HEAVY_WEAPON_ACQUIRED = "scguns:first_heavy_weapon_acquired";
    private static final String FIRST_RAID_FLARE_ACQUIRED = "scguns:first_raid_flare_acquired";
    private static final String FIRST_SCGUNS_ENEMY_DEFEATED = "scguns:first_scguns_enemy_defeated";
    private static final String FIRST_SCGUNS_BULLET_KILL = "scguns:first_scguns_bullet_kill";

    private static final ResourceLocation GUN_BENCH_ADVANCEMENT = scgunsId("main/craft_gun_bench");
    private static final ResourceKey<DamageType> BULLET_DAMAGE_TYPE =
        ResourceKey.create(Registries.DAMAGE_TYPE, scgunsId("bullet"));
    private static final TagKey<Item> GUN_TAG = TagKey.create(Registries.ITEM, scgunsId("enchantable/gun"));

    private static final Map<ResourceLocation, String> ADVANCEMENT_MEMORIES = Map.ofEntries(
        Map.entry(GUN_BENCH_ADVANCEMENT, FIRST_GUN_BENCH_BUILT),
        Map.entry(scgunsId("main/copper_blueprint"), FIRST_BLUEPRINT_ACQUIRED),
        Map.entry(scgunsId("main/iron_blueprint"), FIRST_BLUEPRINT_ACQUIRED),
        Map.entry(scgunsId("main/treated_brass_blueprint"), FIRST_BLUEPRINT_ACQUIRED),
        Map.entry(scgunsId("main/diamond_steel_blueprint"), FIRST_BLUEPRINT_ACQUIRED),
        Map.entry(scgunsId("main/piglin_blueprint"), FIRST_BLUEPRINT_ACQUIRED),
        Map.entry(scgunsId("main/ocean_blueprint"), FIRST_BLUEPRINT_ACQUIRED),
        Map.entry(scgunsId("main/deep_dark_blueprint"), FIRST_BLUEPRINT_ACQUIRED),
        Map.entry(scgunsId("main/end_blueprint"), FIRST_BLUEPRINT_ACQUIRED),
        Map.entry(scgunsId("main/scorched_blueprint"), FIRST_BLUEPRINT_ACQUIRED)
    );

    private static final Set<ResourceLocation> AMMO_ITEM_IDS = Set.of(
        scgunsId("advanced_round"),
        scgunsId("bearpack_shell"),
        scgunsId("beowulf_round"),
        scgunsId("blaze_fuel"),
        scgunsId("bouncy_grenade_round"),
        scgunsId("buckshot"),
        scgunsId("compact_advanced_round"),
        scgunsId("compact_copper_round"),
        scgunsId("energy_cell"),
        scgunsId("energy_core"),
        scgunsId("fire_grenade_round"),
        scgunsId("frog_dart"),
        scgunsId("gas_grenade_round"),
        scgunsId("gibbs_round"),
        scgunsId("grapeshot"),
        scgunsId("hardened_bullet"),
        scgunsId("he_grenade_round"),
        scgunsId("hog_round"),
        scgunsId("krahg_round"),
        scgunsId("microjet"),
        scgunsId("needle"),
        scgunsId("nitro_buckshot"),
        scgunsId("osborne_slug"),
        scgunsId("powder_and_ball"),
        scgunsId("ramrod_round"),
        scgunsId("rocket"),
        scgunsId("sculk_cell"),
        scgunsId("shatter_round"),
        scgunsId("shock_cell"),
        scgunsId("shotball"),
        scgunsId("shotgun_shell"),
        scgunsId("shulkshot"),
        scgunsId("standard_bullet"),
        scgunsId("standard_copper_round"),
        scgunsId("syringe")
    );

    private static final Set<ResourceLocation> BLUEPRINT_ITEM_IDS = Set.of(
        scgunsId("antique_blueprint"),
        scgunsId("blueprint_scrap"),
        scgunsId("copper_blueprint"),
        scgunsId("deep_dark_blueprint"),
        scgunsId("diamond_steel_blueprint"),
        scgunsId("end_blueprint"),
        scgunsId("exo_suit_blueprint"),
        scgunsId("frontier_blueprint"),
        scgunsId("iron_blueprint"),
        scgunsId("netherite_scrap_chunk"),
        scgunsId("ocean_blueprint"),
        scgunsId("piglin_blueprint"),
        scgunsId("scorched_blueprint"),
        scgunsId("treated_brass_blueprint"),
        scgunsId("wrecker_blueprint")
    );

    private static final Set<ResourceLocation> HEAVY_WEAPON_ITEM_IDS = Set.of(
        scgunsId("cyclone"),
        scgunsId("flayed_god"),
        scgunsId("gattaler"),
        scgunsId("hullbreaker"),
        scgunsId("scratches"),
        scgunsId("shard_culler"),
        scgunsId("spitfire"),
        scgunsId("terra_incognita"),
        scgunsId("thunderhead"),
        scgunsId("weevil")
    );

    private static final Set<ResourceLocation> RAID_FLARE_ITEM_IDS = Set.of(
        scgunsId("antique_flare"),
        scgunsId("copper_flare"),
        scgunsId("diamond_steel_flare"),
        scgunsId("frontier_flare"),
        scgunsId("gold_flare"),
        scgunsId("iron_flare"),
        scgunsId("ocean_flare"),
        scgunsId("sculk_flare"),
        scgunsId("treated_brass_flare"),
        scgunsId("wrecker_flare")
    );

    private static final Set<ResourceLocation> SCGUNS_ENEMY_ENTITY_IDS = Set.of(
        scgunsId("cog_minion"),
        scgunsId("cog_knight"),
        scgunsId("sky_carrier"),
        scgunsId("hive"),
        scgunsId("swarm"),
        scgunsId("redcoat"),
        scgunsId("dissident"),
        scgunsId("hornlin"),
        scgunsId("zombified_hornlin"),
        scgunsId("blunderer"),
        scgunsId("adjudicator"),
        scgunsId("subjugator"),
        scgunsId("finforcer"),
        scgunsId("praetor"),
        scgunsId("mother_ghast"),
        scgunsId("sulfurhead"),
        scgunsId("trauma_unit"),
        scgunsId("scamp_tank"),
        scgunsId("scampler")
    );

    private static boolean registered;
    private static int itemScanTicker;

    private NeoForgeScorchedGunsCompat() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NeoForgeScorchedGunsCompat::onAdvancementEarned);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NeoForgeScorchedGunsCompat::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(NeoForgeScorchedGunsCompat::onServerTick);
        DearDiaryMod.LOGGER.info("Dear Diary Scorched Guns gameplay callbacks registered for NeoForge");
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

    private static void onServerTick(ServerTickEvent.Post event) {
        itemScanTicker++;
        if (itemScanTicker < ITEM_SCAN_INTERVAL_TICKS) {
            return;
        }
        itemScanTicker = 0;
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (player.isSpectator()) {
                continue;
            }
            AutomaticEventState state = DearDiaryApi.getDiary(player).automaticEventState();
            if (needsAnyItemMemory(state)) {
                scanItemMemories(player, state);
            }
        }
    }

    private static void scanItemMemories(ServerPlayer player, AutomaticEventState state) {
        boolean needFirearm = needsAutomaticMemory(state, FIRST_FIREARM_ACQUIRED);
        boolean needAmmo = needsAutomaticMemory(state, FIRST_AMMO_ACQUIRED);
        boolean needBlueprint = needsAutomaticMemory(state, FIRST_BLUEPRINT_ACQUIRED);
        boolean needHeavyWeapon = needsAutomaticMemory(state, FIRST_HEAVY_WEAPON_ACQUIRED);
        boolean needRaidFlare = needsAutomaticMemory(state, FIRST_RAID_FLARE_ACQUIRED);

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (!needFirearm && !needAmmo && !needBlueprint && !needHeavyWeapon && !needRaidFlare) {
                return;
            }
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            if (needFirearm && stack.is(GUN_TAG)) {
                triggerAndSync(player, FIRST_FIREARM_ACQUIRED);
                needFirearm = false;
            }

            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (needAmmo && AMMO_ITEM_IDS.contains(itemId)) {
                triggerAndSync(player, FIRST_AMMO_ACQUIRED);
                needAmmo = false;
            }
            if (needBlueprint && BLUEPRINT_ITEM_IDS.contains(itemId)) {
                triggerAndSync(player, FIRST_BLUEPRINT_ACQUIRED);
                needBlueprint = false;
            }
            if (needHeavyWeapon && HEAVY_WEAPON_ITEM_IDS.contains(itemId)) {
                triggerAndSync(player, FIRST_HEAVY_WEAPON_ACQUIRED);
                needHeavyWeapon = false;
            }
            if (needRaidFlare && RAID_FLARE_ITEM_IDS.contains(itemId)) {
                triggerAndSync(player, FIRST_RAID_FLARE_ACQUIRED);
                needRaidFlare = false;
            }
        }
    }

    private static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel)) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof ServerPlayer player) || player.isSpectator()) {
            return;
        }

        AutomaticEventState state = DearDiaryApi.getDiary(player).automaticEventState();
        ResourceLocation victimId = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType());
        if (SCGUNS_ENEMY_ENTITY_IDS.contains(victimId)
            && needsAutomaticMemory(state, FIRST_SCGUNS_ENEMY_DEFEATED)) {
            triggerAndSync(player, FIRST_SCGUNS_ENEMY_DEFEATED);
        }
        if (event.getSource().is(BULLET_DAMAGE_TYPE)
            && needsAutomaticMemory(state, FIRST_SCGUNS_BULLET_KILL)) {
            triggerAndSync(player, FIRST_SCGUNS_BULLET_KILL);
        }
    }

    private static boolean needsAnyItemMemory(AutomaticEventState state) {
        return needsAutomaticMemory(state, FIRST_FIREARM_ACQUIRED)
            || needsAutomaticMemory(state, FIRST_AMMO_ACQUIRED)
            || needsAutomaticMemory(state, FIRST_BLUEPRINT_ACQUIRED)
            || needsAutomaticMemory(state, FIRST_HEAVY_WEAPON_ACQUIRED)
            || needsAutomaticMemory(state, FIRST_RAID_FLARE_ACQUIRED);
    }

    private static boolean needsAutomaticMemory(AutomaticEventState state, String eventId) {
        if (state.isTriggered(eventId)) {
            return false;
        }
        return DearDiaryEventRegistry.get(eventId)
            .map(NeoForgeScorchedGunsCompat::isAllowed)
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
        Optional<DiaryEntry> created = AutomaticDiaryEvents.trigger(player, eventId);
        created.ifPresent(entry -> {
            DearDiaryNetworking.sendDiarySnapshot(player);
            DearDiaryNetworking.sendAutomaticEntryNotice(player, entry);
        });
        return created;
    }

    private static ResourceLocation scgunsId(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
