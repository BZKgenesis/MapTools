package net.bzkgns.maptools.entities.entity_detector;

import io.netty.buffer.ByteBuf;
import net.bzkgns.maptools.network.C2S.UpdateEntityDetectorPayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record EntityDetectorConfig(
        boolean enabled,
        List<EntityDetectorCommand> commands,
        String displayName,
        String zoneId,
        float sizeX,
        float sizeY,
        float sizeZ,
        double posX,
        double posY,
        double posZ
) {
    public static final StreamCodec<ByteBuf, EntityDetectorConfig> CONFIG_CODEC =
            StreamCodec.of(
                    (buf, config) -> {
                        ByteBufCodecs.BOOL.encode(
                                buf,
                                config.enabled()
                        );

                        EntityDetectorCommand.COMMAND_CODEC.apply(
                                ByteBufCodecs.list()).encode(buf, config.commands);

                        ByteBufCodecs.STRING_UTF8.encode(
                                buf,
                                config.displayName()
                        );

                        ByteBufCodecs.STRING_UTF8.encode(
                                buf,
                                config.zoneId()
                        );

                        ByteBufCodecs.FLOAT.encode(
                                buf,
                                config.sizeX()
                        );
                        ByteBufCodecs.FLOAT.encode(
                                buf,
                                config.sizeY()
                        );
                        ByteBufCodecs.FLOAT.encode(
                                buf,
                                config.sizeZ()
                        );
                        ByteBufCodecs.DOUBLE.encode(
                                buf,
                                config.posX()
                        );
                        ByteBufCodecs.DOUBLE.encode(
                                buf,
                                config.posY()
                        );
                        ByteBufCodecs.DOUBLE.encode(
                                buf,
                                config.posZ()
                        );
                    },

                    buf -> new EntityDetectorConfig(
                            ByteBufCodecs.BOOL.decode(buf),
                            EntityDetectorCommand.COMMAND_CODEC.apply(
                                ByteBufCodecs.list()).decode(buf),
                                ByteBufCodecs.STRING_UTF8.decode(buf),
                                ByteBufCodecs.STRING_UTF8.decode(buf),
                                ByteBufCodecs.FLOAT.decode(buf),
                                ByteBufCodecs.FLOAT.decode(buf),
                                ByteBufCodecs.FLOAT.decode(buf),
                                ByteBufCodecs.DOUBLE.decode(buf),
                                ByteBufCodecs.DOUBLE.decode(buf),
                                ByteBufCodecs.DOUBLE.decode(buf)
                    )
            );

    public static final StreamCodec<ByteBuf, UpdateEntityDetectorPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    UpdateEntityDetectorPayload::detectorId,

                    CONFIG_CODEC,
                    UpdateEntityDetectorPayload::config,

                    UpdateEntityDetectorPayload::new
            );
}