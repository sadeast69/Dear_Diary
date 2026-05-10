package com.worldremembers.deardiary.compat.neoforge;

import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.DearDiaryServices;
import com.worldremembers.deardiary.api.DearDiaryApi;
import com.worldremembers.deardiary.data.AutomaticEventState;
import com.worldremembers.deardiary.event.AutomaticDiaryEvents;
import com.worldremembers.deardiary.event.AutomaticEventDefinition;
import com.worldremembers.deardiary.event.DearDiaryEventRegistry;
import com.worldremembers.deardiary.network.DearDiaryNetworking;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * NeoForge-only runtime hooks for Iron's Spells 'n Spellbooks compatibility.
 */
public final class NeoForgeIronsSpellsCompat {
    static final String MOD_ID = "irons_spellbooks";
    private static final int ITEM_SCAN_INTERVAL_TICKS = 40;

    private static final ResourceLocation SPELL_BOOK_EQUIP_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "irons_spellbooks/spell_book_equip");
    private static final ResourceLocation MAKE_INSCRIPTION_TABLE_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "irons_spellbooks/make_inscription_table");
    private static final ResourceLocation MAKE_SCROLL_FORGE_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "irons_spellbooks/make_scroll_forge");
    private static final ResourceLocation MAKE_ARCANE_ANVIL_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "irons_spellbooks/make_arcane_anvil");
    private static final ResourceLocation ENTER_CATACOMBS_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "irons_spellbooks/enter_catacombs");
    private static final ResourceLocation INK_LEGENDARY_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "irons_spellbooks/ink_legendary");
    private static final ResourceLocation DEAD_KING_ENTITY =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "dead_king");
    private static final ResourceLocation FIRE_BOSS_ENTITY =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "fire_boss");
    private static final ResourceLocation SCROLL_ITEM =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "scroll");

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

    private static final Map<ResourceLocation, String> ADVANCEMENT_MEMORIES = Map.of(
            SPELL_BOOK_EQUIP_ADVANCEMENT, FIRST_SPELLBOOK_EQUIPPED,
            MAKE_INSCRIPTION_TABLE_ADVANCEMENT, FIRST_INSCRIPTION_TABLE,
            MAKE_SCROLL_FORGE_ADVANCEMENT, FIRST_SCROLL_FORGE,
            MAKE_ARCANE_ANVIL_ADVANCEMENT, FIRST_ARCANE_ANVIL,
            ENTER_CATACOMBS_ADVANCEMENT, FIRST_CATACOMBS_VISIT,
            INK_LEGENDARY_ADVANCEMENT, FIRST_LEGENDARY_INK
    );

    private static final Map<ResourceLocation, String> BOSS_DEFEAT_MEMORIES = Map.of(
            DEAD_KING_ENTITY, FIRST_DEAD_KING_DEFEATED,
            FIRE_BOSS_ENTITY, FIRST_FIRE_BOSS_DEFEATED
    );

    private static final Map<ResourceLocation, String> ITEM_ACQUISITION_MEMORIES = Map.of(
            SCROLL_ITEM, FIRST_SCROLL_ACQUIRED
    );

    private static boolean registered;
    private static int itemScanTicker;

    private NeoForgeIronsSpellsCompat() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NeoForgeIronsSpellsCompat::onAdvancementEarned);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NeoForgeIronsSpellsCompat::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(NeoForgeIronsSpellsCompat::onServerTick);
        DearDiaryMod.LOGGER.info("Dear Diary Iron's Spells gameplay callbacks registered for NeoForge");
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
        itemScanTicker++;
        if (itemScanTicker < ITEM_SCAN_INTERVAL_TICKS) {
            return;
        }

        itemScanTicker = 0;
        MinecraftServer server = event.getServer();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            scanItemAcquisitions(player);
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
                    && needsAutomaticMemory(state, eventId)) {
                triggerAndSync(player, eventId);
            }
        }
    }

    private static boolean needsAnyItemAcquisitionMemory(AutomaticEventState state) {
        for (String eventId : ITEM_ACQUISITION_MEMORIES.values()) {
            if (needsAutomaticMemory(state, eventId)) {
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
                .map(NeoForgeIronsSpellsCompat::isAllowed)
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
        AutomaticDiaryEvents.trigger(player, eventId).ifPresent(created -> {
            DearDiaryNetworking.sendDiarySnapshot(player);
            DearDiaryNetworking.sendAutomaticEntryNotice(player, created);
        });
    }
}
