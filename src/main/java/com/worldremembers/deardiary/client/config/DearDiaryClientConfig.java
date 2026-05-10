package com.worldremembers.deardiary.client.config;

import com.google.gson.JsonParseException;
import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.data.DiaryJson;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;

public final class DearDiaryClientConfig {
    public static final int DEFAULT_ANCHOR_OFFSET = 112;
    private static final int DEFAULT_NOTIFICATION_DURATION_TICKS = 80;
    private static final String DEFAULT_NOTIFICATION_SOUND = "minecraft:item.book.page_turn";

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("world_remembers")
            .resolve("dear_diary")
            .resolve("client.json");

    private static DearDiaryClientConfig config = new DearDiaryClientConfig();
    private static boolean loaded;

    private boolean showInventoryButton = true;
    private int inventoryButtonX;
    private int inventoryButtonY;
    private boolean useRelativeInventoryButtonPosition = true;
    private boolean inventoryButtonPositionSet;
    private boolean attachedToInventory = true;
    private InventoryAnchorSide inventoryAnchorSide = InventoryAnchorSide.RIGHT;
    private int inventoryAnchorOffset = DEFAULT_ANCHOR_OFFSET;
    private int freeButtonX;
    private int freeButtonY;
    private boolean freeButtonPositionSet;
    private Boolean showNewEntryNotification = true;
    private Boolean playNewEntrySound = true;
    private Integer notificationDurationTicks = DEFAULT_NOTIFICATION_DURATION_TICKS;
    private String notificationSound = DEFAULT_NOTIFICATION_SOUND;
    private String diaryListSortMode = DiaryListSortMode.DATE_DESC.name();

    private DearDiaryClientConfig() {
    }

    public static DearDiaryClientConfig get() {
        load();
        return config;
    }

    public static Path configPath() {
        return CONFIG_PATH;
    }

    public static void load() {
        if (loaded) {
            return;
        }

        loaded = true;
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            if (Files.notExists(CONFIG_PATH)) {
                config = new DearDiaryClientConfig();
                save();
                return;
            }

            try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                DearDiaryClientConfig loadedConfig = DiaryJson.GSON.fromJson(reader, DearDiaryClientConfig.class);
                config = loadedConfig == null ? new DearDiaryClientConfig() : loadedConfig;
            }
        } catch (IOException | JsonParseException exception) {
            DearDiaryMod.LOGGER.error("Failed to load Dear Diary client config, using defaults", exception);
            config = new DearDiaryClientConfig();
        }

        config.normalize();
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                DiaryJson.GSON.toJson(config, writer);
            }
        } catch (IOException exception) {
            DearDiaryMod.LOGGER.error("Failed to save Dear Diary client config", exception);
        }
    }

    public static void resetInventoryButtonPosition() {
        DearDiaryClientConfig current = get();
        current.attachedToInventory = true;
        current.inventoryAnchorSide = InventoryAnchorSide.RIGHT;
        current.inventoryAnchorOffset = DEFAULT_ANCHOR_OFFSET;
        current.inventoryButtonX = 0;
        current.inventoryButtonY = 0;
        current.inventoryButtonPositionSet = false;
        current.freeButtonX = 0;
        current.freeButtonY = 0;
        current.freeButtonPositionSet = false;
        save();
    }

    public boolean showInventoryButton() {
        return showInventoryButton;
    }

    public boolean attachedToInventory() {
        return attachedToInventory;
    }

    public boolean showNewEntryNotification() {
        return showNewEntryNotification == null || showNewEntryNotification;
    }

    public boolean playNewEntrySound() {
        return playNewEntrySound == null || playNewEntrySound;
    }

    public int notificationDurationTicks() {
        return clamp(
                notificationDurationTicks == null ? DEFAULT_NOTIFICATION_DURATION_TICKS : notificationDurationTicks,
                20,
                200
        );
    }

    public String notificationSound() {
        if (notificationSound == null || notificationSound.isBlank()) {
            return DEFAULT_NOTIFICATION_SOUND;
        }

        return notificationSound;
    }

    public DiaryListSortMode diaryListSortMode() {
        return DiaryListSortMode.parse(diaryListSortMode);
    }

    public void saveDiaryListSortMode(DiaryListSortMode mode) {
        diaryListSortMode = (mode == null ? DiaryListSortMode.DATE_DESC : mode).name();
        save();
    }

    public InventoryAnchorSide inventoryAnchorSide() {
        return inventoryAnchorSide == null ? InventoryAnchorSide.RIGHT : inventoryAnchorSide;
    }

    public InventoryButtonPosition inventoryButtonPosition(
            int inventoryX,
            int inventoryY,
            int inventoryWidth,
            int inventoryHeight,
            int screenWidth,
            int screenHeight,
            int buttonSize,
            int gap
    ) {
        if (attachedToInventory) {
            return attachedPosition(inventoryX, inventoryY, inventoryWidth, inventoryHeight, screenWidth, screenHeight, buttonSize, gap);
        }

        int defaultX = defaultFreeX(inventoryX, inventoryWidth, screenWidth, buttonSize, gap);
        int defaultY = defaultFreeY(inventoryY, screenHeight, buttonSize);
        int rawX = freeButtonPositionSet ? freeButtonX : positionValue(defaultX, inventoryButtonX);
        int rawY = freeButtonPositionSet ? freeButtonY : positionValue(defaultY, inventoryButtonY);
        return new InventoryButtonPosition(
                clamp(rawX, 0, Math.max(0, screenWidth - buttonSize)),
                clamp(rawY, 0, Math.max(0, screenHeight - buttonSize))
        );
    }

    public void saveAttachedInventoryButtonPosition(InventoryAnchorSide side, int offset, int inventoryWidth, int inventoryHeight, int buttonSize) {
        attachedToInventory = true;
        inventoryAnchorSide = side == null ? InventoryAnchorSide.RIGHT : side;
        inventoryAnchorOffset = clampAnchorOffset(inventoryAnchorSide, offset, inventoryWidth, inventoryHeight, buttonSize);
        save();
    }

    public void saveFreeInventoryButtonPosition(int x, int y, int screenWidth, int screenHeight, int buttonSize) {
        int clampedX = clamp(x, 0, Math.max(0, screenWidth - buttonSize));
        int clampedY = clamp(y, 0, Math.max(0, screenHeight - buttonSize));
        attachedToInventory = false;
        freeButtonX = clampedX;
        freeButtonY = clampedY;
        freeButtonPositionSet = true;
        inventoryButtonX = clampedX;
        inventoryButtonY = clampedY;
        inventoryButtonPositionSet = true;
        save();
    }

    private int positionValue(int defaultValue, int configuredValue) {
        if (!inventoryButtonPositionSet) {
            return defaultValue;
        }

        return useRelativeInventoryButtonPosition ? defaultValue + configuredValue : configuredValue;
    }

    private void normalize() {
        if (inventoryAnchorSide == null) {
            inventoryAnchorSide = InventoryAnchorSide.RIGHT;
        }
        inventoryButtonX = clamp(inventoryButtonX, -10000, 10000);
        inventoryButtonY = clamp(inventoryButtonY, -10000, 10000);
        freeButtonX = clamp(freeButtonX, -10000, 10000);
        freeButtonY = clamp(freeButtonY, -10000, 10000);
        inventoryAnchorOffset = clamp(inventoryAnchorOffset, -10000, 10000);
        if (showNewEntryNotification == null) {
            showNewEntryNotification = true;
        }
        if (playNewEntrySound == null) {
            playNewEntrySound = true;
        }
        notificationDurationTicks = notificationDurationTicks();
        if (notificationSound == null || notificationSound.isBlank()) {
            notificationSound = DEFAULT_NOTIFICATION_SOUND;
        }
        diaryListSortMode = DiaryListSortMode.parse(diaryListSortMode).name();
    }

    private InventoryButtonPosition attachedPosition(
            int inventoryX,
            int inventoryY,
            int inventoryWidth,
            int inventoryHeight,
            int screenWidth,
            int screenHeight,
            int buttonSize,
            int gap
    ) {
        int offset = clampAnchorOffset(inventoryAnchorSide, inventoryAnchorOffset, inventoryWidth, inventoryHeight, buttonSize);
        int x = switch (inventoryAnchorSide) {
            case LEFT -> inventoryX - buttonSize - gap;
            case RIGHT -> inventoryX + inventoryWidth + gap;
            case TOP, BOTTOM -> inventoryX + offset;
        };
        int y = switch (inventoryAnchorSide) {
            case LEFT, RIGHT -> inventoryY + offset;
            case TOP -> inventoryY - buttonSize - gap;
            case BOTTOM -> inventoryY + inventoryHeight + gap;
        };
        return new InventoryButtonPosition(
                clamp(x, 0, Math.max(0, screenWidth - buttonSize)),
                clamp(y, 0, Math.max(0, screenHeight - buttonSize))
        );
    }

    private static int defaultFreeX(int inventoryX, int inventoryWidth, int screenWidth, int buttonSize, int gap) {
        int x = inventoryX + inventoryWidth + gap;
        if (x + buttonSize > screenWidth) {
            x = inventoryX - buttonSize - gap;
        }

        return clamp(x, 0, Math.max(0, screenWidth - buttonSize));
    }

    private static int defaultFreeY(int inventoryY, int screenHeight, int buttonSize) {
        return clamp(inventoryY + DEFAULT_ANCHOR_OFFSET, 0, Math.max(0, screenHeight - buttonSize));
    }

    private static int clampAnchorOffset(InventoryAnchorSide side, int offset, int inventoryWidth, int inventoryHeight, int buttonSize) {
        int length = switch (side) {
            case LEFT, RIGHT -> inventoryHeight;
            case TOP, BOTTOM -> inventoryWidth;
        };
        return clamp(offset, 0, Math.max(0, length - buttonSize));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public enum InventoryAnchorSide {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM
    }

    public enum DiaryListSortMode {
        DATE_DESC,
        IMPORTANCE_DESC;

        public DiaryListSortMode next() {
            return this == DATE_DESC ? IMPORTANCE_DESC : DATE_DESC;
        }

        private static DiaryListSortMode parse(String value) {
            if (value == null || value.isBlank()) {
                return DATE_DESC;
            }

            try {
                return DiaryListSortMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                return DATE_DESC;
            }
        }
    }

    public record InventoryButtonPosition(int x, int y) {
    }
}
