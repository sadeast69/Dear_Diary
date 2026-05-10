package com.worldremembers.deardiary.client.gui;

import com.worldremembers.deardiary.client.DearDiaryClientActions;
import com.worldremembers.deardiary.client.config.DearDiaryClientConfig;
import com.worldremembers.deardiary.client.config.DearDiaryClientConfig.InventoryAnchorSide;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class InventoryDiaryButtonWidget extends ClickableWidget {
    public static final int SIZE = 22;

    private static final int DRAG_THRESHOLD = 4;
    private static final int SNAP_THRESHOLD = 12;
    private static final Identifier ICON_TEXTURE = Identifier.of("dear_diary", "textures/gui/diary_button_icon.png");
    private static final ItemStack FALLBACK_ICON = new ItemStack(Items.WRITABLE_BOOK);
    private static final int BORDER_DARK = 0xFF1F1F1F;
    private static final int BORDER_SHADOW = 0xFF555555;
    private static final int BORDER_LIGHT = 0xFFE0E0E0;
    private static final int BACKGROUND = 0xFF8A8A8A;
    private static final int BACKGROUND_HOVER = 0xFFA8A8A8;
    private static final int BACKGROUND_PRESSED = 0xFF777777;
    private static final int ATTACHED_BACKGROUND = 0xFFC6C6C6;
    private static final int ATTACHED_BACKGROUND_HOVER = 0xFFD8D8D8;
    private static final int ATTACHED_BACKGROUND_PRESSED = 0xFFB4B4B4;
    private static final int ATTACHED_CONNECTOR = 0xFFA0A0A0;
    private static Boolean customIconAvailable;
    private static final int DEBUG_INVENTORY = 0xAA37D7FF;
    private static final int DEBUG_SNAP = 0x66F5D94B;
    private static final int DEBUG_TEXT = 0xFFFFFFFF;

    private final TextRenderer textRenderer;
    private final InventoryButtonBounds inventoryBounds;
    private final int screenWidth;
    private final int screenHeight;
    private final int buttonGap;
    private final boolean debugOverlay;
    private InventoryAnchorSide attachedSide;
    private boolean pressed;
    private boolean dragging;
    private double pressMouseX;
    private double pressMouseY;
    private int pressX;
    private int pressY;
    private SnapResult currentSnap;

    public InventoryDiaryButtonWidget(
            TextRenderer textRenderer,
            int x,
            int y,
            InventoryButtonBounds inventoryBounds,
            int screenWidth,
            int screenHeight,
            int buttonGap,
            InventoryAnchorSide attachedSide,
            boolean debugOverlay
    ) {
        super(x, y, SIZE, SIZE, Text.translatable("screen.dear_diary.inventory_button"));
        this.textRenderer = textRenderer;
        this.inventoryBounds = inventoryBounds;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.buttonGap = buttonGap;
        this.attachedSide = attachedSide;
        this.debugOverlay = debugOverlay;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !active || !visible || !isMouseOver(mouseX, mouseY)) {
            return false;
        }

        pressed = true;
        dragging = false;
        pressMouseX = mouseX;
        pressMouseY = mouseY;
        pressX = getX();
        pressY = getY();
        setFocused(true);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!pressed || button != 0) {
            return false;
        }

        double movedX = mouseX - pressMouseX;
        double movedY = mouseY - pressMouseY;
        if (!dragging && Math.hypot(movedX, movedY) >= DRAG_THRESHOLD) {
            dragging = true;
        }

        if (dragging) {
            int proposedX = clamp(pressX + (int) Math.round(movedX), 0, Math.max(0, screenWidth - width));
            int proposedY = clamp(pressY + (int) Math.round(movedY), 0, Math.max(0, screenHeight - height));
            currentSnap = findSnap(proposedX, proposedY);
            if (currentSnap == null) {
                setX(proposedX);
                setY(proposedY);
            } else {
                setX(currentSnap.x());
                setY(currentSnap.y());
            }
        }

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!pressed || button != 0) {
            return false;
        }

        pressed = false;
        if (dragging) {
            dragging = false;
            SnapResult snap = currentSnap;
            if (snap == null) {
                attachedSide = null;
                DearDiaryClientConfig.get().saveFreeInventoryButtonPosition(getX(), getY(), screenWidth, screenHeight, width);
            } else {
                attachedSide = snap.side();
                DearDiaryClientConfig.get().saveAttachedInventoryButtonPosition(
                        snap.side(),
                        snap.offset(),
                        inventoryBounds.width(),
                        inventoryBounds.height(),
                        width
                );
            }
            currentSnap = null;
            return true;
        }

        DearDiaryClientActions.openDiary();
        return true;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hovered = isMouseOver(mouseX, mouseY);
        renderDebugOverlay(context);
        InventoryAnchorSide renderSide = dragging
                ? currentSnap == null ? null : currentSnap.side()
                : attachedSide;
        if (renderSide == null) {
            renderFreeButton(context, hovered);
        } else {
            renderAttachedButton(context, hovered, renderSide);
        }

        if (hovered && !dragging) {
            context.drawTooltip(textRenderer, Text.translatable("screen.dear_diary.inventory_button.tooltip"), mouseX, mouseY);
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }

    private void renderFreeButton(DrawContext context, boolean hovered) {
        int fill = pressed ? BACKGROUND_PRESSED : hovered || dragging ? BACKGROUND_HOVER : BACKGROUND;
        context.fill(getX(), getY(), getRight(), getBottom(), BORDER_DARK);
        context.fill(getX() + 1, getY() + 1, getRight() - 1, getBottom() - 1, BORDER_SHADOW);
        context.fill(getX() + 2, getY() + 2, getRight() - 2, getBottom() - 2, fill);
        context.fill(getX() + 2, getY() + 2, getRight() - 3, getY() + 3, BORDER_LIGHT);
        context.fill(getX() + 2, getY() + 2, getX() + 3, getBottom() - 3, BORDER_LIGHT);
        context.fill(getRight() - 3, getY() + 3, getRight() - 2, getBottom() - 2, BORDER_DARK);
        context.fill(getX() + 3, getBottom() - 3, getRight() - 2, getBottom() - 2, BORDER_DARK);

        renderIcon(context, pressed ? 1 : 0);
    }

    private void renderAttachedButton(DrawContext context, boolean hovered, InventoryAnchorSide side) {
        int fill = pressed ? ATTACHED_BACKGROUND_PRESSED : hovered || dragging ? ATTACHED_BACKGROUND_HOVER : ATTACHED_BACKGROUND;
        drawAttachedConnector(context, side, fill);

        drawAttachedFrame(context, side, fill);
        softenAttachedEdge(context, side, fill);

        renderIcon(context, pressed ? 1 : 0);
    }

    private void drawAttachedConnector(DrawContext context, InventoryAnchorSide side, int fill) {
        switch (side) {
            case LEFT -> {
                context.fill(getRight() - 2, getY() + 3, getRight() + 2, getBottom() - 3, ATTACHED_CONNECTOR);
                context.fill(getRight() - 2, getY() + 4, getRight() + 2, getBottom() - 4, fill);
            }
            case RIGHT -> {
                context.fill(getX() - 2, getY() + 3, getX() + 2, getBottom() - 3, ATTACHED_CONNECTOR);
                context.fill(getX() - 2, getY() + 4, getX() + 2, getBottom() - 4, fill);
            }
            case TOP -> {
                context.fill(getX() + 3, getBottom() - 2, getRight() - 3, getBottom() + 2, ATTACHED_CONNECTOR);
                context.fill(getX() + 4, getBottom() - 2, getRight() - 4, getBottom() + 2, fill);
            }
            case BOTTOM -> {
                context.fill(getX() + 3, getY() - 2, getRight() - 3, getY() + 2, ATTACHED_CONNECTOR);
                context.fill(getX() + 4, getY() - 2, getRight() - 4, getY() + 2, fill);
            }
        }
    }

    private void drawAttachedFrame(DrawContext context, InventoryAnchorSide side, int fill) {
        context.fill(getX(), getY(), getRight(), getBottom(), BORDER_DARK);
        context.fill(getX() + 1, getY() + 1, getRight() - 1, getBottom() - 1, BORDER_SHADOW);
        context.fill(getX() + 2, getY() + 2, getRight() - 2, getBottom() - 2, fill);
        context.fill(getX() + 2, getY() + 2, getRight() - 3, getY() + 3, BORDER_LIGHT);
        context.fill(getX() + 2, getY() + 2, getX() + 3, getBottom() - 3, BORDER_LIGHT);
        context.fill(getRight() - 3, getY() + 3, getRight() - 2, getBottom() - 2, BORDER_DARK);
        context.fill(getX() + 3, getBottom() - 3, getRight() - 2, getBottom() - 2, BORDER_DARK);

        switch (side) {
            case RIGHT -> {
                context.fill(getX(), getY() + 1, getX() + 4, getBottom() - 1, ATTACHED_CONNECTOR);
                context.fill(getX(), getY() + 3, getX() + 4, getBottom() - 3, fill);
            }
            case LEFT -> {
                context.fill(getRight() - 4, getY() + 1, getRight(), getBottom() - 1, ATTACHED_CONNECTOR);
                context.fill(getRight() - 4, getY() + 3, getRight(), getBottom() - 3, fill);
            }
            case TOP -> {
                context.fill(getX() + 1, getBottom() - 4, getRight() - 1, getBottom(), ATTACHED_CONNECTOR);
                context.fill(getX() + 3, getBottom() - 4, getRight() - 3, getBottom(), fill);
            }
            case BOTTOM -> {
                context.fill(getX() + 1, getY(), getRight() - 1, getY() + 4, ATTACHED_CONNECTOR);
                context.fill(getX() + 3, getY(), getRight() - 3, getY() + 4, fill);
            }
        }
    }

    private void softenAttachedEdge(DrawContext context, InventoryAnchorSide side, int fill) {
        switch (side) {
            case LEFT -> context.fill(getRight() - 3, getY() + 4, getRight(), getBottom() - 4, fill);
            case RIGHT -> context.fill(getX(), getY() + 4, getX() + 3, getBottom() - 4, fill);
            case TOP -> context.fill(getX() + 4, getBottom() - 3, getRight() - 4, getBottom(), fill);
            case BOTTOM -> context.fill(getX() + 4, getY(), getRight() - 4, getY() + 3, fill);
        }
    }

    private void renderIcon(DrawContext context, int pressOffset) {
        int iconX = getX() + (width - 16) / 2 + pressOffset;
        int iconY = getY() + (height - 16) / 2 + pressOffset;
        if (customIconAvailable()) {
            context.drawTexture(ICON_TEXTURE, iconX, iconY, 0.0F, 0.0F, 16, 16, 16, 16);
            return;
        }

        context.drawItem(FALLBACK_ICON, iconX, iconY);
    }

    private static boolean customIconAvailable() {
        if (customIconAvailable == null) {
            customIconAvailable = MinecraftClient.getInstance().getResourceManager().getResource(ICON_TEXTURE).isPresent();
        }

        return customIconAvailable;
    }

    private void renderDebugOverlay(DrawContext context) {
        if (!debugOverlay) {
            return;
        }

        context.drawBorder(inventoryBounds.x(), inventoryBounds.y(), inventoryBounds.width(), inventoryBounds.height(), DEBUG_INVENTORY);
        context.drawBorder(
                inventoryBounds.x() - SIZE - buttonGap - SNAP_THRESHOLD,
                inventoryBounds.y() - SIZE - buttonGap - SNAP_THRESHOLD,
                inventoryBounds.width() + (SIZE + buttonGap + SNAP_THRESHOLD) * 2,
                inventoryBounds.height() + (SIZE + buttonGap + SNAP_THRESHOLD) * 2,
                DEBUG_SNAP
        );
        InventoryAnchorSide debugSide = dragging
                ? currentSnap == null ? null : currentSnap.side()
                : attachedSide;
        String mode = debugSide == null ? "free" : debugSide.name().toLowerCase();
        context.drawText(textRenderer, mode, getX(), Math.max(0, getY() - 10), DEBUG_TEXT, true);
    }

    private SnapResult findSnap(int x, int y) {
        SnapResult best = null;
        for (InventoryAnchorSide side : InventoryAnchorSide.values()) {
            SnapResult candidate = snapForSide(side, x, y);
            if (candidate != null && (best == null || candidate.distanceSquared() < best.distanceSquared())) {
                best = candidate;
            }
        }

        return best;
    }

    private SnapResult snapForSide(InventoryAnchorSide side, int x, int y) {
        int offset = switch (side) {
            case LEFT, RIGHT -> clamp(y - inventoryBounds.y(), 0, Math.max(0, inventoryBounds.height() - height));
            case TOP, BOTTOM -> clamp(x - inventoryBounds.x(), 0, Math.max(0, inventoryBounds.width() - width));
        };
        int targetX = switch (side) {
            case LEFT -> inventoryBounds.x() - width - buttonGap;
            case RIGHT -> inventoryBounds.right() + buttonGap;
            case TOP, BOTTOM -> inventoryBounds.x() + offset;
        };
        int targetY = switch (side) {
            case LEFT, RIGHT -> inventoryBounds.y() + offset;
            case TOP -> inventoryBounds.y() - height - buttonGap;
            case BOTTOM -> inventoryBounds.bottom() + buttonGap;
        };
        targetX = clamp(targetX, 0, Math.max(0, screenWidth - width));
        targetY = clamp(targetY, 0, Math.max(0, screenHeight - height));

        int dx = x - targetX;
        int dy = y - targetY;
        int distanceSquared = dx * dx + dy * dy;
        if (distanceSquared > SNAP_THRESHOLD * SNAP_THRESHOLD) {
            return null;
        }

        return new SnapResult(side, offset, targetX, targetY, distanceSquared);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record SnapResult(InventoryAnchorSide side, int offset, int x, int y, int distanceSquared) {
    }
}
