package io.github.laptop59.concocti.common.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

/// Class that menus synchronizing extra data extend to do so.
///
/// @param <E> The type of data to synchronize.
public abstract class AbstractConcoctiMachineMenuSyncedExtra<T extends AbstractConcoctiMachineMenu<T>, E> extends AbstractConcoctiMachineMenu<T> {
    protected E syncedExtra;

    protected AbstractConcoctiMachineMenuSyncedExtra(int containerId, Inventory playerInventory, int containerSize, RegistryFriendlyByteBuf buf, Supplier<MenuType<T>> menuSupplier) {
        super(containerId, playerInventory, containerSize, buf, menuSupplier);
    }

    protected AbstractConcoctiMachineMenuSyncedExtra(int containerId, Inventory playerInventory, Container container, Supplier<MenuType<T>> menuSupplier) {
        super(containerId, playerInventory, container, menuSupplier);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateWithSyncedData(SyncedMachineData data) {
        super.updateWithSyncedData(data);

        this.syncedExtra = (E) data.extra().data();
        onExtraSync(syncedExtra);
    }

    protected void onExtraSync(E data) {}
}
