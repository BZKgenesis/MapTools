package net.bzkgns.maptools.network.S2C;

import net.bzkgns.maptools.client.screen.EntityDetectorEditScreen;
import net.bzkgns.maptools.entities.entity_detector.EntityDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public class OpenEntityDetectorPayloadHandler implements IPayloadHandler<OpenEntityDetectorPayload> {

    @Override
    public void handle(@NotNull OpenEntityDetectorPayload data, IPayloadContext context) {
        context.enqueueWork(() -> {

            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.level == null) {
                return;
            }

            Entity entity =
                    minecraft.level.getEntity(data.detectorId());

            if (entity instanceof EntityDetector detector) {
                detector.setConfig(data.config());
                minecraft.setScreen(
                        new EntityDetectorEditScreen(detector)
                );
            }
        });

    }
}
