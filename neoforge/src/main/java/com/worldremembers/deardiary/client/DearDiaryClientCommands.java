package com.worldremembers.deardiary.client;

import com.mojang.brigadier.CommandDispatcher;
import com.worldremembers.deardiary.client.config.DearDiaryClientConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

public final class DearDiaryClientCommands {
    private DearDiaryClientCommands() {
    }

    public static void register(RegisterClientCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("deardiaryclient")
                .then(Commands.literal("open")
                        .executes(context -> {
                            DearDiaryClientNetworking.openDiaryScreen(false);
                            return 1;
                        }))
                .then(Commands.literal("new")
                        .executes(context -> {
                            DearDiaryClientNetworking.openDiaryScreen(true);
                            return 1;
                        }))
                .then(Commands.literal("reset_button")
                        .executes(context -> {
                            DearDiaryClientConfig.resetInventoryButtonPosition();
                            context.getSource().sendSuccess(
                                    () -> Component.translatable("commands.dear_diary.client.reset_button.success"),
                                    false
                            );
                            return 1;
                        }))
                .then(Commands.literal("test_notification")
                        .executes(context -> {
                            DiaryNotificationManager.showTestNotification();
                            context.getSource().sendSuccess(
                                    () -> Component.translatable("commands.dear_diary.client.test_notification.success"),
                                    false
                            );
                            return 1;
                        })));
    }
}
