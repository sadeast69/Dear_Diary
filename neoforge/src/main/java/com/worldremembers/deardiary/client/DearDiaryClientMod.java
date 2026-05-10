package com.worldremembers.deardiary.client;

import com.worldremembers.deardiary.client.config.DearDiaryClientConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class DearDiaryClientMod {
    private static boolean registered;

    private DearDiaryClientMod() {
    }

    public static void register(IEventBus modEventBus) {
        if (registered) {
            return;
        }

        registered = true;
        DearDiaryClientConfig.load();
        modEventBus.addListener(DearDiaryKeybindings::registerKeyMappings);
        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> DearDiaryClientCommands.register(event));
        NeoForge.EVENT_BUS.addListener(DearDiaryKeybindings::onClientTick);
        NeoForge.EVENT_BUS.addListener(DearDiaryInventoryButton::onScreenInit);
        NeoForge.EVENT_BUS.addListener(DearDiaryInventoryButton::onMouseDragged);
        NeoForge.EVENT_BUS.addListener(DearDiaryInventoryButton::onMouseReleased);
        NeoForge.EVENT_BUS.addListener(DiaryNotificationManager::onClientTick);
        NeoForge.EVENT_BUS.addListener(DiaryNotificationManager::onRenderGui);
    }
}
