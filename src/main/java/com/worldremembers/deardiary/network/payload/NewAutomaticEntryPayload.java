package com.worldremembers.deardiary.network.payload;

import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.data.DiaryEntry;
import com.worldremembers.deardiary.data.DiaryJson;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record NewAutomaticEntryPayload(String entryJson) implements CustomPayload {
    public static final Id<NewAutomaticEntryPayload> ID = new Id<>(Identifier.of(DearDiaryMod.MOD_ID, "new_automatic_entry"));
    public static final PacketCodec<RegistryByteBuf, NewAutomaticEntryPayload> CODEC = PacketCodec.tuple(
            DiaryPayloadCodecs.string(DiaryPayloadCodecs.SHORT_TEXT_LIMIT),
            NewAutomaticEntryPayload::entryJson,
            NewAutomaticEntryPayload::new
    );

    public static NewAutomaticEntryPayload fromEntry(DiaryEntry entry) {
        return new NewAutomaticEntryPayload(DiaryJson.GSON.toJson(entry));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
