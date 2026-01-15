package io.github.laptop59.concocti.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import static io.github.laptop59.concocti.common.Concocti.MODID;

/**
 * A class to show the solar bar in a GUI.
 */
public class SolarBar<T extends AbstractContainerMenu> extends Renderable {

    public static final ResourceLocation SOLAR_BAR_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/solar/bar");
    public static final ResourceLocation SOLAR_BAR_OVERLAY_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/solar/overlay");
    public static final ResourceLocation SOLAR_BAR_BASE_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/solar/base");

    Long productionRate = null;

    T menu;
    AbstractContainerScreen<T> screen;
    long left, max;

    public SolarBar(int guiLeft, int guiTop, AbstractContainerScreen<T> screen, T menu) {
        super(guiLeft, guiTop);
        this.screen = screen;
        this.menu = menu;
    }

    public void update(long left, long max, @Nullable Long productionRate) {
        this.left = left;
        this.max = max;
        this.productionRate = productionRate;
    }

    protected void render(GuiGraphics guiGraphics, RenderInfo renderInfo) {
        int height = Mth.ceil(((float) left / max) * 50.0F);
        // Draw the base first.
        guiGraphics.blitSprite(SOLAR_BAR_BASE_SPRITE, 17, 52, 0, 0, screen.getGuiLeft() + guiLeft - 1, screen.getGuiTop() + guiTop - 1, 17, 52);
        guiGraphics.blitSprite(SOLAR_BAR_SPRITE, 15, 50, 0, 0, screen.getGuiLeft() + guiLeft, screen.getGuiTop() + guiTop + (50 - height), 15, height);
        // Draw the overlay afterward.
        guiGraphics.blitSprite(SOLAR_BAR_OVERLAY_SPRITE, 15, 50, 0, 0, screen.getGuiLeft() + guiLeft, screen.getGuiTop() + guiTop, 15, 50);
        // Show a tooltip if required.
        if (renderInfo.isHovering(15, 50)) {
            ArrayList<Component> components = new ArrayList<>();

            components.add(
                    Component.translatable(
                            "screen.concocti.solar_bar",
                            Component.literal(formatSolar(left)).withColor(0xFFFFEC),
                            Component.literal(formatSolar(max)).withColor(0xFFFFB7)
                    ).withColor(0xFFFF9F)
            );

            if (productionRate != null)
                components.add(
                        Component.translatable(
                                "screen.concocti.solar_collection_rate",
                                    Component.literal(formatSolar(productionRate)).withColor(0xFFFFEC)
                        ).withColor(0xFFFF9F)
                );

            renderInfo.renderTooltip(guiGraphics, components);
        }
    }

     public static String formatSolar(long energy) {
        if (energy < 1_000_000) return String.format("%,d SU", energy);
        final String[] prefixes = {"", "k", "M", "G", "T", "P", "E", "Z", "Y", "R", "Q"};
        int index = (int) Math.log10(energy) / 3;
        if (index >= prefixes.length) index = prefixes.length; // This probably won't happen.
        double base = Math.pow(1_000, index);
        double mantissa = energy / base;
        BigDecimal bd = BigDecimal.valueOf(mantissa);
        bd = bd.setScale(2, RoundingMode.DOWN);
        return String.format("%.2f %sSU", bd, prefixes[index]);
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
