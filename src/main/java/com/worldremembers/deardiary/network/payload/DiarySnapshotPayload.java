package com.worldremembers.deardiary.network.payload;

import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.data.DiaryJson;
import com.worldremembers.deardiary.data.PlayerDiary;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record DiarySnapshotPayload(String diaryJson) implements CustomPayload {
    public static final Id<DiarySnapshotPayload> ID = new Id<>(Identifier.of(DearDiaryMod.MOD_ID, "diary_snapshot"));
    public static final PacketCodec<RegistryByteBuf, DiarySnapshotPayload> CODEC = PacketCodec.tuple(
            DiaryPayloadCodecs.string(DiaryPayloadCodecs.DIARY_JSON_LIMIT),
            DiarySnapshotPayload::diaryJson,
            DiarySnapshotPayload::new
    );

    public static DiarySnapshotPayload fromDiary(PlayerDiary diary) {
        return new DiarySnapshotPayload(DiaryJson.GSON.toJson(diary));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

