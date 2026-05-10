package com.worldremembers.deardiary.network.payload;

import com.worldremembers.deardiary.DearDiaryMod;
import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SetFavoritePayload(UUID entryId, boolean favorite) implements CustomPayload {
    public static final Id<SetFavoritePayload> ID = new Id<>(Identifier.of(DearDiaryMod.MOD_ID, "set_favorite"));
    public static final PacketCodec<RegistryByteBuf, SetFavoritePayload> CODEC = PacketCodec.tuple(
            DiaryPayloadCodecs.UUID_CODEC,
            SetFavoritePayload::entryId,
            PacketCodecs.BOOL.cast(),
            SetFavoritePayload::favorite,
            SetFavoritePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

