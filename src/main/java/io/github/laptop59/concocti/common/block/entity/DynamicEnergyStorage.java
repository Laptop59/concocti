package io.github.laptop59.concocti.common.block.entity;

import net.minecraft.util.Mth;
import net.neoforged.neoforge.energy.EnergyStorage;

import java.util.function.Supplier;

/**
 * An {@link net.neoforged.neoforge.energy.EnergyStorage} with a variable capacity.
 */
public class DynamicEnergyStorage extends EnergyStorage {
    Supplier<Mode> mode;

    public static int NONE_FLAG = 0x00;
    public static int INPUT_FLAG = 0x01;
    public static int OUTPUT_FLAG = 0x02;

    protected boolean dirty = false;

    public enum Mode {
        NONE(NONE_FLAG),
        INPUT_ONLY(INPUT_FLAG),
        OUTPUT_ONLY(OUTPUT_FLAG),
        INPUT_OUTPUT(INPUT_FLAG | OUTPUT_FLAG);

        public final int flags;

        Mode(int flags) {
            this.flags = flags;
        }

        public Supplier<Mode> toSupplier() {
            return () -> this;
        }
    }

    public DynamicEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy, Supplier<Mode> isOutput) {
        super(capacity, maxReceive, maxExtract, energy);
        this.mode = isOutput;
    }

    public void setMaxEnergy(int capacity) {
        if (this.capacity != capacity) {
            dirty = true;
        }
        this.capacity = capacity;
        if (energy > capacity) energy = capacity;
    }

    public void setMaxEnergyTransfer(int transfer) {
        if (this.maxReceive != transfer || this.maxExtract != transfer) {
            dirty = true;
        }
        this.maxReceive = transfer;
        this.maxExtract = transfer;
    }

    @Override
    public boolean canExtract() {
        int flags = mode.get().flags;
        if ((flags & OUTPUT_FLAG) == 0) return false;
        return this.maxExtract > 0;
    }

    @Override
    public boolean canReceive() {
        int flags = mode.get().flags;
        if ((flags & INPUT_FLAG) == 0) return false;
        return this.maxReceive > 0;
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        return canReceive() ? forceReceiveEnergy(toReceive, simulate) : 0;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        return canExtract() ? forceExtractEnergy(toExtract, simulate) : 0;
    }

    public int forceReceiveEnergy(int toReceive, boolean simulate) {
        if (toReceive <= 0) {
            return 0;
        }

        int energyReceived = Mth.clamp(this.capacity - this.energy, 0, Math.min(this.maxReceive, toReceive));
        if (!simulate)
            this.energy += energyReceived;
        if (energyReceived != 0) {
            dirty = true;
        }
        return energyReceived;
    }

    public int forceExtractEnergy(int toExtract, boolean simulate) {
        if (toExtract <= 0) {
            return 0;
        }

        int energyExtracted = Math.min(this.energy, Math.min(this.maxExtract, toExtract));
        if (!simulate)
            this.energy -= energyExtracted;
        if (energyExtracted != 0) {
            dirty = true;
        }
        return energyExtracted;
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
