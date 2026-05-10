package com.worldremembers.deardiary.network.payload;

import com.worldremembers.deardiary.DearDiaryMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RequestDiaryPayload() implements CustomPayload {
    public static final Id<RequestDiaryPayload> ID = new Id<>(Identifier.of(DearDiaryMod.MOD_ID, "request_diary"));
    public static final PacketCodec<RegistryByteBuf, RequestDiaryPayload> CODEC = PacketCodec.unit(new RequestDiaryPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

