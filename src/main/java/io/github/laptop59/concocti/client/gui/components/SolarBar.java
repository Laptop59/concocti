package io.github.laptop59.concocti.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static io.github.laptop59.concocti.common.Concocti.MODID;

/**
 * A class to show the solar bar in a GUI.
 */
public class SolarBar extends Renderable {

    public static final ResourceLocation SOLAR_BAR_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/solar/bar");
    public static final ResourceLocation SOLAR_BAR_OVERLAY_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/solar/overlay");
    public static final ResourceLocation SOLAR_BAR_BASE_SPRITE = ResourceLocation.fromNamespaceAndPath(MODID, "container/solar/base");

    Long productionRate = null;

    @Nullable AbstractContainerScreen<?> screen;
    int barLeft = 0, barTop = 0, ticks = 0;
    long left, max, requiredPerTick = 0;
    boolean renderTooltipByItselfUponMouseOver = true;

    public SolarBar(int guiLeft, int guiTop, @Nullable AbstractContainerScreen<?> screen) {
        super(guiLeft, guiTop);
        this.screen = screen;
    }

    public SolarBar(int guiLeft, int guiTop, int barLeft, int barTop) {
        super(guiLeft, guiTop);
        this.barLeft = barLeft;
        this.barTop = barTop;
    }

    public void update(long left, long max, @Nullable Long productionRate) {
        this.left = left;
        this.max = max;
        this.productionRate = productionRate;
        this.requiredPerTick = 0;
        this.ticks = 0;
        this.renderTooltipByItselfUponMouseOver = true;
    }

    public void updateAsRecipeIngredient(long requiredPerTick, int ticks) {
        this.requiredPerTick = requiredPerTick;
        this.max = Math.max(requiredPerTick, 1_000);
        this.left = requiredPerTick;
        this.ticks = ticks;
        this.renderTooltipByItselfUponMouseOver = false;
    }

    protected int left() {
        return (screen != null ? screen.getGuiLeft() + guiLeft : 0) + barLeft;
    }

    protected int top() {
        return (screen != null ? screen.getGuiTop() + guiTop : 0) + barTop;
    }

    protected void render(GuiGraphics guiGraphics, RenderInfo renderInfo) {
        int height = Mth.ceil(((float) left / max) * 50.0F);
        // Draw the base first.
        guiGraphics.blitSprite(SOLAR_BAR_BASE_SPRITE, 17, 52, 0, 0, left() - 1, top() - 1, 17, 52);
        guiGraphics.blitSprite(SOLAR_BAR_SPRITE, 15, 50, 0, 0, left(), top() + (50 - height), 15, height);
        // Draw the overlay afterward.
        guiGraphics.blitSprite(SOLAR_BAR_OVERLAY_SPRITE, 15, 50, 0, 0, left(), top(), 15, 50);
        // Show a tooltip if required.
        if (renderTooltipByItselfUponMouseOver) renderTooltip(guiGraphics, renderInfo);
    }

    public boolean isHovered(RenderInfo renderInfo) {
        return renderInfo.isHovering(15, 50);
    }

    public void renderTooltip(GuiGraphics guiGraphics, RenderInfo renderInfo) {
        if (isHovered(renderInfo)) {
            renderInfo.renderTooltip(guiGraphics, getTooltipComponents());
        }
    }

    public List<Component> getTooltipComponents() {
        ArrayList<Component> components = new ArrayList<>();

        if (requiredPerTick == 0) {
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
        } else {
            components.add(
                    Component.translatable(
                            "screen.concocti.solar_required_per_tick",
                            Component.literal(formatSolar(requiredPerTick)).withColor(0xFFFFEC)
                    ).withColor(0xFFFF9F)
            );
            components.add(
                    Component.translatable(
                            "screen.concocti.solar_required_total",
                            Component.literal(formatSolar(requiredPerTick * ticks)).withColor(0xFFFFD3)
                    ).withColor(0xE6E6A3)
            );
        }

        return components;
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
