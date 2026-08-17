package net.bzkgns.maptools;

import net.bzkgns.maptools.client.renderer.entity_detector.EntityDetectorRenderer;
import net.bzkgns.maptools.client.renderer.redstone_receiver.RedstoneReceiverRenderer;
import net.bzkgns.maptools.entities.ModEntities;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = Maptools.MOD_ID, dist = Dist.CLIENT)
public class MaptoolsClient {
    public MaptoolsClient(IEventBus modEventBus, ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::registerEntityRenderers);
    }

    void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        Maptools.LOGGER.info("HELLO FROM CLIENT SETUP");
        Maptools.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }


    public void registerEntityRenderers(
            EntityRenderersEvent.RegisterRenderers event
    ) {
        event.registerEntityRenderer(
                ModEntities.REDSTONE_RECEIVER.get(),
                RedstoneReceiverRenderer::new
        );
        event.registerEntityRenderer(
                ModEntities.ENTITY_DETECTOR.get(),
                EntityDetectorRenderer::new
        );
    }
}
