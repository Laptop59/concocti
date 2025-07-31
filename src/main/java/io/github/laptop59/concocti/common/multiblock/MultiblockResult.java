package io.github.laptop59.concocti.common.multiblock;

import net.minecraft.world.level.block.state.BlockState;

public record MultiblockResult(BlockState blockState, boolean insteadWasAir) {
}
