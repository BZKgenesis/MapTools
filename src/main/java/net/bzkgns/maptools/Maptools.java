package net.bzkgns.maptools;

import net.bzkgns.maptools.client.datagen.ModItemModelProvider;
import net.bzkgns.maptools.commands.DiscardCommand;
import net.bzkgns.maptools.commands.ShowWarningKillConfigCommand;
import net.bzkgns.maptools.entities.ModEntities;
import net.bzkgns.maptools.entities.entity_detector.EntityDetectorInteractionHandler;
import net.bzkgns.maptools.entities.redstone_receiver.RedstoneReceiverInteractionHandler;
import net.bzkgns.maptools.items.ModCreativeTab;
import net.bzkgns.maptools.items.ModItems;
import net.bzkgns.maptools.network.C2S.UpdateEntityDetectorPayload;
import net.bzkgns.maptools.network.C2S.UpdateEntityDetectorPayloadHandler;
import net.bzkgns.maptools.network.S2C.OpenEntityDetectorPayload;
import net.bzkgns.maptools.network.S2C.OpenEntityDetectorPayloadHandler;
import net.bzkgns.maptools.network.S2C.OpenRedstoneReceiverPayload;
import net.bzkgns.maptools.network.S2C.OpenRedstoneReceiverPayloadHandler;
import net.bzkgns.maptools.network.C2S.UpdateRedstoneReceiverPayload;
import net.bzkgns.maptools.network.C2S.UpdateRedstoneReceiverPayloadHandler;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Maptools.MOD_ID)
public class Maptools {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "maptools";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "maptools" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Maptools(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(this::registerPayload);

        ModEntities.register(modEventBus);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);

        ModItems.register(modEventBus);
        ModCreativeTab.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Maptools) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);


        NeoForge.EVENT_BUS.register(RedstoneReceiverInteractionHandler.class);
        NeoForge.EVENT_BUS.register(EntityDetectorInteractionHandler.class);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {

        DiscardCommand.register(event.getDispatcher());
        ShowWarningKillConfigCommand.register(event.getDispatcher());
    }


    public void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        // other providers here
        generator.addProvider(
                event.includeClient(),
                new ModItemModelProvider(output, existingFileHelper)
        );
    }

    public void registerPayload(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                OpenRedstoneReceiverPayload.TYPE,
                OpenRedstoneReceiverPayload.STREAM_CODEC,
                new OpenRedstoneReceiverPayloadHandler()
        );
        registrar.playToServer(
                UpdateRedstoneReceiverPayload.TYPE,
                UpdateRedstoneReceiverPayload.STREAM_CODEC,
                new UpdateRedstoneReceiverPayloadHandler()
        );

        registrar.playToClient(
                OpenEntityDetectorPayload.TYPE,
                OpenEntityDetectorPayload.STREAM_CODEC,
                new OpenEntityDetectorPayloadHandler()
        );
        registrar.playToServer(
                UpdateEntityDetectorPayload.TYPE,
                UpdateEntityDetectorPayload.STREAM_CODEC,
                new UpdateEntityDetectorPayloadHandler()
        );
    }
}
