package io.github.laptop59.concocti.common.recipe;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiMelterRecipeBuilder implements RecipeBuilder {
    protected final Ingredient inputItem;
    protected final FluidStack pureResult;
    protected final FluidStack byproductResult;
    protected final int ticks;

    public ConcoctiMelterRecipeBuilder(Ingredient inputItem, FluidStack pureResult, FluidStack byproductResult, int ticks) {
        this.inputItem = inputItem;
        this.pureResult = pureResult;
        this.byproductResult = byproductResult;
        this.ticks = ticks;
    }

    @Override
    public @NotNull RecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
        return this;
    }

    @Override
    public @NotNull ConcoctiMelterRecipeBuilder group(@Nullable String group) {
        return this; // No recipe book groups required.
    }

    // Vanilla wants an Item here, not an ItemStack. You still can and should use the ItemStack
    // for serializing the recipes.
    @Override
    public @NotNull Item getResult() {
        return Items.AIR;
    }

    static ResourceLocation getDefaultRecipeId(Ingredient ingredient) {
        ItemStack firstStack = Arrays.stream(ingredient.getItems()).findFirst().orElseThrow();
        return ResourceLocation.fromNamespaceAndPath(MODID,
                "melting/" + BuiltInRegistries.ITEM.getKey(firstStack.getItem()).getPath());
    }

    @Override
    public void save(@NotNull RecipeOutput recipeOutput) {
        this.save(recipeOutput, getDefaultRecipeId(inputItem));
    }

    @Override
    public void save(@NotNull RecipeOutput recipeOutput, @NotNull String id) {
        ResourceLocation resourceLocation = getDefaultRecipeId(inputItem);
        ResourceLocation idLocation = ResourceLocation.parse(id);
        if (ResourceLocation.parse(id).equals(resourceLocation)) {
            throw new IllegalStateException("Recipe " + id + " should remove its 'save' argument as it is equal to default one");
        } else {
            this.save(recipeOutput, idLocation);
        }
    }

    @Override
    public void save(RecipeOutput recipeOutput, @NotNull ResourceLocation id) {
        ConcoctiMelterRecipe recipe = new ConcoctiMelterRecipe(id, this.inputItem, this.pureResult, this.byproductResult, this.ticks);
        recipeOutput.accept(id, recipe, null);
    }
}