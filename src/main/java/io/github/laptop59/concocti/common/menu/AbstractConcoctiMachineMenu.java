package io.github.laptop59.concocti.common.menu;

import io.github.laptop59.concocti.client.gui.components.MachineSettingsSlots;
import io.github.laptop59.concocti.common.abstraction.Complexion;
import io.github.laptop59.concocti.common.abstraction.ComplexionViewer;
import io.github.laptop59.concocti.common.abstraction.Properties;
import io.github.laptop59.concocti.common.abstraction.Property;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A class which serves as a base for a Concocti Machine's menu.
 *
 * @param <T> The type of menu. Should be itself.
 */
public abstract class AbstractConcoctiMachineMenu<T extends AbstractConcoctiMachineMenu<T>> extends AbstractContainerMenu {

    protected final Container container;
    protected final ContainerData data;
    public ComplexionViewer viewer;

    protected final List<Property<?>> BASE_PROPERTIES = List.of(
            Properties.TICKS_LEFT,
            Properties.TOTAL_TICKS,
            Properties.ENERGY_STORED,
            Properties.MAX_ENERGY_STORED,
            Properties.FACING_DIRECTION,
            Properties.MACHINE_SETTINGS_SLOTS,

            Properties.EJECT_ON,
            Properties.PULL_ON
    );

    // client constructor
    public AbstractConcoctiMachineMenu(
            int containerId, Inventory playerInventory, int containerSize, Supplier<MenuType<T>> menuSupplier
    ) {
        this(containerId, playerInventory, new SimpleContainer(containerSize), menuSupplier, false);
        initializeViewer((SimpleContainerData) this.data);
    }

    // server constructor
    public AbstractConcoctiMachineMenu(
            int containerId, Inventory playerInventory, Container container, ContainerData data,
            Supplier<MenuType<T>> menuSupplier) {
        this(containerId, playerInventory, container, data, menuSupplier, false);
        initializeViewer((Complexion) data);
    }


    private AbstractConcoctiMachineMenu(
            int containerId, Inventory playerInventory, Container container,
            Supplier<MenuType<T>> menuSupplier, boolean ignoredViewer) {
        super(menuSupplier.get(), containerId);
        this.container = container;
        this.data = new SimpleContainerData(getPropertiesSize());
        // Place the machine, player inventory and hotbar slots.
        addSlots(playerInventory);
    }

    private AbstractConcoctiMachineMenu(
            int containerId, Inventory playerInventory, Container container, ContainerData data,
            Supplier<MenuType<T>> menuSupplier, boolean ignoredViewer) {
        super(menuSupplier.get(), containerId);
        this.container = container;
        this.data = data;
        // Place the machine, player inventory and hotbar slots.
        addSlots(playerInventory);
    }

    protected void addSlots(Inventory playerInventory) {
        this.addSlot(new ConcoctiUpgradeSlot(container, 0, 153, 7));
        this.addSlot(new ConcoctiFrameSlot(container, 1, 153, 7 + 18));
        this.addOtherSlots();

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

    /**
     * Gets all properties synced by a {@link ContainerData} for this menu.
     */
    @Contract(pure = true)
    public abstract List<Property<?>> getMachineSpecificProperties();

    public List<Property<?>> getMachineProperties() {
        ArrayList<Property<?>> properties = new ArrayList<>(BASE_PROPERTIES);
        properties.addAll(getMachineSpecificProperties());
        return properties;
    }

    protected int getPropertiesSize() {
        int size = 0;
        for (Property<?> property : getMachineProperties())
            size += property.codec().size();
        return size;
    }

    protected void initializeViewer(SimpleContainerData containerData) {
        AbstractConcoctiMachineMenu<T> menu = this;
        viewer = new ComplexionViewer(containerData) {
            @Override
            public List<Property<?>> getProperties() {
                return menu.getMachineProperties();
            }
        };
    }

    protected void initializeViewer(Complexion containerData) {
        AbstractConcoctiMachineMenu<T> menu = this;
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

    /**
     * Gets the facing direction of this machine.
     */
    public Direction getFacingDirection() {
        return viewer.get(Properties.FACING_DIRECTION);
    }

    /**
     * Adds all ITEM slots except the Upgrade Slot.
     */
    abstract protected void addOtherSlots();

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
                // index 0 - upgrade slot
                if (!this.moveItemStackTo(movedStack, 0, 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (ConcoctiFrameSlot.getFrameAttributes(movedStack.getItem()).isPresent()) {
                // index 1 - frame slot
                if (!this.moveItemStackTo(movedStack, 1, 2, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                ItemStack stack = handleOtherQuickMoves(movedStack);
                if (stack != null) return stack;
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

    /**
     * Handles other quick moves. If {@code null}, continues everything else for quick moving.
     */
    protected abstract @Nullable ItemStack handleOtherQuickMoves(ItemStack movedStack);

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    /**
     * Gets all slots of this menu without the base slots.
     */
    public NonNullList<Slot> getSpecificSlots() {
        return NonNullList.copyOf(this.slots.stream().skip(2).toList());
    }

    /**
     * Gets both the upgrade and frame slots of this menu.
     */
    public NonNullList<Slot> getBaseSlots() {
        return NonNullList.copyOf(this.slots.stream().limit(2).toList());
    }

    /**
     * Gets all the machine-specific slots of this menu.
     */
    public List<Slot> getMachineSlots() {
        return this.slots.subList(0, container.getContainerSize());
    }

    public float getProgress() {
        int left = viewer.get(Properties.TICKS_LEFT);
        int total = viewer.get(Properties.TOTAL_TICKS);
        return left != 0 && total != 0 ? Mth.clamp((float) (total - left) / total, 0.0F, 1.0F) : 0.0F;
    }

    /**
     * Returns the amount of energy/maximum energy left in this block.
     */
    public int getNumberEnergyLeft(boolean max) {
        Property<Integer> property =
                max ? Properties.MAX_ENERGY_STORED : Properties.ENERGY_STORED;
        return viewer.get(property);
    }

    public ConcoctiUpgradeSlot getUpgradeSlot() {
        return (ConcoctiUpgradeSlot) this.getSlot(0);
    }

    public ConcoctiFrameSlot getFrameSlot() {
        return (ConcoctiFrameSlot) this.getSlot(1);
    }

    public Container getContainer() {
        return container;
    }
}
