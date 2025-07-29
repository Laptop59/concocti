package io.github.laptop59.concocti.common.multiblock;

import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.ConcoctiHatchBlock;
import io.github.laptop59.concocti.common.block.entity.ConcoctiHatchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Arrays;
import java.util.function.Supplier;

public record MultiblockToughConcoctiBrickLikePredicate(Supplier<? extends ConcoctiHatchBlock>... blocks) implements MultiblockBlockPredicate {
    @SafeVarargs
    public MultiblockToughConcoctiBrickLikePredicate {
    }

    @Override
    public MultiBlockBlockResult getResult(Level level, BlockPos absolutePos, Direction controllerDirection) {
        BlockState blockState = level.getBlockState(absolutePos);
        Block currentBlock = blockState.getBlock();
        if (currentBlock == ConcoctiBlocks.CONCOCTI_BRICKS.get() ||
               Arrays.stream(blocks).anyMatch(block -> block == currentBlock))
            return MultiBlockBlockResult.SATISFIED;
        if (currentBlock == Blocks.AIR) return MultiBlockBlockResult.AIR;
        return MultiBlockBlockResult.UNMATCHED;
    }

    @Override
    public Data getExtraData(Level level, BlockPos absolutePos, Direction controllerDirection) {
        BlockEntity blockEntity = level.getBlockEntity(absolutePos);
        if (blockEntity instanceof ConcoctiHatchBlockEntity entity) return new Data(entity);
        return null;
    }

    public record Data(ConcoctiHatchBlockEntity entity) {}
}
