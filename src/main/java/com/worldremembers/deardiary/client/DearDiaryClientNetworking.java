package com.worldremembers.deardiary.client;

import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.client.gui.DiaryScreen;
import com.worldremembers.deardiary.network.payload.CreateManualEntryPayload;
import com.worldremembers.deardiary.network.payload.DeleteEntryPayload;
import com.worldremembers.deardiary.network.payload.DiarySnapshotPayload;
import com.worldremembers.deardiary.network.payload.EditEntryPayload;
import com.worldremembers.deardiary.network.payload.NewAutomaticEntryPayload;
import com.worldremembers.deardiary.network.payload.OpenDiaryScreenPayload;
import com.worldremembers.deardiary.network.payload.RequestDiaryPayload;
import com.worldremembers.deardiary.network.payload.SetFavoritePayload;
import com.worldremembers.deardiary.network.payload.ShareEntryPayload;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public final class DearDiaryClientNetworking {
    private static boolean registered;

    private DearDiaryClientNetworking() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        ClientPlayNetworking.registerGlobalReceiver(DiarySnapshotPayload.ID, (payload, context) -> {
            ClientDiaryCache.updateFromJson(payload.diaryJson(), context.player().getUuid());
            DearDiaryMod.LOGGER.debug("Received Dear Diary snapshot with {} entries", ClientDiaryCache.entryCount());
        });

        ClientPlayNetworking.registerGlobalReceiver(NewAutomaticEntryPayload.ID, (payload, context) -> {
            ClientDiaryCache.rememberAutomaticEntry(payload.entryJson());
            DiaryNotificationManager.showNewAutomaticEntry();
            DearDiaryMod.LOGGER.debug("Received Dear Diary automatic entry notice");
        });

        ClientPlayNetworking.registerGlobalReceiver(OpenDiaryScreenPayload.ID, (payload, context) -> {
            if (payload.newEntry()) {
                context.client().setScreen(new DiaryScreen(null, false, true));
            } else {
                context.client().setScreen(new DiaryScreen(null));
            }
        });
    }

    public static void requestDiarySnapshot() {
        sendIfPossible(RequestDiaryPayload.ID, new RequestDiaryPayload());
    }

    public static void createManualEntry(String title, String text, boolean includeLocation) {
        sendIfPossible(CreateManualEntryPayload.ID, new CreateManualEntryPayload(title, text, includeLocation));
    }

    public static void createChapterEntry(String title, String text, boolean includeLocation) {
        sendIfPossible(CreateManualEntryPayload.ID, new CreateManualEntryPayload(title, text, includeLocation, true));
    }

    public static void editEntry(UUID entryId, String title, String text) {
        sendIfPossible(EditEntryPayload.ID, new EditEntryPayload(entryId, title, text));
    }

    public static void deleteEntry(UUID entryId) {
        sendIfPossible(DeleteEntryPayload.ID, new DeleteEntryPayload(entryId));
    }

    public static void setFavorite(UUID entryId, boolean favorite) {
        sendIfPossible(SetFavoritePayload.ID, new SetFavoritePayload(entryId, favorite));
    }

    public static void shareEntry(UUID entryId) {
        sendIfPossible(ShareEntryPayload.ID, new ShareEntryPayload(entryId));
    }

    private static void sendIfPossible(net.minecraft.network.packet.CustomPayload.Id<?> id, net.minecraft.network.packet.CustomPayload payload) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && ClientPlayNetworking.canSend(id)) {
            ClientPlayNetworking.send(payload);
        }
    }
}
