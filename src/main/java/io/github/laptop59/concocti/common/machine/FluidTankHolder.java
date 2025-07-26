package io.github.laptop59.concocti.common.machine;

import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;

public interface FluidTankHolder {
    List<IFluidHandler> getIndexedFluidHandlers();
}
