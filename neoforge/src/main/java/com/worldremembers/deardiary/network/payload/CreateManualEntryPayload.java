package com.worldremembers.deardiary.network.payload;

import com.worldremembers.deardiary.DearDiaryMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CreateManualEntryPayload(String title, String text, boolean includeLocation, boolean chapter) implements CustomPacketPayload {
    public static final Type<CreateManualEntryPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(DearDiaryMod.MOD_ID, "create_manual_entry")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, CreateManualEntryPayload> STREAM_CODEC = StreamCodec.composite(
            DiaryPayloadCodecs.string(DiaryPayloadCodecs.SHORT_TEXT_LIMIT),
            CreateManualEntryPayload::title,
            DiaryPayloadCodecs.string(DiaryPayloadCodecs.SHORT_TEXT_LIMIT),
            CreateManualEntryPayload::text,
            ByteBufCodecs.BOOL.cast(),
            CreateManualEntryPayload::includeLocation,
            ByteBufCodecs.BOOL.cast(),
            CreateManualEntryPayload::chapter,
            CreateManualEntryPayload::new
    );

    public CreateManualEntryPayload(String title, String text, boolean includeLocation) {
        this(title, text, includeLocation, false);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
