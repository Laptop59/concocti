package io.github.laptop59.concocti.common.menu;

import io.github.laptop59.concocti.common.block.entity.ConcoctiMelterBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class ConcoctiMelterMenu extends AbstractConcoctiMachineMenu<ConcoctiMelterMenu> {

    public ConcoctiMelterMenu(
            int containerId, Inventory playerInventory
    ) {
        super(containerId, playerInventory, 3, 8, ConcoctiMenus.CONCOCTI_MELTER_MENU);
    }

    public ConcoctiMelterMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(containerId, playerInventory, container, data, ConcoctiMenus.CONCOCTI_MELTER_MENU);
    }

    @Override
    protected void addOtherSlots() {
        this.addSlot(new Slot(container, 2, 44, 39 + 5));
    }

    @Override
    public @Nullable ItemStack handleOtherQuickMoves(ItemStack movedStack) {
        // index 2 - dirty concocti slot
        if (!this.moveItemStackTo(movedStack, 2, 3, true)) {
            return ItemStack.EMPTY;
        }
        return null;
    }

    public FluidStack getPureFluidStack() {
        return new FluidStack(BuiltInRegistries.FLUID.byId(this.data.get(4)), this.data.get(5));
    }

    public FluidStack getByproductFluidStack() {
        return new FluidStack(BuiltInRegistries.FLUID.byId(this.data.get(6)), this.data.get(7));
    }

    public int getMaxFluidLeft() {
        return ConcoctiMelterBlockEntity.TANK_CAPACITY;
    }

    public int getNumberEnergyLeft(boolean max) {
        return this.data.get(max ? 3 : 2);
    }
}
