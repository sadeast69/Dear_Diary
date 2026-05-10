package com.worldremembers.deardiary.network.payload;

import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.data.DiaryEntry;
import com.worldremembers.deardiary.data.DiaryJson;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record NewAutomaticEntryPayload(String entryJson) implements CustomPacketPayload {
    public static final Type<NewAutomaticEntryPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(DearDiaryMod.MOD_ID, "new_automatic_entry")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, NewAutomaticEntryPayload> STREAM_CODEC = StreamCodec.composite(
            DiaryPayloadCodecs.string(DiaryPayloadCodecs.SHORT_TEXT_LIMIT),
            NewAutomaticEntryPayload::entryJson,
            NewAutomaticEntryPayload::new
    );

    public static NewAutomaticEntryPayload fromEntry(DiaryEntry entry) {
        return new NewAutomaticEntryPayload(DiaryJson.GSON.toJson(entry));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
