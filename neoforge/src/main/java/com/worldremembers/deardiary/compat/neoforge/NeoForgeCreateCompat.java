package com.worldremembers.deardiary.compat.neoforge;

import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.DearDiaryServices;
import com.worldremembers.deardiary.api.DearDiaryApi;
import com.worldremembers.deardiary.data.AutomaticEventState;
import com.worldremembers.deardiary.event.AutomaticDiaryEvents;
import com.worldremembers.deardiary.event.AutomaticEventDefinition;
import com.worldremembers.deardiary.event.DearDiaryEventRegistry;
import com.worldremembers.deardiary.network.DearDiaryNetworking;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;

/**
 * NeoForge-only runtime hooks for Create compatibility.
 */
public final class NeoForgeCreateCompat {
    static final String MOD_ID = "create";

    private static final ResourceLocation WATER_WHEEL_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "water_wheel");
    private static final ResourceLocation MECHANICAL_PRESS_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "mechanical_press");
    private static final ResourceLocation MECHANICAL_MIXER_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "mechanical_mixer");
    private static final ResourceLocation DEPLOYER_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "deployer");
    private static final ResourceLocation MECHANICAL_CRAFTER_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "mechanical_crafter");
    private static final ResourceLocation STEAM_ENGINE_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "steam_engine");
    private static final ResourceLocation TRAIN_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "train");
    private static final ResourceLocation FACTORY_GAUGE_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "factory_gauge");
    private static final ResourceLocation CRUSHING_WHEEL_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "crushing_wheel");
    private static final ResourceLocation FAN_PROCESSING_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "fan_processing");
    private static final ResourceLocation SPOUT_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "spout");
    private static final ResourceLocation HOSE_PULLEY_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "hose_pulley");
    private static final ResourceLocation PRECISION_MECHANISM_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "precision_mechanism");
    private static final ResourceLocation MECHANICAL_ARM_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "mechanical_arm");
    private static final ResourceLocation CONDUCTOR_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "conductor");
    private static final ResourceLocation SPEED_CONTROLLER_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "speed_controller");
    private static final ResourceLocation LINKED_CONTROLLER_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "linked_controller");
    private static final ResourceLocation DISPLAY_LINK_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "display_link");
    private static final ResourceLocation PULLEY_MAXED_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "pulley_maxed");

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

    private static final Map<ResourceLocation, String> ADVANCEMENT_MEMORIES = Map.ofEntries(
            Map.entry(WATER_WHEEL_ADVANCEMENT, FIRST_WATER_WHEEL_POWERED),
            Map.entry(MECHANICAL_PRESS_ADVANCEMENT, FIRST_MECHANICAL_PRESS),
            Map.entry(MECHANICAL_MIXER_ADVANCEMENT, FIRST_MECHANICAL_MIXER),
            Map.entry(DEPLOYER_ADVANCEMENT, FIRST_DEPLOYER_POWERED),
            Map.entry(MECHANICAL_CRAFTER_ADVANCEMENT, FIRST_MECHANICAL_CRAFTER_POWERED),
            Map.entry(STEAM_ENGINE_ADVANCEMENT, FIRST_STEAM_ENGINE),
            Map.entry(TRAIN_ADVANCEMENT, FIRST_TRAIN_ASSEMBLED),
            Map.entry(FACTORY_GAUGE_ADVANCEMENT, FIRST_FACTORY_GAUGE_REQUEST),
            Map.entry(CRUSHING_WHEEL_ADVANCEMENT, FIRST_CRUSHING_WHEELS_POWERED),
            Map.entry(FAN_PROCESSING_ADVANCEMENT, FIRST_FAN_PROCESSING),
            Map.entry(SPOUT_ADVANCEMENT, FIRST_SPOUT_FILLING),
            Map.entry(HOSE_PULLEY_ADVANCEMENT, FIRST_HOSE_PULLEY_OPERATION),
            Map.entry(PRECISION_MECHANISM_ADVANCEMENT, FIRST_PRECISION_MECHANISM),
            Map.entry(MECHANICAL_ARM_ADVANCEMENT, FIRST_MECHANICAL_ARM_DELIVERY),
            Map.entry(CONDUCTOR_ADVANCEMENT, FIRST_TRAIN_SCHEDULE_ASSIGNED),
            Map.entry(SPEED_CONTROLLER_ADVANCEMENT, FIRST_ROTATION_SPEED_CONTROLLER_TUNED),
            Map.entry(LINKED_CONTROLLER_ADVANCEMENT, FIRST_LINKED_CONTROLLER_SIGNAL),
            Map.entry(DISPLAY_LINK_ADVANCEMENT, FIRST_DISPLAY_LINK_REPORTING),
            Map.entry(PULLEY_MAXED_ADVANCEMENT, FIRST_ROPE_PULLEY_DEEP_EXTENSION)
    );

    private static boolean registered;

    private NeoForgeCreateCompat() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NeoForgeCreateCompat::onAdvancementEarned);
        DearDiaryMod.LOGGER.info("Dear Diary Create gameplay callbacks registered for NeoForge");
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

    private static boolean needsAutomaticMemory(AutomaticEventState state, String eventId) {
        if (state.isTriggered(eventId)) {
            return false;
        }

        return DearDiaryEventRegistry.get(eventId)
                .map(NeoForgeCreateCompat::isAllowed)
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
