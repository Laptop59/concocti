package io.github.laptop59.concocti.datagen.client;

import io.github.laptop59.concocti.common.item.ConcoctiItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

public class ConcoctiItemModelProvider extends ItemModelProvider {
    public ConcoctiItemModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (DeferredItem<? extends Item> item : ConcoctiItems.ITEM_LIST) {
            Item heldItem = item.get();
            // If the item is a block item, we create a block model.
            if (heldItem instanceof BlockItem) {
                simpleBlockItem(((BlockItem) heldItem).getBlock());
            } else {
                // Otherwise, a basic item model.
                basicItem(heldItem);
            }
        }
    }
}
