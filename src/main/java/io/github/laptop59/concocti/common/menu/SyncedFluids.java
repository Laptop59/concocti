package io.github.laptop59.concocti.common.menu;

import net.neoforged.neoforge.fluids.FluidStack;

/// A structure that synchronizes and stores fluids for a machine's menu.
public final class SyncedFluids {
    private FluidStack[] fluidStacks;

    /// Creates a new instance allocating the given number of fluid stacks.
    public SyncedFluids(int size) {
        fluidStacks = new FluidStack[size];
        for (int i = 0; i < size; i++) {
            fluidStacks[i] = FluidStack.EMPTY;
        }
    }

    /// Creates a new instance from the given data.
    public SyncedFluids(io.github.laptop59.concocti.common.synchronization.SyncedFluids data) {
        FluidStack[] dataStacks = data.fluidStacks();
        fluidStacks = new FluidStack[dataStacks.length];
        for (int i = 0; i < dataStacks.length; i++) {
            fluidStacks[i] = dataStacks[i] == null ? FluidStack.EMPTY : dataStacks[i];
        }
    }

    /// Updates this fluid storage with the given data.
    public void sync(io.github.laptop59.concocti.common.synchronization.SyncedFluids data) {
        if (data.fluidStacks().length != fluidStacks.length) {
            throw new RuntimeException("Lengths don't match!");
        }
        for (int i = 0; i < fluidStacks.length; i++) {
            FluidStack stack = data.fluidStacks()[i];
            if (stack != null)
                fluidStacks[i] = stack;
        }
    }

    /// Gets the synced fluid stack in the given index.
    public FluidStack get(int index) {
        return fluidStacks[index];
    }

    /// Sets the synced fluid stack in the given index.
    public void set(int index, FluidStack stack) {
        fluidStacks[index] = stack;
    }
}
