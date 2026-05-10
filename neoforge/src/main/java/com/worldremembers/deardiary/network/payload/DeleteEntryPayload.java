package com.worldremembers.deardiary.network.payload;

import com.worldremembers.deardiary.DearDiaryMod;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DeleteEntryPayload(UUID entryId) implements CustomPacketPayload {
    public static final Type<DeleteEntryPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(DearDiaryMod.MOD_ID, "delete_entry")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, DeleteEntryPayload> STREAM_CODEC = StreamCodec.composite(
            DiaryPayloadCodecs.UUID_CODEC,
            DeleteEntryPayload::entryId,
            DeleteEntryPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
