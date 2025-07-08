package io.github.laptop59.concocti.datagen.server;

import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluids;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.item.MoldItem;
import io.github.laptop59.concocti.common.recipe.ConcoctiElectronCollectorRecipe;
import io.github.laptop59.concocti.common.recipe.ConcoctiMelterRecipe;
import io.github.laptop59.concocti.common.recipe.ConcoctiMixerRecipe;
import io.github.laptop59.concocti.common.recipe.ConcoctiSolidifierRecipe;
import io.github.laptop59.concocti.common.util.ConcoctiConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiRecipeProvider extends RecipeProvider {
    public ConcoctiRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    record ConcoctiMoldingSolidifierRecipe(ItemStack result, Ingredient input, FluidStack fluidStack, int ticks) {}

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        // Crafting Recipes
        twoStageStorageRecipes(output, ConcoctiItems.DIAMETHYST_CRYSTAL, ConcoctiItems.DIAMETHYST_CRYSTAL_BLOCK);
        threeStageStorageRecipes(output, ConcoctiItems.DIRTY_CONCOCTI_NUGGET, ConcoctiItems.DIRTY_CONCOCTI_INGOT, ConcoctiItems.DIRTY_CONCOCTI_BLOCK);
        threeStageStorageRecipes(output, ConcoctiItems.PURIFIED_CONCOCTI_NUGGET, ConcoctiItems.PURIFIED_CONCOCTI_INGOT, ConcoctiItems.PURIFIED_CONCOCTI_BLOCK);
        threeStageStorageRecipes(output, ConcoctiItems.TOUGH_CONCOCTI_NUGGET, ConcoctiItems.TOUGH_CONCOCTI_INGOT, ConcoctiItems.TOUGH_CONCOCTI_BLOCK);
        threeStageStorageRecipes(output, ConcoctiItems.CONDUCTIVIUM_NUGGET, ConcoctiItems.CONDUCTIVIUM_INGOT, ConcoctiItems.CONDUCTIVIUM_BLOCK);

        moldBaseRecipes(output, Items.IRON_NUGGET, Items.COPPER_INGOT, MoldItem.Material.COPPER);
        moldBaseRecipes(output, ConcoctiItems.PURIFIED_CONCOCTI_NUGGET, Items.DIAMOND, MoldItem.Material.DIAMOND);

        // Concocti Melter Recipes
        concoctiMelterRecipe(output, ConcoctiItems.DIRTY_CONCOCTI_NUGGET, 10,
                new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, ConcoctiConstants.MOLTEN_NUGGET_PURIFIED),
                new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTIZED_DIRT, 3)
        );
        concoctiMelterRecipe(output, ConcoctiItems.DIRTY_CONCOCTI_INGOT, 10 * 8, // discount
                new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, ConcoctiConstants.MOLTEN_INGOT_PURIFIED),
                new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTIZED_DIRT, 3 * 9)
        );
        concoctiMelterRecipe(output, ConcoctiItems.DIRTY_CONCOCTI_BLOCK, 10 * 64,
                new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, ConcoctiConstants.MOLTEN_BLOCK_PURIFIED),
                new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTIZED_DIRT, 3 * 81)
        );
        concoctiMelterRecipe(output, ConcoctiItems.PURIFIED_CONCOCTI_NUGGET, 5,
                new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, ConcoctiConstants.MOLTEN_NUGGET)
        );
        concoctiMelterRecipe(output, ConcoctiItems.PURIFIED_CONCOCTI_INGOT, 5 * 8, // discount
                new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, ConcoctiConstants.MOLTEN_INGOT)
        );
        concoctiMelterRecipe(output, ConcoctiItems.PURIFIED_CONCOCTI_BLOCK, 5 * 64,
                new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, ConcoctiConstants.MOLTEN_BLOCK)
        );
        concoctiMelterRecipe(output, Items.COPPER_INGOT, 5 * 8, // discount
                new FluidStack(ConcoctiFluids.MOLTEN_COPPER, ConcoctiConstants.MOLTEN_INGOT)
        );
        concoctiMelterRecipe(output, Items.COPPER_BLOCK, 5 * 64,
                new FluidStack(ConcoctiFluids.MOLTEN_COPPER, ConcoctiConstants.MOLTEN_BLOCK)
        );
        concoctiMelterRecipe(output, ConcoctiItems.CONDUCTIVIUM_NUGGET, 10,
                new FluidStack(ConcoctiFluids.MOLTEN_CONDUCTIVIUM, ConcoctiConstants.MOLTEN_NUGGET)
        );
        concoctiMelterRecipe(output, ConcoctiItems.CONDUCTIVIUM_INGOT, 10 * 8,
                new FluidStack(ConcoctiFluids.MOLTEN_CONDUCTIVIUM, ConcoctiConstants.MOLTEN_INGOT)
        );
        concoctiMelterRecipe(output, ConcoctiItems.CONDUCTIVIUM_BLOCK, 10 * 64,
                new FluidStack(ConcoctiFluids.MOLTEN_CONDUCTIVIUM, ConcoctiConstants.MOLTEN_BLOCK)
        );
        concoctiMelterRecipe(output, Items.ICE, 40, new FluidStack(Fluids.WATER, 1000));
        concoctiMelterRecipe(output, Items.PACKED_ICE, 80, new FluidStack(Fluids.WATER, 9000));
        concoctiMelterRecipe(output, Items.BLUE_ICE, 640, new FluidStack(Fluids.WATER, 81000));

        // Concocti Solidifier Recipes
        concoctiSolidifierRecipe(output, null, Items.CAULDRON, new FluidStack(Fluids.WATER, 1000),
                40, new ItemStack(Items.ICE));
        concoctiSolidifierRecipe(output, null, Items.CAULDRON,
                new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, ConcoctiConstants.MOLTEN_BLOCK), 5 * 8 * 8,
                new ItemStack(ConcoctiItems.PURIFIED_CONCOCTI_BLOCK.get()));
        concoctiSolidifierRecipe(output, null, Items.CAULDRON,
                new FluidStack(ConcoctiFluids.MOLTEN_COPPER, ConcoctiConstants.MOLTEN_BLOCK), 5 * 8 * 8,
                new ItemStack(Items.COPPER_BLOCK));
        concoctiSolidifierRecipe(output, null, Items.CAULDRON,
                new FluidStack(ConcoctiFluids.MOLTEN_CONDUCTIVIUM, ConcoctiConstants.MOLTEN_BLOCK), 20 * 8 * 8,
                new ItemStack(ConcoctiItems.CONDUCTIVIUM_BLOCK.get()));

        // Using a convenient method for registering multiple recipes with same mold type:

        concoctiSolidifierRecipe(output, MoldItem.Type.NUGGET, List.of(
                new ConcoctiMoldingSolidifierRecipe(
                        new ItemStack(ConcoctiItems.PURIFIED_CONCOCTI_NUGGET.get(), 1),
                        null,
                        new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, ConcoctiConstants.MOLTEN_NUGGET),
                        5
                ),
                new ConcoctiMoldingSolidifierRecipe(
                        new ItemStack(ConcoctiItems.CONDUCTIVIUM_NUGGET.get(), 1),
                        null,
                        new FluidStack(ConcoctiFluids.MOLTEN_CONDUCTIVIUM, ConcoctiConstants.MOLTEN_NUGGET),
                        20
                )
        ));
        concoctiSolidifierRecipe(output, MoldItem.Type.INGOT, List.of(
                new ConcoctiMoldingSolidifierRecipe(
                        new ItemStack(ConcoctiItems.PURIFIED_CONCOCTI_INGOT.get(), 1),
                        null,
                        new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, ConcoctiConstants.MOLTEN_INGOT),
                        5 * 8
                ),
                new ConcoctiMoldingSolidifierRecipe(
                        new ItemStack(Items.COPPER_INGOT, 1),
                        null,
                        new FluidStack(ConcoctiFluids.MOLTEN_COPPER, ConcoctiConstants.MOLTEN_INGOT),
                        5
                ),
                new ConcoctiMoldingSolidifierRecipe(
                        new ItemStack(ConcoctiItems.CONDUCTIVIUM_INGOT.get(), 1),
                        null,
                        new FluidStack(ConcoctiFluids.MOLTEN_CONDUCTIVIUM, ConcoctiConstants.MOLTEN_INGOT),
                        20 * 8
                )
        ));

        // Concocti Mixer Recipes
        concoctiMixerRecipe(output, "molten_conductivium", 10, List.of(), List.of(
                SizedFluidIngredient.of(new FluidStack(ConcoctiFluids.MOLTEN_COPPER, 27)),
                SizedFluidIngredient.of(new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, 9))
        ), null, new FluidStack(ConcoctiFluids.MOLTEN_CONDUCTIVIUM, 18));
        concoctiMixerRecipe(output, "electrostatic_conductivium_nugget_mixing", 20 * 20, List.of(
                SizedIngredient.of(ConcoctiItems.CONDUCTIVIUM_NUGGET.get(), 3),
                SizedIngredient.of(Items.REDSTONE, 16)
        ), List.of(
                SizedFluidIngredient.of(new FluidStack(ConcoctiFluids.MOLTEN_LIGHTNING, 1)),
                SizedFluidIngredient.of(new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, ConcoctiConstants.MOLTEN_NUGGET))
        ), new ItemStack(ConcoctiItems.ELECTROSTATIC_CONDUCTIVIUM_NUGGET.get()), null);
        concoctiMixerRecipe(output, "electrostatic_conductivium_ingot_mixing", 4 * 20 * 20, List.of(
                SizedIngredient.of(ConcoctiItems.ELECTROSTATIC_CONDUCTIVIUM_NUGGET.get(), 9),
                SizedIngredient.of(Items.REDSTONE, 64),
                SizedIngredient.of(ConcoctiItems.PURIFIED_CONCOCTI_INGOT.get(), 1)
        ), List.of(
                SizedFluidIngredient.of(new FluidStack(ConcoctiFluids.MOLTEN_COPPER, ConcoctiConstants.MOLTEN_BLOCK))
        ), new ItemStack(ConcoctiItems.ELECTROSTATIC_CONDUCTIVIUM_INGOT.get()), null);
        concoctiMixerRecipe(output, "electrostatic_conductivium_nugget_from_ingot", 60 * 20, List.of(
                SizedIngredient.of(ConcoctiItems.ELECTROSTATIC_CONDUCTIVIUM_INGOT.get(), 1)
        ), List.of(), new ItemStack(ConcoctiItems.ELECTROSTATIC_CONDUCTIVIUM_NUGGET.get(), 9), null);

        // Concocti Electron Collector Recipe
        concoctiElectronCollectorRecipe(output, 30 * 20, 0.5f, new FluidStack(ConcoctiFluids.MOLTEN_LIGHTNING, 1));
    }

    private static void concoctiSolidifierRecipe(RecipeOutput output, MoldItem.Type type, List<ConcoctiMoldingSolidifierRecipe> recipeList) {
        Ingredient molds = Ingredient.of(ConcoctiItems.Tags.MOLDS.get(type));
        for (ConcoctiMoldingSolidifierRecipe recipe : recipeList) {
            concoctiSolidifierRecipe(output, recipe.input, molds, recipe.fluidStack, recipe.ticks, recipe.result);
        }
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
     * Generates a recipe to create mold recipes from a nugget and ingot.
     */
    private static void moldBaseRecipes(RecipeOutput output, ItemLike nugget, ItemLike ingot, MoldItem.Material moldMaterial) {
        ItemLike moldBase = ConcoctiItems.MOLD_BASES.get(moldMaterial);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, moldBase)
                .define('.', nugget)
                .define('-', ingot)
                .pattern(".")
                .pattern("-")
                .unlockedBy(getHasName(nugget), has(nugget))
                .unlockedBy(getHasName(ingot), has(ingot))
                .save(output, BuiltInRegistries.ITEM.getKey(moldBase.asItem()));

        for (MoldItem.Type type : MoldItem.Type.values()) {
            ResourceLocation loc = switch (type) {
                case INGOT -> ResourceLocation.fromNamespaceAndPath("c", "ingots");
                case NUGGET -> ResourceLocation.fromNamespaceAndPath("c", "nuggets");

                default -> throw new IllegalStateException("Expected a common tag for mold type '" + type.name() + "'.");
            };

            ItemLike resultItem = ConcoctiItems.MOLDS.get(moldMaterial).get(type);

            ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, resultItem)
                    .requires(moldBase)
                    .requires(TagKey.create(Registries.ITEM, loc))
                    .unlockedBy(getHasName(nugget), has(nugget))
                    .unlockedBy(getHasName(ingot), has(ingot))
                    .save(output, BuiltInRegistries.ITEM.getKey(resultItem.asItem()));
        }
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
        new ConcoctiMelterRecipe.Builder(
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
        new ConcoctiMelterRecipe.Builder(
                Ingredient.of(input.asItem()),
                pureResult,
                FluidStack.EMPTY,
                ticks
        ).save(output);
    }

    /**
     * Generates a Concocti Solidifier Recipe that melts an item into fluids.
     */
    private static void concoctiSolidifierRecipe(RecipeOutput output, @Nullable ItemLike baseItem, ItemLike mold,
                                                 FluidStack inputFluid, int ticks, ItemStack outputItem) {
        concoctiSolidifierRecipe(output, baseItem == null ? null : Ingredient.of(baseItem), Ingredient.of(mold), inputFluid, ticks, outputItem);
    }

    /**
     * Generates a Concocti Solidifier Recipe that melts an item into fluids.
     */
    private static void concoctiSolidifierRecipe(RecipeOutput output, @Nullable Ingredient baseItem, Ingredient mold,
                                                 FluidStack inputFluid, int ticks, ItemStack outputItem) {
        new ConcoctiSolidifierRecipe.Builder(
                baseItem == null ? Ingredient.EMPTY : baseItem,
                mold,
                SizedFluidIngredient.of(inputFluid),
                outputItem,
                ticks
        ).save(output);
    }

    /**
     * Generates a Concocti Mixer Recipe.
     */
    private static void concoctiMixerRecipe(
            RecipeOutput output,
            String name,
            int ticks,
            List<SizedIngredient> inputItems,
            List<SizedFluidIngredient> inputFluids,
            ItemStack outputItem,
            FluidStack outputFluid
    ) {
        new ConcoctiMixerRecipe.Builder(
                ResourceLocation.fromNamespaceAndPath(MODID, "mixing/" + name),
                inputItems,
                outputItem,
                inputFluids,
                outputFluid,
                ticks
        ).save(output);
    }

    /**
     * Generates a Concocti Electron Collector Recipe.
     */
    private static void concoctiElectronCollectorRecipe(
            RecipeOutput output,
            int ticks,
            float chance,
            FluidStack outputFluid
    ) {
        new ConcoctiElectronCollectorRecipe.Builder(
                chance,
                outputFluid,
                ticks
        ).save(output);
    }

    private static String withModId(String name) {
        return MODID + ":" + name;
    }
}
