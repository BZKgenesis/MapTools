package net.bzkgns.maptools.entities.redstone_receiver;

import net.bzkgns.maptools.data_components.ModDataComponents;
import net.bzkgns.maptools.entities.ModEntities;
import net.bzkgns.maptools.items.ModItems;
import net.bzkgns.maptools.network.S2C.OpenRedstoneReceiverPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.awt.*;
import java.util.List;

public class RedstoneReceiverInteractionHandler {

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (!stack.is(ModItems.REDSTONE_RECEIVER_EDITOR.get())) {
            return;
        }

        BlockPos pos = event.getPos();
        RedstoneReceiver receiver = findReceiver(level, pos);
        if (receiver == null) {
            return;
        }
        receiver.discard();
        event.getEntity().displayClientMessage(
                Component.literal(receiver.getDisplayName().getString() + " removed.").withColor(Color.RED.getRGB()),
                true);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLeftClickEntity(AttackEntityEvent event) {
        ItemStack stack = event.getEntity().getWeaponItem();
        if (!stack.is(ModItems.REDSTONE_RECEIVER_EDITOR.get())) {
            return;
        }

        if (!(event.getTarget() instanceof RedstoneReceiver receiver))
            return;
        receiver.discard();
        event.getEntity().displayClientMessage(
                Component.literal(receiver.getDisplayName().getString() + " removed.").withColor(Color.RED.getRGB()),
                true);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onMiddleCLick(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isPickBlock()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.hitResult instanceof EntityHitResult hitResult) {
                if (hitResult.getEntity() instanceof RedstoneReceiver redstoneReceiver) {
                    redstoneReceiver.getPickResult();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ModItems.REDSTONE_RECEIVER_EDITOR.get())) {
            return;
        }

        if (!(event.getTarget() instanceof RedstoneReceiver receiver))
            return;

        if (!event.getEntity().isShiftKeyDown()) {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer,
                        new OpenRedstoneReceiverPayload(receiver.getId(),
                                new RedstoneReceiverConfig(
                                        receiver.isEnabled(),
                                        receiver.getCommands(),
                                        receiver.getDisplayName().getString(),
                                        receiver.isXrayVisible())));
            }
        }
        event.setCanceled(true);

    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (!stack.is(ModItems.REDSTONE_RECEIVER_EDITOR.get())) {
            return;
        }

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
                                            receiver.isXrayVisible())));
                }
            } else {
                RedstoneReceiver newReceiver = ModEntities.REDSTONE_RECEIVER.get().create(level);
                if (newReceiver == null) {
                    return;
                }
                newReceiver.setPos(
                        pos.getX() + 0.5,
                        pos.getY(),
                        pos.getZ() + 0.5);
                level.addFreshEntity(newReceiver);
                RedstoneReceiverConfig config = stack.get(ModDataComponents.REDSTONE_RECEIVER_CONFIG.get());
                newReceiver.setConfig(config);

            }
        }
        event.setCanceled(true);
    }

    private static RedstoneReceiver findReceiver(Level level, BlockPos pos) {
        AABB box = new AABB(pos.getX() + 0.2f, pos.getY() + 0.2f, pos.getZ() + 0.2f, pos.getX() + 0.8f,
                pos.getY() + 0.8f, pos.getZ() + 0.8f);

        List<RedstoneReceiver> receivers = level.getEntitiesOfClass(
                RedstoneReceiver.class,
                box);

        return receivers.isEmpty()
                ? null
                : receivers.getFirst();
    }
}
