package com.worldremembers.deardiary.config;

import com.google.gson.JsonParseException;
import com.google.gson.JsonObject;
import com.worldremembers.deardiary.data.DiaryJson;
import com.worldremembers.deardiary.data.DiaryCategory;
import com.worldremembers.deardiary.event.AutomaticEventDefinition;
import com.worldremembers.deardiary.event.DearDiaryEventRegistry;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DearDiaryConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DearDiaryConfigManager.class);
    private static final String CONFIG_GUIDE_FILE = "CONFIG_GUIDE.md";
    private static final String EVENTS_GUIDE_FILE = "EVENTS.md";
    private static final String LEGACY_BACKUP_FILE = "dear_diary.legacy_backup.json";

    private final Path configPath;
    private DearDiaryConfig config = new DearDiaryConfig();

    public DearDiaryConfigManager(Path configPath) {
        this.configPath = configPath;
    }

    public void load() {
        try {
            Files.createDirectories(configPath.getParent());
            if (Files.notExists(configPath)) {
                config = new DearDiaryConfig();
                config.normalize();
                save();
                return;
            }

            try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                JsonObject root = DiaryJson.GSON.fromJson(reader, JsonObject.class);
                if (DearDiaryConfig.isLegacyFormat(root)) {
                    backupLegacyConfig();
                }
                config = DearDiaryConfig.fromJson(root);
                save();
            }
        } catch (IOException | JsonParseException exception) {
            LOGGER.error("Failed to load Dear Diary config, using defaults", exception);
            config = new DearDiaryConfig();
            config.normalize();
        }
    }

    public void save() {
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
                DiaryJson.GSON.toJson(config, writer);
            }
        } catch (IOException exception) {
            LOGGER.error("Failed to save Dear Diary config", exception);
        }
    }

    public void writeSupportFiles() {
        try {
            Files.createDirectories(configPath.getParent());
            Files.writeString(configGuidePath(), configGuideContent(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.writeString(eventsGuidePath(), eventsGuideContent(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException exception) {
            LOGGER.warn("Failed to write Dear Diary config guide files", exception);
        }
    }

    public DearDiaryConfig config() {
        return config;
    }

    public Path configPath() {
        return configPath;
    }

    public Path configGuidePath() {
        return configPath.getParent().resolve(CONFIG_GUIDE_FILE);
    }

    public Path eventsGuidePath() {
        return configPath.getParent().resolve(EVENTS_GUIDE_FILE);
    }

    private void backupLegacyConfig() {
        Path backupPath = configPath.getParent().resolve(LEGACY_BACKUP_FILE);
        if (Files.exists(backupPath)) {
            return;
        }

        try {
            Files.copy(configPath, backupPath, StandardCopyOption.COPY_ATTRIBUTES);
            LOGGER.info("Backed up legacy Dear Diary config to {}", backupPath);
        } catch (IOException exception) {
            LOGGER.warn("Failed to back up legacy Dear Diary config at {}", configPath, exception);
        }
    }

    private String configGuideContent() {
        return """
                # Dear Diary Server Config

                Main file: `dear_diary.json`

                Use this file to control manual entry limits, shared memory time, and automatic memories.

                ## Language

                - `language.defaultDiaryLanguage`: fallback language for automatic diary text when the player's client language is unknown.

                ## Manual entries

                - `manualEntries.maxTitleLength`: maximum title length for manual entries and edits.
                - `manualEntries.maxTextLength`: maximum text length for manual entries and edits.
                - `manualEntries.maxSharedTextLength`: maximum text length shown when a memory is shared to chat.
                - `manualEntries.sharedMemoryTimeZone`: timezone used for shared memories. Use `SERVER`, `UTC`, or an IANA timezone id such as `Europe/Moscow`.

                ## Automatic memories

                - `automaticMemories.enabled`: turns automatic memories on or off.
                - `automaticMemories.createOriginEntry`: creates the first origin memory. Existing origin memories are not duplicated.
                - `automaticMemories.minimumImportance`: hides automatic memories below the selected importance.
                - `automaticMemories.categories`: set a category to `false` to disable it.
                - `automaticMemories.disabledEvents`: exact event ids to disable.

                ## Importance values

                - `MINOR`
                - `NORMAL`
                - `MAJOR`
                - `LEGENDARY`

                ## Category keys

                - `beginning`
                - `discovery`
                - `exploration`
                - `combat`
                - `death`
                - `building`
                - `resources`
                - `pets`
                - `bosses`
                - `rare`
                - `other`

                ## Examples

                Disable death memories:

                ```json
                "categories": {
                  "death": false
                }
                ```

                Disable one event:

                ```json
                "disabledEvents": [
                  "minecraft:death_by_fall"
                ]
                ```

                ## Useful commands

                - `/deardiary config_status`: show the active config summary.
                - `/deardiary config_help`: show the paths to these guide files.
                - `/deardiary events_list`: list registered event ids in game.
                - `/deardiary events_list COMBAT`: list one category.
                - `/deardiary validate_events`: check automatic event definitions and lang keys.

                See `EVENTS.md` for the current automatic event ids.
                """;
    }

    private String eventsGuideContent() {
        StringBuilder builder = new StringBuilder();
        builder.append("# Dear Diary Automatic Events\n\n");
        builder.append("Use these ids in `automaticMemories.disabledEvents` in `dear_diary.json`.\n\n");
        builder.append("Each line shows:\n");
        builder.append("- event id\n");
        builder.append("- importance\n");
        builder.append("- trigger policy\n\n");

        for (DiaryCategory category : DiaryCategory.values()) {
            if (category == DiaryCategory.MANUAL) {
                continue;
            }

            List<AutomaticEventDefinition> definitions = DearDiaryEventRegistry.listByCategory(category).stream()
                    .sorted(Comparator.comparing(AutomaticEventDefinition::eventId))
                    .toList();
            if (definitions.isEmpty()) {
                continue;
            }

            builder.append("## ").append(category.name()).append("\n\n");
            for (AutomaticEventDefinition definition : definitions) {
                builder.append("- `")
                        .append(definition.eventId())
                        .append("` - ")
                        .append(definition.importance().name())
                        .append(", ")
                        .append(definition.triggerPolicy().name())
                        .append("\n");
            }
            builder.append("\n");
        }

        return builder.toString();
    }
}
