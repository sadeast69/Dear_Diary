package com.worldremembers.deardiary.event;

import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.DearDiaryServices;
import com.worldremembers.deardiary.api.DearDiaryApi;
import com.worldremembers.deardiary.data.AutomaticEventState;
import com.worldremembers.deardiary.data.DiaryEntry;
import com.worldremembers.deardiary.network.DearDiaryNetworking;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class VanillaDiaryEvents {
    private static final int INVENTORY_SCAN_INTERVAL_TICKS = 40;
    private static final int LOCATION_SCAN_INTERVAL_TICKS = 20 * 5;
    private static final Map<ResourceLocation, String> ADVANCEMENT_EVENTS = Map.of(
            ResourceLocation.fromNamespaceAndPath("minecraft", "nether/find_fortress"), VanillaDiaryEventDefinitions.FIRST_NETHER_FORTRESS,
            ResourceLocation.fromNamespaceAndPath("minecraft", "nether/find_bastion"), VanillaDiaryEventDefinitions.FIRST_BASTION,
            ResourceLocation.fromNamespaceAndPath("minecraft", "end/find_end_city"), VanillaDiaryEventDefinitions.FIRST_END_CITY,
            ResourceLocation.fromNamespaceAndPath("minecraft", "adventure/minecraft_trials_edition"), VanillaDiaryEventDefinitions.FIRST_TRIAL_CHAMBER,
            ResourceLocation.fromNamespaceAndPath("minecraft", "husbandry/breed_an_animal"), VanillaDiaryEventDefinitions.FIRST_ANIMAL_BREED
    );
    private static final List<ResourceKey<Structure>> VILLAGE_STRUCTURES = List.of(
            BuiltinStructures.VILLAGE_PLAINS,
            BuiltinStructures.VILLAGE_DESERT,
            BuiltinStructures.VILLAGE_SAVANNA,
            BuiltinStructures.VILLAGE_SNOWY,
            BuiltinStructures.VILLAGE_TAIGA
    );

    private static boolean registered;
    private static int inventoryScanTicker;
    private static int locationScanTicker;

    private VanillaDiaryEvents() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        VanillaDiaryEventDefinitions.registerAll();
        NeoForge.EVENT_BUS.addListener(VanillaDiaryEvents::onEndServerTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, VanillaDiaryEvents::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, VanillaDiaryEvents::onBlockPlace);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, VanillaDiaryEvents::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, VanillaDiaryEvents::onLivingUseTotem);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, VanillaDiaryEvents::onPlayerChangedDimension);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, VanillaDiaryEvents::onPlayerSleep);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, VanillaDiaryEvents::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, VanillaDiaryEvents::onAnimalTame);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, VanillaDiaryEvents::onAdvancementProgress);
        DearDiaryMod.LOGGER.info("Dear Diary vanilla gameplay event callbacks registered for NeoForge");
    }

    private static void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled()) {
            return;
        }

        LivingEntity entity = event.getEntity();
        DamageSource damageSource = event.getSource();
        if (entity instanceof ServerPlayer player) {
            Optional<DiaryEntry> firstDeath = triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_DEATH);
            if (firstDeath.isEmpty()) {
                triggerDeathCause(player, damageSource);
            }
            if (damageSource.getEntity() instanceof ServerPlayer killer) {
                handlePlayerKilledPlayer(killer, player);
            }
            return;
        }

        Entity attacker = damageSource.getEntity();
        if (attacker instanceof ServerPlayer player) {
            onPlayerKilledEntity(player, entity);
        }
    }

    private static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) {
            return;
        }

        if (event.getTo().equals(Level.NETHER)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_NETHER_ENTRY);
        } else if (event.getTo().equals(Level.END)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_END_ENTRY);
        }
    }

    private static void onPlayerSleep(CanPlayerSleepEvent event) {
        ServerPlayer player = event.getEntity();
        if (player.isSpectator() || event.getProblem() != null) {
            return;
        }

        triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_SLEEP);
    }

    private static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.isCanceled()
                || event.getLevel().isClientSide()
                || !(event.getEntity() instanceof ServerPlayer player)
                || player.isSpectator()) {
            return;
        }

        if (event.getTarget() instanceof Villager) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_VILLAGER_INTERACTION);
        }
    }

    private static void onAnimalTame(AnimalTameEvent event) {
        if (event.isCanceled()
                || !(event.getTamer() instanceof ServerPlayer player)
                || player.isSpectator()) {
            return;
        }

        Entity animal = event.getAnimal();
        if (animal instanceof Wolf) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_TAMED_WOLF);
        } else if (animal instanceof Cat) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_TAMED_CAT);
        } else if (animal instanceof Parrot) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_TAMED_PARROT);
        } else if (animal instanceof AbstractHorse) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_TAMED_HORSE);
        }
    }

    private static void onLivingUseTotem(LivingUseTotemEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) {
            return;
        }

        triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_TOTEM_USE);
    }

    private static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled()
                || event.getLevel().isClientSide()
                || !(event.getPlayer() instanceof ServerPlayer player)
                || player.isSpectator()) {
            return;
        }

        incrementCounterAndSync(player, VanillaDiaryEventDefinitions.COUNTER_BLOCKS_MINED, 1);
    }

    private static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.isCanceled()
                || event.getLevel().isClientSide()
                || !(event.getEntity() instanceof ServerPlayer player)
                || player.isSpectator()) {
            return;
        }

        BlockState placedState = event.getPlacedBlock();
        if (placedState.isAir()) {
            return;
        }

        triggerPlacedBlockEvents(player, placedState);
        incrementCounterAndSync(player, VanillaDiaryEventDefinitions.COUNTER_BLOCKS_PLACED, 1);
    }

    private static void onAdvancementProgress(AdvancementEvent.AdvancementProgressEvent event) {
        if (event.getProgressType() != AdvancementEvent.AdvancementProgressEvent.ProgressType.GRANT
                || !(event.getEntity() instanceof ServerPlayer player)
                || player.isSpectator()) {
            return;
        }

        String eventId = ADVANCEMENT_EVENTS.get(event.getAdvancement().id());
        if (eventId != null) {
            triggerAndSync(player, eventId);
        }
    }

    public static void onRaidStarted(ServerPlayer player) {
        if (player == null || player.isSpectator()) {
            return;
        }

        triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_RAID_STARTED);
    }

    private static void onEndServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        locationScanTicker++;
        if (locationScanTicker >= LOCATION_SCAN_INTERVAL_TICKS) {
            locationScanTicker = 0;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                scanLocation(player);
            }
        }

        inventoryScanTicker++;
        if (inventoryScanTicker < INVENTORY_SCAN_INTERVAL_TICKS) {
            return;
        }

        inventoryScanTicker = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            scanInventory(player);
        }
    }

    private static void scanLocation(ServerPlayer player) {
        if (player.isSpectator()) {
            return;
        }

        AutomaticEventState state = DearDiaryApi.getDiary(player).automaticEventState();
        boolean needsVillage = needsLocationEvent(state, VanillaDiaryEventDefinitions.FIRST_VILLAGE_VISIT);
        boolean needsAncientCity = needsLocationEvent(state, VanillaDiaryEventDefinitions.FIRST_ANCIENT_CITY);
        boolean needsDeepDark = needsLocationEvent(state, VanillaDiaryEventDefinitions.FIRST_DEEP_DARK_VISIT);
        if (!needsVillage && !needsAncientCity && !needsDeepDark) {
            return;
        }

        ServerLevel world = player.serverLevel();
        if (!world.dimension().equals(Level.OVERWORLD)) {
            return;
        }

        BlockPos pos = player.blockPosition();
        if (needsDeepDark && world.getBiome(pos).is(Biomes.DEEP_DARK)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_DEEP_DARK_VISIT);
        }
        if (needsAncientCity && isInsideStructure(world, pos, BuiltinStructures.ANCIENT_CITY)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ANCIENT_CITY);
        }
        if (needsVillage && isInsideAnyStructure(world, pos, VILLAGE_STRUCTURES)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_VILLAGE_VISIT);
        }
    }

    private static boolean needsLocationEvent(AutomaticEventState state, String eventId) {
        if (state.isTriggered(eventId)) {
            return false;
        }

        return DearDiaryEventRegistry.get(eventId)
                .map(definition -> DearDiaryServices.config().isAutomaticEventAllowed(
                        definition.eventId(),
                        definition.category(),
                        definition.importance()
                ))
                .orElse(false);
    }

    private static boolean isInsideAnyStructure(
            ServerLevel world,
            BlockPos pos,
            List<ResourceKey<Structure>> structureKeys
    ) {
        for (ResourceKey<Structure> structureKey : structureKeys) {
            if (isInsideStructure(world, pos, structureKey)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isInsideStructure(ServerLevel world, BlockPos pos, ResourceKey<Structure> structureKey) {
        Structure structure = world.registryAccess().registryOrThrow(Registries.STRUCTURE).get(structureKey);
        if (structure == null) {
            return false;
        }

        StructureStart structureStart = world.structureManager().getStructureWithPieceAt(pos, structure);
        return structureStart != null && structureStart.isValid();
    }

    private static void scanInventory(ServerPlayer player) {
        if (player.isSpectator()) {
            return;
        }

        triggerStatEvents(player);
        triggerStatusEvents(player);

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
                triggerItemEvents(player, stack);
            }
        }
    }

    private static void triggerItemEvents(ServerPlayer player, ItemStack stack) {
        Item item = stack.getItem();
        if (stack.is(Items.DIAMOND)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_DIAMOND);
        } else if (stack.is(Items.EMERALD)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_EMERALD);
        } else if (stack.is(Items.IRON_INGOT)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_IRON_INGOT);
        } else if (stack.is(Items.GOLD_INGOT)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_GOLD_INGOT);
        } else if (stack.is(Items.REDSTONE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_REDSTONE);
        } else if (stack.is(Items.LAPIS_LAZULI)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_LAPIS);
        } else if (stack.is(Items.OBSIDIAN)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_OBSIDIAN);
        } else if (stack.is(Items.ELYTRA)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ELYTRA);
        } else if (stack.is(Items.NETHERITE_INGOT)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_NETHERITE_INGOT);
        } else if (stack.is(Items.BLAZE_ROD)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BLAZE_ROD);
        } else if (stack.is(Items.ENDER_PEARL)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ENDER_PEARL);
        } else if (stack.is(Items.BOW)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BOW);
        } else if (stack.is(Items.SHIELD)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_SHIELD);
        } else if (stack.is(Items.CRAFTING_TABLE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_CRAFTING_TABLE);
        } else if (stack.is(Items.FURNACE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_FURNACE);
        } else if (stack.is(Items.CHEST)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_CHEST);
        } else if (stack.is(Items.ENCHANTING_TABLE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ENCHANTING_TABLE);
        } else if (isAnvilItem(stack)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ANVIL);
        } else if (stack.is(Items.BREWING_STAND)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BREWING_STAND);
        } else if (stack.is(Items.BEACON)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BEACON);
        } else if (isShulkerBoxItem(stack)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_SHULKER_BOX);
        } else if (stack.is(Items.TORCH) || stack.is(Items.SOUL_TORCH)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_TORCH);
        } else if (stack.is(Items.MAP) || stack.is(Items.FILLED_MAP)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_MAP);
        } else if (item instanceof BoatItem) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BOAT);
        } else if (stack.is(Items.WATER_BUCKET)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_WATER_BUCKET);
        } else if (stack.is(Items.LAVA_BUCKET)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_LAVA_BUCKET);
        } else if (stack.is(Items.FLINT_AND_STEEL)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_FLINT_AND_STEEL);
        } else if (stack.is(Items.SHEARS)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_SHEARS);
        } else if (stack.is(Items.MILK_BUCKET)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BUCKET_MILK);
        } else if (stack.is(Items.BREAD)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BREAD);
        } else if (stack.is(Items.CAKE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_CAKE);
        } else if (stack.is(Items.GOLDEN_APPLE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_GOLDEN_APPLE);
        } else if (stack.is(Items.WHEAT)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_WHEAT);
        } else if (isFishItem(stack)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_FISH);
        } else if (stack.is(Items.COMPASS)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_COMPASS);
        } else if (stack.is(Items.CLOCK)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_CLOCK);
        } else if (stack.is(Items.SPYGLASS)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_SPYGLASS);
        } else if (stack.is(Items.NAME_TAG)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_NAME_TAG);
        } else if (stack.is(Items.ENDER_CHEST)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ENDER_CHEST);
        } else if (stack.is(Items.ENDER_EYE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_EYE_OF_ENDER);
        } else if (isMusicDiscItem(stack)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_MUSIC_DISC);
        } else if (stack.is(Items.GOAT_HORN)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_GOAT_HORN);
        } else if (isPotionItem(stack)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_POTION);
        } else if (stack.is(Items.ENCHANTED_BOOK)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ENCHANTED_BOOK);
        } else if (stack.is(Items.EXPERIENCE_BOTTLE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_EXPERIENCE_BOTTLE);
        } else if (stack.is(Items.TRIAL_KEY)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_TRIAL_KEY);
        } else if (stack.is(Items.OMINOUS_TRIAL_KEY)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_OMINOUS_TRIAL_KEY);
        } else if (stack.is(Items.BREEZE_ROD)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BREEZE_ROD);
        } else if (stack.is(Items.WIND_CHARGE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_WIND_CHARGE);
        } else if (stack.is(Items.MACE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_MACE);
        } else if (stack.is(Items.ECHO_SHARD)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ECHO_SHARD);
        } else if (stack.is(Items.RECOVERY_COMPASS)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_RECOVERY_COMPASS);
        } else if (stack.is(Items.SCULK_SENSOR)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_SCULK_SENSOR);
        } else if (stack.is(Items.SCULK_CATALYST)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_SCULK_CATALYST);
        } else if (stack.is(Items.CALIBRATED_SCULK_SENSOR)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_CALIBRATED_SCULK_SENSOR);
        } else if (stack.is(Items.BRUSH)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BRUSH);
        } else if (isPotterySherdItem(stack)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_POTTERY_SHERD);
        } else if (stack.is(Items.DECORATED_POT)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_DECORATED_POT);
        } else if (isSmithingTemplateItem(stack)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_SMITHING_TEMPLATE);
        } else if (stack.is(Items.SNIFFER_EGG)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_SNIFFER_EGG);
        } else if (stack.is(Items.TORCHFLOWER_SEEDS)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_TORCHFLOWER_SEEDS);
        } else if (stack.is(Items.PITCHER_POD)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_PITCHER_POD);
        } else if (stack.is(Items.HEAVY_CORE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_HEAVY_CORE);
        } else if (stack.is(Items.OMINOUS_BOTTLE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_OMINOUS_BOTTLE);
        } else if (stack.is(Items.DISC_FRAGMENT_5)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_DISC_FRAGMENT);
        }

        if (stack.isEnchanted() && !stack.is(Items.ENCHANTED_BOOK)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ENCHANTED_ITEM);
        }
    }

    private static void triggerStatEvents(ServerPlayer player) {
        if (player.getStats().getValue(Stats.CUSTOM.get(Stats.TRADED_WITH_VILLAGER)) > 0) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_VILLAGER_TRADE);
        }
    }

    private static void triggerStatusEvents(ServerPlayer player) {
        if (player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_HERO_OF_THE_VILLAGE);
        }
    }

    private static void triggerPlacedBlockEvents(ServerPlayer player, BlockState placedState) {
        if (placedState.is(Blocks.CRAFTING_TABLE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_CRAFTING_TABLE);
        } else if (placedState.is(Blocks.FURNACE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_FURNACE);
        } else if (placedState.is(Blocks.CHEST)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_CHEST);
        } else if (placedState.is(Blocks.ENCHANTING_TABLE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ENCHANTING_TABLE);
        } else if (isAnvilBlock(placedState)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ANVIL);
        } else if (placedState.is(Blocks.BREWING_STAND)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BREWING_STAND);
        } else if (placedState.is(Blocks.BEACON)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BEACON);
        } else if (placedState.getBlock() instanceof ShulkerBoxBlock) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_SHULKER_BOX);
        } else if (placedState.is(Blocks.TORCH)
                || placedState.is(Blocks.WALL_TORCH)
                || placedState.is(Blocks.SOUL_TORCH)
                || placedState.is(Blocks.SOUL_WALL_TORCH)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_TORCH);
        }
    }

    private static void onPlayerKilledEntity(ServerPlayer player, LivingEntity entity) {
        if (player.isSpectator()) {
            return;
        }

        if (entity instanceof Monster) {
            incrementCounterAndSync(player, VanillaDiaryEventDefinitions.COUNTER_MOB_KILLS, 1);
        }

        if (entity instanceof EnderMan) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ENDERMAN_KILL);
        } else if (entity instanceof Blaze) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BLAZE_KILL);
        } else if (entity instanceof WitherSkeleton) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_WITHER_SKELETON_KILL);
        } else if (entity instanceof EnderDragon) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ENDER_DRAGON_KILL);
        } else if (entity instanceof WitherBoss) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_WITHER_KILL);
        } else if (entity instanceof ElderGuardian) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ELDER_GUARDIAN_KILL);
        } else if (entity instanceof Warden) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_WARDEN_KILL);
        }
    }

    private static void handlePlayerKilledPlayer(ServerPlayer killer, ServerPlayer victim) {
        if (killer instanceof FakePlayer
                || killer.isSpectator()
                || victim.isSpectator()
                || killer.getUUID().equals(victim.getUUID())) {
            return;
        }

        triggerAndSync(killer, VanillaDiaryEventDefinitions.FIRST_PVP_KILL);
        incrementCounterAndSync(killer, VanillaDiaryEventDefinitions.COUNTER_PVP_KILLS, 1);
    }

    private static void triggerDeathCause(ServerPlayer player, DamageSource damageSource) {
        String eventId = deathCauseEvent(damageSource);
        if (eventId != null) {
            triggerAndSync(player, eventId);
        }
    }

    private static String deathCauseEvent(DamageSource damageSource) {
        if (damageSource.is(DamageTypes.LAVA)) {
            return VanillaDiaryEventDefinitions.DEATH_BY_LAVA;
        }

        if (damageSource.is(DamageTypes.FALL)) {
            return VanillaDiaryEventDefinitions.DEATH_BY_FALL;
        }

        if (damageSource.is(DamageTypes.DROWN)) {
            return VanillaDiaryEventDefinitions.DEATH_BY_DROWNING;
        }

        if (causedBy(damageSource, Creeper.class)) {
            return VanillaDiaryEventDefinitions.DEATH_BY_CREEPER;
        }

        if (causedBy(damageSource, Skeleton.class)) {
            return VanillaDiaryEventDefinitions.DEATH_BY_SKELETON;
        }

        if (causedBy(damageSource, EnderMan.class)) {
            return VanillaDiaryEventDefinitions.DEATH_BY_ENDERMAN;
        }

        return null;
    }

    private static boolean causedBy(DamageSource damageSource, Class<? extends Entity> entityType) {
        return entityType.isInstance(damageSource.getEntity()) || entityType.isInstance(damageSource.getDirectEntity());
    }

    private static boolean isAnvilItem(ItemStack stack) {
        return stack.is(Items.ANVIL) || stack.is(Items.CHIPPED_ANVIL) || stack.is(Items.DAMAGED_ANVIL);
    }

    private static boolean isAnvilBlock(BlockState state) {
        return state.is(Blocks.ANVIL) || state.is(Blocks.CHIPPED_ANVIL) || state.is(Blocks.DAMAGED_ANVIL);
    }

    private static boolean isShulkerBoxItem(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock;
    }

    private static boolean isFishItem(ItemStack stack) {
        return stack.is(Items.COD)
                || stack.is(Items.SALMON)
                || stack.is(Items.PUFFERFISH)
                || stack.is(Items.TROPICAL_FISH);
    }

    private static boolean isPotionItem(ItemStack stack) {
        return stack.is(Items.POTION)
                || stack.is(Items.SPLASH_POTION)
                || stack.is(Items.LINGERING_POTION);
    }

    private static boolean isMusicDiscItem(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().startsWith("music_disc_");
    }

    private static boolean isPotterySherdItem(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("_pottery_sherd");
    }

    private static boolean isSmithingTemplateItem(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("_smithing_template");
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

    static Optional<DiaryEntry> triggerAndSync(ServerPlayer player, String eventId) {
        Optional<DiaryEntry> entry = AutomaticDiaryEvents.trigger(player, eventId);
        entry.ifPresent(created -> {
            DearDiaryNetworking.sendDiarySnapshot(player);
            DearDiaryNetworking.sendAutomaticEntryNotice(player, created);
        });
        return entry;
    }
}
