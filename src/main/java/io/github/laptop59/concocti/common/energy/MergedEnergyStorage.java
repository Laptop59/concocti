package io.github.laptop59.concocti.common.energy;

import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.List;

public class MergedEnergyStorage implements IEnergyStorage {
    protected List<IEnergyStorage> energyStorages;

    public MergedEnergyStorage(List<IEnergyStorage> energyStorages) {
        this.energyStorages = energyStorages;
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        int energyLeft = toReceive;
        for (IEnergyStorage energyStorage : energyStorages) {
            energyLeft -= energyStorage.receiveEnergy(energyLeft, simulate);
            if (energyLeft <= 0) return toReceive;
        }
        return toReceive - energyLeft;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        int energyLeft = toExtract;
        for (IEnergyStorage energyStorage : energyStorages) {
            energyLeft -= energyStorage.extractEnergy(energyLeft, simulate);
            if (energyLeft <= 0) return toExtract;
        }
        return toExtract - energyLeft;
    }

    @Override
    public int getEnergyStored() {
        int stored = 0;
        for (IEnergyStorage energyStorage : energyStorages) {
            int storageStored = energyStorage.getEnergyStored();
            if (storageStored + stored < 0) return Integer.MAX_VALUE; // For integer overflow
            stored += storageStored;
        }
        return stored;
    }

    @Override
    public int getMaxEnergyStored() {
        int stored = 0;
        for (IEnergyStorage energyStorage : energyStorages) {
            int storageStored = energyStorage.getMaxEnergyStored();
            if (storageStored + stored < 0) return Integer.MAX_VALUE; // For integer overflow
            stored += storageStored;
        }
        return stored;
    }

    @Override
    public boolean canExtract() {
        for (IEnergyStorage energyStorage : energyStorages) {
            if (energyStorage.canExtract()) return true;
        }
        return false;
    }

    @Override
    public boolean canReceive() {
        for (IEnergyStorage energyStorage : energyStorages) {
            if (energyStorage.canReceive()) return true;
        }
        return false;
    }
}
