package com.worldremembers.deardiary.client;

import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.client.config.DearDiaryClientConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public final class DearDiaryClientCommands {
    private DearDiaryClientCommands() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommandManager.literal("deardiaryclient")
                        .then(ClientCommandManager.literal("open")
                                .executes(context -> {
                                    DearDiaryClientActions.openDiaryWithFeedback();
                                    return 1;
                                }))
                        .then(ClientCommandManager.literal("new")
                                .executes(context -> {
                                    DearDiaryClientActions.openNewEntryWithFeedback();
                                    return 1;
                                }))
                        .then(ClientCommandManager.literal("reset_button")
                                .executes(context -> {
                                    DearDiaryClientConfig.resetInventoryButtonPosition();
                                    MinecraftClient client = MinecraftClient.getInstance();
                                    Text feedback = Text.translatable("commands.dear_diary.client.reset_button.success");
                                    if (client.player != null) {
                                        client.player.sendMessage(feedback, false);
                                    } else {
                                        DearDiaryMod.LOGGER.info(feedback.getString());
                                    }
                                    return 1;
                                }))
                        .then(ClientCommandManager.literal("test_notification")
                                .executes(context -> {
                                    DiaryNotificationManager.showTestNotification();
                                    MinecraftClient client = MinecraftClient.getInstance();
                                    Text feedback = Text.translatable("commands.dear_diary.client.test_notification.success");
                                    if (client.player != null) {
                                        client.player.sendMessage(feedback, false);
                                    } else {
                                        DearDiaryMod.LOGGER.info(feedback.getString());
                                    }
                                    return 1;
                                }))
        ));
    }
}
