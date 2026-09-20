package io.github.laptop59.concocti.common.menu;

import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMultiblockBlockEntity;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.machine.ConcoctiMultiBlockMachine;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A class which serves as a base for a Concocti Multiblock's menu.
 */
public class ConcoctiMultiblockMenu extends AbstractConcoctiMachineMenuSyncedExtra<ConcoctiMultiblockMenu, ConcoctiMultiBlockMachine.Extra> {
    // Client
    public ConcoctiMultiblockMenu(
            Supplier<MenuType<ConcoctiMultiblockMenu>> menuTypeSupplier,  int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf
    ) {
        super(containerId, playerInventory, 3, buf, menuTypeSupplier);
    }

    // Server
    public ConcoctiMultiblockMenu(Supplier<MenuType<ConcoctiMultiblockMenu>> menuTypeSupplier, int containerId, Inventory playerInventory, Container container) {
        super(containerId, playerInventory, container, menuTypeSupplier);
    }

    protected void addSlots(Inventory playerInventory) {
        this.addSlot(new ConcoctiUpgradeSlot(container, 0, 153, 7));
        this.addSlot(new ConcoctiFrameSlot(container, 1, 153, 7 + 18));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    @Override
    protected void addOtherSlots() {}

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack movedStack = slot.getItem();
            itemStack = movedStack.copy();
            if (index < container.getContainerSize()) {
                // Move items in machine to player inventory.
                if (!this.moveItemStackTo(movedStack, container.getContainerSize(), container.getContainerSize() + 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (movedStack.is(ConcoctiItems.Tags.CONCOCTI_UPGRADES)) {
                // index 0 - upgrade tank
                if (!this.moveItemStackTo(movedStack, 0, 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (ConcoctiFrameSlot.getFrameAttributes(movedStack.getItem()).isPresent()) {
                // index 1 - frame tank
                if (!this.moveItemStackTo(movedStack, 1, 2, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(movedStack, container.getContainerSize(), container.getContainerSize() + 36, false)) {
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

    @Override
    protected @Nullable ItemStack handleOtherQuickMoves(ItemStack movedStack) {
        return null;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public boolean isValid() {
        return syncedExtra.valid();
    }

    public boolean showBuildPreview() {
        return syncedExtra.buildPreview();
    }

    public void changeBuildPreview() {
        ((AbstractConcoctiMultiblockBlockEntity<?, ?>) getContainer()).changeBuildPreview();
    }
}
