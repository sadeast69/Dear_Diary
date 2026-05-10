package com.worldremembers.deardiary.network.payload;

import com.worldremembers.deardiary.DearDiaryMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RequestDiaryPayload() implements CustomPacketPayload {
    public static final Type<RequestDiaryPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(DearDiaryMod.MOD_ID, "request_diary")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestDiaryPayload> STREAM_CODEC =
            StreamCodec.unit(new RequestDiaryPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
