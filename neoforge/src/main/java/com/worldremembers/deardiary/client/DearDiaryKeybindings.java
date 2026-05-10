package com.worldremembers.deardiary.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public final class DearDiaryKeybindings {
    private static final KeyMapping OPEN_NEW_ENTRY = new KeyMapping(
            "key.dear_diary.open_new_entry",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            "key.categories.dear_diary"
    );

    private DearDiaryKeybindings() {
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_NEW_ENTRY);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        while (OPEN_NEW_ENTRY.consumeClick()) {
            if (client.player != null && client.level != null && client.screen == null) {
                DearDiaryClientNetworking.openDiaryScreen(true);
            }
        }
    }
}
