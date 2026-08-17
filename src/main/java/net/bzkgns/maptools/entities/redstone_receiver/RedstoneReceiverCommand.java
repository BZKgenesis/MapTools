package net.bzkgns.maptools.entities.redstone_receiver;

import io.netty.buffer.ByteBuf;
import net.bzkgns.maptools.entities.AbstractCommandData;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;


public class RedstoneReceiverCommand extends AbstractCommandData<RedstoneReceiverTrigger> {
    public static final StreamCodec<ByteBuf, RedstoneReceiverCommand> COMMAND_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    RedstoneReceiverCommand::getCommand,

                    ByteBufCodecs.VAR_INT.map(
                            integer -> RedstoneReceiverTrigger.values()[integer],
                            Enum::ordinal
                    ),
                    RedstoneReceiverCommand::getTrigger,

                    RedstoneReceiverCommand::new
            );

    public RedstoneReceiverCommand(
            String command,
            RedstoneReceiverTrigger trigger
    ) {
        super(command, trigger);
    }
}
