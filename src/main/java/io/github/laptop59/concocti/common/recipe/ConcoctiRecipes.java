package io.github.laptop59.concocti.common.recipe;

import io.github.laptop59.concocti.common.Concocti;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ConcoctiRecipes {
    /* Recipes & their types are defined in this class. */

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Concocti.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Concocti.MODID);

    // CONCOCTI MELTER

    public static final Supplier<RecipeType<ConcoctiMelterRecipe>> CONCOCTI_MELTER_RECIPE_TYPE =
            RECIPE_TYPES.register(
                    "concocti_melter",
                    // We need the qualifying generic here due to generics being generics.
                    () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(Concocti.MODID, "concocti_melter"))
            );
    public static final Supplier<RecipeSerializer<ConcoctiMelterRecipe>> CONCOCTI_MELTER_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("concocti_melter", ConcoctiMelterRecipe.Serializer::new);


    // CONCOCTI SOLIDIFIER

    public static final Supplier<RecipeType<ConcoctiSolidifierRecipe>> CONCOCTI_SOLIDIFIER_RECIPE_TYPE =
            RECIPE_TYPES.register(
                    "concocti_solidifier",
                    () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(Concocti.MODID, "concocti_solidifier"))
            );
    public static final Supplier<RecipeSerializer<ConcoctiSolidifierRecipe>> CONCOCTI_SOLIDIFIER_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("concocti_solidifier", ConcoctiSolidifierRecipe.Serializer::new);

    // CONCOCTI ENERGY GENERATOR

    public static final Supplier<RecipeType<ConcoctiEnergyGeneratorRecipe>> CONCOCTI_ENERGY_GENERATOR_RECIPE_TYPE =
            RECIPE_TYPES.register(
                    "concocti_energy_generator",
                    () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(Concocti.MODID, "concocti_solidifier"))
            );
    public static final Supplier<RecipeSerializer<ConcoctiEnergyGeneratorRecipe>> CONCOCTI_ENERGY_GENERATOR_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("concocti_energy_generator", ConcoctiEnergyGeneratorRecipe.Serializer::new);

    // CONCOCTI MIXER

    public static final Supplier<RecipeType<ConcoctiMixerRecipe>> CONCOCTI_MIXER_RECIPE_TYPE =
            RECIPE_TYPES.register(
                    "concocti_mixer",
                    () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(Concocti.MODID, "concocti_mixer"))
            );
    public static final Supplier<RecipeSerializer<ConcoctiMixerRecipe>> CONCOCTI_MIXER_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("concocti_mixer", ConcoctiMixerRecipe.Serializer::new);

    // CONCOCTI ELECTRON COLLECTOR

    public static final Supplier<RecipeType<ConcoctiElectronCollectorRecipe>> CONCOCTI_ELECTRON_COLLECTOR_RECIPE_TYPE =
            RECIPE_TYPES.register(
                    "concocti_electron_collector",
                    () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(Concocti.MODID, "concocti_electron_collector"))
            );
    public static final Supplier<RecipeSerializer<ConcoctiElectronCollectorRecipe>> CONCOCTI_ELECTRON_COLLECTOR_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("concocti_electron_collector", ConcoctiElectronCollectorRecipe.Serializer::new);
}
