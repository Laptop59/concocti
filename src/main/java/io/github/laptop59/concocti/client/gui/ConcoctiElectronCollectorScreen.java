package io.github.laptop59.concocti.client.gui;

import io.github.laptop59.concocti.client.gui.components.*;
import io.github.laptop59.concocti.common.menu.ConcoctiElectronCollectorMenu;
import io.github.laptop59.concocti.common.menu.ConcoctiMelterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ConcoctiElectronCollectorScreen extends AbstractConcoctiMachineScreen<ConcoctiElectronCollectorMenu> {
    private final FluidBar<ConcoctiElectronCollectorMenu> outputFluid = new FluidBar<>((getWidth() + 20) / 2, 23, this, menu, 0);

    private final EnergyBar<ConcoctiElectronCollectorMenu> energyBar = new EnergyBar<>(10, 18, this, menu);
    private final ArrowProgress arrowProgress = new ArrowProgress(79 - 17, 37);

    public ConcoctiElectronCollectorScreen(
            ConcoctiElectronCollectorMenu menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title);
    }

    public List<Renderable> getUniqueChildren() {
        return List.of(
                arrowProgress,
                energyBar,
                outputFluid
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

        arrowProgress.update(menu.getProgress());

        energyBar.update(menu.getNumberEnergyLeft(false), menu.getNumberEnergyLeft(true));

        // Render the fluids.
        outputFluid.update(menu.getFluidOutput(), menu.getMaxFluidOutput());

        renderChildren(guiGraphics, renderInfo, this.getUniqueChildren());
    }
}
