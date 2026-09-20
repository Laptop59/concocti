package io.github.laptop59.concocti.common.fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.function.Predicate;

public class ConcoctiFluidTank extends FluidTank {
    protected boolean dirty = false;

    public ConcoctiFluidTank(int capacity) {
        super(capacity);
    }

    public ConcoctiFluidTank(int capacity, Predicate<FluidStack> validator) {
        super(capacity, validator);
    }

    @Override
    protected void onContentsChanged() {
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean clearDirtyFlag() {
        boolean flag = dirty;
        dirty = false;
        return flag;
    }
}
