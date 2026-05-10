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
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public final class DearDiaryClientNetworking {
    private DearDiaryClientNetworking() {
    }

    public static void handleDiarySnapshot(DiarySnapshotPayload payload) {
        Minecraft client = Minecraft.getInstance();
        UUID fallbackUuid = client.player == null ? new UUID(0L, 0L) : client.player.getUUID();
        ClientDiaryCache.updateFromJson(payload.diaryJson(), fallbackUuid);
        DearDiaryMod.LOGGER.debug("Received Dear Diary snapshot with {} entries", ClientDiaryCache.entryCount());
    }

    public static void handleNewAutomaticEntry(NewAutomaticEntryPayload payload) {
        ClientDiaryCache.rememberAutomaticEntry(payload.entryJson());
        DiaryNotificationManager.showNewAutomaticEntry();
        DearDiaryMod.LOGGER.debug("Received Dear Diary automatic entry notice");
    }

    public static void handleOpenDiaryScreen(OpenDiaryScreenPayload payload) {
        openDiaryScreen(payload.newEntry());
        DearDiaryMod.LOGGER.debug("Dear Diary GUI open requested on NeoForge, newEntry={}", payload.newEntry());
    }

    public static void openDiaryScreen(boolean newEntry) {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            requestDiarySnapshot();
            if (client.player != null && client.level != null) {
                client.setScreen(new DiaryScreen(null, false, newEntry));
            } else {
                DearDiaryMod.LOGGER.debug("Dear Diary GUI was requested, but no client world is active");
            }
        });
    }

    public static void requestDiarySnapshot() {
        sendToServer(new RequestDiaryPayload());
    }

    public static void createManualEntry(String title, String text, boolean includeLocation) {
        sendToServer(new CreateManualEntryPayload(title, text, includeLocation));
    }

    public static void createChapterEntry(String title, String text, boolean includeLocation) {
        sendToServer(new CreateManualEntryPayload(title, text, includeLocation, true));
    }

    public static void editEntry(UUID entryId, String title, String text) {
        sendToServer(new EditEntryPayload(entryId, title, text));
    }

    public static void deleteEntry(UUID entryId) {
        sendToServer(new DeleteEntryPayload(entryId));
    }

    public static void setFavorite(UUID entryId, boolean favorite) {
        sendToServer(new SetFavoritePayload(entryId, favorite));
    }

    public static void shareEntry(UUID entryId) {
        sendToServer(new ShareEntryPayload(entryId));
    }

    public static void showTemporaryClientMessage(String message) {
        Minecraft client = Minecraft.getInstance();
        Component feedback = Component.literal(message);
        if (client.player != null) {
            client.player.sendSystemMessage(feedback);
        } else {
            DearDiaryMod.LOGGER.info(feedback.getString());
        }
    }

    private static void sendToServer(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            PacketDistributor.sendToServer(payload);
        } else {
            DearDiaryMod.LOGGER.debug("Dear Diary payload {} was not sent because no client player is active", payload.type().id());
        }
    }
}
