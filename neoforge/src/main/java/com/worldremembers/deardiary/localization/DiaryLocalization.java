package com.worldremembers.deardiary.localization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.DearDiaryServices;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-safe resolver for automatic diary localization keys.
 *
 * <p>The resolver reads all {@code assets/dear_diary/lang/<locale>.json}
 * resources on the classpath so optional compat mods can contribute their own
 * memory text keys without touching Dear Diary storage.</p>
 */
public final class DiaryLocalization {
    public static final String FALLBACK_LOCALE = "en_us";

    private static final String LANG_ROOT = "assets/" + DearDiaryMod.MOD_ID + "/lang/";
    private static final Map<String, Map<String, String>> TRANSLATIONS = new ConcurrentHashMap<>();

    private DiaryLocalization() {
    }

    public static String localeFor(ServerPlayer player) {
        String clientLanguage = clientLanguage(player);
        if (!isBlank(clientLanguage)) {
            return normalizeLocale(clientLanguage);
        }

        return normalizeLocale(DearDiaryServices.config().defaultDiaryLanguage());
    }

    public static String normalizeLocale(String locale) {
        String normalized = normalizeLocaleName(locale);
        if (translationsFor(normalized).isEmpty()) {
            return FALLBACK_LOCALE;
        }

        return normalized;
    }

    public static String resolveFor(ServerPlayer player, String key, String fallbackWhenKeyMissing) {
        return resolve(localeFor(player), key, fallbackWhenKeyMissing);
    }

    public static String resolve(String locale, String key, String fallbackWhenKeyMissing) {
        if (isBlank(key)) {
            return fallbackWhenKeyMissing == null ? "" : fallbackWhenKeyMissing;
        }

        String normalizedLocale = normalizeLocale(locale);
        String translated = translationsFor(normalizedLocale).get(key);
        if (translated != null) {
            return translated;
        }

        translated = translationsFor(FALLBACK_LOCALE).get(key);
        return translated == null ? key : translated;
    }

    public static boolean hasTranslation(String locale, String key) {
        if (isBlank(key)) {
            return false;
        }

        return translationsFor(normalizeLocaleName(locale)).containsKey(key);
    }

    private static String clientLanguage(ServerPlayer player) {
        if (player == null) {
            return "";
        }

        return player.clientInformation().language();
    }

    private static Map<String, String> translationsFor(String locale) {
        return TRANSLATIONS.computeIfAbsent(locale, DiaryLocalization::loadTranslations);
    }

    private static Map<String, String> loadTranslations(String locale) {
        String resourcePath = LANG_ROOT + locale + ".json";
        ClassLoader classLoader = DiaryLocalization.class.getClassLoader();
        try {
            Enumeration<URL> resources = classLoader.getResources(resourcePath);
            if (!resources.hasMoreElements()) {
                return Map.of();
            }

            Map<String, String> translations = new LinkedHashMap<>();
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                loadTranslationResource(resourcePath, resource, translations);
            }
            return Map.copyOf(translations);
        } catch (IOException | JsonParseException exception) {
            DearDiaryMod.LOGGER.warn("Failed to load Dear Diary language resource {}", resourcePath, exception);
            return Map.of();
        }
    }

    private static void loadTranslationResource(String resourcePath, URL resource, Map<String, String> translations) throws IOException {
        try (InputStream stream = resource.openStream();
                InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonElement element = JsonParser.parseReader(reader);
            if (!element.isJsonObject()) {
                return;
            }

            JsonObject object = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                if (entry.getValue().isJsonPrimitive()) {
                    translations.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
        } catch (JsonParseException exception) {
            DearDiaryMod.LOGGER.warn("Failed to parse Dear Diary language resource {} from {}", resourcePath, resource, exception);
        }
    }

    private static String normalizeLocaleName(String locale) {
        if (isBlank(locale)) {
            return FALLBACK_LOCALE;
        }

        String normalized = locale.strip().replace('-', '_').toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "ru" -> "ru_ru";
            case "en" -> FALLBACK_LOCALE;
            default -> normalized;
        };
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
