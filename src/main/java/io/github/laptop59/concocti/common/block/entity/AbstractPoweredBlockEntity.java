package io.github.laptop59.concocti.common.block.entity;

import io.github.laptop59.concocti.common.item.ConcoctiItemStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * A class to form a basic block entity, whose block already has energy and item storage available.
 */
public abstract class AbstractPoweredBlockEntity extends BaseContainerBlockEntity implements MenuProvider {

    protected int slotSize;

    public DynamicEnergyStorage energy;

    public int maxEnergy, maxEnergyTransfer;

    protected ConcoctiItemStackHandler itemHandler;

    protected AbstractPoweredBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState, int maxEnergy, int maxEnergyTransfer, int slotSize, Supplier<DynamicEnergyStorage.Mode> mode) {
        super(type, pos, blockState);
        this.slotSize = slotSize;
        this.itemHandler = createItemHandler(slotSize);
        this.maxEnergy = maxEnergy;
        this.maxEnergyTransfer = maxEnergyTransfer;
        this.energy = new DynamicEnergyStorage(this.maxEnergy, maxEnergyTransfer, maxEnergyTransfer, 0, mode);
    }

    public void resetItemHandler(int newSlotsAmount) {
        this.slotSize = newSlotsAmount;
        this.itemHandler.setDirectList(NonNullList.withSize(newSlotsAmount, ItemStack.EMPTY));
    }

    public void setEnergyModeSupplier(Supplier<DynamicEnergyStorage.Mode> supplier) {
        this.energy.mode = supplier;
    }

    private @NotNull ConcoctiItemStackHandler createItemHandler(int slotSize) {
        return new ConcoctiItemStackHandler(slotSize) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                validate(slot);
                return AbstractPoweredBlockEntity.this.isItemValid(slot, stack);
            }
        };
    }

    public void setNewEnergyMultiplier(float multiplier) {
        int newMaxEnergy = (int) (this.maxEnergy * multiplier);
        int newMaxEnergyTransfer = (int) (this.maxEnergyTransfer * multiplier);
        this.energy.setMaxEnergy(newMaxEnergy);
        this.energy.setMaxEnergyTransfer(newMaxEnergyTransfer);
    }

    /**
     * Used to determine the validity of items of a tank of this block entity.
     *
     * @param slot  The index of the tank.
     * @param stack The stack to determine validity for.
     * @return Whether the stack is valid for the tank.
     */
    protected abstract boolean isItemValid(int slot, @NotNull ItemStack stack);

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("items")) itemHandler.deserializeNBT(registries, tag.getCompound("items"));
        if (tag.contains("energy")) {
            energy.deserializeNBT(registries, tag.get("energy"));
        }
    }

    /**
     * Handles the saving of items and energy.
     */
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("energy", energy.serializeNBT(registries));
        tag.put("items", itemHandler.serializeNBT(registries));
    }

}
