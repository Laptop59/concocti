package io.github.laptop59.concocti.integration.jei;

import io.github.laptop59.concocti.common.machine.AbstractConcoctiRecipeCategory;
import io.github.laptop59.concocti.common.machine.ConcoctiMachine;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class JeiRecipeCategory<R extends ProcessingRecipe<R, I>, I extends RecipeInput> implements IRecipeCategory<R> {
    AbstractConcoctiRecipeCategory<R> category;
    IDrawable icon;
    ConcoctiMachine<?, ?, ?, I, R, ?, ?, ?, ?> machine;

    @Nullable Integer cachedWidth = null;
    @Nullable Integer cachedHeight = null;

    public JeiRecipeCategory(AbstractConcoctiRecipeCategory<R> category, IGuiHelper guiHelper, ItemStack icon, ConcoctiMachine<?, ?, ?, I, R, ?, ?, ?, ?> machine) {
        this.category = category;
        this.icon = guiHelper == null ? null : guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, icon);
        this.machine = machine;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull RecipeType<R> getRecipeType() {
        return (RecipeType<R>) category.getJeiRecipeType();
    }

    @Override
    public @NotNull Component getTitle() {
        return category.getTitle();
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        if (cachedWidth == null) {
            int width = 0;
            for (R recipe : ConcoctiJeiPlugin.getJeiRecipes(machine)) {
                width = Math.max(width, category.getWidth(recipe));
            }
            cachedWidth = width;
        }
        return cachedWidth;
    }

    @Override
    public int getHeight() {
        if (cachedHeight == null) {
            int height = 0;
            for (R recipe : ConcoctiJeiPlugin.getJeiRecipes(machine)) {
                height = Math.max(height, category.getHeight(recipe));
            }
            cachedHeight = height;
        }
        return cachedHeight;
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull R recipe,
                           @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        ArrayList<Component> tooltipsToAdd = new ArrayList<>();
        category.tooltip(tooltipsToAdd, recipe, mouseX, mouseY);
        tooltip.addAll(tooltipsToAdd);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull R recipe, @NotNull IFocusGroup focuses) {
        // Use the builder to add slots
        JeiRecipeBuilder jeiRecipeBuilder = new JeiRecipeBuilder(builder, focuses);
        category.set(jeiRecipeBuilder, recipe); // Adds slots in this function
    }

    @Override
    public void draw(@NotNull R recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        category.render(recipe, guiGraphics, mouseX, mouseY);
    }
}
