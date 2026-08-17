package net.bzkgns.maptools.client.datagen;

import net.bzkgns.maptools.Maptools;
import net.bzkgns.maptools.items.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Maptools.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

        basicItem(ModItems.REDSTONE_RECEIVER_EDITOR.get());
        basicItem(ModItems.ENTITY_DETECTOR_EDITOR.get());
    }
}