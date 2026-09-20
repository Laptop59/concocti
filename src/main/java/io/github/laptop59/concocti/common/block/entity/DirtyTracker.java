package io.github.laptop59.concocti.common.block.entity;

import io.github.laptop59.concocti.common.fluid.ConcoctiFluidTank;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.Objects;

/** Tracks what properties should be updated to clients from a machine. */
public class DirtyTracker {
    final AbstractConcoctiMachineBlockEntity<?, ?, ?, ?, ?> entity;

    boolean baseDirty = false;
    boolean settingsDirty = false;
    boolean fluidsDirty = false;
    boolean extraDirty = false;

    // Base
    int ticksLeft;
    int totalTicks;
    int tickMultiplier;
    int energyStored;
    int maxEnergyStored;

    // Settings
    boolean ejectOn;
    boolean pullOn;

    // Fluids
    FluidStack[] fluidStacks;

    // Extra
    Object extraData;

    public DirtyTracker(AbstractConcoctiMachineBlockEntity<?, ?, ?, ?, ?> entity) {
        this.entity = entity;
    }

    public void update() {
        if (ticksLeft != entity.ticksLeft || totalTicks != entity.totalTicks ||
                tickMultiplier != entity.getTickMultiplier() || entity.energy.clearDirtyFlag()) {
            baseDirty = true;
        }

        if (ejectOn != entity.ejectOn || pullOn != entity.pullOn || entity.machineSettings.clearDirtyFlag()) {
            settingsDirty = true;
        }

        List<ConcoctiFluidTank> tanks = entity.getIndexedFluidHandlers();
        fluidStacks = new FluidStack[tanks.size()];
        int i = 0;
        for (ConcoctiFluidTank tank : tanks) {
            if (tank.clearDirtyFlag()) {
                fluidsDirty = true;
                 fluidStacks[i] = tank.getFluid();
            }
            i += 1;
        }

        Object extraData = entity.getExtraData();
        if (Objects.equals(extraData, this.extraData)) {
            extraDirty = true;
        }
        this.extraData = extraData;

        updateProperties();
    }

    protected void updateProperties() {
        ticksLeft = entity.ticksLeft;
        totalTicks = entity.totalTicks;
        tickMultiplier = entity.getTickMultiplier();
        energyStored = entity.energy.getEnergyStored();
        maxEnergyStored = entity.energy.getMaxEnergyStored();
        ejectOn = entity.ejectOn;
        pullOn = entity.pullOn;
    }

    public boolean clearDirtyFlag() {
        boolean flag = baseDirty || settingsDirty || fluidsDirty || extraDirty;
        baseDirty = false;
        settingsDirty = false;
        fluidsDirty = false;
        extraDirty = false;
        return flag;
    }

    public FluidStack[] getDirtyFluidStacks() {
        return fluidStacks;
    }
}
