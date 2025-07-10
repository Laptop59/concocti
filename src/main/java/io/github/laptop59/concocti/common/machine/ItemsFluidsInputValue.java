package io.github.laptop59.concocti.common.machine;

import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

public record ItemsFluidsInputValue(
        IItemHandler itemHandler,
        IFluidHandler fluidHandler
) {
    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }
}
