package net.bzkgns.maptools.data_components;

import net.bzkgns.maptools.Maptools;
import net.bzkgns.maptools.entities.entity_detector.EntityDetectorConfig;
import net.bzkgns.maptools.entities.redstone_receiver.RedstoneReceiverConfig;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents REGISTRAR = DeferredRegister
            .createDataComponents(Registries.DATA_COMPONENT_TYPE, Maptools.MOD_ID);

    public static final Supplier<DataComponentType<RedstoneReceiverConfig>> REDSTONE_RECEIVER_CONFIG = REGISTRAR
            .registerComponentType(
                    "redstone_receiver_config",
                    builder -> builder
                            // The codec to read/write the data to disk
                            .persistent(RedstoneReceiverConfig.CONFIG_CODEC)
                            // The codec to read/write the data across the network
                            .networkSynchronized(RedstoneReceiverConfig.CONFIG_STREAM_CODEC));

    public static final Supplier<DataComponentType<EntityDetectorConfig>> ENTITY_DETECTOR_CONFIG = REGISTRAR
            .registerComponentType(
                    "entity_detector_config",
                    builder -> builder
                            // The codec to read/write the data to disk
                            .persistent(EntityDetectorConfig.CONFIG_CODEC)
                            // The codec to read/write the data across the network
                            .networkSynchronized(EntityDetectorConfig.CONFIG_STREAM_CODEC));

    public static void register(IEventBus modEventBus) {
        Maptools.LOGGER.info("Registering Redstone Receiver Config");
        REGISTRAR.register(modEventBus);
    }
}
