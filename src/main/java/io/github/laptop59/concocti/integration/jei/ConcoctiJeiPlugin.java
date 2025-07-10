package io.github.laptop59.concocti.integration.jei;

import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.machine.*;
import io.github.laptop59.concocti.common.recipe.*;
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
import java.util.function.Consumer;

@JeiPlugin
public class ConcoctiJeiPlugin implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Concocti.MODID, "main");
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        registerForAllMachines(machine -> registerRecipeCatalystFor(registration, machine));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

        registerForAllMachines(machine -> registerRecipeCategoryFor(registration, guiHelper, machine));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) return;
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        registerForAllMachines(machine -> registerRecipesFor(registration, recipeManager, machine));

        registerInfos(registration);
    }

    private <R extends ProcessingRecipe<R, I>, I extends RecipeInput> void registerRecipesFor(
            IRecipeRegistration registration,
            RecipeManager manager,
            ConcoctiMachine<?, ?, ?, I, R, ?, ?, ?, ?> machine
    ) {
        registerRecipesFor(registration, manager, machine.RECIPE_TYPE.get(), machine.JEI_RECIPE_TYPE);
    }

    private <R extends ProcessingRecipe<R, I>, I extends RecipeInput> void registerRecipeCatalystFor(
            IRecipeCatalystRegistration registration,
            ConcoctiMachine<?, ?, ?, I, R, ?, ?, ?, ?> machine
    ) {
        registration.addRecipeCatalyst(new ItemStack(machine.BLOCK.get()), machine.JEI_RECIPE_TYPE);
    }

    private <C extends AbstractConcoctiRecipeCategory<R>,R extends ProcessingRecipe<R, I>, I extends RecipeInput> void registerRecipeCategoryFor(
            IRecipeCategoryRegistration registration,
            IGuiHelper guiHelper,
            ConcoctiMachine<?, ?, ?, I, R, ?, ?, ?, C> machine
    ) {
        registration.addRecipeCategories(machine.newRecipeCategory(guiHelper));
    }

    private <R extends Recipe<I>, I extends RecipeInput> void registerRecipesFor(
            IRecipeRegistration registration,
            RecipeManager manager,
            net.minecraft.world.item.crafting.RecipeType<R> type,
            RecipeType<R> jeiType
    ) {
        List<R> recipes = manager
                .getAllRecipesFor(type)
                .stream()
                .map(RecipeHolder::value)
                .sorted()
                .toList();
        registration.addRecipes(jeiType, recipes);
    }

    private void registerForAllMachines(Consumer<ConcoctiMachine<?, ?, ?, ?, ?, ?, ?, ?, ?>> machineConsumer) {
        for (var machine : ConcoctiMachines.MACHINES)
            machineConsumer.accept(machine);
    }

    private void registerInfos(@NotNull IRecipeRegistration registration) {
        List<String> items = Arrays.asList(
                "concocti_seeds",
                "dirty_concocti_nugget",
                "conductivium_lightning_rod",
                "concocti_electron_collector"
        );

        for (String name : items) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(Concocti.MODID, name));
            Objects.requireNonNull(item);
            registration.addIngredientInfo(new ItemStack(item), VanillaTypes.ITEM_STACK, Component.translatable("info.concocti." + name));
        }
    }
}
