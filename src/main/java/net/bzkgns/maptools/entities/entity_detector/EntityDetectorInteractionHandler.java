package net.bzkgns.maptools.entities.entity_detector;

import net.bzkgns.maptools.items.ModItems;
import net.bzkgns.maptools.network.S2C.OpenEntityDetectorPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class EntityDetectorInteractionHandler {

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof EntityDetector entityDetector) {
            Level level = event.getLevel();
            if (level.isClientSide()) {return;}

            ItemStack stack = event.getItemStack();
            if (!stack.is(ModItems.ENTITY_DETECTOR_EDITOR.get())) {return;}

            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer,
                        new OpenEntityDetectorPayload(entityDetector.getId(),
                                entityDetector.getConfig()
                        )
                );
            }
            event.setCanceled(true);
        }
    }
}
