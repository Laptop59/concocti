package io.github.laptop59.concocti.common.menu;

import io.github.laptop59.concocti.client.gui.components.MachineSettings;
import io.github.laptop59.concocti.common.abstraction.Complexion;
import io.github.laptop59.concocti.common.abstraction.ComplexionViewer;
import io.github.laptop59.concocti.common.abstraction.Properties;
import io.github.laptop59.concocti.common.abstraction.Property;
import io.github.laptop59.concocti.common.block.entity.ConcoctiMelterBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConcoctiMelterMenu extends AbstractConcoctiMachineMenu<ConcoctiMelterMenu> {
    @Contract(pure = true)
    public List<Property<?>> getProperties() {
        return List.of(
                Properties.TICKS_LEFT,
                Properties.TOTAL_TICKS,
                Properties.ENERGY_STORED,
                Properties.MAX_ENERGY_STORED,
                Properties.PURE_FLUID_OUTPUT,
                Properties.BYPRODUCT_FLUID_OUTPUT
        );
    }

    public ConcoctiMelterMenu(
            int containerId, Inventory playerInventory
    ) {
        super(containerId, playerInventory, 3, 8, ConcoctiMenus.CONCOCTI_MELTER_MENU);
        initializeViewer((SimpleContainerData) this.data);
    }

    public ConcoctiMelterMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(containerId, playerInventory, container, data, ConcoctiMenus.CONCOCTI_MELTER_MENU);
        initializeViewer((Complexion) data);
    }

    protected void initializeViewer(SimpleContainerData containerData) {
        ConcoctiMelterMenu menu = this;
        viewer = new ComplexionViewer(containerData) {
            @Override
            public List<Property<?>> getProperties() {
                return menu.getProperties();
            }
        };
    }

    protected void initializeViewer(Complexion containerData) {
        ConcoctiMelterMenu menu = this;
        viewer = new ComplexionViewer(containerData) {
            @Override
            public List<Property<?>> getProperties() {
                return menu.getProperties();
            }
        };
    }

    @Override
    protected void addOtherSlots() {
        this.addSlot(new Slot(container, 2, 44, 39 + 5));
    }

    @Override
    public @Nullable ItemStack handleOtherQuickMoves(ItemStack movedStack) {
        // index 2 - dirty concocti slot
        if (!this.getSlot(2).hasItem() && !this.moveItemStackTo(movedStack, 2, 3, true)) {
            return ItemStack.EMPTY;
        }
        return null;
    }

    public FluidStack getPureFluidStack() {
        return viewer.get(Properties.PURE_FLUID_OUTPUT);
    }

    public FluidStack getByproductFluidStack() {
        return viewer.get(Properties.BYPRODUCT_FLUID_OUTPUT);
    }

    public int getMaxFluidLeft() {
        return ConcoctiMelterBlockEntity.TANK_CAPACITY;
    }

    public int getNumberEnergyLeft(boolean max) {
        Property<Integer> property =
                max ? Properties.MAX_ENERGY_STORED : Properties.ENERGY_STORED;
        return viewer.get(property);
    }
}
