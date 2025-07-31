package io.github.laptop59.concocti.common.fluid;

import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ViewOnlyFluidHandler implements IFluidHandler {
    protected Supplier<IFluidHandler> fluidHandler;

    public ViewOnlyFluidHandler(Supplier<IFluidHandler> fluidHandler) {
        this.fluidHandler = fluidHandler;
    }

    @Override
    public int getTanks() {
        return fluidHandler.get().getTanks();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return fluidHandler.get().getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return fluidHandler.get().getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return fluidHandler.get().isFluidValid(tank, stack);
    }

    @Override
    public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) {
        return FluidStack.EMPTY.copy();
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
        return FluidStack.EMPTY.copy();
    }
}
