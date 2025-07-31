package io.github.laptop59.concocti.common.util;

import io.github.laptop59.concocti.common.Concocti;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

/** Provides utility functions for transferring resources between handlers. */
public class ConcoctiTransferrer {
    private ConcoctiTransferrer() {}

    /**
     * Simulate transferring items from one handler to another.
     *
     * @param from Handler to take items from.
     * @param to   Handler to put items to.
     * @return The total number of items that can be transferred summed up from each slot.
     */
    public static int simulatePossibleItemsToTransfer(@NotNull IItemHandler from, @NotNull IItemHandler to, boolean fillExistingStacks) {
        int insertCount = 0;
        for (int i = 0; i < from.getSlots(); i++) {
            ItemStack toTake = from.extractItem(i, Integer.MAX_VALUE, true);
            if (toTake.isEmpty()) continue;
            int extractCount = toTake.getCount();
            ItemStack left = fillExistingStacks ?
                    ItemHandlerHelper.insertItemStacked(to, toTake, true) :
                    ItemHandlerHelper.insertItem(to, toTake, true);
            insertCount += extractCount - left.getCount();
        }
        return insertCount;
    }

    /**
     * Transfer items from one handler to another.
     *
     * @param from Handler to take items from.
     * @param to   Handler to put items to.
     */
    public static void transfer(@NotNull IItemHandler from, @NotNull IItemHandler to, boolean fillExistingStacks) {
        // Transfer all the items possible from `from` to `to`.
        // Taken from MI.
        // https://github.com/AztechMC/Modern-Industrialization/blob/c5a997baf3be596049c031bc0b4a7915def7b99d/src/main/java/aztech/modern_industrialization/util/TransferHelper.java#L38
        for (int i = 0; i < from.getSlots(); i++) {
            // First, simulate.
            ItemStack toTake = from.extractItem(i, Integer.MAX_VALUE, true);
            if (toTake.isEmpty()) continue;
            int extractCount = toTake.getCount();
            ItemStack left = fillExistingStacks ?
                    ItemHandlerHelper.insertItemStacked(to, toTake, true) :
                    ItemHandlerHelper.insertItem(to, toTake, true);
            int insertCount = extractCount - left.getCount();
            if (insertCount <= 0) continue;
            // Now we can execute the action.
            toTake = from.extractItem(i, insertCount, false);
            if (toTake.isEmpty()) continue;
            left = fillExistingStacks ?
                    ItemHandlerHelper.insertItemStacked(to, toTake, false) :
                    ItemHandlerHelper.insertItem(to, toTake, false);
            if (!left.isEmpty()) {
                // Try to give the taken items back if possible.
                left = from.insertItem(i, left, false);
                if (!left.isEmpty()) {
                    Concocti.LOGGER.warn("Could not provide back {} to item handler {}, voiding them.", left, to);
                }
            }
        }
    }

    /**
     * Transfer fluids from one handler to another.
     *
     * @param from Handler to take fluids from.
     * @param to   Handler to put fluids to.
     */
    public static void transfer(@NotNull IFluidHandler from, @NotNull IFluidHandler to) {
        int iterations = 0;
        final int MAX_FLUID_ITERATIONS = 8192;
        while (!transfer(from, to, Integer.MAX_VALUE, false).isEmpty()) {
            if (++iterations > MAX_FLUID_ITERATIONS) {
                Concocti.LOGGER.warn("Iterating fluid transfer took more than {} iterations!", MAX_FLUID_ITERATIONS);
                break;
            }
        }
    }

    /**
     * Transfer energy from one storage to another.
     *
     * @param from Storage to extract energy from.
     * @param to   Storage to insert energy to.
     */
    public static void transfer(@NotNull IEnergyStorage from, @NotNull IEnergyStorage to) {
        if (!from.canExtract() || !to.canReceive()) return;
        int energy = Math.min(from.extractEnergy(Integer.MAX_VALUE, true), to.receiveEnergy(Integer.MAX_VALUE, true));
        // Now try to extract and put.
        to.receiveEnergy(from.extractEnergy(energy, false), false);
    }

    /**
     * Transfer fluids from one handler to another. This function is a fixed version of NeoForge's handler, which does
     * not handle multi-tank to multi-tank transactions properly.
     *
     * @param from      Handler to take fluids from.
     * @param to        Handler to put fluids to.
     * @param maxAmount The maximum amount of fluid from a tank to take from the {@code from} handler.
     * @param simulated Whether the transfer is simulated or not.
     */
    public static FluidStack transfer(@NotNull IFluidHandler from, @NotNull IFluidHandler to, int maxAmount, boolean simulated) {
        // Taken from MI.
        // https://github.com/AztechMC/Modern-Industrialization/blob/c5a997baf3be596049c031bc0b4a7915def7b99d/src/main/java/aztech/modern_industrialization/util/TransferHelper.java#L147
        int tanks = from.getTanks();
        for (int i = 0; i < tanks; ++i) {
            FluidStack toTry = from.getFluidInTank(i).copy();
            if (toTry.getAmount() > maxAmount) {
                toTry.setAmount(maxAmount);
            }
            FluidStack drainable = from.drain(toTry, IFluidHandler.FluidAction.SIMULATE);
            if (drainable.isEmpty()) {
                continue;
            }
            int fillableAmount = to.fill(drainable, IFluidHandler.FluidAction.SIMULATE);
            if (fillableAmount > 0) {
                drainable.setAmount(fillableAmount);
                if (!simulated) {
                    FluidStack drained = from.drain(drainable, IFluidHandler.FluidAction.EXECUTE);
                    if (!drained.isEmpty()) {
                        drained.setAmount(to.fill(drained, IFluidHandler.FluidAction.EXECUTE));
                        return drained;
                    }
                } else {
                    return drainable;
                }
            }
        }
        return FluidStack.EMPTY;
    }
}
