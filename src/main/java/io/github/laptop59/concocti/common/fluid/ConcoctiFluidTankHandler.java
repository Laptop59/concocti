package io.github.laptop59.concocti.common.fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class ConcoctiFluidTankHandler implements IFluidHandler {
    Supplier<List<IFluidTank>> tanks;

    public ConcoctiFluidTankHandler(Supplier<List<IFluidTank>> tanks) {
        this.tanks = tanks;
    }

    @Override
    public int getTanks() {
        return tanks.get().size();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return tanks.get().get(tank).getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return tanks.get().get(tank).getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return tanks.get().get(tank).isFluidValid(stack);
    }

    @Override
    public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
        FluidStack filler = resource.copy();
        for (IFluidTank tank : tanks.get()) {
            filler.setAmount(filler.getAmount() - tank.fill(resource, action));
            if (filler.getAmount() == 0) return resource.getAmount();
        }
        return resource.getAmount() - filler.getAmount();
    }

    @Override
    public @NotNull FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) {
        FluidStack totalUndrained = resource.copy();
        for (IFluidTank tank : tanks.get()) {
            FluidStack drained = tank.drain(totalUndrained, FluidAction.SIMULATE);
            if (!drained.isEmpty())
                drained = tank.drain(resource, action);
            if (!FluidStack.isSameFluid(drained, totalUndrained)) continue; // This should never happen.
            totalUndrained.setAmount(totalUndrained.getAmount() - drained.getAmount());
            if (totalUndrained.isEmpty()) break;
        }
        FluidStack totalDrained = resource.copy();
        totalDrained.setAmount(resource.getAmount() - totalUndrained.getAmount());
        return totalDrained;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
        FluidStack totalDrained = FluidStack.EMPTY.copy();
        for (IFluidTank tank : tanks.get()) {
            FluidStack drained = tank.drain(maxDrain, FluidAction.SIMULATE);
            boolean drainSuccess = false;
            if (FluidStack.isSameFluidSameComponents(totalDrained, drained)) {
                totalDrained.setAmount(totalDrained.getAmount() + drained.getAmount());
                drainSuccess = true;
            } else if (totalDrained.isEmpty()) {
                totalDrained = drained.copy();
                drainSuccess = true;
            }
            if (drainSuccess && action.execute()) tank.drain(maxDrain, action);
        }
        return totalDrained;
    }

    public IFluidHandler whitelistTanks(IFluidTank... tanks) {
        Set<IFluidTank> whitelisted = new HashSet<>(tanks.length);
        whitelisted.addAll(Arrays.asList(tanks));
        ConcoctiFluidTankHandler parentHandler = this;
        return new IFluidHandler() {
            private boolean isBlacklisted(int tank) {
                return !whitelisted.contains(parentHandler.tanks.get().get(tank));
            }

            @Override
            public int getTanks() {
                return parentHandler.getTanks();
            }

            @Override
            public @NotNull FluidStack getFluidInTank(int tank) {
                if (isBlacklisted(tank)) return FluidStack.EMPTY.copy();
                return parentHandler.getFluidInTank(tank);
            }

            @Override
            public int getTankCapacity(int tank) {
                if (isBlacklisted(tank)) return 0;
                return parentHandler.getTankCapacity(tank);
            }

            @Override
            public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
                if (isBlacklisted(tank)) return false;
                return parentHandler.isFluidValid(tank, stack);
            }

            @Override
            public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
                FluidStack filler = resource.copy();
                for (IFluidTank tank : tanks) {
                    if (!whitelisted.contains(tank)) continue;
                    filler.setAmount(filler.getAmount() - tank.fill(resource, action));
                    if (filler.getAmount() == 0) return resource.getAmount();
                }
                return resource.getAmount() - filler.getAmount();
            }

            @Override
            public @NotNull FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) {
                FluidStack totalUndrained = resource.copy();
                for (IFluidTank tank : tanks) {
                    if (!whitelisted.contains(tank)) continue;
                    FluidStack drained = tank.drain(totalUndrained, FluidAction.SIMULATE);
                    if (!drained.isEmpty())
                        drained = tank.drain(resource, action);
                    if (!FluidStack.isSameFluid(drained, totalUndrained)) continue; // This should never happen.
                    totalUndrained.setAmount(totalUndrained.getAmount() - drained.getAmount());
                    if (totalUndrained.isEmpty()) break;
                }
                FluidStack totalDrained = resource.copy();
                totalDrained.setAmount(resource.getAmount() - totalUndrained.getAmount());
                return totalDrained;
            }

            @Override
            public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
                FluidStack totalDrained = FluidStack.EMPTY.copy();
                for (IFluidTank tank : tanks) {
                    if (!whitelisted.contains(tank)) continue;
                    FluidStack drained = tank.drain(maxDrain, FluidAction.SIMULATE);
                    boolean drainSuccess = false;
                    if (FluidStack.isSameFluidSameComponents(totalDrained, drained)) {
                        totalDrained.setAmount(totalDrained.getAmount() + drained.getAmount());
                        drainSuccess = true;
                    } else if (totalDrained.isEmpty()) {
                        totalDrained = drained.copy();
                        drainSuccess = true;
                    }
                    if (drainSuccess && action.execute()) tank.drain(maxDrain, action);
                }
                return totalDrained;
            }
        };
    }
}
