package io.github.laptop59.concocti.common.block.entity;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public interface FluidHandlerBlockEntity {
    @Nullable
    IFluidHandler getSidedFluidHandler(Direction direction);
}
