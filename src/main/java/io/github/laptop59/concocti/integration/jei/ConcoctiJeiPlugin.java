package io.github.laptop59.concocti.integration.jei;

import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.recipe.ConcoctiMelterRecipe;
import io.github.laptop59.concocti.common.recipe.ConcoctiRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public class ConcoctiJeiPlugin implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Concocti.MODID, Concocti.MODID);
    }

    public static final RecipeType<ConcoctiMelterRecipe> CONCOCTI_MELTER_TYPE = RecipeType.create(
            Concocti.MODID, "concocti_melter", ConcoctiMelterRecipe.class
    );

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ConcoctiBlocks.CONCOCTI_MELTER.get()), CONCOCTI_MELTER_TYPE);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

        registration.addRecipeCategories(new ConcoctiMelterRecipeCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        List<ConcoctiMelterRecipe> recipes = recipeManager
                .getAllRecipesFor(ConcoctiRecipes.CONCOCTI_MELTER_RECIPE_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .sorted()
                .toList();
        registration.addRecipes(CONCOCTI_MELTER_TYPE, recipes);
    }
}
