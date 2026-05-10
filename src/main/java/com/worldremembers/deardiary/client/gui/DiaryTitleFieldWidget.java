package com.worldremembers.deardiary.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

final class DiaryTitleFieldWidget extends ClickableWidget {
    private static final int TEXT_COLOR = 0xFF2B1708;
    private static final int PLACEHOLDER_COLOR = 0xFF8E6843;

    private final TextRenderer textRenderer;
    private String text = "";
    private Text placeholder = Text.empty();
    private int cursor;
    private int maxLength = 80;

    DiaryTitleFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
        this.textRenderer = textRenderer;
    }

    String getText() {
        return text;
    }

    void setText(String text) {
        this.text = text == null ? "" : trimToMax(singleLine(text));
        this.cursor = this.text.length();
    }

    void setMaxLength(int maxLength) {
        this.maxLength = Math.max(0, maxLength);
        setText(text);
    }

    void setPlaceholder(Text placeholder) {
        this.placeholder = placeholder == null ? Text.empty() : placeholder;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int textX = getX();
        int textY = getY() + Math.max(0, (height - textRenderer.fontHeight) / 2);
        int innerWidth = Math.max(1, width);

        if (text.isEmpty() && !isFocused()) {
            context.drawText(textRenderer, textRenderer.trimToWidth(placeholder.getString(), innerWidth), textX, textY, PLACEHOLDER_COLOR, false);
            return;
        }

        context.drawText(textRenderer, visibleText(cursorDisplayText(), innerWidth), textX, textY, TEXT_COLOR, false);
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
            insert(MinecraftClient.getInstance().keyboard.getClipboard());
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
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
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

        String clean = trimToMax(singleLine(value));
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

    private String visibleText(String value, int maxWidth) {
        if (textRenderer.getWidth(value) <= maxWidth) {
            return value;
        }

        return textRenderer.trimToWidth(value, maxWidth, true);
    }

    private String trimToMax(String value) {
        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }

    private String singleLine(String value) {
        return value.replace("\r", "").replace("\n", " ");
    }
}
