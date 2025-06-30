package io.github.laptop59.concocti.integration.jei;

import io.github.laptop59.concocti.client.gui.components.ArrowProgress;
import io.github.laptop59.concocti.client.gui.components.RenderInfo;
import io.github.laptop59.concocti.client.gui.components.Renderable;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.recipe.ConcoctiMelterRecipe;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static io.github.laptop59.concocti.common.Concocti.MODID;

/**
 * A base class to implement common parts of all Concocti recipes.
 * @param <T> The type of recipe represented by this category.
 */
public abstract class AbstractConcoctiRecipeCategory<T extends Recipe<? extends RecipeInput>> implements IRecipeCategory<T> {
    private final IDrawable icon;

    private final ArrowProgress arrowProgress;

    public AbstractConcoctiRecipeCategory(IGuiHelper guiHelper, ItemStack icon) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, icon);
        arrowProgress = new ArrowProgress(getTotalArrowLeft(), 10 - 4);
    }

    public AbstractConcoctiRecipeCategory(IGuiHelper guiHelper, DeferredItem<Item> item) {
        this(guiHelper, item.toStack());
    }

    /** Gets this category's recipe type. */
    @Override
    public abstract @NotNull RecipeType<T> getRecipeType();

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 176 - 8;
    }

    @Override
    public int getHeight() {
        return 36 - 8;
    }

    protected int getHorizontalArrowOffset() { return 0; }

    protected int getTotalArrowLeft() { return 75 - 4 + getHorizontalArrowOffset(); }

    protected abstract ResourceLocation getTexture();

    @Override
    public void draw(@NotNull T recipe, @NotNull IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics, double mouseX, double mouseY) {
        int left = -4;
        int top = -4;
        // Draw the background texture.
        guiGraphics.blit(this.getTexture(), left, top, 0, 0, 176, 36, 176, 36);
        // Draw the arrow progress.
        long absoluteTicks = System.currentTimeMillis() / 50;
        int tickDuration = getTicks(recipe);
        long passedTicks = absoluteTicks % tickDuration;
        double progress = (double) passedTicks / tickDuration;
        arrowProgress.setGuiLeft(getTotalArrowLeft());
        arrowProgress.update((float) (progress * 23) / 22);
        Renderable.renderChildAbsolute(guiGraphics, RenderInfo.withNullifiedOffset(null), arrowProgress);
    }

    /** Gets the duration of a recipe. */
    protected int getTicks(T recipe) {
        int tickDuration = 40;
        if (recipe instanceof ProcessingRecipe<?,?> processingRecipe) {
            tickDuration = processingRecipe.getTicks();
        }
        return tickDuration;
    }

    /** Returns whether the cursor is touching the animating arrow. */
    protected boolean isCursorTouchingArrow(double mouseX, double mouseY) {
        double dx = mouseX - getTotalArrowLeft();
        double dy = mouseY - (10 - 4);
        return dx >= 0 && dx <= 22 && dy >= 0 && dy <= 16;
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull T recipe,
                           @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        // Show the duration, if needed.
        if (isCursorTouchingArrow(mouseX, mouseY))
            tooltip.add(Component.translatable("screen.concocti.duration", (double) getTicks(recipe) / 20));
    }

    /** Tells JEI how to display this type of recipe. */
    @Override
    public abstract void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull T recipe, @NotNull IFocusGroup focuses);

    /** A helper method to create a {@link net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient} slot. */
    protected final IRecipeLayoutBuilder addSizedFluidIngredientSlot(IRecipeLayoutBuilder builder, RecipeIngredientRole role,
            int x, int y, String name, SizedFluidIngredient ingredient) {
        IRecipeSlotBuilder slotBuilder = builder.addSlot(role, x, y);
        for (FluidStack stack : ingredient.getFluids()) {
            slotBuilder.addFluidStack(stack.getFluid(), stack.getAmount());
        }
        slotBuilder.setSlotName(name);
        return builder;
    }
}
