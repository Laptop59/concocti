package io.github.laptop59.concocti.common.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;
import java.util.function.Supplier;

public record MultiblockSimpleBlockPredicate(Supplier<? extends Block> block) implements MultiblockBlockPredicate {

    public MultiblockSimpleBlockPredicate(Block block) {
        this(() -> block);
    }

    @Override
    public MultiblockResult getResult(Level level, BlockPos absolutePos, Direction controllerDirection) {
        Block currentBlock = level.getBlockState(absolutePos).getBlock();
        boolean insteadWasAir = false;
        {
            Optional<HolderSet.Named<Block>> airBlocks = BuiltInRegistries.BLOCK.getTag(BlockTags.AIR);
            if (airBlocks.isPresent()) {
                for (Holder<Block> airBlock : airBlocks.get()) {
                    if (airBlock.value() == currentBlock) {
                        insteadWasAir = true;
                        break;
                    }
                }
            }
        }
        if (currentBlock == block.get()) return null;
        return new MultiblockResult(block.get().defaultBlockState(), insteadWasAir);
    }

    @Override
    public Item getIcon() {
        return block.get().asItem();
    }
}
