package net.bzkgns.maptools.network.S2C;

import io.netty.buffer.ByteBuf;
import net.bzkgns.maptools.Maptools;
import net.bzkgns.maptools.entities.entity_detector.EntityDetectorConfig;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record OpenEntityDetectorPayload(int detectorId,
                                        EntityDetectorConfig config) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenEntityDetectorPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Maptools.MOD_ID, "open_entity_detector"));

    public static final StreamCodec<ByteBuf, OpenEntityDetectorPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            OpenEntityDetectorPayload::detectorId,

            EntityDetectorConfig.CONFIG_CODEC,
            OpenEntityDetectorPayload::config,

            OpenEntityDetectorPayload::new
    );

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
