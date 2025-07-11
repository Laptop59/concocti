package io.github.laptop59.concocti.common.item;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConcoctiItemStackHandler extends ItemStackHandler {
    public ConcoctiItemStackHandler() {
        super();
    }

    public ConcoctiItemStackHandler(int slots) {
        super(slots);
    }

    private boolean indexInvalid(int slot) {
        return slot >= getSlots();
    }

    protected void validate(int slot) {
        if (indexInvalid(slot)) throw new IllegalArgumentException("Size of slots is " + getSlots() + ", got: " + slot);
    }

    @Override
    public int getSlotLimit(int slot) {
        validate(slot);
        return super.getSlotLimit(slot);
    }

    public NonNullList<ItemStack> getDirectList() {
        return this.stacks;
    }

    public void setDirectList(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    /**
     * Creates an item handler around this handler that only shows the provided slots and hides everything else.
     */
    public IItemHandler whitelistSlots(List<Integer> slots) {
        Set<Integer> whitelisted = new HashSet<>(slots.size());
        whitelisted.addAll(slots);
        IItemHandler parentHandler = this;
        return new IItemHandler() {
            @Override
            public int getSlots() {
                return parentHandler.getSlots();
            }

            @Override
            public @NotNull ItemStack getStackInSlot(int slot) {
                if (whitelisted.contains(slot)) return parentHandler.getStackInSlot(slot);
                return ItemStack.EMPTY;
            }

            @Override
            public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                if (whitelisted.contains(slot)) return parentHandler.insertItem(slot, stack, simulate);
                return stack;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (whitelisted.contains(slot)) return parentHandler.extractItem(slot, amount, simulate);
                return ItemStack.EMPTY.copy();
            }

            @Override
            public int getSlotLimit(int slot) {
                if (whitelisted.contains(slot)) return parentHandler.getSlotLimit(slot);
                return 0;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (whitelisted.contains(slot)) return parentHandler.isItemValid(slot, stack);
                return false;
            }
        };
    }
}
