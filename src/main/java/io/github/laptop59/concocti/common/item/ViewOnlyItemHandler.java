package io.github.laptop59.concocti.common.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;
import java.util.function.Supplier;

public class ViewOnlyItemHandler implements IItemHandler {
    protected Supplier<IItemHandler> itemHandler;

    public ViewOnlyItemHandler(Supplier<IItemHandler> itemHandler) {
        this.itemHandler = itemHandler;
    }

    @Override
    public int getSlots() {
        return itemHandler.get().getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return itemHandler.get().getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY.copy();
    }

    @Override
    public int getSlotLimit(int slot) {
        return itemHandler.get().getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return itemHandler.get().isItemValid(slot, stack);
    }
}
