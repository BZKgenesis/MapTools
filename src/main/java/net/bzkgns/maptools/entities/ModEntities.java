package net.bzkgns.maptools.entities;

import net.bzkgns.maptools.entities.entity_detector.EntityDetector;
import net.bzkgns.maptools.entities.redstone_receiver.RedstoneReceiver;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.bzkgns.maptools.Maptools.MOD_ID;


public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<RedstoneReceiver>> REDSTONE_RECEIVER =
            ENTITY_TYPES.register("redstone_receiver", () ->
                    EntityType.Builder
                            .of(RedstoneReceiver::new, MobCategory.MISC)
                            .sized(1.02F, 1.01F)
                            .clientTrackingRange(64)
                            .updateInterval(3)
                            .build(
                                            "redstone_receiver"

                            )
            );
    public static final DeferredHolder<EntityType<?>, EntityType<EntityDetector>> ENTITY_DETECTOR =
            ENTITY_TYPES.register("entity_detector", () ->
                    EntityType.Builder
                            .of(EntityDetector::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)
                            .clientTrackingRange(64)
                            .updateInterval(3)
                            .build(
                                    "entity_detector"

                            )
            );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
