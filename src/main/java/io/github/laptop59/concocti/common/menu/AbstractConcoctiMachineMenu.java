package io.github.laptop59.concocti.common.menu;

import io.github.laptop59.concocti.common.item.ConcoctiItems;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A class which serves as a base for a Concocti Machine's menu.
 * @param <T> The type of menu. Should be itself.
 */
public abstract class AbstractConcoctiMachineMenu<T extends AbstractConcoctiMachineMenu<T>> extends AbstractContainerMenu {

    protected final Container container;
    protected final ContainerData data;

    public AbstractConcoctiMachineMenu(
            int containerId, Inventory playerInventory, int containerSize, int dataSize, Supplier<MenuType<T>> menuSupplier
    ) {
        this(containerId, playerInventory, new SimpleContainer(containerSize), new SimpleContainerData(dataSize), menuSupplier);
    }

    public AbstractConcoctiMachineMenu(
            int containerId, Inventory playerInventory, Container container, ContainerData data,
                                       Supplier<MenuType<T>> menuSupplier) {
        super(menuSupplier.get(), containerId);
        this.container = container;
        this.data = data;
        // Place the machine, player inventory and hotbar slots.
        this.addSlot(new ConcoctiUpgradeSlot(container, 0, 153, 7));
        this.addSlot(new ConcoctiFrameSlot(container, 1, 153, 7 + 18));
        this.addOtherSlots();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }

        this.addDataSlots(data);
    }

    /**
     * Adds all ITEM slots except the Upgrade Slot.
     */
    abstract protected void addOtherSlots();

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack movedStack = slot.getItem();
            itemStack = movedStack.copy();
            System.out.println(container.getContainerSize() + ":" + index);
            if (index < container.getContainerSize()) {
                // Move items in machine to player inventory.
                if (!this.moveItemStackTo(movedStack, container.getContainerSize(), container.getContainerSize() + 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (movedStack.is(ConcoctiItems.Tags.CONCOCTI_UPGRADES)) {
                // index 0 - upgrade slot
                if (!this.moveItemStackTo(movedStack, 0, 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (ConcoctiFrameSlot.getFrameAttributes(movedStack.getItem()).isPresent()) {
                // index 1 - frame slot
                if (!this.moveItemStackTo(movedStack, 1, 2, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                ItemStack stack = handleOtherQuickMoves(movedStack);
                if (stack != null) return stack;
            }

            if (movedStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (movedStack.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, movedStack);
        }

        return itemStack;
    }

    /**
     * Handles other quick moves. If {@code null}, continues everything else for quick moving.
     */
    protected abstract @Nullable ItemStack handleOtherQuickMoves(ItemStack movedStack);

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public float getProgress() {
        int left = this.data.get(0);
        int total = this.data.get(1);
        return left != 0 && total != 0 ? Mth.clamp((float) (total - left) / total, 0.0F, 1.0F) : 0.0F;
    }

    /** Returns the amount of energy/maximum energy left in this block. */
    public abstract int getNumberEnergyLeft(boolean max);

    public ConcoctiUpgradeSlot getUpgradeSlot() { return (ConcoctiUpgradeSlot) this.getSlot(0); }
    public ConcoctiFrameSlot getFrameSlot() { return (ConcoctiFrameSlot) this.getSlot(1); }
}
