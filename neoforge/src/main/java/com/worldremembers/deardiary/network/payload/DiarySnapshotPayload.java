package com.worldremembers.deardiary.network.payload;

import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.data.DiaryJson;
import com.worldremembers.deardiary.data.PlayerDiary;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DiarySnapshotPayload(String diaryJson) implements CustomPacketPayload {
    public static final Type<DiarySnapshotPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(DearDiaryMod.MOD_ID, "diary_snapshot")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, DiarySnapshotPayload> STREAM_CODEC = StreamCodec.composite(
            DiaryPayloadCodecs.string(DiaryPayloadCodecs.DIARY_JSON_LIMIT),
            DiarySnapshotPayload::diaryJson,
            DiarySnapshotPayload::new
    );

    public static DiarySnapshotPayload fromDiary(PlayerDiary diary) {
        return new DiarySnapshotPayload(DiaryJson.GSON.toJson(diary));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
