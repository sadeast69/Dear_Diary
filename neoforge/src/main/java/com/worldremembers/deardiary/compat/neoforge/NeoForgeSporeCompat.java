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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class NeoForgeSporeCompat {
    public static final String MOD_ID = "spore";

    private static final int ITEM_SCAN_INTERVAL_TICKS = 40;
    private static final int STRUCTURE_SCAN_INTERVAL_TICKS = 100;

    private static final String FIRST_MYCELIUM_INFECTION = "spore:first_mycelium_infection";
    private static final String FIRST_INFECTED_HUMAN_DEFEATED = "spore:first_infected_human_defeated";
    private static final String FIRST_EVOLVED_INFECTED_DEFEATED = "spore:first_evolved_infected_defeated";
    private static final String FIRST_PROTO_HIVEMIND_DEFEATED = "spore:first_proto_hivemind_defeated";
    private static final String FIRST_LABORATORY_VISIT = "spore:first_laboratory_visit";
    private static final String FIRST_SCANNER_ACQUIRED = "spore:first_scanner_acquired";
    private static final String INFECTED_KILLS_COUNTER = "spore_infected_kills";

    private static final ResourceLocation MYCELIUM_EFFECT_ID = sporeId("mycelium_ef");
    private static final ResourceLocation INFECTED_HUMAN_ID = sporeId("inf_human");
    private static final ResourceLocation PROTO_HIVEMIND_ID = sporeId("proto");
    private static final ResourceLocation SCANNER_ITEM_ID = sporeId("scanner");
    private static final ResourceKey<Structure> LAB_STRUCTURE_KEY =
            ResourceKey.create(Registries.STRUCTURE, sporeId("lab"));

    private static final Set<ResourceLocation> EVOLVED_INFECTED_IDS = Set.of(
            sporeId("scamper"),
            sporeId("leaper"),
            sporeId("slasher"),
            sporeId("spitter"),
            sporeId("stalker"),
            sporeId("howler")
    );

    private static final Set<ResourceLocation> INFECTED_COUNTER_ENTITY_IDS = Set.of(
            sporeId("inf_human"),
            sporeId("inf_husk"),
            sporeId("inf_player"),
            sporeId("inf_villager"),
            sporeId("inf_diseased_villager"),
            sporeId("inf_wanderer"),
            sporeId("inf_witch"),
            sporeId("inf_pillager"),
            sporeId("inf_vindicator"),
            sporeId("inf_evoker"),
            sporeId("inf_drowned"),
            sporeId("inf_hazmat"),
            sporeId("scamper"),
            sporeId("leaper"),
            sporeId("slasher"),
            sporeId("spitter"),
            sporeId("stalker"),
            sporeId("howler")
    );

    private static boolean registered;
    private static int itemScanTicker;
    private static int structureScanTicker;

    private NeoForgeSporeCompat() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NeoForgeSporeCompat::onMobEffectAdded);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NeoForgeSporeCompat::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(NeoForgeSporeCompat::onServerTick);
        DearDiaryMod.LOGGER.info("Fungal Infection: Spore compatibility hooks registered");
    }

    private static void onMobEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) {
            return;
        }
        if (!(player.level() instanceof ServerLevel)) {
            return;
        }
        MobEffectInstance effectInstance = event.getEffectInstance();
        if (effectInstance == null) {
            return;
        }
        ResourceLocation effectId = BuiltInRegistries.MOB_EFFECT.getKey(effectInstance.getEffect().value());
        if (MYCELIUM_EFFECT_ID.equals(effectId)) {
            triggerAndSync(player, FIRST_MYCELIUM_INFECTION);
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
        ResourceLocation victimId = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType());
        if (INFECTED_HUMAN_ID.equals(victimId)) {
            triggerAndSync(player, FIRST_INFECTED_HUMAN_DEFEATED);
        }
        if (EVOLVED_INFECTED_IDS.contains(victimId)) {
            triggerAndSync(player, FIRST_EVOLVED_INFECTED_DEFEATED);
        }
        if (PROTO_HIVEMIND_ID.equals(victimId)) {
            triggerAndSync(player, FIRST_PROTO_HIVEMIND_DEFEATED);
        }
        if (INFECTED_COUNTER_ENTITY_IDS.contains(victimId)) {
            incrementCounterAndSync(player, INFECTED_KILLS_COUNTER, 1);
        }
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        itemScanTicker++;
        structureScanTicker++;
        boolean scanItems = itemScanTicker >= ITEM_SCAN_INTERVAL_TICKS;
        boolean scanStructures = structureScanTicker >= STRUCTURE_SCAN_INTERVAL_TICKS;
        if (!scanItems && !scanStructures) {
            return;
        }
        if (scanItems) {
            itemScanTicker = 0;
        }
        if (scanStructures) {
            structureScanTicker = 0;
        }
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (player.isSpectator()) {
                continue;
            }
            AutomaticEventState state = DearDiaryApi.getDiary(player).automaticEventState();
            if (scanItems) {
                scanScannerItem(player, state);
            }
            if (scanStructures) {
                scanLaboratoryVisit(player, state);
            }
        }
    }

    private static void scanScannerItem(ServerPlayer player, AutomaticEventState state) {
        if (!needsAutomaticMemory(state, FIRST_SCANNER_ACQUIRED)) {
            return;
        }
        for (ItemStack stack : player.getInventory().items) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (SCANNER_ITEM_ID.equals(itemId)) {
                triggerAndSync(player, FIRST_SCANNER_ACQUIRED);
                return;
            }
        }
    }

    private static void scanLaboratoryVisit(ServerPlayer player, AutomaticEventState state) {
        if (!needsAutomaticMemory(state, FIRST_LABORATORY_VISIT)) {
            return;
        }
        if (!(player.level() instanceof ServerLevel world)) {
            return;
        }
        Structure structure = world.registryAccess()
                .registryOrThrow(Registries.STRUCTURE)
                .get(LAB_STRUCTURE_KEY);
        if (structure == null) {
            return;
        }
        StructureStart structureStart = world.structureManager().getStructureWithPieceAt(player.blockPosition(), structure);
        if (structureStart != null && structureStart.isValid()) {
            triggerAndSync(player, FIRST_LABORATORY_VISIT);
        }
    }

    private static boolean needsAutomaticMemory(AutomaticEventState state, String eventId) {
        if (state.isTriggered(eventId)) {
            return false;
        }
        return DearDiaryEventRegistry.get(eventId)
                .map(NeoForgeSporeCompat::isAllowed)
                .orElse(false);
    }

    private static boolean isAllowed(AutomaticEventDefinition definition) {
        return DearDiaryServices.config().isAutomaticEventAllowed(
                definition.eventId(),
                definition.category(),
                definition.importance()
        );
    }

    private static void triggerAndSync(ServerPlayer player, String eventId) {
        Optional<DiaryEntry> entry = AutomaticDiaryEvents.trigger(player, eventId);
        entry.ifPresent(created -> {
            DearDiaryNetworking.sendDiarySnapshot(player);
            DearDiaryNetworking.sendAutomaticEntryNotice(player, created);
        });
    }

    private static void incrementCounterAndSync(ServerPlayer player, String counterId, int amount) {
        List<DiaryEntry> entries = AutomaticDiaryEvents.incrementCounterAndTriggerMilestones(player, counterId, amount);
        if (!entries.isEmpty()) {
            DearDiaryNetworking.sendDiarySnapshot(player);
            for (DiaryEntry entry : entries) {
                DearDiaryNetworking.sendAutomaticEntryNotice(player, entry);
            }
        }
    }

    private static ResourceLocation sporeId(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
