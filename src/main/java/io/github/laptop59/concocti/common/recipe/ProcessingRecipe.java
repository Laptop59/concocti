package io.github.laptop59.concocti.common.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

/**
 * A recipe, which takes a certain number of ticks to finish processing.
 */
public interface ProcessingRecipe<R extends ProcessingRecipe<R, I>,
        I extends RecipeInput> extends Recipe<I>, Comparable<R> {
    int getTicks();

    ResourceLocation getId();

    @Override
    default int compareTo(@NotNull R o) {
        ResourceLocation id = this.getId();
        ResourceLocation otherId = o.getId();
        if (id == null || otherId == null) return 0;
        return id.compareTo(otherId);
    }
}
