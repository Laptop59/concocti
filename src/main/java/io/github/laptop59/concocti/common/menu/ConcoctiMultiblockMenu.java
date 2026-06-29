package io.github.laptop59.concocti.common.menu;

import io.github.laptop59.concocti.common.abstraction.Complexion;
import io.github.laptop59.concocti.common.abstraction.ComplexionViewer;
import io.github.laptop59.concocti.common.abstraction.Properties;
import io.github.laptop59.concocti.common.abstraction.Property;
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMultiblockBlockEntity;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A class which serves as a base for a Concocti Multiblock's menu.
 */
public class ConcoctiMultiblockMenu extends AbstractConcoctiMachineMenu<ConcoctiMultiblockMenu> {
    protected static final List<Property<?>> BASE_PROPERTIES = List.of(
            Properties.VALID,
            Properties.BUILD_PREVIEW,
            Properties.TICKS_LEFT,
            Properties.TOTAL_TICKS,
            Properties.TICK_MULTIPLIER
    );

    protected String machineId;

    // client constructor
    public ConcoctiMultiblockMenu(
            Supplier<MenuType<ConcoctiMultiblockMenu>> menuTypeSupplier, int containerId, Inventory playerInventory
    ) {
        this(containerId, playerInventory, menuTypeSupplier, false);
        initializeViewer((SimpleContainerData) this.data);
    }

    // server constructor
    public ConcoctiMultiblockMenu(
            Supplier<MenuType<ConcoctiMultiblockMenu>> menuTypeSupplier, int containerId, Inventory playerInventory, Container container, ContainerData data) {
        this(containerId, playerInventory, container, data, menuTypeSupplier, false);
        initializeViewer((Complexion) data);
    }


    private ConcoctiMultiblockMenu(
            int containerId, Inventory playerInventory,
            Supplier<MenuType<ConcoctiMultiblockMenu>> menuSupplier, boolean ignoredViewer) {
        super(containerId, playerInventory, 2, menuSupplier);
        // Place the machine, player inventory and hotbar slots.
        addSlots(playerInventory);
    }

    private ConcoctiMultiblockMenu(
            int containerId, Inventory playerInventory, Container container, ContainerData data,
            Supplier<MenuType<ConcoctiMultiblockMenu>> menuSupplier, boolean ignoredViewer) {
        super(containerId, playerInventory, container, data, menuSupplier);
        // Place the machine, player inventory and hotbar slots.
        addSlots(playerInventory);
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

        this.addDataSlots(data);
    }

    @Override
    public List<Property<?>> getMachineSpecificProperties() {
        return List.of();
    }

    @Override
    public List<Property<?>> getMachineProperties() {
        return new ArrayList<>(BASE_PROPERTIES);
    }

    protected int getPropertiesSize() {
        int size = 0;
        for (Property<?> property : getMachineProperties())
            size += property.codec().size();
        return size;
    }

    protected void initializeViewer(SimpleContainerData containerData) {
        ConcoctiMultiblockMenu menu = this;
        viewer = new ComplexionViewer(containerData) {
            @Override
            public List<Property<?>> getProperties() {
                return menu.getMachineProperties();
            }
        };
    }

    protected void initializeViewer(Complexion containerData) {
        ConcoctiMultiblockMenu menu = this;
        viewer = new ComplexionViewer(containerData) {
            @Override
            public List<Property<?>> getProperties() {
                return menu.getMachineProperties();
            }
        };
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
        return viewer.get(Properties.VALID);
    }

    public boolean showBuildPreview() {
        return viewer.get(Properties.BUILD_PREVIEW);
    }

    public void changeBuildPreview() {
        ((AbstractConcoctiMultiblockBlockEntity<?, ?>) getContainer()).changeBuildPreview();
    }
}
