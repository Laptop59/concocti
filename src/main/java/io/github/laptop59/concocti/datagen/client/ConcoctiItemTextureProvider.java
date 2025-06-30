package io.github.laptop59.concocti.datagen.client;

import io.github.laptop59.concocti.common.item.MoldItem;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ConcoctiItemTextureProvider extends ItemTextureProvider {
    public ConcoctiItemTextureProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void addTextures() {
        // Generate a mold texture for each mold.
        for (MoldItem.Material material : MoldItem.Material.values()) {
            for (MoldItem.Type type : MoldItem.Type.values()) {
                addShapedMoldTexture(material, type);
            }
        }
    }
}
