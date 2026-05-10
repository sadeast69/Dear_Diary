package com.worldremembers.deardiary.client;

import com.worldremembers.deardiary.client.config.DearDiaryClientConfig;
import com.worldremembers.deardiary.client.gui.InventoryButtonBounds;
import com.worldremembers.deardiary.client.gui.InventoryDiaryButtonWidget;
import com.worldremembers.deardiary.client.mixin.HandledScreenAccessor;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

public final class DearDiaryInventoryButton {
    private static final boolean DEBUG_INVENTORY_BUTTON = false;
    private static final int ATTACHED_OVERLAP = 2;
    private static final int ATTACHED_POSITION_GAP = -ATTACHED_OVERLAP;

    private DearDiaryInventoryButton() {
    }

    public static void register() {
        DearDiaryClientConfig.load();
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!shouldAttach(screen) || !DearDiaryClientConfig.get().showInventoryButton()) {
                return;
            }

            InventoryButtonBounds inventoryBounds = inventoryBounds(screen);
            DearDiaryClientConfig config = DearDiaryClientConfig.get();
            DearDiaryClientConfig.InventoryButtonPosition position = config.inventoryButtonPosition(
                    inventoryBounds.x(),
                    inventoryBounds.y(),
                    inventoryBounds.width(),
                    inventoryBounds.height(),
                    scaledWidth,
                    scaledHeight,
                    InventoryDiaryButtonWidget.SIZE,
                    ATTACHED_POSITION_GAP
            );
            Screens.getButtons(screen).add(new InventoryDiaryButtonWidget(
                    Screens.getTextRenderer(screen),
                    position.x(),
                    position.y(),
                    inventoryBounds,
                    scaledWidth,
                    scaledHeight,
                    ATTACHED_POSITION_GAP,
                    config.attachedToInventory() ? config.inventoryAnchorSide() : null,
                    DEBUG_INVENTORY_BUTTON
            ));
        });
    }

    private static boolean shouldAttach(Screen screen) {
        return screen instanceof InventoryScreen || screen instanceof CreativeInventoryScreen;
    }

    private static InventoryButtonBounds inventoryBounds(Screen screen) {
        HandledScreen<?> handledScreen = (HandledScreen<?>) screen;
        HandledScreenAccessor accessor = (HandledScreenAccessor) handledScreen;
        return new InventoryButtonBounds(
                accessor.dearDiary$getX(),
                accessor.dearDiary$getY(),
                accessor.dearDiary$getBackgroundWidth(),
                accessor.dearDiary$getBackgroundHeight()
        );
    }
}
