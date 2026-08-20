package net.bzkgns.maptools.items;

import net.bzkgns.maptools.Maptools;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTab {

    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "maptools" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Maptools.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAP_TOOLS_TAB = CREATIVE_MODE_TABS.register("map_tools_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.maptools")) //The language key for the title of your CreativeModeTab
            .icon(() -> ModItems.REDSTONE_RECEIVER_EDITOR.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.REDSTONE_RECEIVER_EDITOR.get());
                output.accept(ModItems.ENTITY_DETECTOR_EDITOR.get());
            }).build());

    public static void register(IEventBus modEventBus) {
        Maptools.LOGGER.info("Registering Mod Creative Tabs for " + Maptools.MOD_ID);
        CREATIVE_MODE_TABS.register(modEventBus);
    }

}
