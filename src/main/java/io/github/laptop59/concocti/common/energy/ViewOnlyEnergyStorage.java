package io.github.laptop59.concocti.common.energy;

import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.function.Supplier;

public class ViewOnlyEnergyStorage implements IEnergyStorage {
    protected Supplier<IEnergyStorage> energyStorage;

    public ViewOnlyEnergyStorage(Supplier<IEnergyStorage> energyStorage) {
        this.energyStorage = energyStorage;
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return energyStorage.get().getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return energyStorage.get().getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return false;
    }
}
