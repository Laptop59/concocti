package io.github.laptop59.concocti.common.menu;

import io.github.laptop59.concocti.common.abstraction.Properties;
import io.github.laptop59.concocti.common.abstraction.Property;
import io.github.laptop59.concocti.common.block.entity.ConcoctiSolidifierBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConcoctiSolidifierMenu extends AbstractConcoctiMachineMenu<ConcoctiSolidifierMenu> {

    @Contract(pure = true)
    @Override
    public List<Property<?>> getMachineSpecificProperties() {
        return List.of(
                Properties.FLUID_INPUT
        );
    }

    // Client
    public ConcoctiSolidifierMenu(
            int containerId, Inventory playerInventory
    ) {
        super(containerId, playerInventory, 5, ConcoctiMenus.CONCOCTI_SOLIDIFIER_MENU);
    }

    // Server
    public ConcoctiSolidifierMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(containerId, playerInventory, container, data, ConcoctiMenus.CONCOCTI_SOLIDIFIER_MENU);
    }

    @Override
    protected void addOtherSlots() {
        // Mold slot
        this.addSlot(new IconSlot.Generic(container, 2, 88, 60, IconSlot.Icon.MOLD));
        // Base item slot
        this.addSlot(new Slot(container, 3, 55, 37));
        // Output slot
        this.addSlot(new ResultSlot(null, container, 4, 120, 37));
    }

    @Override
    public @Nullable ItemStack handleOtherQuickMoves(ItemStack movedStack) {
        // index 2 - mold slot
        if (!this.getSlot(2).hasItem() && !this.moveItemStackTo(movedStack, 2, 3, true)) {
            // index 3 - base slot
            if (!this.getSlot(3).hasItem() && !this.moveItemStackTo(movedStack, 3, 4, true)) {
                return ItemStack.EMPTY;
            }
        }
        return null;
    }

    public FluidStack getInputFluidStack() {
        return viewer.get(Properties.FLUID_INPUT);
    }

    public int getMaxFluidLeft() {
        return ConcoctiSolidifierBlockEntity.TANK_CAPACITY;
    }
}
