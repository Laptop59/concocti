package io.github.laptop59.concocti.integration.jei;

import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.recipe.ConcoctiMelterRecipe;
import io.github.laptop59.concocti.common.recipe.ConcoctiRecipes;
import io.github.laptop59.concocti.common.recipe.ConcoctiSolidifierRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@JeiPlugin
public class ConcoctiJeiPlugin implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Concocti.MODID, Concocti.MODID);
    }

    public static final RecipeType<ConcoctiMelterRecipe> CONCOCTI_MELTER_TYPE = RecipeType.create(
            Concocti.MODID, "concocti_melter", ConcoctiMelterRecipe.class);
    public static final RecipeType<ConcoctiSolidifierRecipe> CONCOCTI_SOLIDIFIER_TYPE = RecipeType.create(
            Concocti.MODID, "concocti_solidifier", ConcoctiSolidifierRecipe.class);

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ConcoctiBlocks.CONCOCTI_MELTER), CONCOCTI_MELTER_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ConcoctiBlocks.CONCOCTI_SOLIDIFIER), CONCOCTI_SOLIDIFIER_TYPE);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

        registration.addRecipeCategories(new ConcoctiMelterRecipeCategory(guiHelper));
        registration.addRecipeCategories(new ConcoctiSolidifierRecipeCategory(guiHelper));
    }

    private <R extends Recipe<I>, I extends RecipeInput> void registerRecipesFor(IRecipeRegistration registration,
             RecipeManager manager, net.minecraft.world.item.crafting.RecipeType<R> type, RecipeType<R> jeiType) {
        List<R> recipes = manager
                .getAllRecipesFor(type)
                .stream()
                .map(RecipeHolder::value)
                .sorted()
                .toList();
        registration.addRecipes(jeiType, recipes);
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) return;
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        registerRecipesFor(registration, recipeManager, ConcoctiRecipes.CONCOCTI_MELTER_RECIPE_TYPE.get(), CONCOCTI_MELTER_TYPE);
        registerRecipesFor(registration, recipeManager, ConcoctiRecipes.CONCOCTI_SOLIDIFIER_RECIPE_TYPE.get(), CONCOCTI_SOLIDIFIER_TYPE);

        registerInfos(registration);
    }

    private void registerInfos(@NotNull IRecipeRegistration registration) {
        List<String> items = Arrays.asList(
                "concocti_seeds",
                "dirty_concocti_nugget"
        );

        for (String name : items) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(Concocti.MODID, name));
            Objects.requireNonNull(item);
            registration.addIngredientInfo(new ItemStack(item), VanillaTypes.ITEM_STACK, Component.translatable("info.concocti." + name));
        }
    }
}
