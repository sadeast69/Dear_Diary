package com.worldremembers.deardiary.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.client.config.DearDiaryClientConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class DiaryNotificationManager {
    private static final Identifier ICON_TEXTURE = Identifier.of(DearDiaryMod.MOD_ID, "textures/gui/diary_button_icon.png");
    private static final ItemStack FALLBACK_ICON = new ItemStack(Items.WRITABLE_BOOK);
    private static final int WIDTH = 124;
    private static final int HEIGHT = 26;
    private static final int MIN_DURATION_TICKS = 20;
    private static final int FADE_TICKS = 10;
    private static final int BORDER = 0xFF5E3A17;
    private static final int PAPER = 0xFFF4D99D;
    private static final int PAPER_LIGHT = 0xFFFFEABD;
    private static final int SHADOW = 0x66000000;
    private static final int TEXT = 0xFF3B2411;

    private static boolean registered;
    private static int ticksRemaining;
    private static int totalTicks;
    private static Text message = Text.translatable("notification.dear_diary.new_entry");
    private static Boolean customIconAvailable;

    private DiaryNotificationManager() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        DearDiaryClientConfig.load();
        ClientTickEvents.END_CLIENT_TICK.register(DiaryNotificationManager::tick);
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> render(drawContext));
    }

    public static void showNewAutomaticEntry() {
        show(Text.translatable("notification.dear_diary.new_entry"));
    }

    public static void showTestNotification() {
        show(Text.translatable("notification.dear_diary.new_entry"));
    }

    private static void show(Text notificationMessage) {
        MinecraftClient client = MinecraftClient.getInstance();
        DearDiaryClientConfig config = DearDiaryClientConfig.get();

        if (config.showNewEntryNotification()) {
            totalTicks = Math.max(MIN_DURATION_TICKS, config.notificationDurationTicks());
            ticksRemaining = totalTicks;
            message = notificationMessage;
        }

        if (config.playNewEntrySound()) {
            playSound(client, config.notificationSound());
        }
    }

    private static void tick(MinecraftClient client) {
        if (ticksRemaining > 0) {
            ticksRemaining--;
        }
    }

    private static void render(DrawContext context) {
        if (ticksRemaining <= 0) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int width = Math.min(WIDTH, Math.max(76, screenWidth - 16));
        float alpha = currentAlpha();
        int slide = Math.round((1.0F - alpha) * 8.0F);
        int x = Math.max(6, 10 - slide);
        int y = clamp(screenHeight / 4, 12, Math.max(12, screenHeight - HEIGHT - 58));

        drawPanel(context, x, y, width, alpha);
        drawIcon(context, x + 5, y + 5, alpha);

        TextRenderer textRenderer = client.textRenderer;
        String label = textRenderer.trimToWidth(message.getString(), Math.max(20, width - 29));
        int textY = y + (HEIGHT - textRenderer.fontHeight) / 2;
        context.drawText(textRenderer, label, x + 25, textY, withAlpha(TEXT, alpha), false);
    }

    private static void drawPanel(DrawContext context, int x, int y, int width, float alpha) {
        context.fill(x + 2, y + 2, x + width + 2, y + HEIGHT + 2, withAlpha(SHADOW, alpha));
        context.fill(x, y, x + width, y + HEIGHT, withAlpha(BORDER, alpha));
        context.fill(x + 1, y + 1, x + width - 1, y + HEIGHT - 1, withAlpha(PAPER, alpha));
        context.fill(x + 2, y + 2, x + width - 2, y + HEIGHT - 2, withAlpha(PAPER_LIGHT, alpha));
        context.fill(x + 2, y + HEIGHT - 4, x + width - 2, y + HEIGHT - 2, withAlpha(0xFFD2A764, alpha));
        context.fill(x + width - 3, y + 3, x + width - 2, y + HEIGHT - 3, withAlpha(0xFFC08A42, alpha));
    }

    private static void drawIcon(DrawContext context, int x, int y, float alpha) {
        if (customIconAvailable()) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
            context.drawTexture(ICON_TEXTURE, x, y, 0.0F, 0.0F, 16, 16, 16, 16);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            return;
        }

        context.drawItem(FALLBACK_ICON, x, y);
    }

    private static void playSound(MinecraftClient client, String configuredSound) {
        if (client.getSoundManager() == null) {
            return;
        }

        SoundEvent sound = resolveSound(configuredSound);
        client.getSoundManager().play(PositionedSoundInstance.master(sound, 1.12F, 0.35F));
    }

    private static SoundEvent resolveSound(String configuredSound) {
        Identifier id = Identifier.tryParse(configuredSound);
        if (id != null) {
            return Registries.SOUND_EVENT.getOrEmpty(id).orElse(SoundEvents.ITEM_BOOK_PAGE_TURN);
        }

        return SoundEvents.ITEM_BOOK_PAGE_TURN;
    }

    private static float currentAlpha() {
        int elapsed = Math.max(0, totalTicks - ticksRemaining);
        float fadeIn = Math.min(1.0F, elapsed / (float) FADE_TICKS);
        float fadeOut = Math.min(1.0F, ticksRemaining / (float) FADE_TICKS);
        return Math.min(fadeIn, fadeOut);
    }

    private static boolean customIconAvailable() {
        if (customIconAvailable == null) {
            customIconAvailable = MinecraftClient.getInstance().getResourceManager().getResource(ICON_TEXTURE).isPresent();
        }

        return customIconAvailable;
    }

    private static int withAlpha(int argb, float alpha) {
        int baseAlpha = (argb >>> 24) & 0xFF;
        int resolvedAlpha = Math.round(baseAlpha * clamp(alpha, 0.0F, 1.0F));
        return (resolvedAlpha << 24) | (argb & 0x00FFFFFF);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
