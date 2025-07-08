package io.github.laptop59.concocti.common.menu;

import io.github.laptop59.concocti.common.abstraction.Properties;
import io.github.laptop59.concocti.common.abstraction.Property;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConcoctiEnergyGeneratorMenu extends AbstractConcoctiMachineMenu<ConcoctiEnergyGeneratorMenu> {

    @Contract(pure = true)
    @Override
    public List<Property<?>> getMachineSpecificProperties() {
        return List.of();
    }

    // Client
    public ConcoctiEnergyGeneratorMenu(
            int containerId, Inventory playerInventory
    ) {
        super(containerId, playerInventory, 3, ConcoctiMenus.CONCOCTI_ENERGY_GENERATOR_MENU);
    }

    // Server
    public ConcoctiEnergyGeneratorMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(containerId, playerInventory, container, data, ConcoctiMenus.CONCOCTI_ENERGY_GENERATOR_MENU);
    }

    @Override
    protected void addOtherSlots() {
        // Fuel item slot
        this.addSlot(new Slot(container, 2, 75, 28));
    }

    @Override
    public @Nullable ItemStack handleOtherQuickMoves(ItemStack movedStack) {
        // index 2
        if (!this.getSlot(2).hasItem() && !this.moveItemStackTo(movedStack, 2, 3, true)) {
            return ItemStack.EMPTY;
        }
        return null;
    }
}
