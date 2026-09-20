package io.github.laptop59.concocti.common.menu;

import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class which serves as a base for a hatch's menu.
 */
public class ConcoctiEnergyHatchMenu extends AbstractConcoctiMachineMenu<ConcoctiEnergyHatchMenu> {
    // client constructor
    public ConcoctiEnergyHatchMenu(
            int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf
    ) {
        super(containerId, playerInventory, 0, buf, ConcoctiMenus.CONCOCTI_ENERGY_HATCH_MENU);
    }

    // server constructor
    public ConcoctiEnergyHatchMenu(int containerId, Inventory playerInventory, Container container) {
        super(containerId, playerInventory, container, ConcoctiMenus.CONCOCTI_ENERGY_HATCH_MENU);
    }

    protected void addSlots(Inventory playerInventory) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    /**
     * Gets the facing direction of this machine.
     */
    @Override
    public Direction getFacingDirection() {
        return null;
    }

    @Override
    protected void addOtherSlots() {

    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    protected @Nullable ItemStack handleOtherQuickMoves(ItemStack movedStack) {
        // The quickMoveStack is already overridden.
        return null;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }
}
