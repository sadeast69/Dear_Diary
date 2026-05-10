package com.worldremembers.deardiary.event;

import com.worldremembers.deardiary.data.DiaryCategory;
import com.worldremembers.deardiary.localization.DiaryLocalization;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class AutomaticEventRegistryValidator {
    private static final List<String> REQUIRED_LOCALES = List.of("en_us", "ru_ru");
    private static final Set<String> DEFINITION_ONLY_EVENTS = Set.of(
            VanillaDiaryEventDefinitions.FIRST_SNIFFER_HATCH,
            VanillaDiaryEventDefinitions.FIRST_ARCHAEOLOGY_BRUSH_SUCCESS
    );

    private AutomaticEventRegistryValidator() {
    }

    public static Result validate() {
        List<String> problems = new ArrayList<>();
        List<String> missingKeys = new ArrayList<>();
        int definitionOnlyCount = 0;

        for (AutomaticEventDefinition definition : DearDiaryEventRegistry.getAllDefinitions()) {
            if (DEFINITION_ONLY_EVENTS.contains(definition.eventId())) {
                definitionOnlyCount++;
            }
            validateDefinition(definition, problems, missingKeys);
        }

        validateOrigin(problems, missingKeys);
        problems.addAll(missingKeys.stream()
                .limit(12)
                .map(key -> "missing translation: " + key)
                .toList());

        return new Result(
                DearDiaryEventRegistry.getAllDefinitions().size(),
                definitionOnlyCount,
                0,
                missingKeys.size(),
                List.copyOf(problems)
        );
    }

    private static void validateDefinition(
            AutomaticEventDefinition definition,
            List<String> problems,
            List<String> missingKeys
    ) {
        if (!isNamespaced(definition.eventId())) {
            problems.add("event id is not namespaced: " + definition.eventId());
        }
        if (definition.category() == null || definition.category() == DiaryCategory.MANUAL) {
            problems.add("automatic event has invalid category: " + definition.eventId());
        }
        if (definition.importance() == null) {
            problems.add("automatic event has missing importance: " + definition.eventId());
        }
        if (definition.triggerPolicy() == null) {
            problems.add("automatic event has missing trigger policy: " + definition.eventId());
        }
        if (!isNamespaced(definition.icon())) {
            problems.add("automatic event has invalid icon id: " + definition.eventId() + " -> " + definition.icon());
        }
        if (definition.variants().isEmpty()) {
            problems.add("automatic event has no variants: " + definition.eventId());
            return;
        }

        for (DiaryTextVariant variant : definition.variants()) {
            validateKey(definition.eventId(), variant.titleKey(), problems, missingKeys);
            validateKey(definition.eventId(), variant.textKey(), problems, missingKeys);
        }
    }

    private static void validateOrigin(List<String> problems, List<String> missingKeys) {
        if (!isNamespaced(OriginEntryFactory.EVENT_TYPE)) {
            problems.add("origin event id is not namespaced: " + OriginEntryFactory.EVENT_TYPE);
        }
        for (DiaryTextVariant variant : OriginEntryFactory.variants()) {
            validateKey(OriginEntryFactory.EVENT_TYPE, variant.titleKey(), problems, missingKeys);
            validateKey(OriginEntryFactory.EVENT_TYPE, variant.textKey(), problems, missingKeys);
        }
    }

    private static void validateKey(
            String eventId,
            String translationKey,
            List<String> problems,
            List<String> missingKeys
    ) {
        if (translationKey == null || translationKey.isBlank()) {
            problems.add("blank translation key in " + eventId);
            return;
        }

        for (String locale : REQUIRED_LOCALES) {
            if (!DiaryLocalization.hasTranslation(locale, translationKey)) {
                missingKeys.add(locale + ":" + translationKey);
            }
        }
    }

    private static boolean isNamespaced(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }

        int separator = id.indexOf(':');
        return separator > 0 && separator < id.length() - 1;
    }

    public record Result(
            int totalEvents,
            int definitionOnlyCount,
            int duplicateIdsCount,
            int missingKeysCount,
            List<String> problems
    ) {
        public boolean ok() {
            return duplicateIdsCount == 0 && missingKeysCount == 0 && problems.isEmpty();
        }

        public int problemCount() {
            return problems.size();
        }

        public List<String> problemSample(int limit) {
            return problems.stream().limit(limit).toList();
        }
    }
}
