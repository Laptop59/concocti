package io.github.laptop59.concocti.common.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

public class MergedItemHandler implements IItemHandler {
    protected List<IItemHandler> itemHandlers;

    protected record TranslatedSlot(IItemHandler handler, int slot) {}

    public MergedItemHandler(List<IItemHandler> itemHandlers) {
        this.itemHandlers = itemHandlers;
    }

    @Override
    public int getSlots() {
        int size = 0;
        for (IItemHandler handler : itemHandlers) {
            size += handler.getSlots();
        }
        return size;
    }

    protected TranslatedSlot translate(int slot) {
        int size = 0;
        for (IItemHandler handler : itemHandlers) {
            int firstSlot = size;
            size += handler.getSlots();
            if (slot >= size) continue;
            return new TranslatedSlot(handler, slot - firstSlot);
        }
        throw new IllegalArgumentException("Slot is out of bounds (provided was " + slot + ", size is " + getSlots() + ").");
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        TranslatedSlot translatedSlot = translate(slot);
        return translatedSlot.handler.getStackInSlot(translatedSlot.slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        TranslatedSlot translatedSlot = translate(slot);
        return translatedSlot.handler.insertItem(translatedSlot.slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        TranslatedSlot translatedSlot = translate(slot);
        return translatedSlot.handler.extractItem(translatedSlot.slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        TranslatedSlot translatedSlot = translate(slot);
        return translatedSlot.handler.getSlotLimit(translatedSlot.slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        TranslatedSlot translatedSlot = translate(slot);
        return translatedSlot.handler.isItemValid(translatedSlot.slot, stack);
    }
}
