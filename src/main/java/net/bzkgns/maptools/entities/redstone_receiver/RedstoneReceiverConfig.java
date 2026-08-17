package net.bzkgns.maptools.entities.redstone_receiver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record RedstoneReceiverConfig(
        boolean enabled,
        List<RedstoneReceiverCommand> commands,
        String displayName,
        boolean xrayVisible
) {
    public static final StreamCodec<ByteBuf, RedstoneReceiverConfig> CONFIG_STREAM_CODEC =
            StreamCodec.of(
                    (buf, config) -> {
                        ByteBufCodecs.BOOL.encode(
                                buf,
                                config.enabled()
                        );

                        RedstoneReceiverCommand.COMMAND_STREAM_CODEC.apply(
                                ByteBufCodecs.list()
                        ).encode(buf, config.commands);

                        ByteBufCodecs.STRING_UTF8.encode(
                                buf,
                                config.displayName()
                        );

                        ByteBufCodecs.BOOL.encode(
                                buf,
                                config.xrayVisible()
                        );
                    },

                    buf -> new RedstoneReceiverConfig(
                            ByteBufCodecs.BOOL.decode(buf),
                            RedstoneReceiverCommand.COMMAND_STREAM_CODEC.apply(
                                    ByteBufCodecs.list()).decode(buf),
                            ByteBufCodecs.STRING_UTF8.decode(buf),
                            ByteBufCodecs.BOOL.decode(buf)
                    )
            );
    public static final Codec<RedstoneReceiverConfig> CONFIG_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.fieldOf("enabled").forGetter(RedstoneReceiverConfig::enabled),
                    RedstoneReceiverCommand.COMMAND_CODEC
                            .listOf()
                            .fieldOf("commands")
                            .forGetter(RedstoneReceiverConfig::commands),
                    Codec.STRING.fieldOf("display_name").forGetter(RedstoneReceiverConfig::displayName),
                    Codec.BOOL.fieldOf("xray_visible").forGetter(RedstoneReceiverConfig::xrayVisible)
            ).apply(instance, RedstoneReceiverConfig::new)
    );
}
