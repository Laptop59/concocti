package io.github.laptop59.concocti.client.gui;

import io.github.laptop59.concocti.client.gui.components.ArrowProgress;
import io.github.laptop59.concocti.client.gui.components.EnergyBar;
import io.github.laptop59.concocti.client.gui.components.FluidBar;
import io.github.laptop59.concocti.common.menu.ConcoctiMelterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import static io.github.laptop59.concocti.common.Concocti.MODID;

@OnlyIn(Dist.CLIENT)
public class ConcoctiMelterScreen extends AbstractConcoctiMachineScreen<ConcoctiMelterMenu> {

    private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/container/concocti_melter.png");

    private final FluidBar<ConcoctiMelterMenu> pureFluid = new FluidBar<>(this, menu, 105, 28);
    private final FluidBar<ConcoctiMelterMenu> byproductFluid = new FluidBar<>(this, menu, 129, 28);

    private final EnergyBar<ConcoctiMelterMenu> energyBar = new EnergyBar<>(this, menu, 10, 18);
    private final ArrowProgress<ConcoctiMelterMenu> arrowProgress = new ArrowProgress<>(this, menu, 79 - 7, 34 + 10);

    public ConcoctiMelterScreen(
            ConcoctiMelterMenu menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBgSpecific(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Render the melt progress and energy bar.
        arrowProgress.render(guiGraphics, menu.getProgress());
        energyBar.render(guiGraphics, mouseX, mouseY, menu.getNumberEnergyLeft(false), menu.getNumberEnergyLeft(true), font);

        // Render the fluids.
        pureFluid.render(guiGraphics, mouseX, mouseY, menu.getPureFluidStack(), menu.getMaxFluidLeft(), font);
        byproductFluid.render(guiGraphics, mouseX, mouseY, menu.getByproductFluidStack(), menu.getMaxFluidLeft(), font);
    }
}
