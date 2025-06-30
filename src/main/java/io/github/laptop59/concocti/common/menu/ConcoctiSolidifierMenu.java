package io.github.laptop59.concocti.common.menu;

import io.github.laptop59.concocti.client.gui.components.MachineSettings;
import io.github.laptop59.concocti.common.block.entity.ConcoctiSolidifierBlockEntity;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConcoctiSolidifierMenu extends AbstractConcoctiMachineMenu<ConcoctiSolidifierMenu> {

    public ConcoctiSolidifierMenu(
            int containerId, Inventory playerInventory
    ) {
        this(containerId, playerInventory, new SimpleContainer(5), new SimpleContainerData(6));
    }

    public ConcoctiSolidifierMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(containerId, playerInventory, container, data, ConcoctiMenus.CONCOCTI_SOLIDIFIER_MENU);
        // Container data:
        // 0 - ticks left to melt item.
        // 1 - total ticks needed to melt item.
        // 2 - energy stored in the melter.
        // 3 - maximum energy storable in the melter.
        // 4 - fluid id
        // 5 - fluid amount
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
        return new FluidStack(BuiltInRegistries.FLUID.byId(this.data.get(4)), this.data.get(5));
    }

    public int getMaxFluidLeft() {
        return ConcoctiSolidifierBlockEntity.TANK_CAPACITY;
    }

    public int getNumberEnergyLeft(boolean max) {
        return this.data.get(max ? 3 : 2);
    }
}
