package io.github.laptop59.concocti.integration.jei;

import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.item.ConcoctiItemsInfo;
import io.github.laptop59.concocti.common.machine.AbstractConcoctiRecipeCategory;
import io.github.laptop59.concocti.common.machine.ConcoctiMachine;
import io.github.laptop59.concocti.common.machine.ConcoctiMachines;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
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
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@JeiPlugin
public class ConcoctiJeiPlugin implements IModPlugin {
    public static IJeiHelpers JEI_HELPERS;

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Concocti.MODID, "jei");
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        registerForAllMachines(machine -> registerRecipeCatalystFor(registration, machine));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        JEI_HELPERS = jeiHelpers;
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

    @SuppressWarnings("unchecked")
    private <R extends ProcessingRecipe<R, I>, I extends RecipeInput> mezz.jei.api.recipe.RecipeType<R> getJeiRecipeType(ConcoctiMachine<?, ?, ?, I, R, ?, ?, ?, ?> machine) {
        return (mezz.jei.api.recipe.RecipeType<R>) machine.JEI_RECIPE_TYPE.get();
    }

    private <R extends ProcessingRecipe<R, I>, I extends RecipeInput> void registerRecipesFor(
            IRecipeRegistration registration,
            RecipeManager manager,
            ConcoctiMachine<?, ?, ?, I, R, ?, ?, ?, ?> machine
    ) {
        registerRecipesFor(registration, manager, machine.RECIPE_TYPE.get(), getJeiRecipeType(machine));
        registration.addRecipes(
                getJeiRecipeType(machine),
                machine.getRecipeProxies().stream().map(RecipeHolder::value).toList()
        );
    }

    public static <R extends ProcessingRecipe<R, I>, I extends RecipeInput> List<R> getJeiRecipes(
            ConcoctiMachine<?, ?, ?, I, R, ?, ?, ?, ?> machine
    ) {
        RecipeManager manager = Minecraft.getInstance().level.getRecipeManager();
        net.minecraft.world.item.crafting.RecipeType<R> recipeType = machine.getDetails().get().recipeType().get();
        List<R> recipes = manager
                .getAllRecipesFor(recipeType)
                .stream()
                .map(RecipeHolder::value)
                .collect(Collectors.toCollection(ArrayList::new));
        for (RecipeHolder<R> recipeHolder : machine.getRecipeProxies()) {
            recipes.add(recipeHolder.value());
        }
        return recipes;
    }

    private <R extends ProcessingRecipe<R, I>, I extends RecipeInput> void registerRecipeCatalystFor(
            IRecipeCatalystRegistration registration,
            ConcoctiMachine<?, ?, ?, I, R, ?, ?, ?, ?> machine
    ) {
        registration.addRecipeCatalyst(new ItemStack(machine.BLOCK.get()), getJeiRecipeType(machine));
    }

    private <C extends AbstractConcoctiRecipeCategory<R>, R extends ProcessingRecipe<R, I>, I extends RecipeInput> void registerRecipeCategoryFor(
            IRecipeCategoryRegistration registration,
            IGuiHelper guiHelper,
            ConcoctiMachine<?, ?, ?, I, R, ?, ?, ?, C> machine
    ) {
        var category = machine.newRecipeCategory();
        JeiRecipeCategory<R, I> jeiRecipeCategory = new JeiRecipeCategory<>(category, guiHelper, new ItemStack(machine.ITEM.get(), 1), machine);
        registration.addRecipeCategories(jeiRecipeCategory);
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
                .toList();
        registration.addRecipes(jeiType, recipes);
    }

    private void registerForAllMachines(Consumer<ConcoctiMachine<?, ?, ?, ?, ?, ?, ?, ?, ?>> machineConsumer) {
        for (var machine : ConcoctiMachines.MACHINES.values()) {
            machineConsumer.accept(machine);
        }
    }

    private void registerInfos(@NotNull IRecipeRegistration registration) {
        for (DeferredItem<? extends Item> deferredItem : ConcoctiItemsInfo.ITEMS_WITH_INGREDIENT_INFO) {
            Item item = deferredItem.get();
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            Objects.requireNonNull(itemId);
            registration.addIngredientInfo(new ItemStack(item), VanillaTypes.ITEM_STACK, Component.translatable("info.concocti." + itemId));
        }
    }
}
