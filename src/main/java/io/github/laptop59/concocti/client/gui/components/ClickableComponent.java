package io.github.laptop59.concocti.client.gui.components;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

/** An interface to allow the clicking of components. */
public interface ClickableComponent {
    /**
     * Called when this component is clicked.
     * @param mouseX The mouse X of the click.
     * @param mouseY The mouse Y of the click.
     * @param button The button of the click.
     * @param screen The screen of this component, if any.
     * @param menu The menu of this component, if any.
     * @return Whether the click should be consumed.
     */
    boolean onMouseClick(double mouseX, double mouseY, int button, @Nullable AbstractContainerScreen<?> screen,
                         @Nullable AbstractContainerMenu menu);
}
