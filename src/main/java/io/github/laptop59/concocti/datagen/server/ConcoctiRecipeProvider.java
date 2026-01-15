package io.github.laptop59.concocti.datagen.server;

import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.ConcoctiHatchBlock;
import io.github.laptop59.concocti.common.block.HatchPurpose;
import io.github.laptop59.concocti.common.block.HatchType;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluid;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluids;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.item.MoldItem;
import io.github.laptop59.concocti.common.machine.ConcoctiMachines;
import io.github.laptop59.concocti.common.machine.ConcoctiMultiBlockMachine;
import io.github.laptop59.concocti.common.machine.impl.*;
import io.github.laptop59.concocti.common.recipe.*;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
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
        {
            concoctiMelterRecipe(output, ConcoctiItems.DIRTY_CONCOCTI_NUGGET, 10,
                    new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, ConcoctiConstants.MOLTEN_NUGGET_PURIFIED),
                    new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTIZED_DIRT, 3),
                    "dirty_concocti_nugget"
            );
            concoctiMelterRecipe(output, ConcoctiItems.DIRTY_CONCOCTI_INGOT, 10 * 8, // discount
                    new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, ConcoctiConstants.MOLTEN_INGOT_PURIFIED),
                    new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTIZED_DIRT, 3 * 9),
                    "dirty_concocti_ingot"
            );
            concoctiMelterRecipe(output, ConcoctiItems.DIRTY_CONCOCTI_BLOCK, 10 * 64,
                    new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, ConcoctiConstants.MOLTEN_BLOCK_PURIFIED),
                    new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTIZED_DIRT, 3 * 81),
                    "dirty_concoti_block"
            );

            concoctiMelterSolidifierRecipes(output,
                    ConcoctiItems.PURIFIED_CONCOCTI_NUGGET,
                    ConcoctiItems.PURIFIED_CONCOCTI_INGOT,
                    ConcoctiItems.PURIFIED_CONCOCTI_BLOCK,
                    5,
                    15,
                    ConcoctiFluids.MOLTEN_CONCOCTI.get()
            );

            concoctiMelterSolidifierRecipes(output,
                    ConcoctiItems.TOUGH_CONCOCTI_NUGGET,
                    ConcoctiItems.TOUGH_CONCOCTI_INGOT,
                    ConcoctiItems.TOUGH_CONCOCTI_BLOCK,
                    60,
                    20,
                    ConcoctiFluids.MOLTEN_TOUGH_CONCOCTI.get()
            );

            concoctiMelterSolidifierRecipes(output,
                    ConcoctiItems.CONDUCTIVIUM_NUGGET,
                    ConcoctiItems.CONDUCTIVIUM_INGOT,
                    ConcoctiItems.CONDUCTIVIUM_BLOCK,
                    900,
                    45,
                    ConcoctiFluids.MOLTEN_CONDUCTIVIUM.get()
            );

            concoctiMelterSolidifierRecipes(output,
                    ConcoctiItems.LATTICIUM_NUGGET,
                    ConcoctiItems.LATTICIUM_INGOT,
                    ConcoctiItems.LATTICIUM_BLOCK,
                    480,
                    16,
                    ConcoctiFluids.MOLTEN_LATTICIUM.get()
            );

            concoctiMelterSolidifierRecipes(output,
                    Items.IRON_NUGGET,
                    Items.IRON_INGOT,
                    Items.IRON_BLOCK,
                    60,
                    30,
                    ConcoctiFluids.MOLTEN_IRON.get()
            );

            concoctiMelterSolidifierRecipes(output,
                    Items.COPPER_INGOT,
                    Items.COPPER_BLOCK,
                    90,
                    45,
                    ConcoctiFluids.MOLTEN_COPPER.get()
            );

            concoctiMelterSolidifierRecipes(output,
                    Items.REDSTONE,
                    Items.REDSTONE_BLOCK,
                    18,
                    12,
                    ConcoctiFluids.MOLTEN_REDSTONE.get(),
                    MoldItem.Type.DUST
            );

            concoctiMelterRecipe(output, Items.ICE, 400, new FluidStack(Fluids.WATER, 1000), "ice");
            concoctiMelterRecipe(output, Items.PACKED_ICE, 800, new FluidStack(Fluids.WATER, 9000), "packet_ice");
            concoctiMelterRecipe(output, Items.BLUE_ICE, 6400, new FluidStack(Fluids.WATER, 81000), "blue_ice");
        }

        // Concocti Solidifier Recipes
        {
            concoctiSolidifierRecipe(
                output,
                null,
                ItemRecipeIngredient.of(Ingredient.of(Items.ICE), 1, true),
                new FluidStack(Fluids.WATER, 1000),
                200, new ItemStack(Items.ICE)
            );
            concoctiSolidifierRecipe(
                output,
                ItemRecipeIngredient.of(Items.ICE, 9),
                ItemRecipeIngredient.of(Ingredient.of(Items.PACKED_ICE), 1, true),
                new FluidStack(Fluids.WATER, 4000),
                400, new ItemStack(Items.PACKED_ICE)
            );
            concoctiSolidifierRecipe(
                output,
                ItemRecipeIngredient.of(Items.PACKED_ICE, 9),
                ItemRecipeIngredient.of(Ingredient.of(Items.BLUE_ICE), 1, true),
                new FluidStack(Fluids.WATER, 2000),
                800, new ItemStack(Items.BLUE_ICE)
            );
            concoctiSolidifierRecipe(
                output,
                null,
                ItemRecipeIngredient.of(Ingredient.of(Items.LAVA_BUCKET), 1, true),
                new FluidStack(Fluids.WATER, 1000),
                60, new ItemStack(Items.COBBLESTONE)
            );
        }

        // Concocti Mixer Recipes
        {
            concoctiMixerRecipe(output, "molten_conductivium", 10, List.of(), List.of(
                    FluidRecipeIngredient.of(new FluidStack(ConcoctiFluids.MOLTEN_COPPER, 27)),
                    FluidRecipeIngredient.of(new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, 9))
            ), null, new FluidStack(ConcoctiFluids.MOLTEN_CONDUCTIVIUM, 18));

            concoctiMixerRecipe(output, "electrostatic_conductivium_nugget_mixing", 20 * 20, List.of(
                    ItemRecipeIngredient.of(ConcoctiItems.CONDUCTIVIUM_NUGGET.get(), 3),
                    ItemRecipeIngredient.of(Items.REDSTONE, 16)
            ), List.of(
                    FluidRecipeIngredient.of(new FluidStack(ConcoctiFluids.MOLTEN_LIGHTNING, 1)),
                    FluidRecipeIngredient.of(new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, ConcoctiConstants.MOLTEN_NUGGET))
            ), new ItemStack(ConcoctiItems.ELECTROSTATIC_CONDUCTIVIUM_NUGGET.get()), null);

            concoctiMixerRecipe(output, "electrostatic_conductivium_ingot_mixing", 4 * 20 * 20, List.of(
                    ItemRecipeIngredient.of(ConcoctiItems.ELECTROSTATIC_CONDUCTIVIUM_NUGGET.get(), 9),
                    ItemRecipeIngredient.of(Items.REDSTONE, 64),
                    ItemRecipeIngredient.of(ConcoctiItems.PURIFIED_CONCOCTI_INGOT.get(), 1)
            ), List.of(
                    FluidRecipeIngredient.of(new FluidStack(ConcoctiFluids.MOLTEN_COPPER, ConcoctiConstants.MOLTEN_BLOCK))
            ), new ItemStack(ConcoctiItems.ELECTROSTATIC_CONDUCTIVIUM_INGOT.get()), null);

            concoctiMixerRecipe(output, "electrostatic_conductivium_nugget_from_ingot", 60 * 20, List.of(
                    ItemRecipeIngredient.of(ConcoctiItems.ELECTROSTATIC_CONDUCTIVIUM_INGOT.get(), 1)
            ), List.of(), new ItemStack(ConcoctiItems.ELECTROSTATIC_CONDUCTIVIUM_NUGGET.get(), 9), null);

            concoctiMixerRecipe(
                    output,
                    "crystalium_solution",
                    20 * 4,
                    List.of(
                            ItemRecipeIngredient.of(ConcoctiItems.RAW_CRYSTALIUM, 18),
                            ItemRecipeIngredient.of(Items.POPPED_CHORUS_FRUIT, 1)
                    ),
                    List.of(FluidRecipeIngredient.of(Fluids.WATER, 1000)),
                    null,
                    new FluidStack(ConcoctiFluids.CRYSTALIUM_SOLUTION, 1000)
            );

            concoctiMixerRecipe(output,
                    "supersaturated_crystalium_solution",
                    20 * 20,
                    List.of(ItemRecipeIngredient.of(ConcoctiItems.RAW_CRYSTALIUM, 9)),
                    List.of(FluidRecipeIngredient.of(ConcoctiFluids.CRYSTALIUM_SOLUTION.get(), 1000)),
                    null,
                    new FluidStack(ConcoctiFluids.SUPERSATURATED_CRYSTALIUM_SOLUTION, 1000)
            );

            concoctiMixerRecipe(output,
                    "crystalium_nugget",
                    20 * 45,
                    List.of(),
                    List.of(FluidRecipeIngredient.of(ConcoctiFluids.SUPERSATURATED_CRYSTALIUM_SOLUTION.get(), 200)),
                    new ItemStack(ConcoctiItems.CRYSTALIUM_NUGGET.get(), 1),
                    new FluidStack(Fluids.WATER, 100)
            );

            concoctiMixerRecipe(output,
                    "crystalium_ore_extraction",
                    20 * 60,
                    List.of(
                            ItemRecipeIngredient.of(ConcoctiBlocks.CRYSTALIUM_ORE.get(), 3)
                    ),
                    List.of(
                            FluidRecipeIngredient.of(Fluids.WATER, 500)
                    ),
                    new ItemStack(Blocks.END_STONE, 2),
                    new FluidStack(ConcoctiFluids.CRYSTALIUM_SOLUTION.get(), 500)
            );

            concoctiMixerRecipe(output,
                    "boiling_crystalium_solution_to_supersaturation",
                    10 * 15,
                    List.of(
                            ItemRecipeIngredient.of(ConcoctiItems.CRYSTALIUM_NUGGET.get(), 1)
                    ),
                    List.of(
                            FluidRecipeIngredient.of(ConcoctiFluids.CRYSTALIUM_SOLUTION.get(), 300)
                    ),
                    null,
                    new FluidStack(ConcoctiFluids.SUPERSATURATED_CRYSTALIUM_SOLUTION.get(), 200)
            );

            concoctiMixerRecipe(output,
                    "alloying_tough_concocti_ingot",
                    20 * 20,
                    List.of(
                            ItemRecipeIngredient.of(ConcoctiItems.ELECTROSTATIC_CONDUCTIVIUM_INGOT.get(), 1),
                            ItemRecipeIngredient.of(ConcoctiItems.CRYSTALIUM_INGOT.get(), 1)
                    ),
                    List.of(
                            FluidRecipeIngredient.of(ConcoctiFluids.MOLTEN_CONCOCTI.get(), 2 * ConcoctiConstants.MOLTEN_INGOT)
                    ),
                    null,
                    new FluidStack(ConcoctiFluids.MOLTEN_TOUGH_CONCOCTI.get(), 4 * ConcoctiConstants.MOLTEN_INGOT)
            );

            concoctiMixerRecipe(output,
                    "alloying_molten_latticium",
                    20 * 2,
                    List.of(
                            ItemRecipeIngredient.of(Items.COAL, 2),
                            ItemRecipeIngredient.of(Items.QUARTZ, 1)
                    ),
                    List.of(
                            FluidRecipeIngredient.of(ConcoctiFluids.MOLTEN_TOUGH_CONCOCTI.get(), ConcoctiConstants.MOLTEN_NUGGET)
                    ),
                    null,
                    new FluidStack(ConcoctiFluids.MOLTEN_LATTICIUM.get(), 4 * ConcoctiConstants.MOLTEN_NUGGET)
            );

            concoctiMixerRecipe(output,
                    "budding_amethyst_from_amethyst_block",
                    200,
                    List.of(
                            ItemRecipeIngredient.of(Items.AMETHYST_BLOCK, 1),
                            ItemRecipeIngredient.of(Items.AMETHYST_SHARD, 4)
                    ),
                    List.of(
                            FluidRecipeIngredient.of(ConcoctiFluids.MOLTEN_CONCOCTI.get(), ConcoctiConstants.MOLTEN_INGOT)
                    ),
                    new ItemStack(Items.BUDDING_AMETHYST),
                    null
            );

            concoctiMixerRecipe(output,
                    "generating_concocti_with_concocti_seeds",
                    3 * 20,
                    List.of(
                            ItemRecipeIngredient.of(ConcoctiItems.CONCOCTI_SEEDS, 1)
                    ),
                    List.of(),
                    null,
                    new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, ConcoctiConstants.MOLTEN_NUGGET * 6)
            );

            concoctiMixerRecipe(output,
                    "generating_concocti_with_infinity_concocti_seeds",
                    4 * 20,
                    List.of(
                            ItemRecipeIngredient.of(
                                ItemRecipeIngredientOption.of(
                                    Ingredient.of(ConcoctiItems.INFINITY_CONCOCTI_SEEDS),
                                    1,
                                    true
                                )
                            )
                    ),
                    List.of(),
                    null,
                    new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, ConcoctiConstants.MOLTEN_NUGGET * 12)
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
                            material.ticksToMake,
                            List.of(ItemRecipeIngredient.of(moldBase.get(), 1), ItemRecipeIngredient.of(Ingredient.of(type.getTag()), 1, true)),
                            List.of(),
                            new ItemStack(mold.get()),
                            null
                    );
                }
            }

            concoctiMixerRecipe(output, "crying_obsidian", 60, List.of(ItemRecipeIngredient.of(Items.OBSIDIAN, 1)), List.of(
                    FluidRecipeIngredient.of(new FluidStack(ConcoctiFluids.MOLTEN_TOUGH_CONCOCTI, ConcoctiConstants.MOLTEN_INGOT * 4))
            ), new ItemStack(Items.CRYING_OBSIDIAN), null);
            concoctiMixerRecipe(output, "soul_soil", 20, List.of(ItemRecipeIngredient.of(Items.SOUL_SAND, 2)), List.of(
                    FluidRecipeIngredient.of(new FluidStack(Fluids.LAVA, 250))
            ), new ItemStack(Items.SOUL_SOIL), null);
            concoctiMixerRecipe(output, "magma_block", 20, List.of(ItemRecipeIngredient.of(Items.NETHERRACK, 1), ItemRecipeIngredient.of(Items.BLAZE_POWDER, 4)), List.of(
                    FluidRecipeIngredient.of(new FluidStack(Fluids.LAVA, 250))
            ), new ItemStack(Items.MAGMA_BLOCK), null);


        }

        // Concocti Electron Collector Recipe
        concoctiElectronCollectorRecipe(output, 30 * 20, 0.5f, new FluidStack(ConcoctiFluids.MOLTEN_LIGHTNING, 1));

        // Concocti Crystallizer Recipes
        {
            concoctiCrystallizerRecipe(
                    output,
                    Ingredient.of(ConcoctiItems.CRYSTALIUM_NUGGET),
                    new FluidStack(ConcoctiFluids.SUPERSATURATED_CRYSTALIUM_SOLUTION, 50),
                    5 * 20,
                    new ItemStack(ConcoctiItems.CRYSTALIUM_NUGGET.get(), 3),
                    "crystalium_nugget"
            );
            concoctiCrystallizerRecipe(
                    output,
                    Ingredient.of(ConcoctiItems.CRYSTALIUM_INGOT),
                    new FluidStack(ConcoctiFluids.SUPERSATURATED_CRYSTALIUM_SOLUTION, 50 * 9 * 2 / 3),
                    5 * 8 * 20,
                    new ItemStack(ConcoctiItems.CRYSTALIUM_INGOT.get(), 2),
                    "crystalium_ingot"
            );
            concoctiCrystallizerRecipe(
                    output,
                    Ingredient.of(ConcoctiItems.CRYSTALIUM_BLOCK),
                    new FluidStack(ConcoctiFluids.SUPERSATURATED_CRYSTALIUM_SOLUTION, 50 * 81 * 1 / 3),
                    5 * 64 * 20,
                    new ItemStack(ConcoctiItems.CRYSTALIUM_BLOCK.get(), 1),
                    "crystalium_block"
            );
            concoctiCrystallizerRecipe(
                    output,
                    Ingredient.of(Items.ICE),
                    new FluidStack(Fluids.WATER, 1000),
                    5,
                    new ItemStack(Items.ICE, 1),
                    "ice"
            );
            concoctiCrystallizerRecipe(
                    output,
                    Ingredient.of(Items.PACKED_ICE),
                    new FluidStack(Fluids.WATER, 9000),
                    15,
                    new ItemStack(Items.PACKED_ICE, 1),
                   "packed_ice"
            );
            concoctiCrystallizerRecipe(
                    output,
                    Ingredient.of(Items.BLUE_ICE),
                    new FluidStack(Fluids.WATER, 81000),
                    75,
                    new ItemStack(Items.BLUE_ICE, 1),
                    "blue_ice"
            );
            concoctiCrystallizerRecipe(
                    output,
                    Ingredient.of(Items.AMETHYST_SHARD),
                    new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, ConcoctiConstants.MOLTEN_INGOT),
                    15,
                    new ItemStack(Items.AMETHYST_SHARD, 1),
                    "amethyst_shard"
            );
            concoctiCrystallizerRecipe(
                    output,
                    Ingredient.of(Items.AMETHYST_BLOCK),
                    new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, ConcoctiConstants.MOLTEN_INGOT * 4),
                    60,
                    new ItemStack(Items.AMETHYST_BLOCK, 1),
                    "amethyst_block"
            );
            concoctiCrystallizerRecipe(
                    output,
                    Ingredient.of(ConcoctiItems.DIAMETHYST_CRYSTAL),
                    new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, ConcoctiConstants.MOLTEN_INGOT * 4),
                    75,
                    new ItemStack(ConcoctiItems.DIAMETHYST_CRYSTAL.get(), 1),
                    "diamethyst_crystal"
            );
            concoctiCrystallizerRecipe(
                    output,
                    Ingredient.of(ConcoctiItems.DIAMETHYST_BLOCK),
                    new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, ConcoctiConstants.MOLTEN_BLOCK * 4),
                    75,
                    new ItemStack(ConcoctiItems.DIAMETHYST_BLOCK.get(), 1),
                    "diamethyst_block"
            );
            concoctiCrystallizerRecipe(
                    output,
                    Ingredient.of(Items.WARPED_ROOTS),
                    new FluidStack(Fluids.LAVA, 100),
                    3 * 20,
                    new ItemStack(Items.WARPED_ROOTS, 1),
                    "warped_roots"
            );
            concoctiCrystallizerRecipe(
                    output,
                    Ingredient.of(Items.CRIMSON_ROOTS),
                    new FluidStack(Fluids.LAVA, 125),
                    2 * 20,
                    new ItemStack(Items.CRIMSON_ROOTS, 1),
                    "crimson_roots"
            );
            concoctiCrystallizerRecipe(
                    output,
                    Ingredient.of(Items.CALCITE),
                    new FluidStack(Fluids.WATER, 2000),
                    10 * 20,
                    new ItemStack(Items.CALCITE, 1),
                    "calcite"
            );
        }

        // Concocti Compressor recipes

        concoctiCompressorRecipe(
            output,
            "compressed_concocti_nugget",
            50,
            List.of(
                ItemRecipeIngredient.of(ConcoctiItems.TOUGH_CONCOCTI_NUGGET, 4),
                ItemRecipeIngredient.of(ConcoctiItems.DIAMETHYST_BLOCK, 1)
            ),
            List.of(
                FluidRecipeIngredient.of(Fluids.LAVA, 100),
                FluidRecipeIngredient.of(ConcoctiFluids.MOLTEN_LATTICIUM.get(), ConcoctiConstants.MOLTEN_BLOCK)
            ),
            new ItemStack(ConcoctiItems.COMPRESSED_CONCOCTI_NUGGET.get(), 1),
            null
        );
        concoctiCompressorRecipe(
                output,
                "compressed_concocti_ingot",
                200,
                List.of(
                        ItemRecipeIngredient.of(ConcoctiItems.COMPRESSED_CONCOCTI_NUGGET, 9),
                        ItemRecipeIngredient.of(ConcoctiItems.TOUGH_CONCOCTI_INGOT, 4),
                        ItemRecipeIngredient.of(ConcoctiItems.ELECTROSTATIC_CONDUCTIVIUM_INGOT, 9),
                        ItemRecipeIngredient.of(ConcoctiItems.DIAMETHYST_BLOCK, 9)
                ),
                List.of(
                        FluidRecipeIngredient.of(Fluids.LAVA, 1000),
                        FluidRecipeIngredient.of(ConcoctiFluids.MOLTEN_LATTICIUM.get(), 10 * ConcoctiConstants.MOLTEN_BLOCK)
                ),
                new ItemStack(ConcoctiItems.COMPRESSED_CONCOCTI_INGOT.get(), 1),
                null
        );
        concoctiCompressorRecipe(
                output,
                "compressed_concocti_block",
                800,
                List.of(
                        ItemRecipeIngredient.of(ConcoctiItems.COMPRESSED_CONCOCTI_INGOT, 9),
                        ItemRecipeIngredient.of(ConcoctiItems.TOUGH_CONCOCTI_BLOCK, 4),
                        ItemRecipeIngredient.of(ConcoctiItems.ELECTROSTATIC_CONDUCTIVIUM_INGOT, 81)
                ),
                List.of(
                        FluidRecipeIngredient.of(Fluids.LAVA, 10000),
                        FluidRecipeIngredient.of(ConcoctiFluids.MOLTEN_LATTICIUM.get(), 100 * ConcoctiConstants.MOLTEN_BLOCK)
                ),
                new ItemStack(ConcoctiItems.COMPRESSED_CONCOCTI_BLOCK.get(), 1),
                null
        );
        concoctiCompressorRecipe(
            output,
            "diamethyst_crystal",
            250,
            List.of(
                ItemRecipeIngredient.of(Items.DIAMOND, 4),
                ItemRecipeIngredient.of(Items.AMETHYST_SHARD, 1)
            ), List.of(),
            new ItemStack(ConcoctiItems.DIAMETHYST_CRYSTAL.get(), 2), null
        );
        concoctiCompressorRecipe(
            output,
            "diamethyst_crystal_block",
            250 * 8,
            List.of(
                ItemRecipeIngredient.of(ConcoctiItems.DIAMETHYST_CRYSTAL.get(), 9)
            ), List.of(),
            new ItemStack(ConcoctiItems.DIAMETHYST_BLOCK.get(), 1), null
        );
        concoctiCompressorRecipe(
            output,
            "diamond_block",
            150,
            List.of(
                ItemRecipeIngredient.of(Items.DIAMOND, 9)
            ), List.of(),
            new ItemStack(Items.DIAMOND_BLOCK, 1), null
        );
        concoctiCompressorRecipe(
            output,
            "emerald_block",
            100,
            List.of(
                ItemRecipeIngredient.of(Items.EMERALD, 9)
            ), List.of(),
            new ItemStack(Items.EMERALD_BLOCK, 1), null
        );
        concoctiCompressorRecipe(
            output,
            "amethyst_block",
            70,
            List.of(
                ItemRecipeIngredient.of(Items.AMETHYST_SHARD, 4)
            ), List.of(),
            new ItemStack(Items.AMETHYST_BLOCK, 1), null
        );
        concoctiCompressorRecipe(
            output,
            "lapis_lazuli_block",
            50,
            List.of(
                ItemRecipeIngredient.of(Items.LAPIS_LAZULI, 9)
            ), List.of(),
            new ItemStack(Items.LAPIS_BLOCK, 1), null
        );
        concoctiCompressorRecipe(
            output,
            "crystalium_ingot",
            10,
            List.of(
                ItemRecipeIngredient.of(ConcoctiItems.CRYSTALIUM_NUGGET, 9)
            ), List.of(),
            new ItemStack(ConcoctiItems.CRYSTALIUM_INGOT.get(), 1), null
        );
        concoctiCompressorRecipe(
            output,
            "crystalium_block",
            20,
            List.of(
                ItemRecipeIngredient.of(ConcoctiItems.CRYSTALIUM_INGOT, 9)
            ), List.of(),
            new ItemStack(ConcoctiItems.CRYSTALIUM_BLOCK.get(), 1), null
        );
        concoctiCompressorRecipe(
            output,
            "coal_block",
            20,
            List.of(
                ItemRecipeIngredient.of(Items.COAL, 9)
            ), List.of(),
            new ItemStack(Items.COAL_BLOCK, 1), null
        );
        concoctiCompressorRecipe(
            output,
            "netherite_ingot",
            400,
            List.of(
                ItemRecipeIngredient.of(Items.NETHERITE_SCRAP, 4),
                ItemRecipeIngredient.of(Items.GOLD_INGOT, 4)
            ), List.of(),
            new ItemStack(Items.NETHERITE_INGOT, 2), null
        );


        concoctiCompressorRecipe(
                output,
                "brown_mushroom_block",
                20,
                List.of(
                        ItemRecipeIngredient.of(Items.BROWN_MUSHROOM, 9)
                ), List.of(),
                new ItemStack(Items.BROWN_MUSHROOM_BLOCK, 1), null
        );
        concoctiCompressorRecipe(
                output,
                "red_mushroom_block",
                20,
                List.of(
                        ItemRecipeIngredient.of(Items.RED_MUSHROOM, 9)
                ), List.of(),
                new ItemStack(Items.RED_MUSHROOM_BLOCK, 1), null
        );
        concoctiCompressorRecipe(
                output,
                "mushroom_stem",
                20,
                List.of(
                        ItemRecipeIngredient.of(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "mushrooms")), 8),
                        ItemRecipeIngredient.of(Items.BONE_MEAL, 1)
                ), List.of(),
                new ItemStack(Items.MUSHROOM_STEM, 1), null
        );
        concoctiCompressorRecipe(
                output,
                "chorus_plant",
                200,
                List.of(
                        ItemRecipeIngredient.of(Items.CHORUS_FRUIT, 9)
                ), List.of(),
                new ItemStack(Items.CHORUS_PLANT, 1), null
        );
        concoctiCompressorRecipe(
                output,
                "chorus_flower",
                400,
                List.of(
                        ItemRecipeIngredient.of(Items.CHORUS_PLANT, 4),
                        ItemRecipeIngredient.of(Items.BONE_MEAL, 8)
                ), List.of(),
                new ItemStack(Items.CHORUS_FLOWER, 1), null
        );
        concoctiCompressorRecipe(
                output,
                "dripstone_block",
                400,
                List.of(
                        ItemRecipeIngredient.of(Items.POINTED_DRIPSTONE, 4)
                ), List.of(),
                new ItemStack(Items.DRIPSTONE_BLOCK, 1), null
        );
        concoctiCompressorRecipe(
                output,
                "warped_nylium",
                20,
                List.of(
                        ItemRecipeIngredient.of(Items.NETHERRACK, 1),
                        ItemRecipeIngredient.of(Items.WARPED_ROOTS, 4)
                ), List.of(),
                new ItemStack(Items.WARPED_NYLIUM, 1), null
        );
        concoctiCompressorRecipe(
                output,
                "crimson_nylium",
                20,
                List.of(
                        ItemRecipeIngredient.of(Items.NETHERRACK, 1),
                        ItemRecipeIngredient.of(Items.CRIMSON_ROOTS, 4)
                ), List.of(),
                new ItemStack(Items.CRIMSON_NYLIUM, 1), null
        );
        concoctiCompressorRecipe(
                output,
                "gilded_blackstone",
                20,
                List.of(
                        ItemRecipeIngredient.of(Items.BLACKSTONE, 1),
                        ItemRecipeIngredient.of(Items.GOLD_INGOT, 1)
                ), List.of(FluidRecipeIngredient.of(Fluids.LAVA, ConcoctiConstants.MOLTEN_INGOT * 2)),
                new ItemStack(Items.GILDED_BLACKSTONE, 1), null
        );
        concoctiCompressorRecipe(
                output,
                "tuff",
                10 * 20,
                List.of(
                        ItemRecipeIngredient.of(Items.CALCITE, 1),
                        ItemRecipeIngredient.of(Items.DEEPSLATE, 1)
                ), List.of(),
                new ItemStack(Items.TUFF, 2), null
        );
        concoctiCompressorRecipe(
                output,
                "cobweb",
                20,
                List.of(
                        ItemRecipeIngredient.of(Items.STRING, 5)
                ), List.of(),
                new ItemStack(Items.COBWEB, 1), null
        );

        concoctiSolarCollectorRecipe(
                output,
                "converting_solar_to_molten_solarium",
                20,
                ItemRecipeIngredient.of(Ingredient.of(ConcoctiItems.SOLARIUM_CATALYST), 1, true),
                null,
                null,
                FluidOutput.of(ConcoctiFluids.MOLTEN_SOLARIUM.get(), ConcoctiConstants.MOLTEN_NUGGET),
                100
        );

        concoctiMultiblockRecipe(
                output,
                ConcoctiMachines.MAGNETIC_SEPARATOR,
                "magnetic_extraction_of_molten_concocti",
                30,
                List.of(),
                List.of(
                        FluidRecipeIngredient.of(ConcoctiFluids.MOLTEN_CONCOCTI.get(), ConcoctiConstants.MOLTEN_INGOT * 4)
                ),
                List.of(
                        ItemOutput.of(ConcoctiItems.DENSE_CONCOCTI_PELLET.get(), 5)
                ),
                List.of(
                        FluidOutput.of(ConcoctiFluids.MOLTEN_IRON, ConcoctiConstants.MOLTEN_INGOT * 5),
                        FluidOutput.of(ConcoctiFluids.MOLTEN_IRON, ConcoctiConstants.MOLTEN_INGOT * 2, 0.35f),
                        FluidOutput.of(ConcoctiFluids.MOLTEN_COPPER, ConcoctiConstants.MOLTEN_INGOT * 8)
                )
        );
        concoctiMultiblockRecipe(
                output,
                ConcoctiMachines.MAGNETIC_SEPARATOR,
                "magnetic_extraction_of_lava",
                10,
                List.of(),
                List.of(
                        FluidRecipeIngredient.of(Fluids.LAVA, ConcoctiConstants.MOLTEN_BLOCK * 2)
                ),
                List.of(
                        ItemOutput.of(Items.STONE, 1)
                ),
                List.of(
                        FluidOutput.of(ConcoctiFluids.MOLTEN_IRON, ConcoctiConstants.MOLTEN_NUGGET * 2)
                )
        );

        concoctiMultiblockRecipe(
                output,
                ConcoctiMachines.CENTRIFUGE,
                "centrifugation_of_molten_concocti",
                6,
                List.of(),
                List.of(
                        FluidRecipeIngredient.of(ConcoctiFluids.MOLTEN_CONCOCTI.get(), ConcoctiConstants.MOLTEN_NUGGET * 6)
                ),
                List.of(
                        ItemOutput.of(Items.GOLD_NUGGET, 2),
                        ItemOutput.of(Items.GOLD_NUGGET, 1, 0.5f),
                        ItemOutput.of(ConcoctiItems.DENSE_CONCOCTI_PELLET.get(), 1)
                ),
                List.of(
                        FluidOutput.of(ConcoctiFluids.MOLTEN_REDSTONE, ConcoctiConstants.MOLTEN_DUST)
                )
        );

        concoctiMultiblockRecipe(
                output,
                ConcoctiMachines.CENTRIFUGE,
                "centrifugation_of_molten_concoctized_dirt",
                10,
                List.of(),
                List.of(
                        FluidRecipeIngredient.of(ConcoctiFluids.MOLTEN_CONCOCTIZED_DIRT.get(), ConcoctiConstants.MOLTEN_INGOT * 2)
                ),
                List.of(
                        ItemOutput.of(Items.BONE_MEAL, 4),
                        ItemOutput.of(Items.BONE_MEAL, 4, 0.2f),
                        ItemOutput.of(ConcoctiItems.CONCOCTI_SEEDS.get(), 1, 0.05f),
                        ItemOutput.of(ConcoctiItems.PURIFIED_CONCOCTI_NUGGET.get(), 4)
                ),
                List.of()
        );

        concoctiMultiblockRecipe(
                output,
                ConcoctiMachines.CENTRIFUGE,
                "centrifugation_of_water_with_clay",
                40,
                List.of(
                        ItemRecipeIngredient.of(Ingredient.of(Items.CLAY), 1, true)
                ),
                List.of(
                        FluidRecipeIngredient.of(Fluids.WATER, 1000)
                ),
                List.of(
                        ItemOutput.of(Items.CLAY, 1, 0.375f)
                ),
                List.of()
        );

        concoctiMultiblockRecipe(
                output,
                ConcoctiMachines.CENTRIFUGE,
                "centrifugation_of_water_with_sand",
                40,
                List.of(
                        ItemRecipeIngredient.of(Ingredient.of(Items.SAND), 1, true)
                ),
                List.of(
                        FluidRecipeIngredient.of(Fluids.WATER, 1000)
                ),
                List.of(
                        ItemOutput.of(Items.SAND, 1, 0.25f)
                ),
                List.of()
        );

        concoctiMultiblockRecipe(
                output,
                ConcoctiMachines.CENTRIFUGE,
                "centrifugation_of_water_with_gravel",
                40,
                List.of(
                        ItemRecipeIngredient.of(Ingredient.of(Items.GRAVEL), 1, true)
                ),
                List.of(
                        FluidRecipeIngredient.of(Fluids.WATER, 1000)
                ),
                List.of(
                        ItemOutput.of(Items.GRAVEL, 1, 0.125f)
                ),
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
            ItemRecipeIngredient inputItem = recipe.input == null ? null : ItemRecipeIngredient.of(ItemRecipeIngredientOption.of(recipe.input));
            concoctiSolidifierRecipe(
                output,
                inputItem,
                ItemRecipeIngredient.of(
                    ItemRecipeIngredientOption.of(molds, true, true)
                ),
                recipe.fluidStack,
                recipe.ticks,
                recipe.result
            );
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
                List.of(ItemRecipeIngredient.of(ingot, 1), ItemRecipeIngredient.of(nugget, 1)),
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
                                             FluidStack pureResult, FluidStack byproductResult, String name) {
        new ConcoctiMelter.Recipe.Builder(
                ItemRecipeIngredient.of(Ingredient.of(input.asItem())),
                pureResult,
                byproductResult,
                ticks
        ).save(output, ResourceLocation.fromNamespaceAndPath(MODID, "melter/" + name));
    }

    /**
     * Generates a Concocti Melter Recipe that melts an item into fluids. Only has one fluid product.
     */
    private static void concoctiMelterRecipe(RecipeOutput output, ItemLike input, int ticks, FluidStack pureResult, FluidStack byproductResult) {
        new ConcoctiMelter.Recipe.Builder(
            ItemRecipeIngredient.of(Ingredient.of(input.asItem())),
            pureResult,
            byproductResult,
            ticks
        ).save(output, ResourceLocation.fromNamespaceAndPath(MODID, "melter/" + BuiltInRegistries.ITEM.getKey(input.asItem()).getPath()));
    }

    /**
     * Generates a Concocti Melter Recipe that melts an item into fluids. Only has one fluid product.
     */
    private static void concoctiMelterRecipe(RecipeOutput output, ItemLike input, int ticks, FluidStack pureResult, String name) {
        new ConcoctiMelter.Recipe.Builder(
            ItemRecipeIngredient.of(Ingredient.of(input.asItem())),
                pureResult,
                FluidStack.EMPTY,
                ticks
        ).save(output, ResourceLocation.fromNamespaceAndPath(MODID, "melter/" + name));
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
     * Generates a set of Concocti Melter Recipes that melts ingots and blocks of same material into fluids.
     * <p>
     * Note: the {@code ticksPerIngot} supplied is multiplied by 8 for blocks, serving as a discount.
     */
    private static void concoctiMelterRecipes(RecipeOutput output, ItemLike ingot, ItemLike block, int ticksPerIngot, Fluid pureResult) {
        concoctiMelterRecipe(output, ingot, ticksPerIngot,
                new FluidStack(pureResult, ConcoctiConstants.MOLTEN_INGOT),
                FluidStack.EMPTY
        );
        concoctiMelterRecipe(output, block, ticksPerIngot * 64, // discount
                new FluidStack(pureResult, ConcoctiConstants.MOLTEN_BLOCK),
                FluidStack.EMPTY
        );
    }

    /**
     * Generates a set of Concocti Melter AND Solidifier Recipes that melts & solidifies ingots and blocks of same material into fluids.
     * <p>
     * Note: the {@code ticksPerIngot} supplied is multiplied by 8 for blocks, serving as a discount.
     */
    private static void concoctiMelterSolidifierRecipes(RecipeOutput output, ItemLike ingot, ItemLike block, int meltingTicksPerIngot, int solidifyingTicksPerIngot, Fluid fluid) {
        concoctiMelterSolidifierRecipes(output, ingot, block, meltingTicksPerIngot, solidifyingTicksPerIngot, fluid, MoldItem.Type.INGOT);
    }

    /**
     * Generates a set of Concocti Melter AND Solidifier Recipes that melts & solidifies ingots and blocks of same material into fluids.
     * <p>
     * Note: the {@code ticksPerIngot} supplied is multiplied by 8 for blocks, serving as a discount.
     */
    private static void concoctiMelterSolidifierRecipes(RecipeOutput output, ItemLike ingot, ItemLike block, int meltingTicksPerIngot, int solidifyingTicksPerIngot, Fluid fluid, MoldItem.Type type) {
        concoctiMelterRecipe(output,
                ingot,
                meltingTicksPerIngot,
                new FluidStack(fluid, ConcoctiConstants.MOLTEN_INGOT),
                FluidStack.EMPTY
        );
        concoctiMelterRecipe(output,
                block,
                solidifyingTicksPerIngot * 8, // discount
                new FluidStack(fluid, ConcoctiConstants.MOLTEN_BLOCK),
                FluidStack.EMPTY
        );
        concoctiSolidifierRecipe(output, type, List.of(
                new ConcoctiMoldingSolidifierRecipe(
                        new ItemStack(ingot),
                        null,
                        new FluidStack(fluid, ConcoctiConstants.MOLTEN_INGOT),
                        solidifyingTicksPerIngot
                )
        ));
        concoctiSolidifierRecipe(output,
                null, Items.CAULDRON,
                new FluidStack(fluid, ConcoctiConstants.MOLTEN_BLOCK),
                solidifyingTicksPerIngot * 8,
                new ItemStack(block)
        );
    }

    /**
     * Generates a set of Concocti Melter AND Solidifier Recipes that melts & solidifies nuggets, ingots and blocks of same material into fluids.
     */
    private static void concoctiMelterSolidifierRecipes(RecipeOutput output, ItemLike nugget, ItemLike ingot, ItemLike block, int meltingTicksPerNugget, int solidifyingTicksPerNugget, Fluid fluid) {
        concoctiMelterRecipe(output,
                nugget,
                meltingTicksPerNugget,
                new FluidStack(fluid, ConcoctiConstants.MOLTEN_NUGGET),
                FluidStack.EMPTY
        );
        concoctiMelterRecipe(output,
                ingot,
                meltingTicksPerNugget * 8,
                new FluidStack(fluid, ConcoctiConstants.MOLTEN_INGOT),
                FluidStack.EMPTY
        );
        concoctiMelterRecipe(output,
                block,
                meltingTicksPerNugget * 64, // discount
                new FluidStack(fluid, ConcoctiConstants.MOLTEN_BLOCK),
                FluidStack.EMPTY
        );
        concoctiSolidifierRecipe(output, MoldItem.Type.NUGGET, List.of(
                new ConcoctiMoldingSolidifierRecipe(
                        new ItemStack(nugget),
                        null,
                        new FluidStack(fluid, ConcoctiConstants.MOLTEN_NUGGET),
                        solidifyingTicksPerNugget
                )
        ));
        concoctiSolidifierRecipe(output, MoldItem.Type.INGOT, List.of(
                new ConcoctiMoldingSolidifierRecipe(
                        new ItemStack(ingot),
                        null,
                        new FluidStack(fluid, ConcoctiConstants.MOLTEN_INGOT),
                        solidifyingTicksPerNugget * 8
                )
        ));
        concoctiSolidifierRecipe(output,
                null, Items.CAULDRON,
                new FluidStack(fluid, ConcoctiConstants.MOLTEN_BLOCK),
                solidifyingTicksPerNugget * 64,
                new ItemStack(block)
        );
    }

    /**
     * Generates a Concocti Solidifier Recipe that melts an item into fluids.
     */
    private static void concoctiSolidifierRecipe(RecipeOutput output, @Nullable ItemLike baseItem, ItemLike mold,
                                                 FluidStack inputFluid, int ticks, ItemStack outputItem) {
        concoctiSolidifierRecipe(
            output,
            baseItem == null ? null : ItemRecipeIngredient.of(baseItem, 1),
            ItemRecipeIngredient.of(Ingredient.of(mold), true),
            inputFluid,
            ticks,
            outputItem
        );
    }

    /**
     * Generates a Concocti Solidifier Recipe that melts an item into fluids.
     */
    private static void concoctiSolidifierRecipe(RecipeOutput output, @Nullable ItemRecipeIngredient baseItem, ItemRecipeIngredient mold,
                                                 FluidStack inputFluid, int ticks, ItemStack outputItem) {
        new ConcoctiSolidifier.Recipe.Builder(
                Optional.ofNullable(baseItem),
                mold,
                FluidRecipeIngredient.of(inputFluid),
                outputItem,
                ticks
        ).save(output, ResourceLocation.fromNamespaceAndPath(MODID, "solidifying/" + BuiltInRegistries.ITEM.getKey(outputItem.getItem()).getPath()));
    }

    /**
     * Generates a Concocti Solidifier Recipe that melts an item into fluids.
     */
    private static void concoctiSolidifierRecipe(RecipeOutput output, @Nullable ItemRecipeIngredient baseItem, ItemRecipeIngredient mold,
                                                 FluidRecipeIngredient inputFluid, int ticks, ItemStack outputItem) {
        new ConcoctiSolidifier.Recipe.Builder(
            Optional.ofNullable(baseItem),
            mold,
            inputFluid,
            outputItem,
            ticks
        ).save(output, ResourceLocation.fromNamespaceAndPath(MODID, "solidifying/" + BuiltInRegistries.ITEM.getKey(outputItem.getItem()).getPath()));
    }

    /**
     * Generates a Concocti Crystallizer Recipe that uses a seed crystal to crystallize a fluid.
     */
    private static void concoctiCrystallizerRecipe(RecipeOutput output, Ingredient seedCrystal,
                                                   FluidStack inputFluid, int ticks, ItemStack outputItem, String name) {
        new ConcoctiCrystallizer.Recipe.Builder(
                ItemRecipeIngredient.of(seedCrystal, 1, true),
                FluidRecipeIngredient.of(inputFluid),
                outputItem,
                ticks
        ).save(output, ResourceLocation.fromNamespaceAndPath(MODID, "crystallization/" + name));
    }

    /**
     * Generates a Concocti Mixer Recipe.
     */
    private static void concoctiMixerRecipe(
            RecipeOutput output,
            String name,
            int ticks,
            List<ItemRecipeIngredient> inputItems,
            List<FluidRecipeIngredient> inputFluids,
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
            List<ItemRecipeIngredient> inputItems,
            List<FluidRecipeIngredient> inputFluids,
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
            List<ItemRecipeIngredient> inputItems,
            List<FluidRecipeIngredient> inputFluids,
            List<ItemOutput> outputItems,
            List<FluidOutput> outputFluids
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
     * Generates a Concocti Solar Collector Recipe.
     */
    private static void concoctiSolarCollectorRecipe(
            RecipeOutput output,
            String name,
            int ticks,
            ItemRecipeIngredient inputItem,
            FluidRecipeIngredient inputFluid,
            ItemOutput outputItem,
            FluidOutput outputFluid,
            long solar
    ) {
        new ConcoctiSolarCollector.Recipe.Builder(
                inputItem,
                inputFluid,
                outputItem,
                outputFluid,
                solar,
                ticks
        ).save(output, ResourceLocation.fromNamespaceAndPath(MODID, "solar_collecting/" + name));
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
                FluidOutput.of(outputFluid, chance),
                ticks
        ).save(output);
    }

    private static String withModId(String name) {
        return MODID + ":" + name;
    }
}
