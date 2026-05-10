package com.worldremembers.deardiary.client;

import net.fabricmc.api.ClientModInitializer;

public final class DearDiaryClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DiaryNotificationManager.register();
        DearDiaryClientNetworking.register();
        DearDiaryKeybindings.register();
        DearDiaryClientCommands.register();
        DearDiaryInventoryButton.register();
    }
}
