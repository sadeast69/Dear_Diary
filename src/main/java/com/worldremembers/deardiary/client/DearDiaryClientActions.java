package com.worldremembers.deardiary.client;

import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.client.gui.DiaryScreen;
import net.minecraft.client.MinecraftClient;

public final class DearDiaryClientActions {
    private static OpenTarget pendingTarget;
    private static boolean pendingFeedback;

    private DearDiaryClientActions() {
    }

    public static void openDiary() {
        requestOpen(OpenTarget.DIARY, false);
    }

    public static void openDiaryWithFeedback() {
        requestOpen(OpenTarget.DIARY, true);
    }

    public static void openNewEntry() {
        requestOpen(OpenTarget.NEW_ENTRY, false);
    }

    public static void openNewEntryWithFeedback() {
        requestOpen(OpenTarget.NEW_ENTRY, true);
    }

    public static void tick(MinecraftClient client) {
        if (pendingTarget == null) {
            return;
        }

        OpenTarget target = pendingTarget;
        boolean feedback = pendingFeedback;
        pendingTarget = null;
        pendingFeedback = false;

        if (client.player != null && client.world != null) {
            if (target == OpenTarget.NEW_ENTRY) {
                client.setScreen(new DiaryScreen(null, false, true));
            } else {
                client.setScreen(new DiaryScreen(null));
            }
        } else if (feedback) {
            DearDiaryMod.LOGGER.debug("Dear Diary client screen was requested, but no client world is active.");
        }
    }

    private static void requestOpen(OpenTarget target, boolean feedback) {
        pendingTarget = target;
        pendingFeedback = feedback;
    }

    private enum OpenTarget {
        DIARY,
        NEW_ENTRY
    }
}
