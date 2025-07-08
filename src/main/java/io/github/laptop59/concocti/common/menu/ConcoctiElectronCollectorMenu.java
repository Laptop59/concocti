package io.github.laptop59.concocti.common.menu;

import io.github.laptop59.concocti.common.abstraction.Properties;
import io.github.laptop59.concocti.common.abstraction.Property;
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConcoctiElectronCollectorMenu extends AbstractConcoctiMachineMenu<ConcoctiElectronCollectorMenu> {

    @Contract(pure = true)
    @Override
    public List<Property<?>> getMachineSpecificProperties() {
        return List.of(
                Properties.FLUID_OUTPUT
        );
    }

    // Client
    public ConcoctiElectronCollectorMenu(
            int containerId, Inventory playerInventory
    ) {
        super(containerId, playerInventory, 2, ConcoctiMenus.CONCOCTI_ELECTRON_COLLECTOR_MENU);
    }

    // Server
    public ConcoctiElectronCollectorMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(containerId, playerInventory, container, data, ConcoctiMenus.CONCOCTI_ELECTRON_COLLECTOR_MENU);
    }

    @Override
    protected void addOtherSlots() {}

    @Override
    public @Nullable ItemStack handleOtherQuickMoves(ItemStack movedStack) {
        return null;
    }

    public FluidStack getFluidOutput() {
        return viewer.get(Properties.FLUID_OUTPUT);
    }

    public int getMaxFluidOutput() {
        return AbstractConcoctiMachineBlockEntity.TANK_CAPACITY;
    }
}
