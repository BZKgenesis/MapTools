package net.bzkgns.maptools.network.S2C;

import net.bzkgns.maptools.client.screen.RedstoneReceiverEditScreen;
import net.bzkgns.maptools.entities.redstone_receiver.RedstoneReceiver;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public class OpenRedstoneReceiverPayloadHandler implements IPayloadHandler<OpenRedstoneReceiverPayload> {

    @Override
    public void handle(@NotNull OpenRedstoneReceiverPayload data, IPayloadContext context) {
        context.enqueueWork(() -> {

            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.level == null) {
                return;
            }

            Entity entity =
                    minecraft.level.getEntity(data.receiverId());

            if (entity instanceof RedstoneReceiver receiver) {
                receiver.setConfig(data.config());
                minecraft.setScreen(
                        new RedstoneReceiverEditScreen(receiver)
                );
            }
        });

    }
}