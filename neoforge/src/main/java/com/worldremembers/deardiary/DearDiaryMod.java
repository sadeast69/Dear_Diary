package com.worldremembers.deardiary;

import com.worldremembers.deardiary.api.DearDiaryApi;
import com.worldremembers.deardiary.command.DearDiaryCommands;
import com.worldremembers.deardiary.compat.neoforge.NeoForgeCompatBootstrap;
import com.worldremembers.deardiary.config.DearDiaryConfigManager;
import com.worldremembers.deardiary.data.DiaryEntry;
import com.worldremembers.deardiary.data.PlayerDiary;
import com.worldremembers.deardiary.event.OriginEntryFactory;
import com.worldremembers.deardiary.event.VanillaDiaryEvents;
import com.worldremembers.deardiary.network.DearDiaryNetworking;
import com.worldremembers.deardiary.storage.DiaryBackupManager;
import com.worldremembers.deardiary.storage.JsonDiaryStorage;
import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Random;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(DearDiaryMod.MOD_ID)
public final class DearDiaryMod {
    public static final String MOD_ID = "dear_diary";
    public static final String MOD_NAME = "World Remembers: Dear Diary";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public DearDiaryMod(IEventBus modEventBus) {
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path configPath = configDir.resolve("world_remembers").resolve("dear_diary").resolve(MOD_ID + ".json");
        migrateLegacyConfig(configDir.resolve(MOD_ID + ".json"), configPath);

        DearDiaryConfigManager configManager = new DearDiaryConfigManager(configPath);
        configManager.load();
        DearDiaryServices.setConfigManager(configManager);
        VanillaDiaryEvents.register();
        NeoForgeCompatBootstrap.register();
        configManager.writeSupportFiles();

        DearDiaryNetworking.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(DearDiaryCommands::register);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
        NeoForge.EVENT_BUS.addListener(this::onServerStopped);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedOut);
        registerClientOnly(modEventBus);

        LOGGER.info("{} NeoForge foundation initialized", MOD_NAME);
    }

    private static void registerClientOnly(IEventBus modEventBus) {
        if (!FMLEnvironment.dist.isClient()) {
            return;
        }

        try {
            Class.forName("com.worldremembers.deardiary.client.DearDiaryClientMod")
                    .getMethod("register", IEventBus.class)
                    .invoke(null, modEventBus);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException exception) {
            throw new IllegalStateException("Failed to initialize Dear Diary NeoForge client hooks", exception);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("Failed to initialize Dear Diary NeoForge client hooks", exception.getCause());
        }
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

    private void onServerStarting(ServerStartingEvent event) {
        Path worldRoot = event.getServer().getWorldPath(LevelResource.ROOT);
        JsonDiaryStorage storage = new JsonDiaryStorage(worldRoot.resolve("data").resolve(MOD_ID).resolve("players"));
        storage.initialize();
        DearDiaryServices.setStorage(storage);
        LOGGER.info("Dear Diary storage initialized at {}", worldRoot.resolve("data").resolve(MOD_ID));
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            maybeCreateOriginEntry(player);
            DearDiaryNetworking.sendDiarySnapshot(player);
        }
    }

    private void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DiaryBackupManager.backupPlayerDiaryOnLogout(player.getUUID());
        }
    }

    private void onServerStopping(ServerStoppingEvent event) {
        DearDiaryServices.saveAll();
    }

    private void onServerStopped(ServerStoppedEvent event) {
        DearDiaryServices.clearStorage();
    }

    private static void maybeCreateOriginEntry(ServerPlayer player) {
        if (!DearDiaryServices.config().shouldCreateOriginEntry()) {
            return;
        }

        PlayerDiary diary = DearDiaryApi.getDiary(player);
        if (diary.hasEntryWithEventType(OriginEntryFactory.EVENT_TYPE)) {
            if (diary.automaticEventState().markTriggeredEvent(OriginEntryFactory.EVENT_TYPE)) {
                DearDiaryServices.storage().save(player.getUUID());
            }
            return;
        }

        Random random = new Random(player.getUUID().getMostSignificantBits() ^ player.serverLevel().getGameTime());
        Optional<DiaryEntry> entry = DearDiaryApi.createAutomaticEntry(player, OriginEntryFactory.create(random));
        entry.ifPresent(created -> DearDiaryNetworking.sendAutomaticEntryNotice(player, created));
    }
}
