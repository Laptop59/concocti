package io.github.laptop59.concocti.common.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Supplier;

public record MultiblockSimpleBlockPredicate(Supplier<? extends Block> block) implements MultiblockBlockPredicate {
    @Override
    public MultiBlockBlockResult getResult(Level level, BlockPos absolutePos, Direction controllerDirection) {
        Block currentBlock = level.getBlockState(absolutePos).getBlock();
        if (currentBlock == block.get()) return MultiBlockBlockResult.SATISFIED;
        if (currentBlock == Blocks.AIR) return MultiBlockBlockResult.AIR;
        return MultiBlockBlockResult.UNMATCHED;
    }
}
