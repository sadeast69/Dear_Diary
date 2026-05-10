package com.worldremembers.deardiary.client;

import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.client.config.DearDiaryClientConfig;
import com.worldremembers.deardiary.client.gui.InventoryButtonBounds;
import com.worldremembers.deardiary.client.gui.InventoryDiaryButtonWidget;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.neoforge.client.event.ScreenEvent;

public final class DearDiaryInventoryButton {
    private static final boolean DEBUG_INVENTORY_BUTTON = false;
    private static final int ATTACHED_OVERLAP = 2;
    private static final int ATTACHED_POSITION_GAP = -ATTACHED_OVERLAP;
    private static final Field LEFT_POS = containerField("leftPos");
    private static final Field TOP_POS = containerField("topPos");
    private static final Field IMAGE_WIDTH = containerField("imageWidth");
    private static final Field IMAGE_HEIGHT = containerField("imageHeight");
    private static final Map<Screen, InventoryDiaryButtonWidget> BUTTONS = new WeakHashMap<>();

    private DearDiaryInventoryButton() {
    }

    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!shouldAttach(screen) || !DearDiaryClientConfig.get().showInventoryButton()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) screen;
        InventoryButtonBounds inventoryBounds = inventoryBounds(containerScreen, screenWidth, screenHeight);
        DearDiaryClientConfig config = DearDiaryClientConfig.get();
        DearDiaryClientConfig.InventoryButtonPosition position = config.inventoryButtonPosition(
                inventoryBounds.x(),
                inventoryBounds.y(),
                inventoryBounds.width(),
                inventoryBounds.height(),
                screenWidth,
                screenHeight,
                InventoryDiaryButtonWidget.SIZE,
                ATTACHED_POSITION_GAP
        );

        InventoryDiaryButtonWidget button = new InventoryDiaryButtonWidget(
                client.font,
                position.x(),
                position.y(),
                inventoryBounds,
                screenWidth,
                screenHeight,
                ATTACHED_POSITION_GAP,
                config.attachedToInventory() ? config.inventoryAnchorSide() : null,
                DEBUG_INVENTORY_BUTTON
        );
        event.addListener(button);
        BUTTONS.put(screen, button);
    }

    public static void onMouseDragged(ScreenEvent.MouseDragged.Pre event) {
        InventoryDiaryButtonWidget button = BUTTONS.get(event.getScreen());
        if (button != null && button.handleScreenMouseDragged(
                event.getMouseX(),
                event.getMouseY(),
                event.getMouseButton(),
                event.getDragX(),
                event.getDragY()
        )) {
            event.setCanceled(true);
        }
    }

    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        InventoryDiaryButtonWidget button = BUTTONS.get(event.getScreen());
        if (button != null && button.handleScreenMouseReleased(event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        }
    }

    private static boolean shouldAttach(Screen screen) {
        return screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen;
    }

    private static InventoryButtonBounds inventoryBounds(AbstractContainerScreen<?> screen, int screenWidth, int screenHeight) {
        int fallbackWidth = screen instanceof CreativeModeInventoryScreen ? 195 : 176;
        int fallbackHeight = screen instanceof CreativeModeInventoryScreen ? 136 : 166;
        int imageWidth = fieldInt(screen, IMAGE_WIDTH, fallbackWidth);
        int imageHeight = fieldInt(screen, IMAGE_HEIGHT, fallbackHeight);
        int x = fieldInt(screen, LEFT_POS, (screenWidth - imageWidth) / 2);
        int y = fieldInt(screen, TOP_POS, (screenHeight - imageHeight) / 2);
        return new InventoryButtonBounds(x, y, imageWidth, imageHeight);
    }

    private static Field containerField(String name) {
        try {
            Field field = AbstractContainerScreen.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException exception) {
            DearDiaryMod.LOGGER.warn("Dear Diary could not access container screen field {}", name, exception);
            return null;
        }
    }

    private static int fieldInt(AbstractContainerScreen<?> screen, Field field, int fallback) {
        if (field == null) {
            return fallback;
        }

        try {
            return field.getInt(screen);
        } catch (IllegalAccessException exception) {
            DearDiaryMod.LOGGER.warn("Dear Diary could not read inventory screen bounds", exception);
            return fallback;
        }
    }
}
