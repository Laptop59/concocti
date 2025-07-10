package io.github.laptop59.concocti.datagen.server;

import io.github.laptop59.concocti.common.ConcoctiRegisters;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

public class ConcoctiBlockLootSubProvider extends BlockLootSubProvider {
    public ConcoctiBlockLootSubProvider(HolderLookup.Provider lookupProvider) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, lookupProvider);
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        // The contents of our DeferredRegister.
        return ConcoctiRegisters.BLOCKS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .collect(Collectors.toSet());
    }

    @Override
    protected void generate() {
        for (DeferredBlock<? extends Block> deferredBlock : ConcoctiBlocks.BLOCK_MAP.keySet()) {
            Block block = deferredBlock.get();
            if (block instanceof BaseEntityBlock) {
                add(block, createNameableBlockEntityTable(block));
            } else {
                dropSelf(block);
            }
        }
    }
}
