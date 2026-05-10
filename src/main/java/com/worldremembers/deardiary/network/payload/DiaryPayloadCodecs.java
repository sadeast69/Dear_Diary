package com.worldremembers.deardiary.network.payload;

import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

public final class DiaryPayloadCodecs {
    public static final int SHORT_TEXT_LIMIT = 4096;
    public static final int DIARY_JSON_LIMIT = 1_048_576;

    public static final PacketCodec<RegistryByteBuf, UUID> UUID_CODEC = PacketCodec.ofStatic(
            (buf, value) -> buf.writeUuid(value),
            buf -> buf.readUuid()
    );

    private DiaryPayloadCodecs() {
    }

    public static PacketCodec<RegistryByteBuf, String> string(int maxLength) {
        return PacketCodec.ofStatic(
                (buf, value) -> buf.writeString(value == null ? "" : value, maxLength),
                buf -> buf.readString(maxLength)
        );
    }
}
