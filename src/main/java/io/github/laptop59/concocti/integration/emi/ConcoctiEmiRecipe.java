package io.github.laptop59.concocti.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import io.github.laptop59.concocti.integration.jei.AbstractConcoctiRecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConcoctiEmiRecipe<R extends ProcessingRecipe<R, ? extends RecipeInput>> implements EmiRecipe {
    protected EmiRecipeCategory category;
    protected R recipe;
    protected AbstractConcoctiRecipeCategory<R> internalCategory;

    ConcoctiEmiRecipe(EmiRecipeCategory category, R recipe, AbstractConcoctiRecipeCategory<R> internalCategory) {
        this.category = category;
        this.recipe = recipe;
        this.internalCategory = internalCategory;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    public R getRecipe() {
        return recipe;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return recipe.getId();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return internalCategory.getEmiInputs(this);
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return internalCategory.getEmiCatalyst(this);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return internalCategory.getEmiOutputs(this);
    }

    @Override
    public int getDisplayWidth() {
        return internalCategory.getWidth();
    }

    @Override
    public int getDisplayHeight() {
        return internalCategory.getHeight();
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        internalCategory.addEmiWidgets(this, widgets);
    }

    @Nullable
    public RecipeHolder<R> getBackingRecipe() {
        return new RecipeHolder<>(recipe.getId(), recipe);
    }
}
