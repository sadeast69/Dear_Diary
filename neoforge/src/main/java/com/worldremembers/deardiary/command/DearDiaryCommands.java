package com.worldremembers.deardiary.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.DearDiaryServices;
import com.worldremembers.deardiary.api.AutomaticEntryRequest;
import com.worldremembers.deardiary.api.DearDiaryApi;
import com.worldremembers.deardiary.config.DearDiaryConfig;
import com.worldremembers.deardiary.data.AutomaticEventState;
import com.worldremembers.deardiary.data.DiaryCategory;
import com.worldremembers.deardiary.data.DiaryEntry;
import com.worldremembers.deardiary.data.DiaryEntryKind;
import com.worldremembers.deardiary.data.DiaryImportance;
import com.worldremembers.deardiary.data.PlayerDiary;
import com.worldremembers.deardiary.event.AutomaticDiaryEvents;
import com.worldremembers.deardiary.event.AutomaticEventDefinition;
import com.worldremembers.deardiary.event.AutomaticEventRegistryValidator;
import com.worldremembers.deardiary.event.DearDiaryEventRegistry;
import com.worldremembers.deardiary.event.VanillaDiaryEventDefinitions;
import com.worldremembers.deardiary.export.MarkdownDiaryExporter;
import com.worldremembers.deardiary.localization.DiaryLocalization;
import com.worldremembers.deardiary.network.DearDiaryNetworking;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class DearDiaryCommands {
    private static final List<String> COUNTER_SUGGESTIONS = List.of(
            VanillaDiaryEventDefinitions.COUNTER_MOB_KILLS,
            VanillaDiaryEventDefinitions.COUNTER_BLOCKS_MINED,
            VanillaDiaryEventDefinitions.COUNTER_BLOCKS_PLACED
    );

    private DearDiaryCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("deardiary")
                .then(opCommand("add_test")
                        .executes(context -> addTest(context.getSource())))
                .then(opCommand("add_manual_test")
                        .executes(context -> addManualTest(context.getSource())))
                .then(opCommand("clear_self")
                        .executes(context -> clearSelf(context.getSource())))
                .then(opCommand("dump")
                        .executes(context -> dump(context.getSource())))
                .then(opCommand("config_status")
                        .executes(context -> configStatus(context.getSource())))
                .then(opCommand("config_help")
                        .executes(context -> configHelp(context.getSource())))
                .then(opCommand("events_list")
                        .executes(context -> eventsList(context.getSource(), null))
                        .then(Commands.argument("category", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(categorySuggestions(), builder))
                                .executes(context -> eventsList(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "category")
                                ))))
                .then(opCommand("validate_events")
                        .executes(context -> validateEvents(context.getSource())))
                .then(opCommand("trigger_test")
                        .then(Commands.argument("event_id", ResourceLocationArgument.id())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(DearDiaryEventRegistry.eventIds(), builder))
                                .executes(context -> triggerTest(
                                        context.getSource(),
                                        ResourceLocationArgument.getId(context, "event_id").toString(),
                                        false
                                ))
                                .then(Commands.literal("force")
                                        .executes(context -> triggerTest(
                                                context.getSource(),
                                                ResourceLocationArgument.getId(context, "event_id").toString(),
                                                true
                                        )))))
                .then(opCommand("event_state")
                        .executes(context -> eventState(context.getSource())))
                .then(opCommand("add_counter")
                        .then(Commands.argument("counter", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(COUNTER_SUGGESTIONS, builder))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> addCounter(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "counter"),
                                                IntegerArgumentType.getInteger(context, "amount")
                                        )))))
                .then(opCommand("relocalize_self")
                        .then(Commands.argument("locale", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(List.of("ru_ru", "en_us"), builder))
                                .executes(context -> relocalizeSelf(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "locale")
                                ))))
                .then(Commands.literal("open")
                        .executes(context -> open(context.getSource(), false))
                        .then(Commands.literal("new")
                                .executes(context -> open(context.getSource(), true))))
                .then(Commands.literal("list")
                        .executes(context -> list(context.getSource())))
                .then(Commands.literal("chapter")
                        .then(Commands.argument("title", StringArgumentType.greedyString())
                                .executes(context -> addChapter(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "title")
                                ))))
                .then(Commands.literal("export")
                        .then(Commands.literal("markdown")
                                .executes(context -> exportMarkdown(context.getSource()))))
                .then(opCommand("edit_last")
                        .then(Commands.argument("title", StringArgumentType.string())
                                .then(Commands.argument("text", StringArgumentType.greedyString())
                                        .executes(context -> editLast(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "title"),
                                                StringArgumentType.getString(context, "text")
                                        )))))
                .then(opCommand("delete_last")
                        .executes(context -> deleteLast(context.getSource())))
                .then(opCommand("favorite_last")
                        .executes(context -> favoriteLast(context.getSource())))
                .then(opCommand("share_last")
                        .executes(context -> shareLast(context.getSource()))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> opCommand(String name) {
        return Commands.literal(name).requires(DearDiaryCommands::hasOperatorPermission);
    }

    private static boolean hasOperatorPermission(CommandSourceStack source) {
        return source.hasPermission(2);
    }

    private static int addTest(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        AutomaticEntryRequest request = AutomaticEntryRequest.builder("dear_diary:test_memory", DearDiaryMod.MOD_ID)
                .category(DiaryCategory.DISCOVERY)
                .importance(DiaryImportance.NORMAL)
                .titleKey("entry.dear_diary.test_automatic.title")
                .textKey("entry.dear_diary.test_automatic.text")
                .resolvedTitle("A glint in the dark")
                .resolvedText("Today the road offered a small impossible sparkle. I am choosing to take that personally.")
                .icon("minecraft:diamond")
                .includeLocation(true)
                .shareable(true)
                .build();

        Optional<DiaryEntry> entry = DearDiaryApi.createAutomaticEntry(player, request);
        if (entry.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.add_test.disabled"), false);
            return 0;
        }

        source.sendSuccess(
                () -> Component.translatable("commands.dear_diary.add_test.success", entry.get().getId().toString()),
                false
        );
        DearDiaryNetworking.sendDiarySnapshot(player);
        DearDiaryNetworking.sendAutomaticEntryNotice(player, entry.get());
        return 1;
    }

    private static int addManualTest(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        DiaryEntry entry = DearDiaryApi.createManualEntry(
                player,
                "A note in the margin",
                "I wrote this one myself. The page did not complain, which is encouraging.",
                true
        );

        source.sendSuccess(
                () -> Component.translatable("commands.dear_diary.add_manual_test.success", entry.getId().toString()),
                false
        );
        DearDiaryNetworking.sendDiarySnapshot(player);
        return 1;
    }

    private static int clearSelf(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        DearDiaryApi.clearDiary(player);
        source.sendSuccess(() -> Component.translatable("commands.dear_diary.clear_self.success"), false);
        DearDiaryNetworking.sendDiarySnapshot(player);
        return 1;
    }

    private static int dump(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        PlayerDiary diary = DearDiaryApi.getDiary(player);
        int count = diary.entriesView().size();
        Path file = DearDiaryServices.storage().getPlayerFile(player.getUUID());

        source.sendSuccess(() -> Component.translatable("commands.dear_diary.dump.summary", count), false);
        DearDiaryMod.LOGGER.info(
                "Diary dump for {} ({}): {} entries stored at {}",
                player.getName().getString(),
                player.getUUID(),
                count,
                file
        );
        return count;
    }

    private static int exportMarkdown(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.export.markdown.player_only"), false);
            return 0;
        }

        PlayerDiary diary = DearDiaryApi.getDiary(player);
        if (diary.entriesView().isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.export.markdown.empty"), false);
            return 0;
        }

        try {
            MarkdownDiaryExporter.ExportResult result = MarkdownDiaryExporter.export(player, diary);
            source.sendSuccess(
                    () -> Component.translatable("commands.dear_diary.export.markdown.success", result.relativePath()),
                    false
            );
            return result.entryCount();
        } catch (IOException exception) {
            DearDiaryMod.LOGGER.error("Failed to export Dear Diary markdown for {}", player.getUUID(), exception);
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.export.markdown.failed"), false);
            return 0;
        }
    }

    private static int addChapter(CommandSourceStack source, String rawTitle) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.chapter.player_only"), false);
            return 0;
        }

        String title = rawTitle == null ? "" : rawTitle.strip();
        if (title.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.chapter.empty_title"), false);
            return 0;
        }

        if (title.length() > DearDiaryServices.config().maxManualTitleLength()) {
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.chapter.title_too_long"), false);
            return 0;
        }

        DiaryEntry entry = DearDiaryApi.createChapterEntry(player, title);
        DearDiaryNetworking.sendDiarySnapshot(player);
        source.sendSuccess(() -> Component.translatable("commands.dear_diary.chapter.created", entry.getResolvedTitle()), false);
        return 1;
    }

    private static int configStatus(CommandSourceStack source) {
        DearDiaryConfig config = DearDiaryServices.config();
        source.sendSuccess(
                () -> Component.translatable("commands.dear_diary.config_status.path", DearDiaryServices.configPath().toString()),
                false
        );
        source.sendSuccess(
                () -> Component.translatable(
                        "commands.dear_diary.config_status",
                        config.version(),
                        config.enableAutomaticEntries(),
                        config.enableOriginEntry(),
                        config.minAutomaticImportance().name(),
                        config.defaultDiaryLanguage(),
                        config.sharedMemoryTimeZone()
                ),
                false
        );
        source.sendSuccess(
                () -> Component.translatable(
                        "commands.dear_diary.config_status.categories",
                        listOrNone(config.disabledAutomaticCategories())
                ),
                false
        );
        source.sendSuccess(
                () -> Component.translatable(
                        "commands.dear_diary.config_status.disabled_events",
                        config.disabledAutomaticEvents().size(),
                        sample(config.disabledAutomaticEvents(), 8),
                        DearDiaryEventRegistry.getAllDefinitions().size()
                ),
                false
        );
        source.sendSuccess(
                () -> Component.translatable(
                        "commands.dear_diary.config_status.guides",
                        DearDiaryServices.configGuidePath().toString(),
                        DearDiaryServices.eventsGuidePath().toString()
                ),
                false
        );
        List<String> invalidCategories = invalidCategorySample(config);
        if (!invalidCategories.isEmpty()) {
            source.sendSuccess(
                    () -> Component.translatable("commands.dear_diary.config_status.invalid_categories", String.join(", ", invalidCategories)),
                    false
            );
        }
        List<String> unknownEvents = unknownDisabledEvents(config);
        if (!unknownEvents.isEmpty()) {
            source.sendSuccess(
                    () -> Component.translatable("commands.dear_diary.config_status.unknown_events", sample(unknownEvents, 8)),
                    false
            );
        }
        source.sendSuccess(() -> Component.translatable("commands.dear_diary.config_status.hint"), false);
        return 1;
    }

    private static int configHelp(CommandSourceStack source) {
        source.sendSuccess(
                () -> Component.translatable("commands.dear_diary.config_help.path", DearDiaryServices.configPath().toString()),
                false
        );
        source.sendSuccess(
                () -> Component.translatable("commands.dear_diary.config_help.guide", DearDiaryServices.configGuidePath().toString()),
                false
        );
        source.sendSuccess(
                () -> Component.translatable("commands.dear_diary.config_help.events_file", DearDiaryServices.eventsGuidePath().toString()),
                false
        );
        source.sendSuccess(() -> Component.translatable("commands.dear_diary.config_help.events"), false);
        return 1;
    }

    private static int eventsList(CommandSourceStack source, String rawCategory) {
        List<AutomaticEventDefinition> definitions;
        String categoryName = rawCategory == null ? "ALL" : rawCategory.strip().toUpperCase();
        if (rawCategory == null || rawCategory.isBlank()) {
            definitions = DearDiaryEventRegistry.getAllDefinitions().stream()
                    .sorted(Comparator.comparing(AutomaticEventDefinition::eventId))
                    .toList();
        } else {
            DiaryCategory category;
            try {
                category = DiaryCategory.valueOf(categoryName);
            } catch (IllegalArgumentException exception) {
                source.sendSuccess(() -> Component.translatable("commands.dear_diary.events_list.unknown_category", rawCategory), false);
                return 0;
            }

            definitions = DearDiaryEventRegistry.listByCategory(category);
        }

        String sample = definitions.stream()
                .map(AutomaticEventDefinition::eventId)
                .limit(12)
                .reduce((left, right) -> left + ", " + right)
                .orElse("-");
        source.sendSuccess(
                () -> Component.translatable("commands.dear_diary.events_list.summary", definitions.size(), categoryName, sample),
                false
        );
        return definitions.size();
    }

    private static int validateEvents(CommandSourceStack source) {
        AutomaticEventRegistryValidator.Result result = AutomaticEventRegistryValidator.validate();
        source.sendSuccess(
                () -> Component.translatable(
                        "commands.dear_diary.validate_events.summary",
                        result.totalEvents(),
                        result.definitionOnlyCount(),
                        result.missingKeysCount(),
                        result.duplicateIdsCount(),
                        result.problemCount()
                ),
                false
        );
        if (result.ok()) {
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.validate_events.ok"), false);
            return 1;
        }

        for (String problem : result.problemSample(8)) {
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.validate_events.problem", problem), false);
        }
        return 0;
    }

    private static int triggerTest(CommandSourceStack source, String eventId, boolean force) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (DearDiaryEventRegistry.get(eventId).isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.trigger_test.unknown", eventId), false);
            return 0;
        }

        Optional<DiaryEntry> entry = AutomaticDiaryEvents.trigger(player, eventId, force);
        if (entry.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.trigger_test.blocked", eventId), false);
            return 0;
        }

        DearDiaryNetworking.sendDiarySnapshot(player);
        DearDiaryNetworking.sendAutomaticEntryNotice(player, entry.get());
        source.sendSuccess(
                () -> Component.translatable("commands.dear_diary.trigger_test.success", eventId, shortId(entry.get())),
                false
        );
        return 1;
    }

    private static int eventState(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        AutomaticEventState state = DearDiaryApi.getDiary(player).automaticEventState();
        long nowMillis = System.currentTimeMillis();
        source.sendSuccess(
                () -> Component.translatable(
                        "commands.dear_diary.event_state.summary",
                        state.triggeredEventCount(),
                        state.activeCooldownCount(nowMillis),
                        state.counterCount()
                ),
                false
        );
        if (!state.triggeredEventsView().isEmpty()) {
            String sample = String.join(", ", state.triggeredEventsView().stream().limit(8).toList());
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.event_state.triggered", sample), false);
        }
        if (!state.cooldownsView().isEmpty()) {
            String sample = cooldownSample(state.cooldownsView(), nowMillis);
            if (!sample.isBlank()) {
                source.sendSuccess(() -> Component.translatable("commands.dear_diary.event_state.cooldowns", sample), false);
            }
        }
        if (!state.countersView().isEmpty()) {
            String sample = counterSample(state.countersView());
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.event_state.counters", sample), false);
        }
        return state.triggeredEventCount();
    }

    private static int addCounter(CommandSourceStack source, String counter, int amount) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        List<DiaryEntry> entries = AutomaticDiaryEvents.incrementCounterAndTriggerMilestones(player, counter, amount);
        if (!entries.isEmpty()) {
            DearDiaryNetworking.sendDiarySnapshot(player);
            for (DiaryEntry entry : entries) {
                DearDiaryNetworking.sendAutomaticEntryNotice(player, entry);
            }
        }

        int value = DearDiaryApi.getDiary(player).automaticEventState().countersView().getOrDefault(counter, 0);
        source.sendSuccess(
                () -> Component.translatable("commands.dear_diary.add_counter.success", counter, amount, value, entries.size()),
                false
        );
        return entries.size();
    }

    private static int relocalizeSelf(CommandSourceStack source, String requestedLocale) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        String locale = DiaryLocalization.normalizeLocale(requestedLocale);
        PlayerDiary diary = DearDiaryApi.getDiary(player);
        int updated = 0;

        for (DiaryEntry entry : diary.entriesView()) {
            if (entry.getEntryKind() != DiaryEntryKind.AUTOMATIC || isBlank(entry.getTitleKey()) || isBlank(entry.getTextKey())) {
                continue;
            }

            String title = DiaryLocalization.resolve(locale, entry.getTitleKey(), entry.getResolvedTitle());
            String text = DiaryLocalization.resolve(locale, entry.getTextKey(), entry.getResolvedText());
            if (!title.equals(entry.getResolvedTitle()) || !text.equals(entry.getResolvedText())) {
                DearDiaryServices.storage().updateEntryText(player.getUUID(), entry.getId(), title, text);
                updated++;
            }
        }

        DearDiaryNetworking.sendDiarySnapshot(player);
        int updatedCount = updated;
        source.sendSuccess(
                () -> Component.translatable("commands.dear_diary.relocalize_self.success", updatedCount, locale),
                false
        );
        return updated;
    }

    private static int open(CommandSourceStack source, boolean newEntry) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        boolean sent = DearDiaryNetworking.openDiaryScreen(player, newEntry);
        if (!sent) {
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.open.unavailable"), false);
            return 0;
        }

        source.sendSuccess(() -> Component.translatable("commands.dear_diary.open.success"), false);
        return 1;
    }

    private static int list(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        List<DiaryEntry> entries = DearDiaryApi.getDiary(player).entriesView();
        if (entries.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.list.empty"), false);
            return 0;
        }

        int shown = Math.min(5, entries.size());
        source.sendSuccess(() -> Component.translatable("commands.dear_diary.list.header", shown, entries.size()), false);
        for (int index = entries.size() - 1; index >= entries.size() - shown; index--) {
            DiaryEntry entry = entries.get(index);
            source.sendSuccess(
                    () -> Component.translatable(
                            "commands.dear_diary.list.entry",
                            shortId(entry),
                            entry.getEntryKind().serializedName(),
                            entry.isFavorite(),
                            entry.getResolvedTitle()
                    ),
                    false
            );
        }

        return shown;
    }

    private static int editLast(CommandSourceStack source, String title, String text) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<DiaryEntry> entry = DearDiaryApi.getDiary(player).findLastEntry();
        if (entry.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.error.not_found"), false);
            return 0;
        }

        if (!entry.get().isEditable()) {
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.error.not_editable"), false);
            return 0;
        }

        if (!DearDiaryApi.editEntryText(player, entry.get().getId(), title, text)) {
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.error.edit_failed"), false);
            return 0;
        }

        source.sendSuccess(() -> Component.translatable("commands.dear_diary.edit_last.success", shortId(entry.get())), false);
        DearDiaryNetworking.sendDiarySnapshot(player);
        return 1;
    }

    private static int deleteLast(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<DiaryEntry> entry = DearDiaryApi.getDiary(player).findLastEntry();
        if (entry.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.error.not_found"), false);
            return 0;
        }

        if (!DearDiaryApi.deleteEntry(player, entry.get().getId())) {
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.error.not_found"), false);
            return 0;
        }

        source.sendSuccess(() -> Component.translatable("commands.dear_diary.delete_last.success", shortId(entry.get())), false);
        DearDiaryNetworking.sendDiarySnapshot(player);
        return 1;
    }

    private static int favoriteLast(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<DiaryEntry> entry = DearDiaryApi.getDiary(player).findLastEntry();
        if (entry.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.error.not_found"), false);
            return 0;
        }

        boolean favorite = !entry.get().isFavorite();
        if (!DearDiaryApi.toggleFavorite(player, entry.get().getId())) {
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.error.not_found"), false);
            return 0;
        }

        source.sendSuccess(
                () -> Component.translatable("commands.dear_diary.favorite_last.success", shortId(entry.get()), favorite),
                false
        );
        DearDiaryNetworking.sendDiarySnapshot(player);
        return 1;
    }

    private static int shareLast(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<DiaryEntry> entry = DearDiaryApi.getDiary(player).findLastEntry();
        if (entry.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.error.not_found"), false);
            return 0;
        }

        if (!DearDiaryApi.shareEntryToChat(player, entry.get().getId())) {
            source.sendSuccess(() -> Component.translatable("commands.dear_diary.error.share_failed"), false);
            return 0;
        }

        source.sendSuccess(() -> Component.translatable("commands.dear_diary.share_last.success", shortId(entry.get())), false);
        DearDiaryNetworking.sendDiarySnapshot(player);
        return 1;
    }

    private static String shortId(DiaryEntry entry) {
        return entry.getId().toString().substring(0, 8);
    }

    private static String counterSample(Map<String, Integer> counters) {
        return String.join(", ", counters.entrySet().stream()
                .limit(8)
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .toList());
    }

    private static String cooldownSample(Map<String, Long> cooldowns, long nowMillis) {
        return String.join(", ", cooldowns.entrySet().stream()
                .filter(entry -> entry.getValue() > nowMillis)
                .limit(8)
                .map(entry -> entry.getKey() + "=" + Math.max(0L, (entry.getValue() - nowMillis) / 1000L) + "s")
                .toList());
    }

    private static List<String> categorySuggestions() {
        return Stream.of(DiaryCategory.values())
                .map(DiaryCategory::name)
                .toList();
    }

    private static String listOrNone(List<String> values) {
        return values.isEmpty() ? "-" : String.join(", ", values);
    }

    private static String sample(List<String> values, int limit) {
        if (values.isEmpty()) {
            return "-";
        }

        return String.join(", ", values.stream().limit(limit).toList());
    }

    private static List<String> invalidCategorySample(DearDiaryConfig config) {
        return config.invalidAutomaticCategories().stream()
                .distinct()
                .limit(8)
                .toList();
    }

    private static List<String> unknownDisabledEvents(DearDiaryConfig config) {
        return config.disabledAutomaticEvents().stream()
                .filter(eventId -> !DearDiaryEventRegistry.isRegistered(eventId))
                .toList();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

