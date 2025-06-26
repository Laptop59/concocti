package io.github.laptop59.concocti.common.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

// The generic parameter for Recipe<T> is SingleRecipeInput.
public class ConcoctiMelterRecipe implements ProcessingRecipe<SingleRecipeInput> {
    // An in-code representation of our recipe data. This can be basically anything you want.
    // Common things to have here is a processing time integer of some kind, or an experience reward.
    // Note that we now use an ingredient instead of an item stack for the input.
    private final Ingredient inputItem;
    private final FluidStack outputPureFluid;
    private final FluidStack outputByproductFluid;
    private final int ticks;

    private static final HashMap<Ingredient, ResourceLocation> idMap = new HashMap<>();

    // Add a constructor that sets all properties.
    public ConcoctiMelterRecipe(ResourceLocation id, Ingredient inputItem, FluidStack outputPureFluid, FluidStack outputByproductFluid, int ticks) {
        this.inputItem = inputItem;
        this.outputPureFluid = outputPureFluid;
        this.outputByproductFluid = outputByproductFluid;
        this.ticks = ticks;
        idMap.put(inputItem, id);
    }

    public ConcoctiMelterRecipe(Ingredient inputItem, FluidStack outputPureFluid, FluidStack outputByproductFluid, int ticks) {
        this.inputItem = inputItem;
        this.outputPureFluid = outputPureFluid;
        this.outputByproductFluid = outputByproductFluid;
        this.ticks = ticks;
    }

    public Ingredient getInputItem() {
        return inputItem;
    }

    public FluidStack getOutputPureFluid() {
        return outputPureFluid;
    }

    public FluidStack getOutputByproductFluid() {
        return outputByproductFluid;
    }

    // A list of our ingredients. Does not need to be overridden if you have no ingredients
    // (the default implementation returns an empty list here). It makes sense to cache larger lists in a field.
    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(this.inputItem);
        return list;
    }

    // Grid-based recipes should return whether their recipe can fit in the given dimensions.
    // We don't have a grid, so we just return if any item can be placed in there.
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    // Check whether the given input matches this recipe. The first parameter matches the generic.
    // We check our blockstate and our item stack, and only return true if both match.
    @Override
    public boolean matches(SingleRecipeInput input, @NotNull Level level) {
        return this.inputItem.test(input.item());
    }

    // Return an UNMODIFIABLE version of your result here. The result of this method is mainly intended
    // for the recipe book, and commonly used by JEI and other recipe viewers as well.
    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return ItemStack.EMPTY; // Only creates fluids.
    }

    // Return the result of the recipe here, based on the given input. The first parameter matches the generic.
    // IMPORTANT: Always call .copy() if you use an existing result! If you don't, things can and will break,
    // as the result exists once per recipe, but the assembled stack is created each time the recipe is crafted.
    @Override
    public @NotNull ItemStack assemble(@NotNull SingleRecipeInput input, HolderLookup.@NotNull Provider registries) {
        return ItemStack.EMPTY.copy(); // Only creates fluids.
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ConcoctiRecipes.CONCOCTI_MELTER_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ConcoctiRecipes.CONCOCTI_MELTER_RECIPE_TYPE.get();
    }

    @Override
    public int getTicks() {
        return ticks;
    }

    public ResourceLocation getId() {
        return idMap.getOrDefault(inputItem, null);
    }
}