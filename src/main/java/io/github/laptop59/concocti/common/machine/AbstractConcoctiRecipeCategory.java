package io.github.laptop59.concocti.common.machine;

import io.github.laptop59.concocti.client.gui.components.ArrowProgress;
import io.github.laptop59.concocti.client.gui.components.RenderInfo;
import io.github.laptop59.concocti.client.gui.components.Renderable;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import io.github.laptop59.concocti.common.util.Lazy;
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
    private final Lazy<ResourceLocation> texture =
            new Lazy<>(this::getTexturePath);

    public AbstractConcoctiRecipeCategory() {
        arrowProgress = new ArrowProgress(-1_000_000_000, 10 - 4);
    }

    public ResourceLocation getTexturePath() {
        return ResourceLocation.fromNamespaceAndPath(
                MODID,
                "textures/gui/recipe_viewer/recipe_background.png"
        );
    }

    public int getWidth() {
        return 176 - 8;
    }

    public int getHeight() {
        return 36 - 8;
    }

    public abstract @NotNull Object getJeiRecipeType();

    public abstract Component getTitle();

    protected int getHorizontalArrowOffset(@NotNull R recipe) {
        return 0;
    }

    protected int getTotalArrowLeft(@NotNull R recipe) {
        return 75 - 4 + getHorizontalArrowOffset(recipe);
    }

    protected final ResourceLocation getTexture() {
        return texture.get();
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
        double dy = mouseY - (10 - 4);
        return dx >= 0 && dx <= 22 && dy >= 0 && dy <= 16;
    }

    public void tooltip(@NotNull List<Component> tooltipBuilder, @NotNull R recipe, double mouseX, double mouseY) {
        // Show the duration, if needed.
        if (isCursorTouchingArrow(mouseX, mouseY, recipe))
            tooltipBuilder.add(Component.translatable("screen.concocti.duration", (double) getTicks(recipe) / 20));
    }

    /**
     * Sets the required recipe.
     */
    public abstract void set(@NotNull RecipeBuilder builder, @NotNull R recipe);

    /**
     * Gets the machine instance of the machine using this category
     */
    protected abstract ConcoctiMachine<?, ?, ?, ?, R, ?, ?, ?, ?> getMachineInstance();

    public void render(@NotNull R recipe, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        int left = -4;
        int top = -4;
        // Draw the background texture.
        guiGraphics.blit(getTexture(), left, top, 0, 0, 176, 36, 176, 36);
        // Draw the arrow progress.
        long absoluteTicks = System.currentTimeMillis() / 50;
        int tickDuration = getTicks(recipe);
        long passedTicks = absoluteTicks % tickDuration;
        double progress = (double) passedTicks / tickDuration;
        arrowProgress.setGuiLeft(getTotalArrowLeft(recipe));
        arrowProgress.setGuiTop(6);
        arrowProgress.update((float) (progress * 23) / 22);
        Renderable.renderChildAbsolute(guiGraphics, RenderInfo.withNullifiedOffset(null), arrowProgress);
    }
}
