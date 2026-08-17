package net.bzkgns.maptools.entities.entity_detector;

import io.netty.buffer.ByteBuf;
import net.bzkgns.maptools.network.C2S.UpdateEntityDetectorPayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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
                double posZ) {
        public static final StreamCodec<ByteBuf, EntityDetectorConfig> CONFIG_STREAM_CODEC = StreamCodec.of(
                        (buf, config) -> {
                                ByteBufCodecs.BOOL.encode(
                                                buf,
                                                config.enabled());

                                EntityDetectorCommand.COMMAND_STREAM_CODEC.apply(
                                                ByteBufCodecs.list()).encode(buf, config.commands);

                                ByteBufCodecs.STRING_UTF8.encode(
                                                buf,
                                                config.displayName());

                                ByteBufCodecs.STRING_UTF8.encode(
                                                buf,
                                                config.zoneId());

                                ByteBufCodecs.FLOAT.encode(
                                                buf,
                                                config.sizeX());
                                ByteBufCodecs.FLOAT.encode(
                                                buf,
                                                config.sizeY());
                                ByteBufCodecs.FLOAT.encode(
                                                buf,
                                                config.sizeZ());
                                ByteBufCodecs.DOUBLE.encode(
                                                buf,
                                                config.posX());
                                ByteBufCodecs.DOUBLE.encode(
                                                buf,
                                                config.posY());
                                ByteBufCodecs.DOUBLE.encode(
                                                buf,
                                                config.posZ());
                        },

                        buf -> new EntityDetectorConfig(
                                        ByteBufCodecs.BOOL.decode(buf),
                                        EntityDetectorCommand.COMMAND_STREAM_CODEC.apply(
                                                        ByteBufCodecs.list()).decode(buf),
                                        ByteBufCodecs.STRING_UTF8.decode(buf),
                                        ByteBufCodecs.STRING_UTF8.decode(buf),
                                        ByteBufCodecs.FLOAT.decode(buf),
                                        ByteBufCodecs.FLOAT.decode(buf),
                                        ByteBufCodecs.FLOAT.decode(buf),
                                        ByteBufCodecs.DOUBLE.decode(buf),
                                        ByteBufCodecs.DOUBLE.decode(buf),
                                        ByteBufCodecs.DOUBLE.decode(buf)));

        public static final Codec<EntityDetectorConfig> CONFIG_CODEC = RecordCodecBuilder.create(instance -> instance
                        .group(
                                        Codec.BOOL.fieldOf("enabled").forGetter(EntityDetectorConfig::enabled),
                                        EntityDetectorCommand.COMMAND_CODEC
                                                        .listOf()
                                                        .fieldOf("commands")
                                                        .forGetter(EntityDetectorConfig::commands),
                                        Codec.STRING.fieldOf("display_name")
                                                        .forGetter(EntityDetectorConfig::displayName),
                                        Codec.STRING.fieldOf("zone_id").forGetter(EntityDetectorConfig::zoneId),
                                        Codec.FLOAT.fieldOf("size_x").forGetter(EntityDetectorConfig::sizeX),
                                        Codec.FLOAT.fieldOf("size_y").forGetter(EntityDetectorConfig::sizeY),
                                        Codec.FLOAT.fieldOf("size_z").forGetter(EntityDetectorConfig::sizeZ),
                                        Codec.DOUBLE.fieldOf("pos_x").forGetter(EntityDetectorConfig::posX),
                                        Codec.DOUBLE.fieldOf("pos_y").forGetter(EntityDetectorConfig::posY),
                                        Codec.DOUBLE.fieldOf("pos_z").forGetter(EntityDetectorConfig::posZ))
                        .apply(instance, EntityDetectorConfig::new));

}
