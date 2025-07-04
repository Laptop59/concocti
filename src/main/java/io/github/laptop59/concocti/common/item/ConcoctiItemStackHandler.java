package io.github.laptop59.concocti.common.item;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class ConcoctiItemStackHandler extends ItemStackHandler {
    public ConcoctiItemStackHandler() { super(); }
    public ConcoctiItemStackHandler(int slots) { super(slots); }

    private boolean indexInvalid(int slot) { return slot >= getSlots(); }

    protected void validate(int slot) {  if (indexInvalid(slot)) throw new IllegalArgumentException("Size of slots is " + getSlots() + ", got: " + slot); }

    @Override
    public int getSlotLimit(int slot) {
        validate(slot);
        return 64;
    }

    public NonNullList<ItemStack> getDirectList() {
        return this.stacks;
    }

    public void setDirectList(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }
}
