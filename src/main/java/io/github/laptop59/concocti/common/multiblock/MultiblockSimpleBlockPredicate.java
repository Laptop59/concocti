package io.github.laptop59.concocti.common.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public record MultiblockSimpleBlockPredicate(Supplier<? extends Block> block) implements MultiblockBlockPredicate {
    @Override
    public BlockState getResult(Level level, BlockPos absolutePos, Direction controllerDirection) {
        Block currentBlock = level.getBlockState(absolutePos).getBlock();
        if (currentBlock == block.get()) return null;
        return block.get().defaultBlockState();
    }

    @Override
    public Item getIcon() {
        return block.get().asItem();
    }
}
