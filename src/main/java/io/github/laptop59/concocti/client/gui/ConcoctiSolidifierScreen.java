package io.github.laptop59.concocti.client.gui;

import io.github.laptop59.concocti.client.gui.components.ArrowProgress;
import io.github.laptop59.concocti.client.gui.components.EnergyBar;
import io.github.laptop59.concocti.client.gui.components.FluidBar;
import io.github.laptop59.concocti.common.menu.ConcoctiSolidifierMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiSolidifierScreen extends AbstractContainerScreen<ConcoctiSolidifierMenu> {

    private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/container/concocti_solidifier.png");

    private final FluidBar<ConcoctiSolidifierMenu> fluid = new FluidBar<>(this, menu, 42, 28);

    private final EnergyBar<ConcoctiSolidifierMenu> energyBar = new EnergyBar<>(this, menu, 10, 18);
    private final ArrowProgress<ConcoctiSolidifierMenu> arrowProgress = new ArrowProgress<>(this, menu, 77, 34);

    public ConcoctiSolidifierScreen(ConcoctiSolidifierMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        energyBar.render(guiGraphics, mouseX, mouseY, menu.getNumberEnergyLeft(false), menu.getNumberEnergyLeft(true), font);
        arrowProgress.render(guiGraphics, menu.getBurnProgress());

        // Render the fluids.
        fluid.setToSolidifierFluid(menu.getFluidId());
        // TODO: change this screen too.
        fluid.render(guiGraphics, mouseX, mouseY, new FluidStack(
                BuiltInRegistries.FLUID.byId(menu.getFluidId()), menu.getNumberFluidLeft()), menu.getMaxFluidLeft(), font);
    }
}
