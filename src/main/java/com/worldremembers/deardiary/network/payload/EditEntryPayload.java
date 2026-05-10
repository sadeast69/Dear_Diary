package com.worldremembers.deardiary.network.payload;

import com.worldremembers.deardiary.DearDiaryMod;
import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record EditEntryPayload(UUID entryId, String title, String text) implements CustomPayload {
    public static final Id<EditEntryPayload> ID = new Id<>(Identifier.of(DearDiaryMod.MOD_ID, "edit_entry"));
    public static final PacketCodec<RegistryByteBuf, EditEntryPayload> CODEC = PacketCodec.tuple(
            DiaryPayloadCodecs.UUID_CODEC,
            EditEntryPayload::entryId,
            DiaryPayloadCodecs.string(DiaryPayloadCodecs.SHORT_TEXT_LIMIT),
            EditEntryPayload::title,
            DiaryPayloadCodecs.string(DiaryPayloadCodecs.SHORT_TEXT_LIMIT),
            EditEntryPayload::text,
            EditEntryPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

