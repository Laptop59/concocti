package io.github.laptop59.concocti.common.fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;

public class MergedViewOnlyFluidHandler implements IFluidHandler {
    protected List<IFluidHandler> fluidHandlers;

    protected record TranslatedSlot(IFluidHandler handler, int slot) {}

    protected TranslatedTank translate(int tank) {
        int size = 0;
        for (IFluidHandler handler : fluidHandlers) {
            int firstTank = size;
            size += handler.getTanks();
            if (tank >= size) continue;
            return new TranslatedTank(handler, tank - firstTank);
        }
        throw new IllegalArgumentException("Tank is out of bounds (provided was " + tank + ", size is " + getTanks() + ").");
    }

    @Override
    public int getTanks() {
        int tanks = 0;
        for (IFluidHandler fluidHandler : fluidHandlers)
            tanks += fluidHandler.getTanks();
        return tanks;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        TranslatedTank translatedTank = translate(tank);
        return translatedTank.handler.getFluidInTank(translatedTank.tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        TranslatedTank translatedTank = translate(tank);
        return translatedTank.handler.getTankCapacity(translatedTank.tank);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        TranslatedTank translatedTank = translate(tank);
        return translatedTank.handler.isFluidValid(translatedTank.tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY.copy();
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY.copy();
    }

    protected record TranslatedTank(IFluidHandler handler, int tank) {}

    public MergedViewOnlyFluidHandler(List<IFluidHandler> fluidHandlers) {
        this.fluidHandlers = fluidHandlers;
    }
}
