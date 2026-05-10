package com.worldremembers.deardiary.network.payload;

import com.worldremembers.deardiary.DearDiaryMod;
import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record DeleteEntryPayload(UUID entryId) implements CustomPayload {
    public static final Id<DeleteEntryPayload> ID = new Id<>(Identifier.of(DearDiaryMod.MOD_ID, "delete_entry"));
    public static final PacketCodec<RegistryByteBuf, DeleteEntryPayload> CODEC = PacketCodec.tuple(
            DiaryPayloadCodecs.UUID_CODEC,
            DeleteEntryPayload::entryId,
            DeleteEntryPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

