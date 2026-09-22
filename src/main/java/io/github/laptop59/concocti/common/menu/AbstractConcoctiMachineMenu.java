package io.github.laptop59.concocti.common.menu;

import io.github.laptop59.concocti.client.gui.components.MachineSettingsSlots;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.synchronization.SyncedBase;
import io.github.laptop59.concocti.common.synchronization.SyncedMachineData;
import io.github.laptop59.concocti.common.synchronization.SyncedMachineDataUpdate;
import io.github.laptop59.concocti.common.synchronization.SyncedSettings;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * A class which serves as a base for a Concocti Machine's menu.
 * If you want to sync extra data for this machine type, look at {@link AbstractConcoctiMachineMenuSyncedExtra}.
 *
 * @param <T> The type of menu. Should be itself.
 */
public abstract class AbstractConcoctiMachineMenu<T extends AbstractConcoctiMachineMenu<T>> extends AbstractContainerMenu {
    protected final Container container;

    protected SyncedBase syncedBase;
    protected SyncedSettings syncedSettings;
    protected SyncedFluids syncedFluids;

    // server constructor
    protected AbstractConcoctiMachineMenu(
            int containerId, Inventory playerInventory, Container container,
            Supplier<MenuType<T>> menuSupplier) {
        super(menuSupplier.get(), containerId);
        this.container = container;
        // Place the machine, player inventory and hotbar slots.
        addSlots(playerInventory);
    }

    // client constructor
    protected AbstractConcoctiMachineMenu(
            int containerId, Inventory playerInventory, int containerSize, RegistryFriendlyByteBuf buf,
            Supplier<MenuType<T>> menuSupplier) {
        super(menuSupplier.get(), containerId);
        this.container = new SimpleContainer(containerSize);
        // Sync the data.
        updateWithSyncedData(SyncedMachineData.STREAM_CODEC.decode(buf));
        // Place the machine, player inventory and hotbar slots.
        addSlots(playerInventory);
    }

    public void updateWithSyncedData(SyncedMachineData data) {
        syncedBase = data.base();
        syncedSettings = data.settings();
        syncedFluids = new SyncedFluids(data.fluids());
    }

    public void updateWithSyncedData(SyncedMachineDataUpdate update) {
        update.base().ifPresent(base -> syncedBase = base);
        update.settings().ifPresent(settings -> syncedSettings = settings);
        update.fluids().ifPresent(fluids -> syncedFluids.sync(fluids));
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
    }

    /**
     * Gets the machine settings slots associated with this menu.
     */
    public MachineSettingsSlots getMachineSettingsSlots() {
        return syncedSettings.machineSettingsSlots();
    }

    /**
     * Whether this machine is set to eject.
     */
    public boolean shouldEject() {
        return syncedSettings.ejectOn();
    }

    /**
     * Whether this machine is set to pull.
     */
    public boolean shouldPull() {
        return syncedSettings.pullOn();
    }

    /**
     * Gets the facing direction of this machine.
     */
    public @Nullable Direction getFacingDirection() {
        return syncedSettings.facingDirection().orElse(null);
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

    /**
     * Gets the ticks remaining before the recipe completes.
     */
    public int getTicksLeft() {
        return syncedBase.ticksLeft();
    }

    /**
     * Gets the total number of ticks the current recipe requires.
     */
    public int getTotalTicks() {
        return syncedBase.totalTicks();
    }

    /**
     * Gets the current ticks advanced per game tick.
     */
    public int getTickMultiplier() {
        return syncedBase.tickMultiplier();
    }

    public float getProgress() {
        int left = getTicksLeft();
        int total = getTotalTicks();
        return left != 0 && total != 0 ? Mth.clamp((float) (total - left) / total, 0.0F, 1.0F) : 0.0F;
    }

    public float getInterpolatedProgress(float partialTick) {
        int left = getTicksLeft();
        int total = getTotalTicks();
        int by = getTickMultiplier();

        float progressInTicks = (total - left + partialTick * by) % total;

        return left != 0 && total != 0 ? Mth.clamp(progressInTicks / total, 0.0F, 1.0F) : 0.0F;
    }

    public double getProgressCompletedPerTick() {
        int total = getTotalTicks();
        int by = getTickMultiplier();

        return total <= 0 ? 0.0 : (double) by / total;
    }

    public double getSecondsLeft() {
        int left = getTicksLeft();
        int total = getTotalTicks();
        int by = getTickMultiplier();

        return total <= 0 ? 0.0 : (double) left / by / 20;
    }

    /**
     * Returns the amount of energy/maximum energy left in this block.
     */
    public int getNumberEnergyLeft(boolean max) {
        return max ? syncedBase.maxEnergyStored() : syncedBase.energyStored();
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
