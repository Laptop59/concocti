package io.github.laptop59.concocti.common.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

public record MultiblockHorizontalDirectionalBlockPredicate(Block block) implements MultiblockBlockPredicate {
    @Override
    public MultiBlockBlockResult getResult(Level level, BlockPos absolutePos, Direction controllerDirection) {
        BlockState blockState = level.getBlockState(absolutePos);
        Block currentBlock = blockState.getBlock();
        if (currentBlock != block) return currentBlock == Blocks.AIR ? MultiBlockBlockResult.AIR : MultiBlockBlockResult.SATISFIED;
        // Now check block state.
        if (blockState.getValue(HorizontalDirectionalBlock.FACING) == controllerDirection) return MultiBlockBlockResult.SATISFIED;
        return MultiBlockBlockResult.UNMATCHED;
    }
}
