package com.worldremembers.deardiary.network.payload;

import com.worldremembers.deardiary.DearDiaryMod;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SetFavoritePayload(UUID entryId, boolean favorite) implements CustomPacketPayload {
    public static final Type<SetFavoritePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(DearDiaryMod.MOD_ID, "set_favorite")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SetFavoritePayload> STREAM_CODEC = StreamCodec.composite(
            DiaryPayloadCodecs.UUID_CODEC,
            SetFavoritePayload::entryId,
            ByteBufCodecs.BOOL.cast(),
            SetFavoritePayload::favorite,
            SetFavoritePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
