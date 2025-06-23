package io.github.laptop59.concocti.datagen.server;

import io.github.laptop59.concocti.common.item.ConcoctiItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiRecipeProvider extends RecipeProvider {
    public ConcoctiRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        twoStageStorageRecipes(output, ConcoctiItems.DIAMETHYST_CRYSTAL, ConcoctiItems.DIAMETHYST_CRYSTAL_BLOCK);
        threeStageStorageRecipes(output, ConcoctiItems.DIRTY_CONCOCTI_NUGGET, ConcoctiItems.DIRTY_CONCOCTI_INGOT, ConcoctiItems.DIRTY_CONCOCTI_BLOCK);
        threeStageStorageRecipes(output, ConcoctiItems.PURIFIED_CONCOCTI_NUGGET, ConcoctiItems.PURIFIED_CONCOCTI_INGOT, ConcoctiItems.PURIFIED_CONCOCTI_BLOCK);
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

    private static String withModId(String name) {
        return MODID + ":" + name;
    }
}
