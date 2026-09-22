package io.github.laptop59.concocti.common.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public interface MenuServerConstructor<T extends AbstractContainerMenu> {
    T create(int containerId, Inventory playerInventory, Container container);
}
