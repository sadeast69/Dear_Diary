package com.worldremembers.deardiary.network.payload;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class DiaryPayloadCodecs {
    public static final int SHORT_TEXT_LIMIT = 4096;
    public static final int DIARY_JSON_LIMIT = 1_048_576;

    public static final StreamCodec<RegistryFriendlyByteBuf, UUID> UUID_CODEC = StreamCodec.of(
            (buf, value) -> buf.writeUUID(value),
            buf -> buf.readUUID()
    );

    private DiaryPayloadCodecs() {
    }

    public static StreamCodec<RegistryFriendlyByteBuf, String> string(int maxLength) {
        return StreamCodec.of(
                (buf, value) -> buf.writeUtf(value == null ? "" : value, maxLength),
                buf -> buf.readUtf(maxLength)
        );
    }
}
