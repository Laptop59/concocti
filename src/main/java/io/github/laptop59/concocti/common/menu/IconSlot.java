package io.github.laptop59.concocti.common.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

/** A interface for a slot which allows some custom icons to be placed on top. */
public interface IconSlot {

    Icon getIcon();

    /** A simple slot class that adds a custom icon as a class. */
    class Generic extends Slot implements IconSlot {

        private final Icon icon;

        public Generic(Container container, int slot, int x, int y, Icon icon) {
            super(container, slot, x, y);
            this.icon = icon;
        }

        @Override
        public Icon getIcon() {
            return icon;
        }
    }

    /** A list of icons that can be placed on a slot. */
    enum Icon {
        CONCOCTI_UPGRADE("concocti_upgrade"),
        MOLD("mold"),
        FRAME("frame");

        public final String path;

        Icon(String path) {
            this.path = path;
        }
    }
}
