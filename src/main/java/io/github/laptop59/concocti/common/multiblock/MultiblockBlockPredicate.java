package io.github.laptop59.concocti.common.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface MultiblockBlockPredicate {
    /** Gets the result of this predicate. */
    MultiblockResult getResult(Level level, BlockPos absolutePos, Direction controllerDirection);

    /**
     * Gets the result of this predicate.
     * If this function returns {@code null}, then the predicate is fully (100%) satisfied.
     * Otherwise, the block state returned is the one required for satisfaction.
     */
    default MultiblockResult getResult(Level level, int x, int y, int z, Direction controllerDirection) {
        return getResult(level, new BlockPos(x, y, z), controllerDirection);
    }

    default Item getIcon() {
        return null;
    }

    /** Gets extra data from this predicate. */
    default @Nullable Object getExtraData(Level level, BlockPos absolutePos, Direction controllerDirection) {
        return null;
    }
}
