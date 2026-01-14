package io.github.laptop59.concocti.common.machine;

import io.github.laptop59.concocti.client.gui.components.ArrowProgress;
import io.github.laptop59.concocti.client.gui.components.RenderInfo;
import io.github.laptop59.concocti.client.gui.components.Renderable;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import io.github.laptop59.concocti.common.util.Lazy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.github.laptop59.concocti.common.Concocti.MODID;

/**
 * A base class to implement common parts of all Concocti recipes.
 *
 * @param <R> The type of recipe represented by this category.
 */
public abstract class AbstractConcoctiRecipeCategory<R extends ProcessingRecipe<R, ? extends RecipeInput>> {
    public final static ResourceLocation SLOT = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/recipe_viewer/slot.png");

    private final ArrowProgress arrowProgress;

    public AbstractConcoctiRecipeCategory() {
        arrowProgress = new ArrowProgress(-1_000_000_000, 10 - 4);
    }

    public int getWidth(@NotNull R recipe) {
        return 176 - 8;
    }

    public int getHeight(@NotNull R recipe) {
        return 18 + getSlotsHeight(recipe) * 18;
    }

    public abstract @NotNull Object getJeiRecipeType();

    public abstract Component getTitle();

    protected int getHorizontalArrowOffset(@NotNull R recipe) {
        return 0;
    }

    protected int getTotalArrowLeft(@NotNull R recipe) {
        return getWidth(recipe) / 2 - 11 + getHorizontalArrowOffset(recipe);
    }

    protected int getTotalArrowTop(@NotNull R recipe) {
        return 16 - 9 + getSlotsHeight(recipe) * 9;
    }

    protected int getSlotsHeight(@NotNull R recipe) {
        return 1;
    }

    /**
     * Gets the duration of a recipe.
     */
    protected int getTicks(R recipe) {
        int tickDuration = 40;
        if (recipe instanceof ProcessingRecipe<?, ?> processingRecipe) {
            tickDuration = processingRecipe.getTicks();
        }
        return tickDuration;
    }

    /**
     * Returns whether the cursor is touching the animating arrow.
     */
    protected boolean isCursorTouchingArrow(double mouseX, double mouseY, @NotNull R recipe) {
        double dx = mouseX - getTotalArrowLeft(recipe);
        double dy = mouseY - getTotalArrowTop(recipe);
        return dx >= 0 && dx <= 22 && dy >= 0 && dy <= 16;
    }

    public void tooltip(@NotNull List<Component> tooltipBuilder, @NotNull R recipe, double mouseX, double mouseY) {
        // Show the duration, if needed.
    }

    /**
     * Sets the required recipe.
     */
    public abstract void set(@NotNull RecipeBuilder builder, @NotNull R recipe);

    /**
     * Gets the machine instance of the machine using this category
     */
    protected abstract ConcoctiMachine<?, ?, ?, ?, R, ?, ?, ?, ?> getMachineInstance();

    protected void renderBackground(GuiGraphics guiGraphics, R recipe) {
        int width = getWidth(recipe) + 8;
        int height = getHeight(recipe) + 8;

        int left = -4;
        int top = -4;
        int u = (256 - width) / 2;
        int v = (256 - height) / 2;

        guiGraphics.blit(
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/recipe_viewer/recipe_background_edges_template.png"),
                left + 1,
                top + 1,
                width - 2,
                height - 2,
                u + 1,
                v + 1,
                width,
                height,
                256,
                256
        );
        guiGraphics.blit(
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/recipe_viewer/recipe_background_template.png"),
                left + 3,
                top + 3,
                width - 6,
                height - 6,
                u + 3,
                v + 3,
                width,
                height,
                256,
                256
        );
        guiGraphics.blitSprite(
                ResourceLocation.fromNamespaceAndPath(MODID, "recipe_viewer/recipe_background_edges"),
                left,
                top,
                width,
                height
        );
    }

    public void render(@NotNull R recipe, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        renderBackground(guiGraphics, recipe);

        // Draw the arrow progress.
        long absoluteTicks = System.currentTimeMillis() / 50;
        int tickDuration = getTicks(recipe);
        long passedTicks = absoluteTicks % tickDuration;
        double progress = (double) passedTicks / tickDuration;
        arrowProgress.setGuiLeft(getTotalArrowLeft(recipe));
        arrowProgress.setGuiTop(getTotalArrowTop(recipe));
        arrowProgress.update((float) (progress * 23) / 22);
        Renderable.renderChildAbsolute(guiGraphics, RenderInfo.withNullifiedOffset(null), arrowProgress);

        String toDraw = String.format("%.1fs", recipe.getTicks() / 20d);
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                toDraw,
                getWidth(recipe) - 2 - Minecraft.getInstance().font.width(toDraw),
                2,
                0xFF26174a,
                false
        );
    }
}
