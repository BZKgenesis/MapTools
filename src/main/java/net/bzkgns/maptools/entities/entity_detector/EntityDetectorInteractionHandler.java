package net.bzkgns.maptools.entities.entity_detector;

import net.bzkgns.maptools.data_components.ModDataComponents;
import net.bzkgns.maptools.entities.ModEntities;
import net.bzkgns.maptools.items.ModItems;
import net.bzkgns.maptools.network.S2C.OpenEntityDetectorPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.awt.*;

public class EntityDetectorInteractionHandler {

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof EntityDetector entityDetector) {
            Level level = event.getLevel();
            if (level.isClientSide())
                return;

            ItemStack stack = event.getItemStack();
            if (!stack.is(ModItems.ENTITY_DETECTOR_EDITOR.get()))
                return;

            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer,
                        new OpenEntityDetectorPayload(entityDetector.getId(),
                                entityDetector.getConfig()));
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLeftClickEntity(AttackEntityEvent event) {
        if (!(event.getTarget() instanceof EntityDetector detector))
            return;
        Player player = event.getEntity();
        if (!player.getMainHandItem().is(ModItems.ENTITY_DETECTOR_EDITOR.get()))
            return;
        if (player.level().isClientSide())
            return;
        detector.discard();
        event.getEntity().displayClientMessage(
                Component.literal(detector.getDisplayName().getString() + " removed.").withColor(Color.RED.getRGB()),
                true);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getItemStack().is(ModItems.ENTITY_DETECTOR_EDITOR.get()))
            return;
        if (event.getLevel().isClientSide())
            return;
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        BlockHitResult hit = event.getHitVec();
        Vec3 pos = hit.getLocation();
        EntityDetector detector = ModEntities.ENTITY_DETECTOR.get().create(player.level());
        if (detector == null)
            return;
        EntityDetectorConfig config = event.getItemStack().get(ModDataComponents.ENTITY_DETECTOR_CONFIG.get());

        detector.setPos(pos);
        player.level().addFreshEntity(detector);
        if (config != null) {
            detector.setConfigNoPos(config);
        }
        event.setCanceled(true);
    }
}
