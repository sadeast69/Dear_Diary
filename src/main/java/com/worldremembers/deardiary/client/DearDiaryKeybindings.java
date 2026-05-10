package com.worldremembers.deardiary.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public final class DearDiaryKeybindings {
    private static KeyBinding openNewEntry;

    private DearDiaryKeybindings() {
    }

    public static void register() {
        openNewEntry = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dear_diary.open_new_entry",
                InputUtil.Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(),
                "key.categories.dear_diary"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            DearDiaryClientActions.tick(client);
            while (openNewEntry.wasPressed()) {
                if (client.player != null && client.world != null && client.currentScreen == null) {
                    DearDiaryClientActions.openNewEntry();
                }
            }
        });
    }
}
