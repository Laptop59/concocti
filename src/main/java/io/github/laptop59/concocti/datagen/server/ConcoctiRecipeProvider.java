package io.github.laptop59.concocti.datagen.server;

import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.ConcoctiHatchBlock;
import io.github.laptop59.concocti.common.block.HatchPurpose;
import io.github.laptop59.concocti.common.block.HatchType;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluids;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.item.MoldItem;
import io.github.laptop59.concocti.common.machine.ConcoctiMachines;
import io.github.laptop59.concocti.common.machine.ConcoctiMultiBlockMachine;
import io.github.laptop59.concocti.common.machine.impl.*;
import io.github.laptop59.concocti.common.util.ConcoctiConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiRecipeProvider extends RecipeProvider {
    public ConcoctiRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    record ConcoctiMoldingSolidifierRecipe(ItemStack result, Ingredient input, FluidStack fluidStack, int ticks) {
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        // Crafting Recipes
        twoStageStorageRecipes(output, ConcoctiItems.DIAMETHYST_CRYSTAL, ConcoctiItems.DIAMETHYST_BLOCK);
        threeStageStorageRecipes(output, ConcoctiItems.DIRTY_CONCOCTI_NUGGET, ConcoctiItems.DIRTY_CONCOCTI_INGOT, ConcoctiItems.DIRTY_CONCOCTI_BLOCK);
        threeStageStorageRecipes(output, ConcoctiItems.PURIFIED_CONCOCTI_NUGGET, ConcoctiItems.PURIFIED_CONCOCTI_INGOT, ConcoctiItems.PURIFIED_CONCOCTI_BLOCK);
        threeStageStorageRecipes(output, ConcoctiItems.TOUGH_CONCOCTI_NUGGET, ConcoctiItems.TOUGH_CONCOCTI_INGOT, ConcoctiItems.TOUGH_CONCOCTI_BLOCK);
        threeStageStorageRecipes(output, ConcoctiItems.CONDUCTIVIUM_NUGGET, ConcoctiItems.CONDUCTIVIUM_INGOT, ConcoctiItems.CONDUCTIVIUM_BLOCK);
        threeStageStorageRecipes(output, ConcoctiItems.CRYSTALIUM_NUGGET, ConcoctiItems.CRYSTALIUM_INGOT, ConcoctiItems.CRYSTALIUM_BLOCK);
        threeStageStorageRecipes(output, ConcoctiItems.LATTICIUM_NUGGET, ConcoctiItems.LATTICIUM_INGOT, ConcoctiItems.LATTICIUM_BLOCK);

        smeltingResultFromBase(
                output,
                ConcoctiItems.RAW_CRYSTALIUM,
                ConcoctiItems.CRYSTALIUM_ORE
        );

        moldBaseRecipes(output, Items.IRON_NUGGET, Items.COPPER_INGOT, MoldItem.Material.COPPER);
        moldBaseRecipes(output, ConcoctiItems.PURIFIED_CONCOCTI_NUGGET, Items.DIAMOND, MoldItem.Material.DIAMOND);
        moldBaseRecipes(output, ConcoctiItems.TOUGH_CONCOCTI_NUGGET, ConcoctiItems.LATTICIUM_INGOT, MoldItem.Material.LATTICIUM);

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

        concoctiMelterRecipes(output,
                ConcoctiItems.PURIFIED_CONCOCTI_NUGGET,
                ConcoctiItems.PURIFIED_CONCOCTI_INGOT,
                ConcoctiItems.PURIFIED_CONCOCTI_BLOCK,
                5,
                ConcoctiFluids.MOLTEN_CONCOCTI.get()
        );

        concoctiMelterRecipes(output,
                ConcoctiItems.TOUGH_CONCOCTI_NUGGET,
                ConcoctiItems.TOUGH_CONCOCTI_INGOT,
                ConcoctiItems.TOUGH_CONCOCTI_BLOCK,
                20,
                ConcoctiFluids.MOLTEN_TOUGH_CONCOCTI.get()
        );

        concoctiMelterRecipes(output,
                ConcoctiItems.CONDUCTIVIUM_NUGGET,
                ConcoctiItems.CONDUCTIVIUM_INGOT,
                ConcoctiItems.CONDUCTIVIUM_BLOCK,
                10,
                ConcoctiFluids.MOLTEN_CONDUCTIVIUM.get()
        );

        concoctiMelterRecipes(output,
                ConcoctiItems.LATTICIUM_NUGGET,
                ConcoctiItems.LATTICIUM_INGOT,
                ConcoctiItems.LATTICIUM_BLOCK,
                320, // 16 seconds
                ConcoctiFluids.MOLTEN_LATTICIUM.get()
        );

        concoctiMelterRecipe(output, Items.COPPER_INGOT, 5 * 8, // discount
                new FluidStack(ConcoctiFluids.MOLTEN_COPPER, ConcoctiConstants.MOLTEN_INGOT)
        );
        concoctiMelterRecipe(output, Items.COPPER_BLOCK, 5 * 64,
                new FluidStack(ConcoctiFluids.MOLTEN_COPPER, ConcoctiConstants.MOLTEN_BLOCK)
        );
        concoctiMelterRecipe(output, Items.ICE, 40, new FluidStack(Fluids.WATER, 1000));
        concoctiMelterRecipe(output, Items.PACKED_ICE, 80, new FluidStack(Fluids.WATER, 9000));
        concoctiMelterRecipe(output, Items.BLUE_ICE, 640, new FluidStack(Fluids.WATER, 81000));

        // Concocti Solidifier Recipes
        {
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
            concoctiSolidifierRecipe(output, null, Items.CAULDRON,
                    new FluidStack(ConcoctiFluids.MOLTEN_TOUGH_CONCOCTI, ConcoctiConstants.MOLTEN_BLOCK), 80 * 8 * 8,
                    new ItemStack(ConcoctiItems.TOUGH_CONCOCTI_BLOCK.get()));
            concoctiSolidifierRecipe(output, null, Items.CAULDRON,
                    new FluidStack(ConcoctiFluids.MOLTEN_LATTICIUM, ConcoctiConstants.MOLTEN_BLOCK), 10 * 8 * 8,
                    new ItemStack(ConcoctiItems.LATTICIUM_BLOCK.get()));
        }

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
                ),
                new ConcoctiMoldingSolidifierRecipe(
                        new ItemStack(ConcoctiItems.TOUGH_CONCOCTI_NUGGET.get(), 1),
                        null,
                        new FluidStack(ConcoctiFluids.MOLTEN_TOUGH_CONCOCTI, ConcoctiConstants.MOLTEN_NUGGET),
                        80
                ),
                new ConcoctiMoldingSolidifierRecipe(
                        new ItemStack(ConcoctiItems.LATTICIUM_NUGGET.get(), 1),
                        null,
                        new FluidStack(ConcoctiFluids.MOLTEN_LATTICIUM, ConcoctiConstants.MOLTEN_NUGGET),
                        10
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
                ),
                new ConcoctiMoldingSolidifierRecipe(
                        new ItemStack(ConcoctiItems.TOUGH_CONCOCTI_INGOT.get(), 1),
                        null,
                        new FluidStack(ConcoctiFluids.MOLTEN_TOUGH_CONCOCTI, ConcoctiConstants.MOLTEN_INGOT),
                        80 * 8
                ),
                new ConcoctiMoldingSolidifierRecipe(
                        new ItemStack(ConcoctiItems.LATTICIUM_INGOT.get(), 1),
                        null,
                        new FluidStack(ConcoctiFluids.MOLTEN_LATTICIUM, ConcoctiConstants.MOLTEN_INGOT),
                        10 * 8
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

        concoctiMixerRecipe(
                output,
                "crystalium_solution",
                20 * 4,
                List.of(
                        SizedIngredient.of(ConcoctiItems.RAW_CRYSTALIUM, 18),
                    SizedIngredient.of(Items.POPPED_CHORUS_FRUIT, 1)
                ),
                List.of(SizedFluidIngredient.of(Fluids.WATER, 1000)),
                null,
                new FluidStack(ConcoctiFluids.CRYSTALIUM_SOLUTION, 1000)
        );

        concoctiMixerRecipe(output,
                "supersaturated_crystalium_solution",
                20 * 20,
                List.of(SizedIngredient.of(ConcoctiItems.RAW_CRYSTALIUM, 9)),
                List.of(SizedFluidIngredient.of(ConcoctiFluids.CRYSTALIUM_SOLUTION.get(), 1000)),
                null,
                new FluidStack(ConcoctiFluids.SUPERSATURATED_CRYSTALIUM_SOLUTION, 1000)
        );

        concoctiMixerRecipe(output,
                "crystalium_nugget",
                20 * 45,
                List.of(),
                List.of(SizedFluidIngredient.of(ConcoctiFluids.SUPERSATURATED_CRYSTALIUM_SOLUTION.get(), 200)),
                new ItemStack(ConcoctiItems.CRYSTALIUM_NUGGET.get(), 1),
                new FluidStack(Fluids.WATER, 100)
        );

        concoctiMixerRecipe(output,
                "crystalium_ore_extraction",
                20 * 60,
                List.of(
                        SizedIngredient.of(ConcoctiBlocks.CRYSTALIUM_ORE.get(), 3)
                ),
                List.of(
                        SizedFluidIngredient.of(Fluids.WATER, 500)
                ),
                new ItemStack(Blocks.END_STONE, 2),
                new FluidStack(ConcoctiFluids.CRYSTALIUM_SOLUTION.get(), 500)
        );

        concoctiMixerRecipe(output,
                "boiling_crystalium_solution_to_supersaturation",
                10 * 15,
                List.of(
                        SizedIngredient.of(ConcoctiItems.CRYSTALIUM_NUGGET.get(), 1)
                ),
                List.of(
                        SizedFluidIngredient.of(ConcoctiFluids.CRYSTALIUM_SOLUTION.get(), 300)
                ),
                null,
                new FluidStack(ConcoctiFluids.SUPERSATURATED_CRYSTALIUM_SOLUTION.get(), 200)
        );

        concoctiMixerRecipe(output,
                "alloying_tough_concocti_ingot",
                20 * 20,
                List.of(
                        SizedIngredient.of(ConcoctiItems.ELECTROSTATIC_CONDUCTIVIUM_INGOT.get(), 1),
                        SizedIngredient.of(ConcoctiItems.CRYSTALIUM_INGOT.get(), 1)
                ),
                List.of(
                        SizedFluidIngredient.of(ConcoctiFluids.MOLTEN_CONCOCTI.get(), 2 * ConcoctiConstants.MOLTEN_INGOT)
                ),
                null,
                new FluidStack(ConcoctiFluids.MOLTEN_TOUGH_CONCOCTI.get(), 4 * ConcoctiConstants.MOLTEN_INGOT)
        );

        concoctiMixerRecipe(output,
                "alloying_molten_latticium",
                20 * 2,
                List.of(
                        SizedIngredient.of(Items.COAL, 2),
                        SizedIngredient.of(Items.QUARTZ, 1)
                ),
                List.of(
                        SizedFluidIngredient.of(ConcoctiFluids.MOLTEN_TOUGH_CONCOCTI.get(), ConcoctiConstants.MOLTEN_NUGGET)
                ),
                null,
                new FluidStack(ConcoctiFluids.MOLTEN_LATTICIUM.get(), 4 * ConcoctiConstants.MOLTEN_NUGGET)
        );

        concoctiMixerRecipe(output,
                "generating_concocti_with_concocti_seeds",
                20 * 32,
                List.of(
                        SizedIngredient.of(ConcoctiItems.CONCOCTI_SEEDS, 1)
                ),
                List.of(),
                null,
                new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, ConcoctiConstants.MOLTEN_BLOCK_PURIFIED)
        );

        concoctiMixerRecipe(output,
                "generating_concocti_with_infinity_concocti_seeds",
                20 * 32,
                List.of(
                        SizedIngredient.of(ConcoctiItems.INFINITY_CONCOCTI_SEEDS, 1)
                ),
                List.of(),
                new ItemStack(ConcoctiItems.INFINITY_CONCOCTI_SEEDS.get(), 1),
                new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, ConcoctiConstants.MOLTEN_BLOCK_PURIFIED * 2)
        );

        // Mold recipes
        for (var entryMaterial : ConcoctiItems.MOLDS.entrySet()) {
            MoldItem.Material material = entryMaterial.getKey();
            DeferredItem<? extends Item> moldBase = ConcoctiItems.MOLD_BASES.get(material);
            String moldBaseName = BuiltInRegistries.ITEM.getKey(moldBase.get()).getPath();
            for (var entryType : entryMaterial.getValue().entrySet()) {
                MoldItem.Type type = entryType.getKey();
                DeferredItem<? extends Item> mold = entryType.getValue();
                String moldName = BuiltInRegistries.ITEM.getKey(mold.get()).getPath();
                String recipeName =
                        moldName + "_from_" + moldBaseName;
                concoctiMixerRecipe(
                        output,
                        recipeName,
                        2 * 20,
                        List.of(SizedIngredient.of(moldBase.get(), 1), SizedIngredient.of(type.getTag(), 1)),
                        List.of(),
                        new ItemStack(mold.get()),
                        null
                );
            }
        }

        // Concocti Electron Collector Recipe
        concoctiElectronCollectorRecipe(output, 30 * 20, 0.5f, new FluidStack(ConcoctiFluids.MOLTEN_LIGHTNING, 1));

        // Concocti Crystallizer Recipes
        concoctiCrystallizerRecipe(
                output,
                Ingredient.of(ConcoctiItems.CRYSTALIUM_NUGGET),
                new FluidStack(ConcoctiFluids.SUPERSATURATED_CRYSTALIUM_SOLUTION, 50),
                5 * 20,
                new ItemStack(ConcoctiItems.CRYSTALIUM_NUGGET.get(), 4)
        );
        concoctiCrystallizerRecipe(
                output,
                Ingredient.of(ConcoctiItems.CRYSTALIUM_INGOT),
                new FluidStack(ConcoctiFluids.SUPERSATURATED_CRYSTALIUM_SOLUTION, 50 * 9 * 2/3),
                5 * 8 * 20,
                new ItemStack(ConcoctiItems.CRYSTALIUM_INGOT.get(), 3)
        );
        concoctiCrystallizerRecipe(
                output,
                Ingredient.of(ConcoctiItems.CRYSTALIUM_BLOCK),
                new FluidStack(ConcoctiFluids.SUPERSATURATED_CRYSTALIUM_SOLUTION, 50 * 81 * 1/3),
                5 * 64 * 20,
                new ItemStack(ConcoctiItems.CRYSTALIUM_BLOCK.get(), 2)
        );
        concoctiCrystallizerRecipe(
                output,
                Ingredient.of(Items.ICE),
                new FluidStack(Fluids.WATER, 1000),
                5,
                new ItemStack(Items.ICE, 2)
        );
        concoctiCrystallizerRecipe(
                output,
                Ingredient.of(Items.PACKED_ICE),
                new FluidStack(Fluids.WATER, 9000),
                15,
                new ItemStack(Items.PACKED_ICE, 2)
        );
        concoctiCrystallizerRecipe(
                output,
                Ingredient.of(Items.BLUE_ICE),
                new FluidStack(Fluids.WATER, 81000),
                75,
                new ItemStack(Items.BLUE_ICE, 2)
        );

        // Concocti Compressor recipes
        concoctiCompressorRecipe(
            output,
            "compressed_concocti_nugget",
            50,
            List.of(
                SizedIngredient.of(ConcoctiItems.TOUGH_CONCOCTI_NUGGET, 4),
                SizedIngredient.of(ConcoctiItems.DIAMETHYST_BLOCK, 1)
            ),
            List.of(
                SizedFluidIngredient.of(Fluids.LAVA, 100),
                SizedFluidIngredient.of(ConcoctiFluids.MOLTEN_LATTICIUM.get(), 1_000)
            ),
            new ItemStack(ConcoctiItems.COMPRESSED_CONCOCTI_NUGGET.get(), 1),
            null
        );
        concoctiCompressorRecipe(
                output,
                "compressed_concocti_ingot",
                200,
                List.of(
                        SizedIngredient.of(ConcoctiItems.COMPRESSED_CONCOCTI_NUGGET, 9),
                        SizedIngredient.of(ConcoctiItems.TOUGH_CONCOCTI_INGOT, 4),
                        SizedIngredient.of(ConcoctiItems.ELECTROSTATIC_CONDUCTIVIUM_INGOT, 9),
                        SizedIngredient.of(ConcoctiItems.DIAMETHYST_BLOCK, 9)
                ),
                List.of(
                        SizedFluidIngredient.of(Fluids.LAVA, 1000),
                        SizedFluidIngredient.of(ConcoctiFluids.MOLTEN_LATTICIUM.get(), 8_000)
                ),
                new ItemStack(ConcoctiItems.COMPRESSED_CONCOCTI_INGOT.get(), 1),
                null
        );
        concoctiCompressorRecipe(
                output,
                "compressed_concocti_block",
                800,
                List.of(
                        SizedIngredient.of(ConcoctiItems.COMPRESSED_CONCOCTI_INGOT, 9),
                        SizedIngredient.of(ConcoctiItems.TOUGH_CONCOCTI_BLOCK, 4),
                        SizedIngredient.of(ConcoctiItems.ELECTROSTATIC_CONDUCTIVIUM_INGOT, 64),
                        SizedIngredient.of(ConcoctiItems.ELECTROSTATIC_CONDUCTIVIUM_INGOT, 81 - 64)
                ),
                List.of(
                        SizedFluidIngredient.of(Fluids.LAVA, 10000),
                        SizedFluidIngredient.of(ConcoctiFluids.MOLTEN_LATTICIUM.get(), 64_000)
                ),
                new ItemStack(ConcoctiItems.COMPRESSED_CONCOCTI_BLOCK.get(), 1),
                null
        );
        concoctiMultiblockRecipe(
                output,
                ConcoctiMachines.MAGNETIC_SEPARATOR,
                "magnetic_extraction_of_iron_nugget",
                10,
                List.of(),
                List.of(
                        SizedFluidIngredient.of(ConcoctiFluids.MOLTEN_CONCOCTI.get(), ConcoctiConstants.MOLTEN_INGOT * 2)
                ),
                List.of(new ItemStack(Items.IRON_NUGGET, 4), new ItemStack(ConcoctiItems.DENSE_CONCOCTI_PELLET.get(), 1)),
                List.of()
        );

        // Hatch Recipes
        for (HatchType type : HatchType.values()) {
            var map = ConcoctiBlocks.HATCHES.get(type);
            ConcoctiHatchBlock inputHatch = map.get(HatchPurpose.INPUT).get();
            ConcoctiHatchBlock outputHatch = map.get(HatchPurpose.OUTPUT).get();
            Item subIngredient = switch (type) {
                case ITEM -> Items.HOPPER;
                case FLUID -> Items.CAULDRON;
                case ENERGY -> Items.REDSTONE;
            };
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, inputHatch.asItem())
                    .define('#', ConcoctiBlocks.TOUGH_CONCOCTI_BRICKS)
                    .define('X', subIngredient)
                    .pattern(" X ")
                    .pattern(" # ")
                    .pattern("   ")
                    .group(null)
                    .unlockedBy(getHasName(ConcoctiBlocks.TOUGH_CONCOCTI_BRICKS), has(subIngredient))
                    .save(output, BuiltInRegistries.ITEM.getKey(inputHatch.asItem()));
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, outputHatch.asItem())
                    .define('#', ConcoctiBlocks.TOUGH_CONCOCTI_BRICKS)
                    .define('X', subIngredient)
                    .pattern("   ")
                    .pattern(" # ")
                    .pattern(" X ")
                    .group(null)
                    .unlockedBy(getHasName(ConcoctiBlocks.TOUGH_CONCOCTI_BRICKS), has(subIngredient))
                    .save(output, BuiltInRegistries.ITEM.getKey(outputHatch.asItem()));
        }
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
            ResourceLocation loc = type.tag;

            ItemLike resultItem = ConcoctiItems.MOLDS.get(moldMaterial).get(type);

            ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, resultItem)
                    .requires(moldBase)
                    .requires(TagKey.create(Registries.ITEM, loc))
                    .unlockedBy(getHasName(nugget), has(nugget))
                    .unlockedBy(getHasName(ingot), has(ingot))
                    .save(output, BuiltInRegistries.ITEM.getKey(resultItem.asItem()));
        }

        String moldBaseName = BuiltInRegistries.ITEM.getKey(moldBase.asItem()).getPath();
        concoctiMixerRecipe(
                output,
                moldBaseName,
                4 * 20,
                List.of(SizedIngredient.of(ingot, 1), SizedIngredient.of(nugget, 1)),
                List.of(),
                new ItemStack(moldBase),
                null
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
        new ConcoctiMelter.Recipe.Builder(
                SizedIngredient.of(input.asItem(), 1),
                pureResult,
                byproductResult,
                ticks
        ).save(output);
    }

    /**
     * Generates a Concocti Melter Recipe that melts an item into fluids. Only has one fluid product.
     */
    private static void concoctiMelterRecipe(RecipeOutput output, ItemLike input, int ticks, FluidStack pureResult) {
        new ConcoctiMelter.Recipe.Builder(
                SizedIngredient.of(input.asItem(), 1),
                pureResult,
                FluidStack.EMPTY,
                ticks
        ).save(output);
    }

    /**
     * Generates a set of Concocti Melter Recipes that melts nuggets, ingots and blocks of same material into fluids.
     * <p>
     * Note: the {@code ticksPerNugget} supplied is multiplied by 8 for ingots and multiplied by 64 for nuggets, serving as a discount.
     */
    private static void concoctiMelterRecipes(RecipeOutput output, ItemLike nugget, ItemLike ingot, ItemLike block, int ticksPerNugget, Fluid pureResult) {
        concoctiMelterRecipe(output, nugget, ticksPerNugget,
                new FluidStack(pureResult, ConcoctiConstants.MOLTEN_NUGGET),
                FluidStack.EMPTY
        );
        concoctiMelterRecipe(output, ingot, ticksPerNugget * 8, // discount
                new FluidStack(pureResult, ConcoctiConstants.MOLTEN_INGOT),
                FluidStack.EMPTY
        );
        concoctiMelterRecipe(output, block, ticksPerNugget * 64,
                new FluidStack(pureResult, ConcoctiConstants.MOLTEN_BLOCK),
                FluidStack.EMPTY
        );
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
        new ConcoctiSolidifier.Recipe.Builder(
                baseItem == null ? Ingredient.EMPTY : baseItem,
                mold,
                SizedFluidIngredient.of(inputFluid),
                outputItem,
                ticks
        ).save(output);
    }

    /**
     * Generates a Concocti Crystallizer Recipe that uses a seed crystal to crystallize a fluid.
     */
    private static void concoctiCrystallizerRecipe(RecipeOutput output, Ingredient seedCrystal,
                                                   FluidStack inputFluid, int ticks, ItemStack outputItem) {
        new ConcoctiCrystallizer.Recipe.Builder(
                seedCrystal,
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
        new ConcoctiMixer.Recipe.Builder(
                ResourceLocation.fromNamespaceAndPath(MODID, "mixing/" + name),
                inputItems,
                outputItem,
                inputFluids,
                outputFluid,
                ticks
        ).save(output);
    }

    /**
     * Generates a Concocti Compressor Recipe.
     */
    private static void concoctiCompressorRecipe(
            RecipeOutput output,
            String name,
            int ticks,
            List<SizedIngredient> inputItems,
            List<SizedFluidIngredient> inputFluids,
            ItemStack outputItem,
            FluidStack outputFluid
    ) {
        new ConcoctiCompressor.Recipe.Builder(
                ResourceLocation.fromNamespaceAndPath(MODID, "compressing/" + name),
                inputItems,
                outputItem,
                inputFluids,
                outputFluid,
                ticks
        ).save(output);
    }

    /**
     * Generates a Concocti Multiblock's Recipe.
     */
    private static void concoctiMultiblockRecipe(
            RecipeOutput output,
            ConcoctiMultiBlockMachine multiBlockMachine,
            String name,
            int ticks,
            List<SizedIngredient> inputItems,
            List<SizedFluidIngredient> inputFluids,
            List<ItemStack> outputItems,
            List<FluidStack> outputFluids
    ) {
        new ConcoctiMultiBlockMachine.Recipe.Builder(
                multiBlockMachine.ID,
                ResourceLocation.fromNamespaceAndPath(MODID, "multiblock/" + name),
                inputItems,
                outputItems,
                inputFluids,
                outputFluids,
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
        new ConcoctiElectronCollector.Recipe.Builder(
                chance,
                outputFluid,
                ticks
        ).save(output);
    }

    private static String withModId(String name) {
        return MODID + ":" + name;
    }
}
