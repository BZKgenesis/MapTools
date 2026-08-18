package net.bzkgns.maptools.entity_data_serializers;

import net.bzkgns.maptools.Maptools;
import net.bzkgns.maptools.entities.entity_detector.EntityDetectorCommand;
import net.bzkgns.maptools.entities.redstone_receiver.RedstoneReceiverCommand;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

public class ModEntityDataSerializers {
        public static final DeferredRegister<EntityDataSerializer<?>> REGISTRAR = DeferredRegister
                        .create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, Maptools.MOD_ID);

        public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<List<RedstoneReceiverCommand>>> REDSTONE_RECEIVER_COMMANDS = REGISTRAR
                        .register(
                                        "redstone_receiver_commands",
                                        () -> EntityDataSerializer.forValueType(
                                                        RedstoneReceiverCommand.COMMAND_STREAM_CODEC
                                                                        .apply(ByteBufCodecs.list())));

        public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<List<EntityDetectorCommand>>> ENTITY_DETECTOR_COMMANDS = REGISTRAR
                        .register(
                                        "entity_detector_commands",
                                        () -> EntityDataSerializer.forValueType(
                                                        EntityDetectorCommand.COMMAND_STREAM_CODEC
                                                                        .apply(ByteBufCodecs.list())));
}
