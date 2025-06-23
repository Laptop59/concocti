package io.github.laptop59.concocti.common.menu;

import io.github.laptop59.concocti.common.item.ConcoctiItems;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.function.Supplier;

public class ConcoctiMelterMenu extends AbstractContainerMenu {

    private final Container container;
    private final ContainerData data;

    public ConcoctiMelterMenu(
            int containerId, Inventory playerInventory
    ) {
        this(containerId, playerInventory, new SimpleContainer(3), new SimpleContainerData(7));
    }

    public ConcoctiMelterMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ConcoctiMenus.CONCOCTI_MELTER_MENU.get(), containerId);
        this.container = container;
        // Container data:
        // 0 - ticks left to melt item.
        // 1 - total ticks needed to melt item.
        // 2 - energy stored in the melter.
        // 3 - maximum energy storable in the melter.
        // 4 - fluid units of Molten Concocti stored.
        // 5 - fluid units of Molten Concoctized Dirt stored.
        // 6 - the last smelted item ID.
        this.data = data;
        // Place the melter, player inventory and hotbar slots.
        this.addSlot(new DirtyConcoctiSlot(container, 0, 44, 39 + 5));
        this.addSlot(new ConcoctiUpgradeSlot(container, 1, 153, 7));

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
            } else if (movedStack.is(ConcoctiItems.Tags.MELTABLE_CONCOCTI_ITEMS)) {
                // index 0 - dirty concocti slot
                if (!this.moveItemStackTo(movedStack, 0, 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (movedStack.is(ConcoctiItems.Tags.CONCOCTI_UPGRADES)) {
                // index 1 - upgrade slot
                if (!this.moveItemStackTo(movedStack, 1, 2, true)) {
                    return ItemStack.EMPTY;
                }
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
     * A type of slot which only allows meltable Concocti nuggets, ingots and blocks, or only items
     * specified in the {@code #concocti:dirty_concocti_items} item tag.
     */
    static class DirtyConcoctiSlot extends Slot {
        public DirtyConcoctiSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return mayPlaceItem(stack);
        }

        /**
         * Returns {@code true} if the given {@link net.minecraft.world.item.ItemStack} can be melted.
         */
        public static boolean mayPlaceItem(ItemStack itemStack) {
            return itemStack.is(ConcoctiItems.Tags.MELTABLE_CONCOCTI_ITEMS);
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public float getBurnProgress() {
        int left = this.data.get(0);
        int total = this.data.get(1);
        return left != 0 && total != 0 ? Mth.clamp((float) (total - left) / total, 0.0F, 1.0F) : 0.0F;
    }

    public int getNumberFluidLeft(boolean dirt) {
        return this.data.get(dirt ? 5 : 4);
    }

    public int getMaxFluidLeft() {
        return 8000;
    }

    public int getNumberEnergyLeft(boolean max) {
        return this.data.get(max ? 3 : 2);
    }

    public ConcoctiUpgradeSlot getUpgradeSlot() { return (ConcoctiUpgradeSlot) this.getSlot(1); }
}
