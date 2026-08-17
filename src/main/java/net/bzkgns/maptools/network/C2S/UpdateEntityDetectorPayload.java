package net.bzkgns.maptools.network.C2S;

import io.netty.buffer.ByteBuf;
import net.bzkgns.maptools.Maptools;
import net.bzkgns.maptools.entities.entity_detector.EntityDetectorConfig;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record UpdateEntityDetectorPayload(int detectorId,
        EntityDetectorConfig config) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateEntityDetectorPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(Maptools.MOD_ID, "update_entity_detector"));

    public static final StreamCodec<ByteBuf, UpdateEntityDetectorPayload> STREAM_CODEC = StreamCodec.composite(

            ByteBufCodecs.VAR_INT,
            UpdateEntityDetectorPayload::detectorId,

            EntityDetectorConfig.CONFIG_STREAM_CODEC,
            UpdateEntityDetectorPayload::config,

            UpdateEntityDetectorPayload::new);

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
