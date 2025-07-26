package io.github.laptop59.concocti.common.menu;

import io.github.laptop59.concocti.client.gui.components.MachineSettingsSlots;
import io.github.laptop59.concocti.common.abstraction.Complexion;
import io.github.laptop59.concocti.common.abstraction.ComplexionViewer;
import io.github.laptop59.concocti.common.abstraction.Properties;
import io.github.laptop59.concocti.common.abstraction.Property;
import io.github.laptop59.concocti.common.block.entity.ConcoctiHatchBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * A class which serves as a base for a hatch's menu.
 */
public class ConcoctiEnergyHatchMenu extends AbstractConcoctiMachineMenu<ConcoctiEnergyHatchMenu> {
    protected static final List<Property<?>> BASE_PROPERTIES = List.of(
            Properties.EJECT_ON,
            Properties.PULL_ON,
            Properties.MACHINE_SETTINGS_SLOTS,
            Properties.ENERGY_STORED,
            Properties.MAX_ENERGY_STORED,
            Properties.FLUID_TANK
    );

    // client constructor
    public ConcoctiEnergyHatchMenu(
            int containerId, Inventory playerInventory
    ) {
        this(containerId, playerInventory, new SimpleContainer(0), ConcoctiMenus.CONCOCTI_ENERGY_HATCH_MENU, false);
        initializeViewer((SimpleContainerData) this.data);
    }

    // server constructor
    public ConcoctiEnergyHatchMenu(
            int containerId, Inventory playerInventory, Container container, ContainerData data
    ) {
        this(containerId, playerInventory, container, data, ConcoctiMenus.CONCOCTI_ENERGY_HATCH_MENU, false);
        initializeViewer((Complexion) data);
    }


    private ConcoctiEnergyHatchMenu(
            int containerId, Inventory playerInventory, Container container,
            Supplier<MenuType<ConcoctiEnergyHatchMenu>> menuSupplier, boolean ignoredViewer) {
        super(containerId, playerInventory, 0, menuSupplier);
        // Place the machine, player inventory and hotbar slots.
        addSlots(playerInventory);
    }

    private ConcoctiEnergyHatchMenu(
            int containerId, Inventory playerInventory, Container container, ContainerData data,
            Supplier<MenuType<ConcoctiEnergyHatchMenu>> menuSupplier, boolean ignoredViewer) {
        super(containerId, playerInventory, container, data, menuSupplier);
        // Place the machine, player inventory and hotbar slots.
        addSlots(playerInventory);
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

        this.addDataSlots(data);
    }

    @Override
    public List<Property<?>> getMachineSpecificProperties() {
        return getMachineProperties();
    }

    public List<Property<?>> getMachineProperties() {
        return BASE_PROPERTIES;
    }

    protected int getPropertiesSize() {
        int size = 0;
        for (Property<?> property : getMachineProperties())
            size += property.codec().size();
        return size;
    }

    /**
     * Gets the facing direction of this machine.
     */
    @Override
    public Direction getFacingDirection() {
        return null;
    }

    protected void initializeViewer(SimpleContainerData containerData) {
        ConcoctiEnergyHatchMenu menu = this;
        viewer = new ComplexionViewer(containerData) {
            @Override
            public List<Property<?>> getProperties() {
                return menu.getMachineProperties();
            }
        };
    }

    protected void initializeViewer(Complexion containerData) {
        ConcoctiEnergyHatchMenu menu = this;
        viewer = new ComplexionViewer(containerData) {
            @Override
            public List<Property<?>> getProperties() {
                return menu.getMachineProperties();
            }
        };
    }

    /**
     * Gets the machine settings slots associated with this menu.
     */
    public MachineSettingsSlots getMachineSettingsSlots() {
        return viewer.get(Properties.MACHINE_SETTINGS_SLOTS);
    }

    /**
     * Whether this machine is set to eject.
     */
    public boolean shouldEject() {
        return viewer.get(Properties.EJECT_ON);
    }

    /**
     * Whether this machine is set to pull.
     */
    public boolean shouldPull() {
        return viewer.get(Properties.PULL_ON);
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
