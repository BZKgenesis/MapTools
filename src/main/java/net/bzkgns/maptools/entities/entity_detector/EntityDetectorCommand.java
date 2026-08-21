package net.bzkgns.maptools.entities.entity_detector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.bzkgns.maptools.entities.AbstractCommandData;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class EntityDetectorCommand extends AbstractCommandData<EntityDetectorTrigger> {
        public static final StreamCodec<ByteBuf, EntityDetectorCommand> COMMAND_STREAM_CODEC = StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8,
                        EntityDetectorCommand::getCommand,

                        ByteBufCodecs.VAR_INT.map(
                                        integer -> EntityDetectorTrigger.values()[integer],
                                        Enum::ordinal),
                        EntityDetectorCommand::getTrigger,

                        ByteBufCodecs.BOOL,
                        EntityDetectorCommand::isEnabled,

                        EntityDetectorCommand::new);

        public static final Codec<EntityDetectorCommand> COMMAND_CODEC = RecordCodecBuilder.create(instance -> instance
                        .group(
                                        Codec.STRING.fieldOf("command").forGetter(EntityDetectorCommand::getCommand),
                                        Codec.INT.fieldOf("trigger").forGetter(
                                                        (receiverCommand) -> receiverCommand.getTrigger().ordinal()),
                                        Codec.BOOL.fieldOf("enabled").forGetter(EntityDetectorCommand::isEnabled))
                        .apply(instance, EntityDetectorCommand::new));

        public EntityDetectorCommand(
                        String command,
                        EntityDetectorTrigger trigger,
                        boolean enabled) {
                super(command, trigger, enabled);
        }

        public EntityDetectorCommand(
                        String command,
                        int trigger,
                        boolean enabled) {
                super(command, EntityDetectorTrigger.values()[trigger], enabled);
        }
}
