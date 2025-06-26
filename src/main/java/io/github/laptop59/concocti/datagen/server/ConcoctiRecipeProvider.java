package io.github.laptop59.concocti.datagen.server;

import io.github.laptop59.concocti.common.fluid.ConcoctiFluids;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.recipe.ConcoctiMelterRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiRecipeProvider extends RecipeProvider {
    public ConcoctiRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        // Crafting Recipes
        twoStageStorageRecipes(output, ConcoctiItems.DIAMETHYST_CRYSTAL, ConcoctiItems.DIAMETHYST_CRYSTAL_BLOCK);
        threeStageStorageRecipes(output, ConcoctiItems.DIRTY_CONCOCTI_NUGGET, ConcoctiItems.DIRTY_CONCOCTI_INGOT, ConcoctiItems.DIRTY_CONCOCTI_BLOCK);
        threeStageStorageRecipes(output, ConcoctiItems.PURIFIED_CONCOCTI_NUGGET, ConcoctiItems.PURIFIED_CONCOCTI_INGOT, ConcoctiItems.PURIFIED_CONCOCTI_BLOCK);
        threeStageStorageRecipes(output, ConcoctiItems.TOUGH_CONCOCTI_NUGGET, ConcoctiItems.TOUGH_CONCOCTI_INGOT, ConcoctiItems.TOUGH_CONCOCTI_BLOCK);

        // Concocti Melter Recipes
        concoctiMelterRecipe(output, ConcoctiItems.DIRTY_CONCOCTI_NUGGET, 10,
                new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, 12),
                new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTIZED_DIRT, 3)
        );
        concoctiMelterRecipe(output, ConcoctiItems.DIRTY_CONCOCTI_INGOT, 10 * 8, // discount
                new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, 12 * 9),
                new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTIZED_DIRT, 3 * 9)
        );
        concoctiMelterRecipe(output, ConcoctiItems.DIRTY_CONCOCTI_BLOCK, 10 * 64,
                new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, 12 * 81),
                new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTIZED_DIRT, 3 * 81)
        );

        concoctiMelterRecipe(output, ConcoctiItems.PURIFIED_CONCOCTI_NUGGET, 5,
                new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, 15)
        );
        concoctiMelterRecipe(output, ConcoctiItems.PURIFIED_CONCOCTI_INGOT, 5 * 8, // discount
                new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, 15 * 9)
        );
        concoctiMelterRecipe(output, ConcoctiItems.PURIFIED_CONCOCTI_BLOCK, 5 * 64,
                new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, 15 * 81)
        );

        concoctiMelterRecipe(output, Items.ICE, 40, new FluidStack(Fluids.WATER, 1000));
    }

    /**
     * Generates 4 recipes, one for a "nugget" to "ingot" conversion and another for an "ingot" to "block" conversion.
     */
    private static void threeStageStorageRecipes(RecipeOutput output, ItemLike nugget, ItemLike ingot, ItemLike block) {
        nineBlockStorageRecipes(
                output, RecipeCategory.MISC, nugget, RecipeCategory.MISC, ingot,
                withModId(getItemName(ingot) + "_from_nuggets"), null, withModId(getItemName(nugget) + "_from_ingot"), null
        );
        nineBlockStorageRecipes(
                output, RecipeCategory.MISC, ingot, RecipeCategory.BUILDING_BLOCKS, block,
                withModId(getItemName(block) + "_from_ingots"), null, withModId(getItemName(ingot) + "_from_block"), null
        );
    }

    /**
     * Generates 2 recipes, one for an "item" to "block" conversion and back.
     */
    private static void twoStageStorageRecipes(RecipeOutput output, ItemLike item, ItemLike block) {
        nineBlockStorageRecipes(
                output, RecipeCategory.MISC, item, RecipeCategory.BUILDING_BLOCKS, block,
                withModId(getItemName(block)), null, withModId(getItemName(item) + "_from_block"), null
        );
    }

    /**
     * Generates a Concocti Melter Recipe that melts an item into fluids.
     */
    private static void concoctiMelterRecipe(RecipeOutput output, ItemLike input, int ticks,
                                             FluidStack pureResult, FluidStack byproductResult) {
        new ConcoctiMelterRecipeBuilder(
                Ingredient.of(input.asItem()),
                pureResult,
                byproductResult,
                ticks
        ).save(output);
    }

    /**
     * Generates a Concocti Melter Recipe that melts an item into fluids. Only has one fluid product.
     */
    private static void concoctiMelterRecipe(RecipeOutput output, ItemLike input, int ticks, FluidStack pureResult) {
        new ConcoctiMelterRecipeBuilder(
                Ingredient.of(input.asItem()),
                pureResult,
                FluidStack.EMPTY,
                ticks
        ).save(output);
    }

    private static String withModId(String name) {
        return MODID + ":" + name;
    }
}
