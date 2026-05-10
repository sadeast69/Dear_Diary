package com.worldremembers.deardiary.network.payload;

import com.worldremembers.deardiary.DearDiaryMod;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record EditEntryPayload(UUID entryId, String title, String text) implements CustomPacketPayload {
    public static final Type<EditEntryPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(DearDiaryMod.MOD_ID, "edit_entry")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, EditEntryPayload> STREAM_CODEC = StreamCodec.composite(
            DiaryPayloadCodecs.UUID_CODEC,
            EditEntryPayload::entryId,
            DiaryPayloadCodecs.string(DiaryPayloadCodecs.SHORT_TEXT_LIMIT),
            EditEntryPayload::title,
            DiaryPayloadCodecs.string(DiaryPayloadCodecs.SHORT_TEXT_LIMIT),
            EditEntryPayload::text,
            EditEntryPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
