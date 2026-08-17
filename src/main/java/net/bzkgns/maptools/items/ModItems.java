package net.bzkgns.maptools.items;

import net.bzkgns.maptools.Maptools;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Maptools.MOD_ID);
    public static final DeferredItem<Item> REDSTONE_RECEIVER_EDITOR = ITEMS.registerSimpleItem("redstone_receiver_editor", new Item.Properties());

    public static final DeferredItem<Item> ENTITY_DETECTOR_EDITOR = ITEMS.registerSimpleItem("entity_detector_editor", new Item.Properties());

    public static void register(IEventBus modEventBus) {
        Maptools.LOGGER.info("Registering Mod Items for " + Maptools.MOD_ID);
        ITEMS.register(modEventBus);
    }
}
