package io.github.laptop59.concocti.common.recipe;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;

/** A recipe, which takes a certain number of ticks to finish processing. */
public interface ProcessingRecipe<I extends RecipeInput> extends Recipe<I> {
    public abstract int getTicks();
}
