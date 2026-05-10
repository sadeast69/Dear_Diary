package com.worldremembers.deardiary.client.gui;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

final class DiaryTextAreaWidget extends AbstractWidget {
    private static final int FIELD_COLOR = 0xFFFFF0CD;
    private static final int FIELD_FOCUSED_COLOR = 0xFFFFF4D9;
    private static final int FIELD_BORDER = 0xFFC08B50;
    private static final int FIELD_BORDER_FOCUSED = 0xFF9D6330;
    private static final int TEXT_COLOR = 0xFF2B1708;
    private static final int PLACEHOLDER_COLOR = 0xFF8E6843;

    private final Font textRenderer;
    private String text = "";
    private Component placeholder = Component.empty();
    private int cursor;
    private int maxLength = 2000;

    DiaryTextAreaWidget(Font textRenderer, int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
        this.textRenderer = textRenderer;
    }

    String getText() {
        return text;
    }

    void setText(String text) {
        this.text = text == null ? "" : trimToMax(text.replace("\r", ""));
        this.cursor = this.text.length();
    }

    void setMaxLength(int maxLength) {
        this.maxLength = Math.max(0, maxLength);
        setText(text);
    }

    void setPlaceholder(Component placeholder) {
        this.placeholder = placeholder == null ? Component.empty() : placeholder;
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        int fill = isFocused() ? FIELD_FOCUSED_COLOR : FIELD_COLOR;
        int border = isFocused() ? FIELD_BORDER_FOCUSED : FIELD_BORDER;
        context.fill(getX(), getY(), getRight(), getBottom(), fill);
        context.renderOutline(getX(), getY(), width, height, border);

        int textX = getX() + 6;
        int textY = getY() + 6;
        int innerWidth = Math.max(10, width - 12);
        int visibleLines = Math.max(1, (height - 12) / 10);

        if (text.isEmpty() && !isFocused()) {
            renderLines(context, textRenderer.split(placeholder, innerWidth), textX, textY, visibleLines, PLACEHOLDER_COLOR);
            return;
        }

        String display = cursorDisplayText();
        List<FormattedCharSequence> lines = textRenderer.split(Component.literal(display), innerWidth);
        int firstLine = Math.max(0, lines.size() - visibleLines);
        renderLines(context, lines.subList(firstLine, lines.size()), textX, textY, visibleLines, TEXT_COLOR);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        setFocused(true);
        cursor = text.length();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isFocused() || !active || !visible) {
            return false;
        }

        if (Screen.isPaste(keyCode)) {
            insert(Minecraft.getInstance().keyboardHandler.getClipboard());
            return true;
        }

        return switch (keyCode) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                removeBeforeCursor();
                yield true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                removeAfterCursor();
                yield true;
            }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                insert("\n");
                yield true;
            }
            case GLFW.GLFW_KEY_LEFT -> {
                cursor = Math.max(0, cursor - 1);
                yield true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                cursor = Math.min(text.length(), cursor + 1);
                yield true;
            }
            case GLFW.GLFW_KEY_HOME -> {
                cursor = 0;
                yield true;
            }
            case GLFW.GLFW_KEY_END -> {
                cursor = text.length();
                yield true;
            }
            default -> false;
        };
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!isFocused() || !active || !visible || Character.isISOControl(chr)) {
            return false;
        }

        insert(String.valueOf(chr));
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        defaultButtonNarrationText(builder);
    }

    private void renderLines(GuiGraphics context, List<FormattedCharSequence> lines, int x, int y, int visibleLines, int color) {
        int lineY = y;
        int count = Math.min(visibleLines, lines.size());
        for (int index = 0; index < count; index++) {
            context.drawString(textRenderer, lines.get(index), x, lineY, color, false);
            lineY += 10;
        }
    }

    private String cursorDisplayText() {
        if (!isFocused()) {
            return text;
        }

        int safeCursor = Math.max(0, Math.min(cursor, text.length()));
        return text.substring(0, safeCursor) + "|" + text.substring(safeCursor);
    }

    private void insert(String value) {
        if (value == null || value.isEmpty() || text.length() >= maxLength) {
            return;
        }

        String clean = trimToMax(value.replace("\r", ""));
        int available = Math.max(0, maxLength - text.length());
        if (clean.length() > available) {
            clean = clean.substring(0, available);
        }

        text = text.substring(0, cursor) + clean + text.substring(cursor);
        cursor += clean.length();
    }

    private void removeBeforeCursor() {
        if (cursor <= 0 || text.isEmpty()) {
            return;
        }

        text = text.substring(0, cursor - 1) + text.substring(cursor);
        cursor--;
    }

    private void removeAfterCursor() {
        if (cursor >= text.length() || text.isEmpty()) {
            return;
        }

        text = text.substring(0, cursor) + text.substring(cursor + 1);
    }

    private String trimToMax(String value) {
        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }
}
