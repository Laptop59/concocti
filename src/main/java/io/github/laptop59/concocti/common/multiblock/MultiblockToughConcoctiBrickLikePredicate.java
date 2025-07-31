package io.github.laptop59.concocti.common.multiblock;

import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.ConcoctiHatchBlock;
import io.github.laptop59.concocti.common.block.entity.ConcoctiHatchBlockEntity;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
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
    public MultiblockToughConcoctiBrickLikePredicate {}

    @Override
    public MultiblockResult getResult(Level level, BlockPos absolutePos, Direction controllerDirection) {
        BlockState blockState = level.getBlockState(absolutePos);
        Block currentBlock = blockState.getBlock();
        boolean insteadWasAir = currentBlock == Blocks.AIR;
        if (currentBlock == ConcoctiBlocks.TOUGH_CONCOCTI_BRICKS.get() ||
               Arrays.stream(blocks).anyMatch(block -> block.get() == currentBlock))
            return null;
        return new MultiblockResult(
                ConcoctiBlocks.TOUGH_CONCOCTI_BRICKS.get().defaultBlockState(),
                insteadWasAir
        );
    }

    @Override
    public Data getExtraData(Level level, BlockPos absolutePos, Direction controllerDirection) {
        BlockEntity blockEntity = level.getBlockEntity(absolutePos);
        if (blockEntity instanceof ConcoctiHatchBlockEntity entity) return new Data(entity);
        return null;
    }

    public record Data(ConcoctiHatchBlockEntity entity) {}

    @Override
    public Item getIcon() {
        return ConcoctiItems.TOUGH_CONCOCTI_BRICKS.get();
    }
}
