package io.github.laptop59.concocti.datagen.server;

import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ConcoctiBlockTagsProvider extends BlockTagsProvider {
    public ConcoctiBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, modId, existingFileHelper);
    }

    private final HashMap<ResourceLocation, IntrinsicTagAppender<Block>> blockTags = new HashMap<>();

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        // Create new tags.
        EnumMap<ConcoctiBlocks.BlockToolRank, IntrinsicTagAppender<Block>> toolRankTags = new EnumMap<>(ConcoctiBlocks.BlockToolRank.class);
        for (ConcoctiBlocks.BlockToolRank rank : ConcoctiBlocks.BlockToolRank.values()) {
            toolRankTags.put(rank,
                    this.tag(TagKey.create(Registries.BLOCK,
                            // There is a netherite tool tag, but in the neoforge namespace!
                            ResourceLocation.fromNamespaceAndPath(
                                    rank == ConcoctiBlocks.BlockToolRank.NETHERITE ? "neoforge" : ResourceLocation.DEFAULT_NAMESPACE,
                                    "needs_" + rank.rank + "_tool")
                    ))
            );
        }
        EnumMap<ConcoctiBlocks.BlockToolType, IntrinsicTagAppender<Block>> toolTypeTags = new EnumMap<>(ConcoctiBlocks.BlockToolType.class);
        for (ConcoctiBlocks.BlockToolType type : ConcoctiBlocks.BlockToolType.values()) {
            toolTypeTags.put(type,
                    this.tag(TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("mineable/" + type.type)))
            );
        }
        // Then, sort blocks into these tags.
        for (Map.Entry<DeferredBlock<? extends Block>, ConcoctiBlocks.BlockData> block : ConcoctiBlocks.BLOCK_MAP.entrySet()) {
            ConcoctiBlocks.BlockData data = block.getValue();
            IntrinsicTagAppender<Block> rankTag = toolRankTags.get(data.toolRank());
            IntrinsicTagAppender<Block> typeTag = toolTypeTags.get(data.toolType());
            rankTag.add(block.getKey().get());
            typeTag.add(block.getKey().get());
        }

        // Add blocks to other tags.
        addToTag(ConcoctiBlocks.Tags.GUARDED_BY_ENDERMEN, ConcoctiBlocks.CRYSTALIUM_ORE, ConcoctiBlocks.CRYSTALIUM_BLOCK);
    }

    @SafeVarargs
    protected final void addToTag(TagKey<Block> tag, DeferredBlock<Block>... blocks) {
        // Make a tag appender if there isn't one already.
        IntrinsicTagAppender<Block> addedTo;
        if (!blockTags.containsKey(tag.location())) {
            addedTo = this.tag(tag);
            blockTags.put(tag.location(), addedTo);
        } else addedTo = blockTags.get(tag.location());
        // Add the block to the tag.
        for (DeferredBlock<Block> block : blocks) addedTo.add(block.get());
    }

}
