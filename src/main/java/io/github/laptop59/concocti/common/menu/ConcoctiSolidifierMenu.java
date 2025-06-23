package io.github.laptop59.concocti.common.menu;

import io.github.laptop59.concocti.common.item.ConcoctiItems;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ConcoctiSolidifierMenu extends AbstractContainerMenu {

    private final Container container;
    private final ContainerData data;

    public ConcoctiSolidifierMenu(
            int containerId, Inventory playerInventory
    ) {
        this(containerId, playerInventory, new SimpleContainer(3), new SimpleContainerData(6));
    }

    public ConcoctiSolidifierMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ConcoctiMenus.CONCOCTI_SOLIDIFIER_MENU.get(), containerId);
        this.container = container;
        // Container data:
        // 0 - ticks left to melt item.
        // 1 - total ticks needed to melt item.
        // 2 - energy stored in the melter.
        // 3 - maximum energy storable in the melter.
        // 4 - fluid id
        // 5 - fluid amount
        this.data = data;
        // Place the melter, player inventory and hotbar slots.
        this.addSlot(new ResultSlot(playerInventory.player, container, 0, 119, 44));
        this.addSlot(new ConcoctiMoldSlot(container, 1, 80, 55));
        this.addSlot(new ConcoctiUpgradeSlot(container, 2, 153, 7));

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
     * A type of slot which only allows Concocti molds
     * specified in the {@code #concocti:concocti_molds} item tag.
     */
    static class ConcoctiMoldSlot extends Slot {

        public ConcoctiMoldSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return mayPlaceItem(stack);
        }

        public static boolean mayPlaceItem(ItemStack itemStack) {
            return itemStack.is(ConcoctiItems.Tags.CONCOCTI_MOLDS);
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack movedStack = slot.getItem();
            itemStack = movedStack.copy();
            if (index < 2) {
                // Move items in melter to player inventory.
                if (!this.moveItemStackTo(movedStack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (movedStack.is(ConcoctiItems.Tags.CONCOCTI_MOLDS)) {
                // index 1 - mold slot
                if (this.slots.get(1).hasItem() || !this.slots.get(1).mayPlace(movedStack)) {
                    return ItemStack.EMPTY;
                }
                this.slots.get(1).setByPlayer(movedStack);
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

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.container.stillValid(player);
    }

    public int getNumberFluidLeft() {
        return this.data.get(5);
    }

    public int getFluidId() {
        return this.data.get(4);
    }

    public int getMaxFluidLeft() {
        return 8000;
    }

    public int getNumberEnergyLeft(boolean max) {
        return this.data.get(max ? 3 : 2);
    }

    public float getBurnProgress() {
        int left = this.data.get(0);
        int total = this.data.get(1);
        return left != 0 && total != 0 ? Mth.clamp((float) (total - left) / total, 0.0F, 1.0F) : 0.0F;
    }
}
