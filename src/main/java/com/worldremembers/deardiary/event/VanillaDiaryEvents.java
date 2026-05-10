package com.worldremembers.deardiary.event;

import com.worldremembers.deardiary.DearDiaryServices;
import com.worldremembers.deardiary.api.DearDiaryApi;
import com.worldremembers.deardiary.compat.fabric.FabricFarmersDelightCompat;
import com.worldremembers.deardiary.compat.fabric.FabricWaystonesCompat;
import com.worldremembers.deardiary.data.AutomaticEventState;
import com.worldremembers.deardiary.data.DiaryEntry;
import com.worldremembers.deardiary.network.DearDiaryNetworking;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;
import net.minecraft.world.World;

public final class VanillaDiaryEvents {
    private static final int INVENTORY_SCAN_INTERVAL_TICKS = 40;
    private static final int LOCATION_SCAN_INTERVAL_TICKS = 20 * 5;
    private static final List<RegistryKey<Structure>> VILLAGE_STRUCTURES = List.of(
            StructureKeys.VILLAGE_PLAINS,
            StructureKeys.VILLAGE_DESERT,
            StructureKeys.VILLAGE_SAVANNA,
            StructureKeys.VILLAGE_SNOWY,
            StructureKeys.VILLAGE_TAIGA
    );
    private static final List<PendingTameCheck> PENDING_TAME_CHECKS = new ArrayList<>();
    private static final List<PendingBlockPlaceCheck> PENDING_BLOCK_PLACE_CHECKS = new ArrayList<>();

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
        registerDeathEvents();
        registerDimensionEvents();
        registerSleepEvents();
        registerTameEvents();
        registerTotemEvents();
        registerBlockEvents();
        ServerTickEvents.END_SERVER_TICK.register(VanillaDiaryEvents::onEndServerTick);
    }

    private static void registerDeathEvents() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity player) {
                Optional<DiaryEntry> firstDeath = triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_DEATH);
                if (firstDeath.isEmpty()) {
                    triggerDeathCause(player, damageSource);
                }
                if (damageSource.getAttacker() instanceof ServerPlayerEntity killer) {
                    handlePlayerKilledPlayer(killer, player);
                }
                return;
            }

            Entity attacker = damageSource.getAttacker();
            if (attacker instanceof ServerPlayerEntity player) {
                onPlayerKilledEntity(player, entity);
            }
        });
    }

    private static void registerDimensionEvents() {
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            if (destination.getRegistryKey().equals(World.NETHER)) {
                triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_NETHER_ENTRY);
            } else if (destination.getRegistryKey().equals(World.END)) {
                triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_END_ENTRY);
            }
        });
    }

    private static void registerSleepEvents() {
        EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
            if (entity instanceof ServerPlayerEntity player) {
                triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_SLEEP);
            }
        });
    }

    private static void registerTameEvents() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) {
                return ActionResult.PASS;
            }

            ItemStack stack = serverPlayer.getStackInHand(hand);
            String eventId = potentialTameEvent(entity, stack);
            if (eventId != null) {
                PENDING_TAME_CHECKS.add(new PendingTameCheck(serverPlayer.getUuid(), entity.getUuid(), eventId, 2));
            }

            if (entity instanceof VillagerEntity) {
                triggerAndSync(serverPlayer, VanillaDiaryEventDefinitions.FIRST_VILLAGER_INTERACTION);
            }

            return ActionResult.PASS;
        });
    }

    private static void registerTotemEvents() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (entity instanceof ServerPlayerEntity player && hasTotemInHand(player)) {
                triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_TOTEM_USE);
            }
            return true;
        });
    }

    private static void registerBlockEvents() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayerEntity serverPlayer && !serverPlayer.isSpectator()) {
                incrementCounterAndSync(serverPlayer, VanillaDiaryEventDefinitions.COUNTER_BLOCKS_MINED, 1);
            }
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer) || serverPlayer.isSpectator()) {
                return ActionResult.PASS;
            }

            ItemStack stack = serverPlayer.getStackInHand(hand);
            if (!(stack.getItem() instanceof BlockItem)) {
                return ActionResult.PASS;
            }

            BlockPos clickedPos = hitResult.getBlockPos().toImmutable();
            BlockPos offsetPos = clickedPos.offset(hitResult.getSide()).toImmutable();
            PENDING_BLOCK_PLACE_CHECKS.add(new PendingBlockPlaceCheck(
                    serverPlayer.getUuid(),
                    clickedPos,
                    world.getBlockState(clickedPos),
                    offsetPos,
                    world.getBlockState(offsetPos),
                    hand,
                    stack.getCount(),
                    2
            ));

            return ActionResult.PASS;
        });
    }

    private static void onEndServerTick(MinecraftServer server) {
        processPendingTameChecks(server);
        processPendingBlockPlaceChecks(server);

        locationScanTicker++;
        if (locationScanTicker >= LOCATION_SCAN_INTERVAL_TICKS) {
            locationScanTicker = 0;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                scanLocation(player);
            }
        }

        inventoryScanTicker++;
        if (inventoryScanTicker < INVENTORY_SCAN_INTERVAL_TICKS) {
            return;
        }

        inventoryScanTicker = 0;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            scanInventory(player);
        }
    }

    private static void scanLocation(ServerPlayerEntity player) {
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

        ServerWorld world = player.getServerWorld();
        if (!world.getRegistryKey().equals(World.OVERWORLD)) {
            return;
        }

        BlockPos pos = player.getBlockPos();
        if (needsDeepDark && world.getBiome(pos).matchesKey(BiomeKeys.DEEP_DARK)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_DEEP_DARK_VISIT);
        }
        if (needsAncientCity && isInsideStructure(world, pos, StructureKeys.ANCIENT_CITY)) {
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
            ServerWorld world,
            BlockPos pos,
            List<RegistryKey<Structure>> structureKeys
    ) {
        for (RegistryKey<Structure> structureKey : structureKeys) {
            if (isInsideStructure(world, pos, structureKey)) {
                return true;
            }
        }

        return false;
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

    private static void scanInventory(ServerPlayerEntity player) {
        if (player.isSpectator()) {
            return;
        }

        triggerStatEvents(player);
        triggerStatusEvents(player);

        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (stack.isEmpty()) {
                continue;
            }

            triggerItemEvents(player, stack);
        }
    }

    private static void triggerItemEvents(ServerPlayerEntity player, ItemStack stack) {
        Item item = stack.getItem();
        if (stack.isOf(Items.DIAMOND)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_DIAMOND);
        } else if (stack.isOf(Items.EMERALD)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_EMERALD);
        } else if (stack.isOf(Items.IRON_INGOT)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_IRON_INGOT);
        } else if (stack.isOf(Items.GOLD_INGOT)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_GOLD_INGOT);
        } else if (stack.isOf(Items.REDSTONE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_REDSTONE);
        } else if (stack.isOf(Items.LAPIS_LAZULI)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_LAPIS);
        } else if (stack.isOf(Items.OBSIDIAN)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_OBSIDIAN);
        } else if (stack.isOf(Items.ELYTRA)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ELYTRA);
        } else if (stack.isOf(Items.NETHERITE_INGOT)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_NETHERITE_INGOT);
        } else if (stack.isOf(Items.BLAZE_ROD)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BLAZE_ROD);
        } else if (stack.isOf(Items.ENDER_PEARL)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ENDER_PEARL);
        } else if (stack.isOf(Items.BOW)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BOW);
        } else if (stack.isOf(Items.SHIELD)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_SHIELD);
        } else if (stack.isOf(Items.CRAFTING_TABLE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_CRAFTING_TABLE);
        } else if (stack.isOf(Items.FURNACE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_FURNACE);
        } else if (stack.isOf(Items.CHEST)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_CHEST);
        } else if (stack.isOf(Items.ENCHANTING_TABLE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ENCHANTING_TABLE);
        } else if (isAnvilItem(stack)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ANVIL);
        } else if (stack.isOf(Items.BREWING_STAND)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BREWING_STAND);
        } else if (stack.isOf(Items.BEACON)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BEACON);
        } else if (isShulkerBoxItem(stack)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_SHULKER_BOX);
        } else if (stack.isOf(Items.TORCH) || stack.isOf(Items.SOUL_TORCH)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_TORCH);
        } else if (stack.isOf(Items.MAP) || stack.isOf(Items.FILLED_MAP)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_MAP);
        } else if (item instanceof BoatItem) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BOAT);
        } else if (stack.isOf(Items.WATER_BUCKET)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_WATER_BUCKET);
        } else if (stack.isOf(Items.LAVA_BUCKET)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_LAVA_BUCKET);
        } else if (stack.isOf(Items.FLINT_AND_STEEL)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_FLINT_AND_STEEL);
        } else if (stack.isOf(Items.SHEARS)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_SHEARS);
        } else if (stack.isOf(Items.MILK_BUCKET)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BUCKET_MILK);
        } else if (stack.isOf(Items.BREAD)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BREAD);
        } else if (stack.isOf(Items.CAKE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_CAKE);
        } else if (stack.isOf(Items.GOLDEN_APPLE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_GOLDEN_APPLE);
        } else if (stack.isOf(Items.WHEAT)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_WHEAT);
        } else if (isFishItem(stack)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_FISH);
        } else if (stack.isOf(Items.COMPASS)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_COMPASS);
        } else if (stack.isOf(Items.CLOCK)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_CLOCK);
        } else if (stack.isOf(Items.SPYGLASS)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_SPYGLASS);
        } else if (stack.isOf(Items.NAME_TAG)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_NAME_TAG);
        } else if (stack.isOf(Items.ENDER_CHEST)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ENDER_CHEST);
        } else if (stack.isOf(Items.ENDER_EYE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_EYE_OF_ENDER);
        } else if (isMusicDiscItem(stack)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_MUSIC_DISC);
        } else if (stack.isOf(Items.GOAT_HORN)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_GOAT_HORN);
        } else if (isPotionItem(stack)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_POTION);
        } else if (stack.isOf(Items.ENCHANTED_BOOK)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ENCHANTED_BOOK);
        } else if (stack.isOf(Items.EXPERIENCE_BOTTLE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_EXPERIENCE_BOTTLE);
        } else if (stack.isOf(Items.TRIAL_KEY)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_TRIAL_KEY);
        } else if (stack.isOf(Items.OMINOUS_TRIAL_KEY)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_OMINOUS_TRIAL_KEY);
        } else if (stack.isOf(Items.BREEZE_ROD)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BREEZE_ROD);
        } else if (stack.isOf(Items.WIND_CHARGE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_WIND_CHARGE);
        } else if (stack.isOf(Items.MACE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_MACE);
        } else if (stack.isOf(Items.ECHO_SHARD)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ECHO_SHARD);
        } else if (stack.isOf(Items.RECOVERY_COMPASS)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_RECOVERY_COMPASS);
        } else if (stack.isOf(Items.SCULK_SENSOR)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_SCULK_SENSOR);
        } else if (stack.isOf(Items.SCULK_CATALYST)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_SCULK_CATALYST);
        } else if (stack.isOf(Items.CALIBRATED_SCULK_SENSOR)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_CALIBRATED_SCULK_SENSOR);
        } else if (stack.isOf(Items.BRUSH)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BRUSH);
        } else if (isPotterySherdItem(stack)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_POTTERY_SHERD);
        } else if (stack.isOf(Items.DECORATED_POT)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_DECORATED_POT);
        } else if (isSmithingTemplateItem(stack)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_SMITHING_TEMPLATE);
        } else if (stack.isOf(Items.SNIFFER_EGG)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_SNIFFER_EGG);
        } else if (stack.isOf(Items.TORCHFLOWER_SEEDS)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_TORCHFLOWER_SEEDS);
        } else if (stack.isOf(Items.PITCHER_POD)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_PITCHER_POD);
        } else if (stack.isOf(Items.HEAVY_CORE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_HEAVY_CORE);
        } else if (stack.isOf(Items.OMINOUS_BOTTLE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_OMINOUS_BOTTLE);
        } else if (stack.isOf(Items.DISC_FRAGMENT_5)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_DISC_FRAGMENT);
        }

        if (stack.hasEnchantments() && !stack.isOf(Items.ENCHANTED_BOOK)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ENCHANTED_ITEM);
        }
    }

    private static void triggerStatEvents(ServerPlayerEntity player) {
        if (player.getStatHandler().getStat(Stats.CUSTOM, Stats.TRADED_WITH_VILLAGER) > 0) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_VILLAGER_TRADE);
        }
    }

    private static void triggerStatusEvents(ServerPlayerEntity player) {
        if (player.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_HERO_OF_THE_VILLAGE);
        }
    }

    private static void processPendingTameChecks(MinecraftServer server) {
        List<PendingTameCheck> retry = new ArrayList<>();
        Iterator<PendingTameCheck> iterator = PENDING_TAME_CHECKS.iterator();
        while (iterator.hasNext()) {
            PendingTameCheck check = iterator.next();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(check.playerUuid());
            if (player == null) {
                iterator.remove();
                continue;
            }

            Entity entity = player.getServerWorld().getEntity(check.entityUuid());
            if (entity instanceof TameableEntity tameable
                    && tameable.isTamed()
                    && player.getUuid().equals(tameable.getOwnerUuid())) {
                triggerAndSync(player, check.eventId());
                iterator.remove();
                continue;
            }

            PendingTameCheck next = check.tickDown();
            iterator.remove();
            if (next.ticksRemaining() > 0) {
                retry.add(next);
            }
        }

        PENDING_TAME_CHECKS.addAll(retry);
    }

    private static void processPendingBlockPlaceChecks(MinecraftServer server) {
        List<PendingBlockPlaceCheck> retry = new ArrayList<>();
        Iterator<PendingBlockPlaceCheck> iterator = PENDING_BLOCK_PLACE_CHECKS.iterator();
        while (iterator.hasNext()) {
            PendingBlockPlaceCheck check = iterator.next();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(check.playerUuid());
            iterator.remove();
            if (player == null) {
                continue;
            }

            PendingBlockPlaceCheck next = check.tickDown();
            if (next.ticksRemaining() > 0) {
                retry.add(next);
                continue;
            }

            BlockState placedState = placedBlockState(player, check);
            if (placedState != null) {
                triggerPlacedBlockEvents(player, placedState);
                FabricFarmersDelightCompat.onBlockPlaced(player, placedState);
                FabricWaystonesCompat.onBlockPlaced(player, placedState);
                incrementCounterAndSync(player, VanillaDiaryEventDefinitions.COUNTER_BLOCKS_PLACED, 1);
            }
        }

        PENDING_BLOCK_PLACE_CHECKS.addAll(retry);
    }

    private static BlockState placedBlockState(ServerPlayerEntity player, PendingBlockPlaceCheck check) {
        ServerWorld world = player.getServerWorld();
        boolean stackChanged = player.isCreative()
                || player.getStackInHand(check.hand()).isEmpty()
                || player.getStackInHand(check.hand()).getCount() < check.originalCount();
        if (!stackChanged) {
            return null;
        }

        BlockState clickedState = changedToBlock(world, check.clickedPos(), check.clickedState());
        if (clickedState != null) {
            return clickedState;
        }

        return changedToBlock(world, check.offsetPos(), check.offsetState());
    }

    private static BlockState changedToBlock(ServerWorld world, BlockPos pos, BlockState originalState) {
        BlockState currentState = world.getBlockState(pos);
        return !currentState.isAir() && !currentState.equals(originalState) ? currentState : null;
    }

    private static void triggerPlacedBlockEvents(ServerPlayerEntity player, BlockState placedState) {
        if (placedState.isOf(Blocks.CRAFTING_TABLE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_CRAFTING_TABLE);
        } else if (placedState.isOf(Blocks.FURNACE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_FURNACE);
        } else if (placedState.isOf(Blocks.CHEST)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_CHEST);
        } else if (placedState.isOf(Blocks.ENCHANTING_TABLE)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ENCHANTING_TABLE);
        } else if (isAnvilBlock(placedState)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ANVIL);
        } else if (placedState.isOf(Blocks.BREWING_STAND)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BREWING_STAND);
        } else if (placedState.isOf(Blocks.BEACON)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BEACON);
        } else if (placedState.getBlock() instanceof ShulkerBoxBlock) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_SHULKER_BOX);
        } else if (placedState.isOf(Blocks.TORCH)
                || placedState.isOf(Blocks.WALL_TORCH)
                || placedState.isOf(Blocks.SOUL_TORCH)
                || placedState.isOf(Blocks.SOUL_WALL_TORCH)) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_TORCH);
        }
    }

    private static void onPlayerKilledEntity(ServerPlayerEntity player, LivingEntity entity) {
        if (player.isSpectator()) {
            return;
        }

        if (entity instanceof HostileEntity) {
            incrementCounterAndSync(player, VanillaDiaryEventDefinitions.COUNTER_MOB_KILLS, 1);
        }

        if (entity instanceof EndermanEntity) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ENDERMAN_KILL);
        } else if (entity instanceof BlazeEntity) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_BLAZE_KILL);
        } else if (entity instanceof WitherSkeletonEntity) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_WITHER_SKELETON_KILL);
        } else if (entity instanceof EnderDragonEntity) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ENDER_DRAGON_KILL);
        } else if (entity instanceof WitherEntity) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_WITHER_KILL);
        } else if (entity instanceof ElderGuardianEntity) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_ELDER_GUARDIAN_KILL);
        } else if (entity instanceof WardenEntity) {
            triggerAndSync(player, VanillaDiaryEventDefinitions.FIRST_WARDEN_KILL);
        }
    }

    private static void handlePlayerKilledPlayer(ServerPlayerEntity killer, ServerPlayerEntity victim) {
        if (killer.isSpectator() || victim.isSpectator() || killer.getUuid().equals(victim.getUuid())) {
            return;
        }

        triggerAndSync(killer, VanillaDiaryEventDefinitions.FIRST_PVP_KILL);
        incrementCounterAndSync(killer, VanillaDiaryEventDefinitions.COUNTER_PVP_KILLS, 1);
    }

    private static void triggerDeathCause(ServerPlayerEntity player, DamageSource damageSource) {
        String eventId = deathCauseEvent(damageSource);
        if (eventId != null) {
            triggerAndSync(player, eventId);
        }
    }

    private static String deathCauseEvent(DamageSource damageSource) {
        if (damageSource.isOf(DamageTypes.LAVA)) {
            return VanillaDiaryEventDefinitions.DEATH_BY_LAVA;
        }

        if (damageSource.isOf(DamageTypes.FALL)) {
            return VanillaDiaryEventDefinitions.DEATH_BY_FALL;
        }

        if (damageSource.isOf(DamageTypes.DROWN)) {
            return VanillaDiaryEventDefinitions.DEATH_BY_DROWNING;
        }

        if (causedBy(damageSource, CreeperEntity.class)) {
            return VanillaDiaryEventDefinitions.DEATH_BY_CREEPER;
        }

        if (causedBy(damageSource, SkeletonEntity.class)) {
            return VanillaDiaryEventDefinitions.DEATH_BY_SKELETON;
        }

        if (causedBy(damageSource, EndermanEntity.class)) {
            return VanillaDiaryEventDefinitions.DEATH_BY_ENDERMAN;
        }

        return null;
    }

    private static boolean causedBy(DamageSource damageSource, Class<? extends Entity> entityType) {
        return entityType.isInstance(damageSource.getAttacker()) || entityType.isInstance(damageSource.getSource());
    }

    private static String potentialTameEvent(Entity entity, ItemStack stack) {
        if (entity instanceof WolfEntity && stack.isOf(Items.BONE)) {
            return VanillaDiaryEventDefinitions.FIRST_TAMED_WOLF;
        }

        if (entity instanceof CatEntity && (stack.isOf(Items.COD) || stack.isOf(Items.SALMON))) {
            return VanillaDiaryEventDefinitions.FIRST_TAMED_CAT;
        }

        if (entity instanceof ParrotEntity && isParrotFood(stack)) {
            return VanillaDiaryEventDefinitions.FIRST_TAMED_PARROT;
        }

        return null;
    }

    private static boolean hasTotemInHand(LivingEntity entity) {
        return entity.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING)
                || entity.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
    }

    private static boolean isAnvilItem(ItemStack stack) {
        return stack.isOf(Items.ANVIL) || stack.isOf(Items.CHIPPED_ANVIL) || stack.isOf(Items.DAMAGED_ANVIL);
    }

    private static boolean isAnvilBlock(BlockState state) {
        return state.isOf(Blocks.ANVIL) || state.isOf(Blocks.CHIPPED_ANVIL) || state.isOf(Blocks.DAMAGED_ANVIL);
    }

    private static boolean isShulkerBoxItem(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock;
    }

    private static boolean isFishItem(ItemStack stack) {
        return stack.isOf(Items.COD)
                || stack.isOf(Items.SALMON)
                || stack.isOf(Items.PUFFERFISH)
                || stack.isOf(Items.TROPICAL_FISH);
    }

    private static boolean isPotionItem(ItemStack stack) {
        return stack.isOf(Items.POTION)
                || stack.isOf(Items.SPLASH_POTION)
                || stack.isOf(Items.LINGERING_POTION);
    }

    private static boolean isMusicDiscItem(ItemStack stack) {
        return Registries.ITEM.getId(stack.getItem()).getPath().startsWith("music_disc_");
    }

    private static boolean isPotterySherdItem(ItemStack stack) {
        return Registries.ITEM.getId(stack.getItem()).getPath().endsWith("_pottery_sherd");
    }

    private static boolean isSmithingTemplateItem(ItemStack stack) {
        return Registries.ITEM.getId(stack.getItem()).getPath().endsWith("_smithing_template");
    }

    private static boolean isParrotFood(ItemStack stack) {
        return stack.isOf(Items.WHEAT_SEEDS)
                || stack.isOf(Items.MELON_SEEDS)
                || stack.isOf(Items.PUMPKIN_SEEDS)
                || stack.isOf(Items.BEETROOT_SEEDS);
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

    static Optional<DiaryEntry> triggerAndSync(ServerPlayerEntity player, String eventId) {
        Optional<DiaryEntry> entry = AutomaticDiaryEvents.trigger(player, eventId);
        entry.ifPresent(created -> {
            DearDiaryNetworking.sendDiarySnapshot(player);
            DearDiaryNetworking.sendAutomaticEntryNotice(player, created);
        });
        return entry;
    }

    private record PendingTameCheck(UUID playerUuid, UUID entityUuid, String eventId, int ticksRemaining) {
        private PendingTameCheck tickDown() {
            return new PendingTameCheck(playerUuid, entityUuid, eventId, ticksRemaining - 1);
        }
    }

    private record PendingBlockPlaceCheck(
            UUID playerUuid,
            BlockPos clickedPos,
            BlockState clickedState,
            BlockPos offsetPos,
            BlockState offsetState,
            Hand hand,
            int originalCount,
            int ticksRemaining
    ) {
        private PendingBlockPlaceCheck tickDown() {
            return new PendingBlockPlaceCheck(
                    playerUuid,
                    clickedPos,
                    clickedState,
                    offsetPos,
                    offsetState,
                    hand,
                    originalCount,
                    ticksRemaining - 1
            );
        }
    }
}
