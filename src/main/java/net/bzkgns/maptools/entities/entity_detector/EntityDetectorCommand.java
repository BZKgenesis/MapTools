package net.bzkgns.maptools.entities.entity_detector;

import io.netty.buffer.ByteBuf;
import net.bzkgns.maptools.entities.AbstractCommandData;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class EntityDetectorCommand extends AbstractCommandData<EntityDetectorTrigger> {
    public static final StreamCodec<ByteBuf, EntityDetectorCommand> COMMAND_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    EntityDetectorCommand::getCommand,

                    ByteBufCodecs.VAR_INT.map(
                            integer -> EntityDetectorTrigger.values()[integer],
                            Enum::ordinal
                    ),
                    EntityDetectorCommand::getTrigger,

                    EntityDetectorCommand::new
            );

    public EntityDetectorCommand(
            String command,
            EntityDetectorTrigger trigger
    ) {
        super(command, trigger);
    }
}
