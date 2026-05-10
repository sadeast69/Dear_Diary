package com.worldremembers.deardiary.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.time.Instant;

public final class DiaryJson {
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .registerTypeAdapter(DiaryEntryKind.class, new EntryKindAdapter())
            .create();

    private DiaryJson() {
    }

    private static final class InstantAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
        @Override
        public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                return Instant.parse(json.getAsString());
            } catch (RuntimeException exception) {
                throw new JsonParseException("Invalid diary timestamp: " + json, exception);
            }
        }
    }

    private static final class EntryKindAdapter implements JsonSerializer<DiaryEntryKind>, JsonDeserializer<DiaryEntryKind> {
        @Override
        public JsonElement serialize(DiaryEntryKind src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.serializedName());
        }

        @Override
        public DiaryEntryKind deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return DiaryEntryKind.fromSerializedName(json.getAsString());
        }
    }
}

