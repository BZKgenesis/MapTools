package net.bzkgns.maptools.entities.redstone_receiver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.bzkgns.maptools.entities.AbstractCommandData;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;


public class RedstoneReceiverCommand extends AbstractCommandData<RedstoneReceiverTrigger> {
    public static final StreamCodec<ByteBuf, RedstoneReceiverCommand> COMMAND_STREAM_CODEC =
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
    public static final Codec<RedstoneReceiverCommand> COMMAND_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("command").forGetter(RedstoneReceiverCommand::getCommand),
                    Codec.INT.fieldOf("trigger").forGetter((receiverCommand)-> receiverCommand.getTrigger().ordinal())
            ).apply(instance, RedstoneReceiverCommand::new));

    public RedstoneReceiverCommand(
            String command,
            RedstoneReceiverTrigger trigger
    ) {
        super(command, trigger);
    }

    public RedstoneReceiverCommand(
            String command,
            int trigger
    ) {
        super(command, RedstoneReceiverTrigger.values()[trigger]);
    }
}
