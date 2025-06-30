package io.github.laptop59.concocti.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static io.github.laptop59.concocti.common.Concocti.MODID;

/**
 * A class to show the energy bar in a GUI.
 */
public class EnergyBar<T extends AbstractContainerMenu> extends Renderable {

    public static final ResourceLocation ENERGY_BAR_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/energy/bar");
    public static final ResourceLocation ENERGY_BAR_OVERLAY_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/energy/overlay");
    public static final ResourceLocation ENERGY_BAR_BASE_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/energy/base");

    T menu;
    AbstractContainerScreen<T> screen;
    int left, max;

    public EnergyBar(int guiLeft, int guiTop, AbstractContainerScreen<T> screen, T menu) {
        super(guiLeft, guiTop);
        this.screen = screen;
        this.menu = menu;
    }

    public void update(int left, int max) {
        this.left = left;
        this.max = max;
    }

    protected void render(GuiGraphics guiGraphics, RenderInfo renderInfo) {
        int height = Mth.ceil(((float) left / max) * 50.0F);
        // Draw the base first.
        guiGraphics.blitSprite(ENERGY_BAR_BASE_SPRITE, 17, 52, 0, 0, screen.getGuiLeft() + guiLeft - 1, screen.getGuiTop() + guiTop - 1, 17, 52);
        guiGraphics.blitSprite(ENERGY_BAR_SPRITE, 15, 50, 0, 0, screen.getGuiLeft() + guiLeft, screen.getGuiTop() + guiTop + (50 - height), 15, height);
        // Draw the overlay afterward.
        guiGraphics.blitSprite(ENERGY_BAR_OVERLAY_SPRITE, 15, 50, 0, 0, screen.getGuiLeft() + guiLeft, screen.getGuiTop() + guiTop, 15, 50);
        // Show a tooltip if required.
        if (renderInfo.isHovering(15, 50)) {
            renderInfo.renderTooltip(guiGraphics, Component.translatable("screen.concocti.energy_bar",
                    left, max));
        }
    }

    @Override
    public int getWidth() {
        return 15;
    }

    @Override
    public int getHeight() {
        return 50;
    }
}
