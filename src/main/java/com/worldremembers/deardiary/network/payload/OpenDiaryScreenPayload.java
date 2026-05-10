package com.worldremembers.deardiary.network.payload;

import com.worldremembers.deardiary.DearDiaryMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record OpenDiaryScreenPayload(boolean newEntry) implements CustomPayload {
    public static final Id<OpenDiaryScreenPayload> ID = new Id<>(Identifier.of(DearDiaryMod.MOD_ID, "open_diary_screen"));
    public static final PacketCodec<RegistryByteBuf, OpenDiaryScreenPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL.cast(),
            OpenDiaryScreenPayload::newEntry,
            OpenDiaryScreenPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
