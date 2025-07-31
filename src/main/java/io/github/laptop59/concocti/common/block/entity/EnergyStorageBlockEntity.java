package io.github.laptop59.concocti.common.block.entity;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public interface EnergyStorageBlockEntity {
    @Nullable
    IEnergyStorage getSidedEnergyStorage(Direction direction);
}
