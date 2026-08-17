package net.bzkgns.maptools.entities.redstone_receiver;

import net.bzkgns.maptools.entities.ModEntities;
import net.bzkgns.maptools.items.ModItems;
import net.bzkgns.maptools.network.S2C.OpenRedstoneReceiverPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.awt.*;
import java.util.List;


public class RedstoneReceiverInteractionHandler {


    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) {return;}

        ItemStack stack = event.getItemStack();
        if (!stack.is(ModItems.REDSTONE_RECEIVER_EDITOR.get())) {return;}

        BlockPos pos = event.getPos();
        RedstoneReceiver receiver = findReceiver(level, pos);
        if (receiver == null ) {return;}
        receiver.discard();
        event.getEntity().displayClientMessage(
                Component.literal("Redstone Receiver removed.").withColor(Color.RED.getRGB()),
                true
        );
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) {return;}

        ItemStack stack = event.getItemStack();
        if (!stack.is(ModItems.REDSTONE_RECEIVER_EDITOR.get())) {return;}

        BlockPos pos = event.getPos();
        RedstoneReceiver receiver = findReceiver(level, pos);

        if (!event.getEntity().isShiftKeyDown()) {
            if (receiver != null) {
                if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                    PacketDistributor.sendToPlayer(serverPlayer,
                            new OpenRedstoneReceiverPayload(receiver.getId(),
                                    new RedstoneReceiverConfig(
                                            receiver.isEnabled(),
                                            receiver.getCommands(),
                                            receiver.getDisplayName().getString(),
                                            receiver.isXrayVisible()
                                    ))
                    );
                }
            } else {
                if (!event.getEntity().isShiftKeyDown()){
                    RedstoneReceiver newReceiver =
                            ModEntities.REDSTONE_RECEIVER.get().create(level);
                    if (newReceiver == null) {
                        return;
                    }
                    newReceiver.setPos(
                            pos.getX() + 0.5,
                            pos.getY(),
                            pos.getZ() + 0.5
                    );
                    level.addFreshEntity(newReceiver);
                }
            }
        }
        event.setCanceled(true);
    }

    private static RedstoneReceiver findReceiver(Level level, BlockPos pos) {
        AABB box = new AABB(pos);

        List<RedstoneReceiver> receivers =
                level.getEntitiesOfClass(
                        RedstoneReceiver.class,
                        box
                );

        return receivers.isEmpty()
                ? null
                : receivers.getFirst();
    }
}
