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

    // CONCOCTI MELTER
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Concocti.MODID);

    public static final Supplier<RecipeType<ConcoctiMelterRecipe>> CONCOCTI_MELTER_RECIPE_TYPE =
            RECIPE_TYPES.register(
                    "concocti_melter",
                    // We need the qualifying generic here due to generics being generics.
                    () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(Concocti.MODID, "concocti_melter"))
            );
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Concocti.MODID);
    public static final Supplier<RecipeSerializer<ConcoctiMelterRecipe>> CONCOCTI_MELTER_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("concocti_melter", ConcoctiMelterRecipe.Serializer::new);
}
