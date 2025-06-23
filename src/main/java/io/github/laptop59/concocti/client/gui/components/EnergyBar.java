package io.github.laptop59.concocti.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static io.github.laptop59.concocti.common.Concocti.MODID;

/**
 * A class to show the energy bar in a GUI.
 */
public class EnergyBar<T extends AbstractContainerMenu> {

    private final ResourceLocation energyBarSprite = ResourceLocation.fromNamespaceAndPath(MODID, "container/energy/bar");
    private final ResourceLocation energyBarOverlaySprite = ResourceLocation.fromNamespaceAndPath(MODID, "container/energy/overlay");
    private final ResourceLocation energyBarBaseSprite = ResourceLocation.fromNamespaceAndPath(MODID, "container/energy/base");

    T menu;
    AbstractContainerScreen<T> screen;
    int guiLeft;
    int guiTop;

    public EnergyBar(AbstractContainerScreen<T> screen, T menu, int guiLeft, int guiTop) {
        this.screen = screen;
        this.menu = menu;
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, int left, int max, Font font) {
        int height = Mth.ceil(((float) left / max) * 50.0F);
        // Draw the base first.
        guiGraphics.blitSprite(this.energyBarBaseSprite, 17, 52, 0, 0, screen.getGuiLeft() + guiLeft - 1, screen.getGuiTop() + guiTop - 1, 17, 52);
        guiGraphics.blitSprite(this.energyBarSprite, 15, 50, 0, 0, screen.getGuiLeft() + guiLeft, screen.getGuiTop() + guiTop + (50 - height), 15, height);
        // Draw the overlay afterward.
        guiGraphics.blitSprite(this.energyBarOverlaySprite, 15, 50, 0, 0, screen.getGuiLeft() + guiLeft, screen.getGuiTop() + guiTop, 15, 50);
        // Show a tooltip if required.
        if (screen.isHovering(guiLeft, guiTop, 15, 50, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.translatable("screen.concocti.energy_bar",
                    left, max), mouseX, mouseY);
        }
    }
}
