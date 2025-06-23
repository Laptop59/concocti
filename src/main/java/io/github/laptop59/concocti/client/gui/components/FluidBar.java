package io.github.laptop59.concocti.client.gui.components;

import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

import static io.github.laptop59.concocti.common.Concocti.MODID;

/**
 * A class used to render a fluid bar.
 * @param <T> The type of menu whose screen the bar should render for.
 */
public class FluidBar<T extends AbstractContainerMenu> implements MenuAccess<T> {
    T menu;
    AbstractContainerScreen<T> screen;
    ResourceLocation fluid;
    int guiLeft;
    int guiTop;

    private final ResourceLocation fluidBaseSprite = ResourceLocation.fromNamespaceAndPath(MODID, "container/fluids/base");
    private final ResourceLocation fluidBlackSprite = ResourceLocation.fromNamespaceAndPath(MODID, "container/fluids/black");

    public FluidBar(AbstractContainerScreen<T> screen, T menu, ResourceLocation fluid, int guiLeft, int guiTop) {
        this.screen = screen;
        this.menu = menu;
        this.fluid = fluid;
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
    }

    public FluidBar(AbstractContainerScreen<T> screen, T menu, String name, int guiLeft, int guiTop) {
        this(screen, menu, ResourceLocation.fromNamespaceAndPath(MODID, name), guiLeft, guiTop);
    }

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, int left, int max, Font font) {
        int height = Mth.ceil(((float) left / max) * 40.0F);
        guiGraphics.blitSprite(fluidBaseSprite, 17, 42, 0, 0, screen.getGuiLeft() + guiLeft - 1, screen.getGuiTop() + guiTop - 1, 17, 42);
        // Draw the full fluid.
        int incremented;
        if (!fluidIsEmpty()) {
            int tick = (int) ((Util.getMillis() / 50 / 2) % 38);
            if (tick > 19) tick = 20 + 18 - tick;
            for (int i = 0; i < 40; i += incremented) {
                incremented = Math.min(40 - i, 16);
                guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(fluid.getNamespace(), "textures/block/" + fluid.getPath() + "_still.png"),
                        screen.getGuiLeft() + guiLeft, screen.getGuiTop() + guiTop + i, 0, tick * 16, 15, incremented, 16, 16 * 20);
            }
        }
        // Draw the blackened part of the fluid.
        for (int blackenedLeft = 40 - height; blackenedLeft > 0; blackenedLeft -= incremented) {
            incremented = Math.min(blackenedLeft, 15);
            guiGraphics.blitSprite(fluidBlackSprite, 15, 15, 0, 0,
                    screen.getGuiLeft() + guiLeft, screen.getGuiTop() + guiTop + 40 - blackenedLeft - height, 15, incremented);
        }
        if (screen.isHovering(guiLeft, guiTop, 15, 40, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.translatable("screen.concocti.fluid_bar",
                    Component.translatable(getFluidTranslation()).getString(), left, max), mouseX, mouseY);
        }
    }

    private boolean fluidIsEmpty() {
        return fluid.equals(ResourceLocation.withDefaultNamespace("empty"));
    }

    private String getFluidTranslation() {
        if (fluidIsEmpty()) return "mco.configure.world.slot.empty"; // Found an empty translation???
        return "block." + fluid.getNamespace() + "." + fluid.getPath();
    }

    public void setToSolidifierFluid(int id) {
        switch (id) {
            case 1: fluid = ResourceLocation.withDefaultNamespace("water"); break;
            case 2: fluid = ResourceLocation.withDefaultNamespace("lava"); break;
            case 3: fluid = ResourceLocation.fromNamespaceAndPath(MODID, "molten_concocti"); break;
            case 4: fluid = ResourceLocation.fromNamespaceAndPath(MODID, "molten_concoctized_dirt"); break;
            default: fluid = ResourceLocation.withDefaultNamespace("empty"); break;
        }
    }

    @Override
    public @NotNull T getMenu() {
        return menu;
    }
}
