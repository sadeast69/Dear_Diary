package com.worldremembers.deardiary;

import com.worldremembers.deardiary.command.DearDiaryCommands;
import com.worldremembers.deardiary.compat.fabric.FabricCompatBootstrap;
import com.worldremembers.deardiary.config.DearDiaryConfigManager;
import com.worldremembers.deardiary.data.DiaryEntry;
import com.worldremembers.deardiary.data.PlayerDiary;
import com.worldremembers.deardiary.event.OriginEntryFactory;
import com.worldremembers.deardiary.event.VanillaDiaryEvents;
import com.worldremembers.deardiary.api.DearDiaryApi;
import com.worldremembers.deardiary.network.DearDiaryNetworking;
import com.worldremembers.deardiary.storage.DiaryBackupManager;
import com.worldremembers.deardiary.storage.JsonDiaryStorage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Random;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DearDiaryMod implements ModInitializer {
    public static final String MOD_ID = "dear_diary";
    public static final String MOD_NAME = "World Remembers: Dear Diary";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configPath = configDir.resolve("world_remembers").resolve("dear_diary").resolve(MOD_ID + ".json");
        migrateLegacyConfig(configDir.resolve(MOD_ID + ".json"), configPath);

        DearDiaryConfigManager configManager = new DearDiaryConfigManager(configPath);
        configManager.load();
        DearDiaryServices.setConfigManager(configManager);

        DearDiaryNetworking.register();
        VanillaDiaryEvents.register();
        FabricCompatBootstrap.register();
        configManager.writeSupportFiles();
        DearDiaryCommands.register();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            Path worldRoot = server.getSavePath(WorldSavePath.ROOT);
            JsonDiaryStorage storage = new JsonDiaryStorage(worldRoot.resolve("data").resolve(MOD_ID).resolve("players"));
            storage.initialize();
            DearDiaryServices.setStorage(storage);
            LOGGER.info("Dear Diary storage initialized at {}", worldRoot.resolve("data").resolve(MOD_ID));
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            maybeCreateOriginEntry(handler.player);
            DearDiaryNetworking.sendDiarySnapshot(handler.player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                DiaryBackupManager.backupPlayerDiaryOnLogout(handler.player.getUuid()));

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> DearDiaryServices.saveAll());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> DearDiaryServices.clearStorage());
    }

    private static void migrateLegacyConfig(Path legacyPath, Path configPath) {
        if (Files.exists(configPath) || Files.notExists(legacyPath)) {
            return;
        }

        try {
            Files.createDirectories(configPath.getParent());
            Files.copy(legacyPath, configPath);
            LOGGER.info("Migrated Dear Diary config from {} to {}", legacyPath, configPath);
        } catch (IOException exception) {
            LOGGER.warn("Failed to migrate Dear Diary config from {} to {}", legacyPath, configPath, exception);
        }
    }

    private static void maybeCreateOriginEntry(ServerPlayerEntity player) {
        if (!DearDiaryServices.config().shouldCreateOriginEntry()) {
            return;
        }

        PlayerDiary diary = DearDiaryApi.getDiary(player);
        if (diary.hasEntryWithEventType(OriginEntryFactory.EVENT_TYPE)) {
            if (diary.automaticEventState().markTriggeredEvent(OriginEntryFactory.EVENT_TYPE)) {
                DearDiaryServices.storage().save(player.getUuid());
            }
            return;
        }

        Random random = new Random(player.getUuid().getMostSignificantBits() ^ player.getWorld().getTime());
        Optional<DiaryEntry> entry = DearDiaryApi.createAutomaticEntry(player, OriginEntryFactory.create(random));
        entry.ifPresent(created -> DearDiaryNetworking.sendAutomaticEntryNotice(player, created));
    }
}
