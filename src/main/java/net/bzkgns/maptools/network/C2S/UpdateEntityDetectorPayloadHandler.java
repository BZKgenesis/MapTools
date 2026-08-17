package net.bzkgns.maptools.network.C2S;

import net.bzkgns.maptools.entities.entity_detector.EntityDetector;
import net.bzkgns.maptools.entities.entity_detector.EntityDetectorConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public class UpdateEntityDetectorPayloadHandler implements IPayloadHandler<UpdateEntityDetectorPayload> {
    @Override
    public void handle(@NotNull UpdateEntityDetectorPayload data, @NotNull IPayloadContext context) {
        context.enqueueWork(() -> {

            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            ServerLevel level = player.serverLevel();

            Entity entity = level.getEntity(data.detectorId());

            if (!(entity instanceof EntityDetector detector)) {
                return;
            }
            EntityDetectorConfig config = data.config();

            detector.setConfig(config);
        });
    }
}