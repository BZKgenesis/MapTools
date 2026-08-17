package net.bzkgns.maptools.network.C2S;

import net.bzkgns.maptools.entities.redstone_receiver.RedstoneReceiver;
import net.bzkgns.maptools.entities.redstone_receiver.RedstoneReceiverConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public class UpdateRedstoneReceiverPayloadHandler implements IPayloadHandler<UpdateRedstoneReceiverPayload> {
    @Override
    public void handle(@NotNull UpdateRedstoneReceiverPayload data, @NotNull IPayloadContext context) {
        context.enqueueWork(() -> {

            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            ServerLevel level = player.serverLevel();

            Entity entity = level.getEntity(data.receiverId());

            if (!(entity instanceof RedstoneReceiver receiver)) {
                return;
            }
            RedstoneReceiverConfig config = data.config();

            receiver.setConfig(config);
        });
    }
}