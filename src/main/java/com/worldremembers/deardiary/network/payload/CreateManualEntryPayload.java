package com.worldremembers.deardiary.network.payload;

import com.worldremembers.deardiary.DearDiaryMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CreateManualEntryPayload(String title, String text, boolean includeLocation, boolean chapter) implements CustomPayload {
    public static final Id<CreateManualEntryPayload> ID = new Id<>(Identifier.of(DearDiaryMod.MOD_ID, "create_manual_entry"));
    public static final PacketCodec<RegistryByteBuf, CreateManualEntryPayload> CODEC = PacketCodec.tuple(
            DiaryPayloadCodecs.string(DiaryPayloadCodecs.SHORT_TEXT_LIMIT),
            CreateManualEntryPayload::title,
            DiaryPayloadCodecs.string(DiaryPayloadCodecs.SHORT_TEXT_LIMIT),
            CreateManualEntryPayload::text,
            PacketCodecs.BOOL.cast(),
            CreateManualEntryPayload::includeLocation,
            PacketCodecs.BOOL.cast(),
            CreateManualEntryPayload::chapter,
            CreateManualEntryPayload::new
    );

    public CreateManualEntryPayload(String title, String text, boolean includeLocation) {
        this(title, text, includeLocation, false);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
