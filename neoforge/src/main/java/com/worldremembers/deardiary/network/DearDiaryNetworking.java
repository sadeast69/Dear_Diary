package com.worldremembers.deardiary.network;

import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.DearDiaryServices;
import com.worldremembers.deardiary.api.DearDiaryApi;
import com.worldremembers.deardiary.data.DiaryEntry;
import com.worldremembers.deardiary.network.payload.CreateManualEntryPayload;
import com.worldremembers.deardiary.network.payload.DeleteEntryPayload;
import com.worldremembers.deardiary.network.payload.DiarySnapshotPayload;
import com.worldremembers.deardiary.network.payload.EditEntryPayload;
import com.worldremembers.deardiary.network.payload.NewAutomaticEntryPayload;
import com.worldremembers.deardiary.network.payload.OpenDiaryScreenPayload;
import com.worldremembers.deardiary.network.payload.RequestDiaryPayload;
import com.worldremembers.deardiary.network.payload.SetFavoritePayload;
import com.worldremembers.deardiary.network.payload.ShareEntryPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class DearDiaryNetworking {
    private static final String NETWORK_VERSION = "1";

    private DearDiaryNetworking() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(DearDiaryNetworking::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(DearDiaryMod.MOD_ID).versioned(NETWORK_VERSION);

        registrar.playToServer(RequestDiaryPayload.TYPE, RequestDiaryPayload.STREAM_CODEC, DearDiaryNetworking::handleRequestDiary);
        registrar.playToServer(CreateManualEntryPayload.TYPE, CreateManualEntryPayload.STREAM_CODEC, DearDiaryNetworking::handleCreateManualEntry);
        registrar.playToServer(EditEntryPayload.TYPE, EditEntryPayload.STREAM_CODEC, DearDiaryNetworking::handleEditEntry);
        registrar.playToServer(DeleteEntryPayload.TYPE, DeleteEntryPayload.STREAM_CODEC, DearDiaryNetworking::handleDeleteEntry);
        registrar.playToServer(SetFavoritePayload.TYPE, SetFavoritePayload.STREAM_CODEC, DearDiaryNetworking::handleSetFavorite);
        registrar.playToServer(ShareEntryPayload.TYPE, ShareEntryPayload.STREAM_CODEC, DearDiaryNetworking::handleShareEntry);

        registrar.playToClient(DiarySnapshotPayload.TYPE, DiarySnapshotPayload.STREAM_CODEC, DearDiaryNetworking::handleDiarySnapshot);
        registrar.playToClient(NewAutomaticEntryPayload.TYPE, NewAutomaticEntryPayload.STREAM_CODEC, DearDiaryNetworking::handleNewAutomaticEntry);
        registrar.playToClient(OpenDiaryScreenPayload.TYPE, OpenDiaryScreenPayload.STREAM_CODEC, DearDiaryNetworking::handleOpenDiaryScreen);
    }

    private static void handleRequestDiary(RequestDiaryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> withServerPlayer(context, DearDiaryNetworking::sendDiarySnapshot));
    }

    private static void handleCreateManualEntry(CreateManualEntryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> withServerPlayer(context, player -> {
            try {
                if (payload.chapter()) {
                    DearDiaryApi.createChapterEntry(player, payload.title(), payload.text(), payload.includeLocation());
                } else {
                    DearDiaryApi.createManualEntry(player, payload.title(), payload.text(), payload.includeLocation());
                }
            } catch (IllegalArgumentException exception) {
                player.sendSystemMessage(Component.translatable(createEntryErrorKey(payload)));
            }
            sendDiarySnapshot(player);
        }));
    }

    private static String createEntryErrorKey(CreateManualEntryPayload payload) {
        if (payload.chapter()) {
            String title = payload.title() == null ? "" : payload.title().strip();
            if (title.length() > DearDiaryServices.config().maxManualTitleLength()) {
                return "commands.dear_diary.chapter.title_too_long";
            }
            return "commands.dear_diary.chapter.empty_title";
        }
        return "message.dear_diary.error.empty_manual_text";
    }

    private static void handleEditEntry(EditEntryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> withServerPlayer(context, player -> {
            if (!DearDiaryApi.editEntryText(player, payload.entryId(), payload.title(), payload.text())) {
                player.sendSystemMessage(Component.translatable("message.dear_diary.error.edit_failed"));
            }
            sendDiarySnapshot(player);
        }));
    }

    private static void handleDeleteEntry(DeleteEntryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> withServerPlayer(context, player -> {
            if (!DearDiaryApi.deleteEntry(player, payload.entryId())) {
                player.sendSystemMessage(Component.translatable("message.dear_diary.error.not_found"));
            }
            sendDiarySnapshot(player);
        }));
    }

    private static void handleSetFavorite(SetFavoritePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> withServerPlayer(context, player -> {
            if (!DearDiaryApi.setFavorite(player, payload.entryId(), payload.favorite())) {
                player.sendSystemMessage(Component.translatable("message.dear_diary.error.not_found"));
            }
            sendDiarySnapshot(player);
        }));
    }

    private static void handleShareEntry(ShareEntryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> withServerPlayer(context, player -> {
            if (!DearDiaryApi.shareEntryToChat(player, payload.entryId())) {
                player.sendSystemMessage(Component.translatable("message.dear_diary.error.share_failed"));
            }
            sendDiarySnapshot(player);
        }));
    }

    private static void handleDiarySnapshot(DiarySnapshotPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                ClientboundHandlers.handleDiarySnapshot(payload);
            }
        });
    }

    private static void handleNewAutomaticEntry(NewAutomaticEntryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                ClientboundHandlers.handleNewAutomaticEntry(payload);
            }
        });
    }

    private static void handleOpenDiaryScreen(OpenDiaryScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                ClientboundHandlers.handleOpenDiaryScreen(payload);
            }
        });
    }

    private static void withServerPlayer(IPayloadContext context, ServerPlayerConsumer consumer) {
        Player player = context.player();
        if (player instanceof ServerPlayer serverPlayer) {
            consumer.accept(serverPlayer);
        }
    }

    public static void sendDiarySnapshot(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, DiarySnapshotPayload.fromDiary(DearDiaryApi.getDiary(player)));
    }

    public static void sendAutomaticEntryNotice(ServerPlayer player, DiaryEntry entry) {
        PacketDistributor.sendToPlayer(player, NewAutomaticEntryPayload.fromEntry(entry));
    }

    public static boolean openDiaryScreen(ServerPlayer player, boolean newEntry) {
        try {
            PacketDistributor.sendToPlayer(player, new OpenDiaryScreenPayload(newEntry));
            sendDiarySnapshot(player);
            return true;
        } catch (RuntimeException exception) {
            DearDiaryMod.LOGGER.warn("Failed to send Dear Diary open screen payload to {}", player.getGameProfile().getName(), exception);
            return false;
        }
    }

    @FunctionalInterface
    private interface ServerPlayerConsumer {
        void accept(ServerPlayer player);
    }

    private static final class ClientboundHandlers {
        private ClientboundHandlers() {
        }

        private static void handleDiarySnapshot(DiarySnapshotPayload payload) {
            com.worldremembers.deardiary.client.DearDiaryClientNetworking.handleDiarySnapshot(payload);
        }

        private static void handleNewAutomaticEntry(NewAutomaticEntryPayload payload) {
            com.worldremembers.deardiary.client.DearDiaryClientNetworking.handleNewAutomaticEntry(payload);
        }

        private static void handleOpenDiaryScreen(OpenDiaryScreenPayload payload) {
            com.worldremembers.deardiary.client.DearDiaryClientNetworking.handleOpenDiaryScreen(payload);
        }
    }
}
