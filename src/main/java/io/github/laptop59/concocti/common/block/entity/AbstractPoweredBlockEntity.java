package io.github.laptop59.concocti.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * A class to form a basic block entity, whose block already has energy and item storage available.
 */
public abstract class AbstractPoweredBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, MenuProvider {

    protected final int SIZE;

    public DynamicEnergyStorage energy;

    // Our item stack list. This is not final due to #setItems existing.
    protected NonNullList<ItemStack> items;

    public final int baseEnergy, baseEnergyTransfer;

    public final IItemHandler itemHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return SIZE;
        }

        private boolean indexInvalid(int slot) { return slot >= getSlots(); }

        private void validate(int slot) {  if (indexInvalid(slot)) throw new IllegalArgumentException("Size of slots is " + SIZE + ", got: " + slot); }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            validate(slot);
            return items.get(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) return ItemStack.EMPTY;
            if (!isItemValid(slot, stack)) return stack;
            validate(slot);
            ItemStack existing = items.get(slot);
            int limit = getStackLimit(slot, stack);
            if (!existing.isEmpty()) {
                if (!ItemStack.isSameItemSameComponents(stack, existing))
                    return stack;
                limit -= existing.getCount();
            }
            if (limit <= 0) return stack;
            boolean reachedLimit = stack.getCount() > limit;
            if (!simulate) {
                if (existing.isEmpty()) {
                    items.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack);
                } else {
                    existing.grow(reachedLimit ? limit : stack.getCount());
                }
            }
            return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
        }

        private int getStackLimit(int slot, ItemStack stack) {
            validate(slot);
            return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount == 0) return ItemStack.EMPTY;
            validate(slot);
            ItemStack existing = items.get(slot);
            if (existing.isEmpty())
                return ItemStack.EMPTY;
            int toExtract = Math.min(amount, existing.getMaxStackSize());
            if (existing.getCount() <= toExtract) {
                if (!simulate) {
                    items.set(slot, ItemStack.EMPTY);
                    return existing;
                } else {
                    return existing.copy();
                }
            } else {
                if (!simulate) {
                    items.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
                }
                return existing.copyWithCount(toExtract);
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            validate(slot);
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            validate(slot);
            return AbstractPoweredBlockEntity.this.isItemValid(slot, stack);
        }
    };

    protected AbstractPoweredBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState, int maxEnergy, int maxEnergyTransfer, int slotSize) {
        super(type, pos, blockState);
        this.SIZE = slotSize;
        this.items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        this.baseEnergy = maxEnergy;
        this.baseEnergyTransfer = maxEnergyTransfer;
        this.energy = new DynamicEnergyStorage(baseEnergy, maxEnergyTransfer, maxEnergyTransfer, 0);
    }

    public void setNewEnergyMultiplier(float multiplier) {
        int newMaxEnergy = (int) (this.baseEnergy * multiplier);
        int newMaxEnergyTransfer = (int) (this.baseEnergyTransfer * multiplier);
        this.energy.setMaxEnergy(newMaxEnergy);
        this.energy.setMaxEnergyTransfer(newMaxEnergyTransfer);
    }

    /**
     * Used to determine the validity of items of a slot of this block entity.
     * @param slot The index of the slot.
     * @param stack The stack to determine validity for.
     * @return Whether the stack is valid for the slot.
     */
    protected abstract boolean isItemValid(int slot, @NotNull ItemStack stack);

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
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
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }

    /** An {@link net.neoforged.neoforge.energy.EnergyStorage} with a variable capacity. */
    public static class DynamicEnergyStorage extends EnergyStorage {
        public DynamicEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy) {
            super(capacity, maxReceive, maxExtract, energy);
        }

        public void setMaxEnergy(int capacity) {
            this.capacity = capacity;
            if (energy > capacity) energy = capacity;
        }

        public void setMaxEnergyTransfer(int transfer) {
            this.maxReceive = transfer;
            this.maxExtract = transfer;
        }
    }
}
