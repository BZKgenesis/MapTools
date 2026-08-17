package net.bzkgns.maptools.entities.redstone_receiver;

import io.netty.buffer.ByteBuf;
import net.bzkgns.maptools.network.C2S.UpdateRedstoneReceiverPayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record RedstoneReceiverConfig(
        boolean enabled,
        List<RedstoneReceiverCommand> commands,
        String displayName,
        boolean xrayVisible
) {
    public static final StreamCodec<ByteBuf, RedstoneReceiverConfig> CONFIG_CODEC =
            StreamCodec.of(
                    (buf, config) -> {
                        ByteBufCodecs.BOOL.encode(
                                buf,
                                config.enabled()
                        );

                        RedstoneReceiverCommand.COMMAND_CODEC.apply(
                                ByteBufCodecs.list()).encode(buf, config.commands);

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
                            RedstoneReceiverCommand.COMMAND_CODEC.apply(
                                    ByteBufCodecs.list()).decode(buf),
                            ByteBufCodecs.STRING_UTF8.decode(buf),
                            ByteBufCodecs.BOOL.decode(buf)
                    )
            );
    public static final StreamCodec<ByteBuf, UpdateRedstoneReceiverPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    UpdateRedstoneReceiverPayload::receiverId,

                    CONFIG_CODEC,
                    UpdateRedstoneReceiverPayload::config,

                    UpdateRedstoneReceiverPayload::new
            );
}
