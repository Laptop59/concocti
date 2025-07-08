package io.github.laptop59.concocti.client.gui;

import io.github.laptop59.concocti.client.gui.components.*;
import io.github.laptop59.concocti.common.menu.ConcoctiMelterMenu;
import io.github.laptop59.concocti.common.menu.ConcoctiMixerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ConcoctiMixerScreen extends AbstractConcoctiMachineScreen<ConcoctiMixerMenu> {
    private final FluidBar<ConcoctiMixerMenu> inputFluid1 = new FluidBar<>(30, 9, this, menu, 0);
    private final FluidBar<ConcoctiMixerMenu> inputFluid2 = new FluidBar<>(30+18, 9, this, menu, 1);
    private final FluidBar<ConcoctiMixerMenu> inputFluid3 = new FluidBar<>(30+36, 9, this, menu, 2);
    private final FluidBar<ConcoctiMixerMenu> inputFluid4 = new FluidBar<>(30+54, 9, this, menu, 3);

    private final FluidBar<ConcoctiMixerMenu> outputFluid = new FluidBar<>(30+104, 9, this, menu, 4);

    private final EnergyBar<ConcoctiMixerMenu> energyBar = new EnergyBar<>(10, 18, this, menu);
    private final ArrowProgress arrowProgress = new ArrowProgress(106, 34);

    public ConcoctiMixerScreen(
            ConcoctiMixerMenu menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title);
    }

    public List<Renderable> getUniqueChildren() {
        return List.of(
                arrowProgress,
                energyBar,
                inputFluid1,
                inputFluid2,
                inputFluid3,
                inputFluid4,
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
        outputFluid.update(menu.getOutputFluidStack(), menu.getOutputFluidStackSize());
        List<FluidBar<ConcoctiMixerMenu>> fluidBars = List.of(inputFluid1, inputFluid2, inputFluid3, inputFluid4);
        List<FluidStack> fluidStacks = menu.getInputFluidStacks();
        for (int i = 0; i < 4; i++) {
            fluidBars.get(i).update(fluidStacks.get(i), menu.getInputFluidStackSize());
        }

        renderChildren(guiGraphics, renderInfo, this.getUniqueChildren());
    }
}
