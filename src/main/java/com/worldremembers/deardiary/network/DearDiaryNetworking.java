package com.worldremembers.deardiary.network;

import com.worldremembers.deardiary.api.DearDiaryApi;
import com.worldremembers.deardiary.DearDiaryServices;
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
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class DearDiaryNetworking {
    private static boolean registered;

    private DearDiaryNetworking() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        registerPayloadTypes();
        registerServerReceivers();
    }

    private static void registerPayloadTypes() {
        PayloadTypeRegistry.playC2S().register(RequestDiaryPayload.ID, RequestDiaryPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CreateManualEntryPayload.ID, CreateManualEntryPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(EditEntryPayload.ID, EditEntryPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DeleteEntryPayload.ID, DeleteEntryPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SetFavoritePayload.ID, SetFavoritePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ShareEntryPayload.ID, ShareEntryPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(DiarySnapshotPayload.ID, DiarySnapshotPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NewAutomaticEntryPayload.ID, NewAutomaticEntryPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenDiaryScreenPayload.ID, OpenDiaryScreenPayload.CODEC);
    }

    private static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(RequestDiaryPayload.ID, (payload, context) ->
                sendDiarySnapshot(context.player()));

        ServerPlayNetworking.registerGlobalReceiver(CreateManualEntryPayload.ID, (payload, context) -> {
            try {
                if (payload.chapter()) {
                    DearDiaryApi.createChapterEntry(context.player(), payload.title(), payload.text(), payload.includeLocation());
                } else {
                    DearDiaryApi.createManualEntry(context.player(), payload.title(), payload.text(), payload.includeLocation());
                }
            } catch (IllegalArgumentException exception) {
                context.player().sendMessage(Text.translatable(createEntryErrorKey(payload)), false);
            }
            sendDiarySnapshot(context.player());
        });

        ServerPlayNetworking.registerGlobalReceiver(EditEntryPayload.ID, (payload, context) -> {
            if (!DearDiaryApi.editEntryText(context.player(), payload.entryId(), payload.title(), payload.text())) {
                context.player().sendMessage(Text.translatable("message.dear_diary.error.edit_failed"), false);
            }
            sendDiarySnapshot(context.player());
        });

        ServerPlayNetworking.registerGlobalReceiver(DeleteEntryPayload.ID, (payload, context) -> {
            if (!DearDiaryApi.deleteEntry(context.player(), payload.entryId())) {
                context.player().sendMessage(Text.translatable("message.dear_diary.error.not_found"), false);
            }
            sendDiarySnapshot(context.player());
        });

        ServerPlayNetworking.registerGlobalReceiver(SetFavoritePayload.ID, (payload, context) -> {
            if (!DearDiaryApi.setFavorite(context.player(), payload.entryId(), payload.favorite())) {
                context.player().sendMessage(Text.translatable("message.dear_diary.error.not_found"), false);
            }
            sendDiarySnapshot(context.player());
        });

        ServerPlayNetworking.registerGlobalReceiver(ShareEntryPayload.ID, (payload, context) -> {
            if (!DearDiaryApi.shareEntryToChat(context.player(), payload.entryId())) {
                context.player().sendMessage(Text.translatable("message.dear_diary.error.share_failed"), false);
            }
            sendDiarySnapshot(context.player());
        });
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

    public static void sendDiarySnapshot(ServerPlayerEntity player) {
        if (ServerPlayNetworking.canSend(player, DiarySnapshotPayload.ID)) {
            ServerPlayNetworking.send(player, DiarySnapshotPayload.fromDiary(DearDiaryApi.getDiary(player)));
        }
    }

    public static void sendAutomaticEntryNotice(ServerPlayerEntity player, DiaryEntry entry) {
        if (ServerPlayNetworking.canSend(player, NewAutomaticEntryPayload.ID)) {
            ServerPlayNetworking.send(player, NewAutomaticEntryPayload.fromEntry(entry));
        }
    }

    public static boolean openDiaryScreen(ServerPlayerEntity player, boolean newEntry) {
        if (!ServerPlayNetworking.canSend(player, OpenDiaryScreenPayload.ID)) {
            return false;
        }

        ServerPlayNetworking.send(player, new OpenDiaryScreenPayload(newEntry));
        sendDiarySnapshot(player);
        return true;
    }
}
