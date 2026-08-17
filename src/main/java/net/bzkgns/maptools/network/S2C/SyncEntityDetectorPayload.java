package net.bzkgns.maptools.network.S2C;

import io.netty.buffer.ByteBuf;
import net.bzkgns.maptools.Maptools;
import net.bzkgns.maptools.entities.entity_detector.EntityDetectorConfig;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record SyncEntityDetectorPayload(int detectorId,
        EntityDetectorConfig config) implements CustomPacketPayload {
    public static final Type<SyncEntityDetectorPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Maptools.MOD_ID, "sync_entity_detector"));

    public static final StreamCodec<ByteBuf, SyncEntityDetectorPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SyncEntityDetectorPayload::detectorId,

            EntityDetectorConfig.CONFIG_STREAM_CODEC,
            SyncEntityDetectorPayload::config,

            SyncEntityDetectorPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
