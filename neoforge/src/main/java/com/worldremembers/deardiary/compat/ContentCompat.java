package com.worldremembers.deardiary.compat;

import com.worldremembers.deardiary.data.DiaryCategory;
import com.worldremembers.deardiary.data.DiaryImportance;
import com.worldremembers.deardiary.event.AutomaticEventDefinition;
import com.worldremembers.deardiary.event.DearDiaryEventRegistry;
import com.worldremembers.deardiary.event.TriggerPolicy;
import java.util.Objects;

/**
 * Shared registration point for future optional content-mod integrations.
 *
 * <p>Compat event definitions should use the source mod namespace, for example
 * {@code twilightforest:first_naga_defeated}. Runtime hooks should call
 * {@code AutomaticDiaryEvents.trigger(...)} and must never write diary storage
 * directly.</p>
 */
public final class ContentCompat {
    private static final String TWILIGHT_FOREST_MOD_ID = "twilightforest";
    private static final String AETHER_MOD_ID = "aether";
    private static final String DEEPER_DARKER_MOD_ID = "deeperdarker";
    private static final String IRONS_SPELLBOOKS_MOD_ID = "irons_spellbooks";
    private static final String CATACLYSM_MOD_ID = "cataclysm";
    private static final String CREATE_MOD_ID = "create";
    private static final String FARMERS_DELIGHT_MOD_ID = "farmersdelight";
    private static final String WAYSTONES_MOD_ID = "waystones";
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
    private static final String FIRST_KNIGHT_PHANTOM_DEFEATED = "twilightforest:first_knight_phantom_defeated";
    private static final String FIRST_MAGIC_BEANS = "twilightforest:first_magic_beans";
    private static final String FIRST_LAMP_OF_CINDERS = "twilightforest:first_lamp_of_cinders";
    private static final String FIRST_FINAL_CASTLE_VISIT = "twilightforest:first_final_castle_visit";
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
    private static final String FIRST_SPELLBOOK_EQUIPPED =
            "irons_spellbooks:first_spellbook_equipped";
    private static final String FIRST_SCROLL_ACQUIRED =
            "irons_spellbooks:first_scroll_acquired";
    private static final String FIRST_INSCRIPTION_TABLE =
            "irons_spellbooks:first_inscription_table";
    private static final String FIRST_SCROLL_FORGE = "irons_spellbooks:first_scroll_forge";
    private static final String FIRST_ARCANE_ANVIL = "irons_spellbooks:first_arcane_anvil";
    private static final String FIRST_DEAD_KING_DEFEATED =
            "irons_spellbooks:first_dead_king_defeated";
    private static final String FIRST_FIRE_BOSS_DEFEATED =
            "irons_spellbooks:first_fire_boss_defeated";
    private static final String FIRST_CATACOMBS_VISIT = "irons_spellbooks:first_catacombs_visit";
    private static final String FIRST_LEGENDARY_INK = "irons_spellbooks:first_legendary_ink";
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
    private static final String FIRST_WATER_WHEEL_POWERED =
            "create:first_water_wheel_powered";
    private static final String FIRST_MECHANICAL_PRESS =
            "create:first_mechanical_press";
    private static final String FIRST_MECHANICAL_MIXER =
            "create:first_mechanical_mixer";
    private static final String FIRST_DEPLOYER_POWERED =
            "create:first_deployer_powered";
    private static final String FIRST_MECHANICAL_CRAFTER_POWERED =
            "create:first_mechanical_crafter_powered";
    private static final String FIRST_STEAM_ENGINE = "create:first_steam_engine";
    private static final String FIRST_TRAIN_ASSEMBLED = "create:first_train_assembled";
    private static final String FIRST_FACTORY_GAUGE_REQUEST =
            "create:first_factory_gauge_request";
    private static final String FIRST_CRUSHING_WHEELS_POWERED =
            "create:first_crushing_wheels_powered";
    private static final String FIRST_FAN_PROCESSING = "create:first_fan_processing";
    private static final String FIRST_SPOUT_FILLING = "create:first_spout_filling";
    private static final String FIRST_HOSE_PULLEY_OPERATION =
            "create:first_hose_pulley_operation";
    private static final String FIRST_PRECISION_MECHANISM =
            "create:first_precision_mechanism";
    private static final String FIRST_MECHANICAL_ARM_DELIVERY =
            "create:first_mechanical_arm_delivery";
    private static final String FIRST_TRAIN_SCHEDULE_ASSIGNED =
            "create:first_train_schedule_assigned";
    private static final String FIRST_ROTATION_SPEED_CONTROLLER_TUNED =
            "create:first_rotation_speed_controller_tuned";
    private static final String FIRST_LINKED_CONTROLLER_SIGNAL =
            "create:first_linked_controller_signal";
    private static final String FIRST_DISPLAY_LINK_REPORTING =
            "create:first_display_link_reporting";
    private static final String FIRST_ROPE_PULLEY_DEEP_EXTENSION =
            "create:first_rope_pulley_deep_extension";
    private static final String FD_FIRST_KNIFE_ACQUIRED =
            "farmersdelight:first_knife_acquired";
    private static final String FD_FIRST_CUTTING_BOARD_USED =
            "farmersdelight:first_cutting_board_used";
    private static final String FD_FIRST_COOKING_POT_PLACED =
            "farmersdelight:first_cooking_pot_placed";
    private static final String FD_FIRST_SKILLET_COOKED =
            "farmersdelight:first_skillet_cooked";
    private static final String FD_FIRST_NOURISHING_MEAL =
            "farmersdelight:first_nourishing_meal";
    private static final String FD_FIRST_FEAST_SERVED =
            "farmersdelight:first_feast_served";
    private static final String FD_MEALS_EATEN_10 =
            "farmersdelight:meals_eaten_10";
    private static final String FD_MEALS_EATEN_50 =
            "farmersdelight:meals_eaten_50";
    private static final String FD_FEASTS_SERVED_5 =
            "farmersdelight:feasts_served_5";
    private static final String FD_FIRST_STOVE_PLACED =
            "farmersdelight:first_stove_placed";
    private static final String FD_FIRST_PANTRY_CABINET =
            "farmersdelight:first_pantry_cabinet";
    private static final String FD_FIRST_RICH_SOIL_CREATED =
            "farmersdelight:first_rich_soil_created";
    private static final String WAYSTONES_FIRST_WAYSTONE_ACTIVATED =
            "waystones:first_waystone_activated";
    private static final String WAYSTONES_FIRST_WAYSTONE_PLACED =
            "waystones:first_waystone_placed";
    private static final String WAYSTONES_FIRST_WARP_STONE_ACQUIRED =
            "waystones:first_warp_stone_acquired";
    private static final String WAYSTONES_FIRST_RETURN_SCROLL_USED =
            "waystones:first_return_scroll_used";
    private static final String WAYSTONES_ACTIVATED_WAYSTONES_5 =
            "waystones:activated_waystones_5";
    private static final String WAYSTONES_ACTIVATED_COUNTER = "waystones_activated";
    private static final String FALLBACK_TEXT = "A Twilight Forest memory.";
    private static final String AETHER_FALLBACK_TEXT = "An Aether memory.";
    private static final String DEEPER_DARKER_FALLBACK_TEXT = "A Deeper and Darker memory.";
    private static final String IRONS_SPELLBOOKS_FALLBACK_TEXT = "An Iron's Spells memory.";
    private static final String CATACLYSM_FALLBACK_TEXT = "A Cataclysm memory.";
    private static final String CREATE_FALLBACK_TEXT = "A Create memory.";
    private static final String FARMERS_DELIGHT_FALLBACK_TEXT = "A Farmer's Delight memory.";
    private static final String WAYSTONES_FALLBACK_TEXT = "A Waystones memory.";

    private ContentCompat() {
    }

    public static void register(CompatEnvironment environment) {
        Objects.requireNonNull(environment, "environment");
        registerDefinitions(environment);
        registerGameplayHooks(environment);
    }

    private static void registerDefinitions(CompatEnvironment environment) {
        Objects.requireNonNull(environment, "environment");
        if (environment.isModLoaded(TWILIGHT_FOREST_MOD_ID)) {
            registerTwilightForestDefinitions();
        }
        if (environment.isModLoaded(AETHER_MOD_ID)) {
            registerAetherDefinitions();
        }
        if (environment.isModLoaded(DEEPER_DARKER_MOD_ID)) {
            registerDeeperDarkerDefinitions();
        }
        if (environment.isModLoaded(IRONS_SPELLBOOKS_MOD_ID)) {
            registerIronsSpellbooksDefinitions();
        }
        if (environment.isModLoaded(CATACLYSM_MOD_ID)) {
            registerCataclysmDefinitions();
        }
        if (environment.isModLoaded(CREATE_MOD_ID)) {
            registerCreateDefinitions();
        }
        if (environment.isModLoaded(FARMERS_DELIGHT_MOD_ID)) {
            registerFarmersDelightDefinitions();
        }
        if (environment.isModLoaded(WAYSTONES_MOD_ID)) {
            registerWaystonesDefinitions();
        }
    }

    private static void registerGameplayHooks(CompatEnvironment environment) {
        Objects.requireNonNull(environment, "environment");
        // Future integrations check environment.isModLoaded(modId) before
        // loading loader-specific hook classes for optional content mods.
    }

    private static void registerTwilightForestDefinitions() {
        registerTwilightForestDefinition(
                FIRST_TWILIGHT_FOREST_ENTRY,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                "twilightforest:twilight_portal_miniature_structure",
                12,
                "Beneath a Stranger Twilight"
        );
        registerTwilightForestDefinition(
                FIRST_NAGA_DEFEATED,
                DiaryCategory.BOSSES,
                DiaryImportance.MAJOR,
                "twilightforest:naga_courtyard_miniature_structure",
                15,
                "The Courtyard Coiled"
        );
        registerTwilightForestDefinition(
                FIRST_LICH_DEFEATED,
                DiaryCategory.BOSSES,
                DiaryImportance.MAJOR,
                "twilightforest:lich_tower_miniature_structure",
                15,
                "The Crown That Would Not Fall"
        );
        registerTwilightForestDefinition(
                FIRST_NAGA_COURTYARD_VISIT,
                DiaryCategory.EXPLORATION,
                DiaryImportance.NORMAL,
                "twilightforest:naga_courtyard_miniature_structure",
                12,
                "The Ring in the Grass"
        );
        registerTwilightForestDefinition(
                FIRST_LICH_TOWER_VISIT,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                "twilightforest:lich_tower_miniature_structure",
                15,
                "A Tower That Still Commands"
        );
        registerTwilightForestDefinition(
                FIRST_LABYRINTH_VISIT,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                "twilightforest:minotaur_labyrinth_miniature_structure",
                15,
                "The Maze Beneath the Forest"
        );
        registerTwilightForestDefinition(
                FIRST_DARK_TOWER_VISIT,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                "twilightforest:dark_tower_miniature_structure",
                15,
                "The Tower with Bad Intentions"
        );
        registerTwilightForestDefinition(
                FIRST_AURORA_PALACE_VISIT,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                "twilightforest:aurora_block",
                15,
                "A Palace Cut from Cold Light"
        );
        registerTwilightForestDefinition(
                FIRST_MINOSHROOM_DEFEATED,
                DiaryCategory.BOSSES,
                DiaryImportance.MAJOR,
                "twilightforest:minoshroom_trophy",
                15,
                "The Beast in the Maze"
        );
        registerTwilightForestDefinition(
                FIRST_HYDRA_DEFEATED,
                DiaryCategory.BOSSES,
                DiaryImportance.LEGENDARY,
                "twilightforest:hydra_trophy",
                20,
                "The Many-Headed Fire"
        );
        registerTwilightForestDefinition(
                FIRST_UR_GHAST_DEFEATED,
                DiaryCategory.BOSSES,
                DiaryImportance.LEGENDARY,
                "twilightforest:ur_ghast_trophy",
                20,
                "The Tower Stopped Weeping"
        );
        registerTwilightForestDefinition(
                FIRST_ALPHA_YETI_DEFEATED,
                DiaryCategory.BOSSES,
                DiaryImportance.MAJOR,
                "twilightforest:alpha_yeti_trophy",
                15,
                "Winter Hit Back"
        );
        registerTwilightForestDefinition(
                FIRST_SNOW_QUEEN_DEFEATED,
                DiaryCategory.BOSSES,
                DiaryImportance.LEGENDARY,
                "twilightforest:snow_queen_trophy",
                20,
                "The Crown of Ice Fell"
        );
        registerTwilightForestDefinition(
                FIRST_MAGIC_MAP,
                DiaryCategory.EXPLORATION,
                DiaryImportance.NORMAL,
                "twilightforest:filled_magic_map",
                10,
                "A Map for the Dusk"
        );
        registerTwilightForestDefinition(
                FIRST_MAZE_MAP,
                DiaryCategory.EXPLORATION,
                DiaryImportance.NORMAL,
                "twilightforest:filled_maze_map",
                12,
                "A Map for Getting Lost Properly"
        );
        registerTwilightForestDefinition(
                FIRST_CHARM_OF_LIFE,
                DiaryCategory.RARE,
                DiaryImportance.MAJOR,
                "twilightforest:charm_of_life_1",
                12,
                "A Small Argument with Death"
        );
        registerTwilightForestDefinition(
                FIRST_CHARM_OF_KEEPING,
                DiaryCategory.RARE,
                DiaryImportance.MAJOR,
                "twilightforest:charm_of_keeping_1",
                12,
                "Insurance Against Disaster"
        );
        registerTwilightForestDefinition(
                FIRST_MAZEBREAKER,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                "twilightforest:mazebreaker_pickaxe",
                15,
                "An Answer to Walls"
        );
        registerTwilightForestDefinition(
                FIRST_CRUMBLE_HORN,
                DiaryCategory.RARE,
                DiaryImportance.MAJOR,
                "twilightforest:crumble_horn",
                15,
                "The Horn That Persuades Stone"
        );
        registerTwilightForestDefinition(
                FIRST_KNIGHT_PHANTOM_DEFEATED,
                DiaryCategory.BOSSES,
                DiaryImportance.MAJOR,
                "twilightforest:knight_phantom_trophy",
                15,
                "The Company Falls"
        );
        registerTwilightForestDefinition(
                FIRST_MAGIC_BEANS,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                "twilightforest:magic_beans",
                12,
                "Beans with Altitude"
        );
        registerTwilightForestDefinition(
                FIRST_LAMP_OF_CINDERS,
                DiaryCategory.RARE,
                DiaryImportance.LEGENDARY,
                "twilightforest:lamp_of_cinders",
                20,
                "The Lamp of Cinders"
        );
        registerTwilightForestDefinition(
                FIRST_FINAL_CASTLE_VISIT,
                DiaryCategory.EXPLORATION,
                DiaryImportance.LEGENDARY,
                "twilightforest:castle_brick",
                20,
                "At the Final Castle"
        );
    }

    private static void registerAetherDefinitions() {
        registerAetherDefinition(
                FIRST_AETHER_ENTRY,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                12,
                "Above the World"
        );
        registerAetherDefinition(
                FIRST_SLIDER_DEFEATED,
                DiaryCategory.BOSSES,
                DiaryImportance.MAJOR,
                15,
                "The Stone That Learned to Charge"
        );
        registerAetherDefinition(
                FIRST_ENCHANTED_GRAVITITE_ACQUIRED,
                DiaryCategory.RARE,
                DiaryImportance.MAJOR,
                12,
                "A Stone That Disagrees with Falling"
        );
        registerAetherDefinition(
                FIRST_VALKYRIE_QUEEN_DEFEATED,
                DiaryCategory.BOSSES,
                DiaryImportance.MAJOR,
                15,
                "The Silver Challenge"
        );
        registerAetherDefinition(
                FIRST_SUN_SPIRIT_DEFEATED,
                DiaryCategory.BOSSES,
                DiaryImportance.LEGENDARY,
                20,
                "An Argument with the Sun"
        );
        registerAetherDefinition(
                FIRST_BRONZE_DUNGEON_VISIT,
                DiaryCategory.EXPLORATION,
                DiaryImportance.NORMAL,
                10,
                "Bronze Under the Blue"
        );
        registerAetherDefinition(
                FIRST_SILVER_DUNGEON_VISIT,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                12,
                "The Silver Halls"
        );
        registerAetherDefinition(
                FIRST_GOLD_DUNGEON_VISIT,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                15,
                "Gold Before the Trial"
        );
        registerAetherDefinition(
                FIRST_VALKYRIE_LANCE,
                DiaryCategory.RARE,
                DiaryImportance.MAJOR,
                12,
                "A Lance of Challenge"
        );
        registerAetherDefinition(
                FIRST_HAMMER_OF_KINGBDOGZ,
                DiaryCategory.RARE,
                DiaryImportance.MAJOR,
                12,
                "The Named Hammer"
        );
        registerAetherDefinition(
                FIRST_REGENERATION_STONE,
                DiaryCategory.RARE,
                DiaryImportance.MAJOR,
                12,
                "Stone of Second Breath"
        );
        registerAetherDefinition(
                FIRST_PHOENIX_ARMOR,
                DiaryCategory.RARE,
                DiaryImportance.MAJOR,
                12,
                "Fire Worn Carefully"
        );
        registerAetherDefinition(
                FIRST_MOA_EGG,
                DiaryCategory.EXPLORATION,
                DiaryImportance.NORMAL,
                10,
                "A Small Sky Promise"
        );
    }

    private static void registerDeeperDarkerDefinitions() {
        registerDeeperDarkerDefinition(
                FIRST_OTHERSIDE_ENTRY,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                12,
                "Past the Last Silence"
        );
        registerDeeperDarkerDefinition(
                FIRST_STALKER_DEFEATED,
                DiaryCategory.BOSSES,
                DiaryImportance.MAJOR,
                15,
                "The Stalker Stopped"
        );
        registerDeeperDarkerDefinition(
                FIRST_SONOROUS_STAFF_ACQUIRED,
                DiaryCategory.RARE,
                DiaryImportance.MAJOR,
                15,
                "A Staff of Echoes"
        );
        registerDeeperDarkerDefinition(
                FIRST_ANCIENT_TEMPLE_VISIT,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                15,
                "The Temple That Waited Below"
        );
        registerDeeperDarkerDefinition(
                FIRST_SCULK_TRANSMITTER,
                DiaryCategory.RARE,
                DiaryImportance.NORMAL,
                10,
                "A Quiet Little Transmission"
        );
        registerDeeperDarkerDefinition(
                FIRST_REINFORCED_ECHO_SHARD,
                DiaryCategory.RARE,
                DiaryImportance.MAJOR,
                12,
                "Echo, Given Armor"
        );
        registerDeeperDarkerDefinition(
                FIRST_SOUL_ELYTRA,
                DiaryCategory.RARE,
                DiaryImportance.MAJOR,
                12,
                "Wings with a Memory"
        );
    }

    private static void registerIronsSpellbooksDefinitions() {
        registerIronsSpellbooksDefinition(
                FIRST_SPELLBOOK_EQUIPPED,
                DiaryCategory.RARE,
                DiaryImportance.MAJOR,
                15,
                "A Book at My Side"
        );
        registerIronsSpellbooksDefinition(
                FIRST_SCROLL_ACQUIRED,
                DiaryCategory.RARE,
                DiaryImportance.NORMAL,
                12,
                "Ink With Intent"
        );
        registerIronsSpellbooksDefinition(
                FIRST_INSCRIPTION_TABLE,
                DiaryCategory.RARE,
                DiaryImportance.NORMAL,
                12,
                "The Table That Expects Care"
        );
        registerIronsSpellbooksDefinition(
                FIRST_SCROLL_FORGE,
                DiaryCategory.RARE,
                DiaryImportance.MAJOR,
                12,
                "A Forge for Bad Ideas"
        );
        registerIronsSpellbooksDefinition(
                FIRST_ARCANE_ANVIL,
                DiaryCategory.RARE,
                DiaryImportance.MAJOR,
                15,
                "The Anvil Hums Back"
        );
        registerIronsSpellbooksDefinition(
                FIRST_DEAD_KING_DEFEATED,
                DiaryCategory.BOSSES,
                DiaryImportance.LEGENDARY,
                20,
                "The Dead King Falls"
        );
        registerIronsSpellbooksDefinition(
                FIRST_FIRE_BOSS_DEFEATED,
                DiaryCategory.BOSSES,
                DiaryImportance.MAJOR,
                20,
                "Echo of Tyros"
        );
        registerIronsSpellbooksDefinition(
                FIRST_CATACOMBS_VISIT,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                15,
                "The Catacombs Below"
        );
        registerIronsSpellbooksDefinition(
                FIRST_LEGENDARY_INK,
                DiaryCategory.RARE,
                DiaryImportance.MAJOR,
                15,
                "Legendary Ink"
        );
    }

    private static void registerCataclysmDefinitions() {
        registerCataclysmDefinition(
                FIRST_RUINED_CITADEL_VISIT,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                15,
                "The Citadel in Ruins"
        );
        registerCataclysmDefinition(
                FIRST_ENDER_GUARDIAN_DEFEATED,
                DiaryCategory.BOSSES,
                DiaryImportance.LEGENDARY,
                20,
                "The Guardian Falls"
        );
        registerCataclysmDefinition(
                FIRST_NETHERITE_MONSTROSITY_DEFEATED,
                DiaryCategory.BOSSES,
                DiaryImportance.LEGENDARY,
                20,
                "The Furnace Goes Quiet"
        );
        registerCataclysmDefinition(
                FIRST_IGNIS_DEFEATED,
                DiaryCategory.BOSSES,
                DiaryImportance.LEGENDARY,
                20,
                "Ignis Goes Cold"
        );
        registerCataclysmDefinition(
                FIRST_GAUNTLET_OF_GUARD_ACQUIRED,
                DiaryCategory.RARE,
                DiaryImportance.MAJOR,
                15,
                "The Guardian's Gauntlet"
        );
        registerCataclysmDefinition(
                FIRST_ANCIENT_FACTORY_VISIT,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                15,
                "The Ancient Factory"
        );
        registerCataclysmDefinition(
                FIRST_SUNKEN_CITY_VISIT,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                15,
                "The Sunken City"
        );
        registerCataclysmDefinition(
                FIRST_CURSED_PYRAMID_VISIT,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                15,
                "The Cursed Pyramid"
        );
        registerCataclysmDefinition(
                FIRST_HARBINGER_DEFEATED,
                DiaryCategory.BOSSES,
                DiaryImportance.LEGENDARY,
                20,
                "The Harbinger Falls"
        );
        registerCataclysmDefinition(
                FIRST_LEVIATHAN_DEFEATED,
                DiaryCategory.BOSSES,
                DiaryImportance.LEGENDARY,
                20,
                "The Leviathan Falls"
        );
        registerCataclysmDefinition(
                FIRST_ANCIENT_REMNANT_DEFEATED,
                DiaryCategory.BOSSES,
                DiaryImportance.LEGENDARY,
                20,
                "The Ancient Remnant Falls"
        );
        registerCataclysmDefinition(
                FIRST_MALEDICTUS_DEFEATED,
                DiaryCategory.BOSSES,
                DiaryImportance.LEGENDARY,
                20,
                "Maledictus Falls"
        );
        registerCataclysmDefinition(
                FIRST_SCYLLA_DEFEATED,
                DiaryCategory.BOSSES,
                DiaryImportance.LEGENDARY,
                20,
                "Scylla Falls"
        );
        registerCataclysmDefinition(
                FIRST_FROSTED_PRISON_VISIT,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                15,
                "The Frosted Prison"
        );
        registerCataclysmDefinition(
                FIRST_ACROPOLIS_VISIT,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                15,
                "The Acropolis"
        );
        registerCataclysmDefinition(
                FIRST_BURNING_ARENA_VISIT,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                15,
                "The Burning Arena"
        );
        registerCataclysmDefinition(
                FIRST_SOUL_BLACK_SMITH_VISIT,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                15,
                "The Soul Black Smith"
        );
    }

    private static void registerCreateDefinitions() {
        registerCreateDefinition(
                FIRST_WATER_WHEEL_POWERED,
                DiaryCategory.BUILDING,
                DiaryImportance.MAJOR,
                15,
                "The First Turning Wheel"
        );
        registerCreateDefinition(
                FIRST_MECHANICAL_PRESS,
                DiaryCategory.BUILDING,
                DiaryImportance.NORMAL,
                10,
                "Under the Press"
        );
        registerCreateDefinition(
                FIRST_MECHANICAL_MIXER,
                DiaryCategory.BUILDING,
                DiaryImportance.NORMAL,
                10,
                "The Mixer Turns"
        );
        registerCreateDefinition(
                FIRST_DEPLOYER_POWERED,
                DiaryCategory.BUILDING,
                DiaryImportance.NORMAL,
                10,
                "A Hand of Brass"
        );
        registerCreateDefinition(
                FIRST_MECHANICAL_CRAFTER_POWERED,
                DiaryCategory.BUILDING,
                DiaryImportance.MAJOR,
                15,
                "A Machine That Crafts"
        );
        registerCreateDefinition(
                FIRST_STEAM_ENGINE,
                DiaryCategory.BUILDING,
                DiaryImportance.MAJOR,
                15,
                "Steam in the Workshop"
        );
        registerCreateDefinition(
                FIRST_TRAIN_ASSEMBLED,
                DiaryCategory.BUILDING,
                DiaryImportance.MAJOR,
                15,
                "The First Train"
        );
        registerCreateDefinition(
                FIRST_FACTORY_GAUGE_REQUEST,
                DiaryCategory.BUILDING,
                DiaryImportance.NORMAL,
                10,
                "The Factory Answers"
        );
        registerCreateDefinition(
                FIRST_CRUSHING_WHEELS_POWERED,
                DiaryCategory.BUILDING,
                DiaryImportance.MAJOR,
                15,
                "Between the Crushing Wheels"
        );
        registerCreateDefinition(
                FIRST_FAN_PROCESSING,
                DiaryCategory.BUILDING,
                DiaryImportance.NORMAL,
                10,
                "Work on the Air"
        );
        registerCreateDefinition(
                FIRST_SPOUT_FILLING,
                DiaryCategory.BUILDING,
                DiaryImportance.NORMAL,
                10,
                "A Careful Pour"
        );
        registerCreateDefinition(
                FIRST_HOSE_PULLEY_OPERATION,
                DiaryCategory.BUILDING,
                DiaryImportance.MAJOR,
                15,
                "The Long Pipe Down"
        );
        registerCreateDefinition(
                FIRST_PRECISION_MECHANISM,
                DiaryCategory.BUILDING,
                DiaryImportance.MAJOR,
                15,
                "Precision at Last"
        );
        registerCreateDefinition(
                FIRST_MECHANICAL_ARM_DELIVERY,
                DiaryCategory.BUILDING,
                DiaryImportance.MAJOR,
                15,
                "The Arm Knows Where"
        );
        registerCreateDefinition(
                FIRST_TRAIN_SCHEDULE_ASSIGNED,
                DiaryCategory.BUILDING,
                DiaryImportance.MAJOR,
                15,
                "A Route With a Timetable"
        );
        registerCreateDefinition(
                FIRST_ROTATION_SPEED_CONTROLLER_TUNED,
                DiaryCategory.BUILDING,
                DiaryImportance.MAJOR,
                15,
                "Speed Under Control"
        );
        registerCreateDefinition(
                FIRST_LINKED_CONTROLLER_SIGNAL,
                DiaryCategory.BUILDING,
                DiaryImportance.NORMAL,
                10,
                "A Signal in Hand"
        );
        registerCreateDefinition(
                FIRST_DISPLAY_LINK_REPORTING,
                DiaryCategory.BUILDING,
                DiaryImportance.NORMAL,
                10,
                "The Factory Reports"
        );
        registerCreateDefinition(
                FIRST_ROPE_PULLEY_DEEP_EXTENSION,
                DiaryCategory.BUILDING,
                DiaryImportance.MAJOR,
                15,
                "The Long Rope Down"
        );
    }

    private static void registerFarmersDelightDefinitions() {
        registerFarmersDelightDefinition(
                FD_FIRST_KNIFE_ACQUIRED,
                DiaryCategory.BUILDING,
                DiaryImportance.NORMAL,
                10,
                "A Proper Kitchen Knife"
        );
        registerFarmersDelightDefinition(
                FD_FIRST_CUTTING_BOARD_USED,
                DiaryCategory.BUILDING,
                DiaryImportance.NORMAL,
                10,
                "Work on the Board"
        );
        registerFarmersDelightDefinition(
                FD_FIRST_COOKING_POT_PLACED,
                DiaryCategory.BUILDING,
                DiaryImportance.NORMAL,
                10,
                "A Pot on the Fire"
        );
        registerFarmersDelightDefinition(
                FD_FIRST_SKILLET_COOKED,
                DiaryCategory.BUILDING,
                DiaryImportance.NORMAL,
                10,
                "The First Skillet Meal"
        );
        registerFarmersDelightDefinition(
                FD_FIRST_NOURISHING_MEAL,
                DiaryCategory.BUILDING,
                DiaryImportance.MAJOR,
                15,
                "A Meal That Lasts"
        );
        registerFarmersDelightDefinition(
                FD_FIRST_FEAST_SERVED,
                DiaryCategory.BUILDING,
                DiaryImportance.MAJOR,
                15,
                "A Feast on the Table"
        );
        registerFarmersDelightMilestoneDefinition(
                FD_MEALS_EATEN_10,
                DiaryCategory.BUILDING,
                DiaryImportance.NORMAL,
                10,
                "Ten Proper Meals",
                "farmersdelight_meals_eaten",
                10
        );
        registerFarmersDelightMilestoneDefinition(
                FD_MEALS_EATEN_50,
                DiaryCategory.BUILDING,
                DiaryImportance.MAJOR,
                15,
                "A Cook's Routine",
                "farmersdelight_meals_eaten",
                50
        );
        registerFarmersDelightMilestoneDefinition(
                FD_FEASTS_SERVED_5,
                DiaryCategory.BUILDING,
                DiaryImportance.MAJOR,
                15,
                "Five Feasts Served",
                "farmersdelight_feasts_served",
                5
        );
        registerFarmersDelightDefinition(
                FD_FIRST_STOVE_PLACED,
                DiaryCategory.BUILDING,
                DiaryImportance.NORMAL,
                10,
                "A Stove for the Kitchen"
        );
        registerFarmersDelightDefinition(
                FD_FIRST_PANTRY_CABINET,
                DiaryCategory.BUILDING,
                DiaryImportance.NORMAL,
                10,
                "A Cabinet for Supplies"
        );
        registerFarmersDelightDefinition(
                FD_FIRST_RICH_SOIL_CREATED,
                DiaryCategory.BUILDING,
                DiaryImportance.NORMAL,
                10,
                "Soil Worth Saving"
        );
    }

    private static void registerWaystonesDefinitions() {
        registerWaystonesDefinition(
                WAYSTONES_FIRST_WAYSTONE_ACTIVATED,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                15,
                "The First Waystone"
        );
        registerWaystonesDefinition(
                WAYSTONES_FIRST_WAYSTONE_PLACED,
                DiaryCategory.BUILDING,
                DiaryImportance.NORMAL,
                10,
                "A Stone of My Own"
        );
        registerWaystonesDefinition(
                WAYSTONES_FIRST_WARP_STONE_ACQUIRED,
                DiaryCategory.RARE,
                DiaryImportance.NORMAL,
                10,
                "A Stone for the Road"
        );
        registerWaystonesDefinition(
                WAYSTONES_FIRST_RETURN_SCROLL_USED,
                DiaryCategory.EXPLORATION,
                DiaryImportance.NORMAL,
                10,
                "A Way Back"
        );
        registerWaystonesMilestoneDefinition(
                WAYSTONES_ACTIVATED_WAYSTONES_5,
                DiaryCategory.EXPLORATION,
                DiaryImportance.MAJOR,
                15,
                "A Small Network",
                WAYSTONES_ACTIVATED_COUNTER,
                5
        );
    }

    private static void registerTwilightForestDefinition(
            String eventId,
            DiaryCategory category,
            DiaryImportance importance,
            String icon,
            int variantCount,
            String fallbackTitle
    ) {
        if (DearDiaryEventRegistry.isRegistered(eventId)) {
            return;
        }

        String baseKey = langBaseKey(eventId);
        String titleKey = baseKey + ".title";
        AutomaticEventDefinition.Builder builder = AutomaticEventDefinition.builder(eventId)
                .source(TWILIGHT_FOREST_MOD_ID)
                .category(category)
                .importance(importance)
                .triggerPolicy(TriggerPolicy.ONCE_PER_PLAYER)
                .icon(icon)
                .includeLocation(true)
                .shareable(true);

        for (int index = 0; index < variantCount; index++) {
            builder.variant(
                    Integer.toString(index),
                    titleKey,
                    baseKey + ".text." + index,
                    fallbackTitle,
                    FALLBACK_TEXT
            );
        }

        DearDiaryEventRegistry.register(builder.build());
    }

    private static void registerAetherDefinition(
            String eventId,
            DiaryCategory category,
            DiaryImportance importance,
            int variantCount,
            String fallbackTitle
    ) {
        if (DearDiaryEventRegistry.isRegistered(eventId)) {
            return;
        }

        String baseKey = langBaseKey(eventId);
        String titleKey = baseKey + ".title";
        AutomaticEventDefinition.Builder builder = AutomaticEventDefinition.builder(eventId)
                .source(AETHER_MOD_ID)
                .category(category)
                .importance(importance)
                .triggerPolicy(TriggerPolicy.ONCE_PER_PLAYER)
                .includeLocation(true)
                .shareable(true);

        for (int index = 0; index < variantCount; index++) {
            builder.variant(
                    Integer.toString(index),
                    titleKey,
                    baseKey + ".text." + index,
                    fallbackTitle,
                    AETHER_FALLBACK_TEXT
            );
        }

        DearDiaryEventRegistry.register(builder.build());
    }

    private static void registerDeeperDarkerDefinition(
            String eventId,
            DiaryCategory category,
            DiaryImportance importance,
            int variantCount,
            String fallbackTitle
    ) {
        if (DearDiaryEventRegistry.isRegistered(eventId)) {
            return;
        }

        String baseKey = langBaseKey(eventId);
        String titleKey = baseKey + ".title";
        AutomaticEventDefinition.Builder builder = AutomaticEventDefinition.builder(eventId)
                .source(DEEPER_DARKER_MOD_ID)
                .category(category)
                .importance(importance)
                .triggerPolicy(TriggerPolicy.ONCE_PER_PLAYER)
                .includeLocation(true)
                .shareable(true);

        for (int index = 0; index < variantCount; index++) {
            builder.variant(
                    Integer.toString(index),
                    titleKey,
                    baseKey + ".text." + index,
                    fallbackTitle,
                    DEEPER_DARKER_FALLBACK_TEXT
            );
        }

        DearDiaryEventRegistry.register(builder.build());
    }

    private static void registerIronsSpellbooksDefinition(
            String eventId,
            DiaryCategory category,
            DiaryImportance importance,
            int variantCount,
            String fallbackTitle
    ) {
        if (DearDiaryEventRegistry.isRegistered(eventId)) {
            return;
        }

        String baseKey = langBaseKey(eventId);
        String titleKey = baseKey + ".title";
        AutomaticEventDefinition.Builder builder = AutomaticEventDefinition.builder(eventId)
                .source(IRONS_SPELLBOOKS_MOD_ID)
                .category(category)
                .importance(importance)
                .triggerPolicy(TriggerPolicy.ONCE_PER_PLAYER)
                .includeLocation(true)
                .shareable(true);

        for (int index = 0; index < variantCount; index++) {
            builder.variant(
                    Integer.toString(index),
                    titleKey,
                    baseKey + ".text." + index,
                    fallbackTitle,
                    IRONS_SPELLBOOKS_FALLBACK_TEXT
            );
        }

        DearDiaryEventRegistry.register(builder.build());
    }

    private static void registerCataclysmDefinition(
            String eventId,
            DiaryCategory category,
            DiaryImportance importance,
            int variantCount,
            String fallbackTitle
    ) {
        if (DearDiaryEventRegistry.isRegistered(eventId)) {
            return;
        }

        String baseKey = langBaseKey(eventId);
        String titleKey = baseKey + ".title";
        AutomaticEventDefinition.Builder builder = AutomaticEventDefinition.builder(eventId)
                .source(CATACLYSM_MOD_ID)
                .category(category)
                .importance(importance)
                .triggerPolicy(TriggerPolicy.ONCE_PER_PLAYER)
                .includeLocation(true)
                .shareable(true);

        for (int index = 0; index < variantCount; index++) {
            builder.variant(
                    Integer.toString(index),
                    titleKey,
                    baseKey + ".text." + index,
                    fallbackTitle,
                    CATACLYSM_FALLBACK_TEXT
            );
        }

        DearDiaryEventRegistry.register(builder.build());
    }

    private static void registerCreateDefinition(
            String eventId,
            DiaryCategory category,
            DiaryImportance importance,
            int variantCount,
            String fallbackTitle
    ) {
        if (DearDiaryEventRegistry.isRegistered(eventId)) {
            return;
        }

        String baseKey = langBaseKey(eventId);
        String titleKey = baseKey + ".title";
        AutomaticEventDefinition.Builder builder = AutomaticEventDefinition.builder(eventId)
                .source(CREATE_MOD_ID)
                .category(category)
                .importance(importance)
                .triggerPolicy(TriggerPolicy.ONCE_PER_PLAYER)
                .includeLocation(true)
                .shareable(true);

        for (int index = 0; index < variantCount; index++) {
            builder.variant(
                    Integer.toString(index),
                    titleKey,
                    baseKey + ".text." + index,
                    fallbackTitle,
                    CREATE_FALLBACK_TEXT
            );
        }

        DearDiaryEventRegistry.register(builder.build());
    }

    private static void registerFarmersDelightDefinition(
            String eventId,
            DiaryCategory category,
            DiaryImportance importance,
            int variantCount,
            String fallbackTitle
    ) {
        if (DearDiaryEventRegistry.isRegistered(eventId)) {
            return;
        }

        String baseKey = langBaseKey(eventId);
        String titleKey = baseKey + ".title";
        AutomaticEventDefinition.Builder builder = AutomaticEventDefinition.builder(eventId)
                .source(FARMERS_DELIGHT_MOD_ID)
                .category(category)
                .importance(importance)
                .triggerPolicy(TriggerPolicy.ONCE_PER_PLAYER)
                .includeLocation(true)
                .shareable(true);

        for (int index = 0; index < variantCount; index++) {
            builder.variant(
                    Integer.toString(index),
                    titleKey,
                    baseKey + ".text." + index,
                    fallbackTitle,
                    FARMERS_DELIGHT_FALLBACK_TEXT
            );
        }

        DearDiaryEventRegistry.register(builder.build());
    }

    private static void registerFarmersDelightMilestoneDefinition(
            String eventId,
            DiaryCategory category,
            DiaryImportance importance,
            int variantCount,
            String fallbackTitle,
            String counterId,
            int threshold
    ) {
        if (DearDiaryEventRegistry.isRegistered(eventId)) {
            return;
        }

        String baseKey = langBaseKey(eventId);
        String titleKey = baseKey + ".title";
        AutomaticEventDefinition.Builder builder = AutomaticEventDefinition.builder(eventId)
                .source(FARMERS_DELIGHT_MOD_ID)
                .category(category)
                .importance(importance)
                .triggerPolicy(TriggerPolicy.MILESTONE)
                .milestoneCounter(counterId)
                .milestoneThreshold(threshold)
                .includeLocation(true)
                .shareable(true);

        for (int index = 0; index < variantCount; index++) {
            builder.variant(
                    Integer.toString(index),
                    titleKey,
                    baseKey + ".text." + index,
                    fallbackTitle,
                    FARMERS_DELIGHT_FALLBACK_TEXT
            );
        }

        DearDiaryEventRegistry.register(builder.build());
    }

    private static void registerWaystonesDefinition(
            String eventId,
            DiaryCategory category,
            DiaryImportance importance,
            int variantCount,
            String fallbackTitle
    ) {
        if (DearDiaryEventRegistry.isRegistered(eventId)) {
            return;
        }

        String baseKey = langBaseKey(eventId);
        String titleKey = baseKey + ".title";
        AutomaticEventDefinition.Builder builder = AutomaticEventDefinition.builder(eventId)
                .source(WAYSTONES_MOD_ID)
                .category(category)
                .importance(importance)
                .triggerPolicy(TriggerPolicy.ONCE_PER_PLAYER)
                .includeLocation(true)
                .shareable(true);

        for (int index = 0; index < variantCount; index++) {
            builder.variant(
                    Integer.toString(index),
                    titleKey,
                    baseKey + ".text." + index,
                    fallbackTitle,
                    WAYSTONES_FALLBACK_TEXT
            );
        }

        DearDiaryEventRegistry.register(builder.build());
    }

    private static void registerWaystonesMilestoneDefinition(
            String eventId,
            DiaryCategory category,
            DiaryImportance importance,
            int variantCount,
            String fallbackTitle,
            String counterId,
            int threshold
    ) {
        if (DearDiaryEventRegistry.isRegistered(eventId)) {
            return;
        }

        String baseKey = langBaseKey(eventId);
        String titleKey = baseKey + ".title";
        AutomaticEventDefinition.Builder builder = AutomaticEventDefinition.builder(eventId)
                .source(WAYSTONES_MOD_ID)
                .category(category)
                .importance(importance)
                .triggerPolicy(TriggerPolicy.MILESTONE)
                .milestoneCounter(counterId)
                .milestoneThreshold(threshold)
                .includeLocation(true)
                .shareable(true);

        for (int index = 0; index < variantCount; index++) {
            builder.variant(
                    Integer.toString(index),
                    titleKey,
                    baseKey + ".text." + index,
                    fallbackTitle,
                    WAYSTONES_FALLBACK_TEXT
            );
        }

        DearDiaryEventRegistry.register(builder.build());
    }

    private static String langBaseKey(String eventId) {
        return "entry.dear_diary." + eventId.replace(':', '.');
    }
}
