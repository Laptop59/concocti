package io.github.laptop59.concocti.client.gui;

import io.github.laptop59.concocti.client.gui.components.*;
import io.github.laptop59.concocti.common.menu.ConcoctiSolidifierMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ConcoctiSolidifierScreen extends AbstractConcoctiMachineScreen<ConcoctiSolidifierMenu> {

    private final FluidBar<ConcoctiSolidifierMenu> fluid = new FluidBar<>(30, 28, this, menu, 0);

    private final EnergyBar<ConcoctiSolidifierMenu> energyBar = new EnergyBar<>(10, 18, this, menu);
    private final ArrowProgress arrowProgress = new ArrowProgress(85, 36);

    public ConcoctiSolidifierScreen(ConcoctiSolidifierMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    public List<Renderable> getUniqueChildren() {
        return List.of(
                arrowProgress,
                energyBar,
                fluid
        );
    }

    @Override
    public List<Renderable> getChildren() {
        ArrayList<Renderable> children = new ArrayList<>(super.getChildren());
        children.addAll(getUniqueChildren());
        return List.copyOf(children);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, RenderInfo renderInfo) {
        // Don't forget to first render the abstract screen!
        super.render(guiGraphics, renderInfo);

        energyBar.update(menu.getNumberEnergyLeft(false), menu.getNumberEnergyLeft(true));
        arrowProgress.update(menu.getProgress());

        // Render the fluids.
        fluid.update(menu.getInputFluidStack(), menu.getMaxFluidLeft());

        renderChildrenAbsolute(guiGraphics, renderInfo, this.getUniqueChildren());
    }
}
