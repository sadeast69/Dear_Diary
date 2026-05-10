package com.worldremembers.deardiary.network.payload;

import com.worldremembers.deardiary.DearDiaryMod;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ShareEntryPayload(UUID entryId) implements CustomPacketPayload {
    public static final Type<ShareEntryPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(DearDiaryMod.MOD_ID, "share_entry")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ShareEntryPayload> STREAM_CODEC = StreamCodec.composite(
            DiaryPayloadCodecs.UUID_CODEC,
            ShareEntryPayload::entryId,
            ShareEntryPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
