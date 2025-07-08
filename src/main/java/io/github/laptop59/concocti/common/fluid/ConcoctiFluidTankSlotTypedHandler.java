package io.github.laptop59.concocti.common.fluid;

import io.github.laptop59.concocti.client.gui.components.SlotFlag;
import io.github.laptop59.concocti.client.gui.components.SlotType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class ConcoctiFluidTankSlotTypedHandler extends ConcoctiFluidTankHandler {
    final SlotType slotType;

    public ConcoctiFluidTankSlotTypedHandler(Supplier<List<IFluidTank>> tanks, SlotType slotType) {
        super(tanks);
        this.slotType = slotType;
    }

    @Override
    public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
        if (slotType != null && !slotType.isSet(SlotFlag.INPUT)) return 0;
        FluidStack filler = resource.copy();
        for (IFluidTank tank : tanks.get()) {
            filler.setAmount(filler.getAmount() - tank.fill(resource, action));
            if (filler.getAmount() == 0) return resource.getAmount();
        }
        return resource.getAmount() - filler.getAmount();
    }

    @Override
    public @NotNull FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) {
        if (slotType != null && !slotType.isSet(SlotFlag.OUTPUT)) {
            FluidStack empty = FluidStack.EMPTY.copy();
            return empty;
        }
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
        if (slotType != null && !slotType.isSet(SlotFlag.OUTPUT)) {
            FluidStack empty = FluidStack.EMPTY.copy();
            return empty;
        }
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
}
