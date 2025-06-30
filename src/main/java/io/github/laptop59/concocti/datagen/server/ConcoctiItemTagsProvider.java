package io.github.laptop59.concocti.datagen.server;

import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.item.MoldItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.internal.NeoForgeItemTagsProvider;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ConcoctiItemTagsProvider extends ItemTagsProvider {
    public ConcoctiItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                    CompletableFuture<TagsProvider.TagLookup<Block>> parentProvider,
                                    String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, parentProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        // Create new tags.
        for (Map.Entry<MoldItem.Material, Map<MoldItem.Type, DeferredItem<? extends Item>>> entry : ConcoctiItems.MOLDS.entrySet()) {
            for (Map.Entry<MoldItem.Type, DeferredItem<? extends Item>> entry2 : entry.getValue().entrySet()) {
                this.tag(ConcoctiItems.Tags.MOLDS.get(entry2.getKey())).add(entry2.getValue().get());
            }
        }
    }
}
