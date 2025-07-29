package io.github.laptop59.concocti.common.multiblock;

import io.github.laptop59.concocti.common.block.entity.ConcoctiHatchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface MultiblockBlockPredicate {
    /** Gets the result of this predicate. */
    MultiBlockBlockResult getResult(Level level, BlockPos absolutePos, Direction controllerDirection);

    /** Gets the result of this predicate. */
    default MultiBlockBlockResult getResult(Level level, int x, int y, int z, Direction controllerDirection) {
        return getResult(level, new BlockPos(x, y, z), controllerDirection);
    }

    /** Gets extra data from this predicate. */
    default @Nullable Object getExtraData(Level level, BlockPos absolutePos, Direction controllerDirection) {
        return null;
    }
}
