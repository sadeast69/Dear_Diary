package com.worldremembers.deardiary.network.payload;

import com.worldremembers.deardiary.DearDiaryMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenDiaryScreenPayload(boolean newEntry) implements CustomPacketPayload {
    public static final Type<OpenDiaryScreenPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(DearDiaryMod.MOD_ID, "open_diary_screen")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenDiaryScreenPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL.cast(),
            OpenDiaryScreenPayload::newEntry,
            OpenDiaryScreenPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
