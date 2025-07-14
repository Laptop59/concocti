package io.github.laptop59.concocti.integration.jei;

import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.machine.ConcoctiMachine;
import io.github.laptop59.concocti.common.machine.ConcoctiMachines;
import io.github.laptop59.concocti.common.machine.impl.ConcoctiEnergyGenerator;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
        if (machine == ConcoctiMachines.ENERGY_GENERATOR) {
            final var registry = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.ITEM);
            var datamap = registry.getDataMap(NeoForgeDataMaps.FURNACE_FUELS);
            ArrayList<ConcoctiEnergyGenerator.Recipe> proxies = new ArrayList<>();
            Set<Ingredient> unproxiedIngredients = manager
                    .getAllRecipesFor(ConcoctiMachines.ENERGY_GENERATOR.RECIPE_TYPE.get())
                    .stream()
                    .map(RecipeHolder::value)
                    .map(ConcoctiEnergyGenerator.Recipe::getInputItem)
                    .collect(Collectors.toUnmodifiableSet());
            outer:
            for (Map.Entry<ResourceKey<Item>, FurnaceFuel> entry : datamap.entrySet().stream().sorted(Comparator.comparingInt(
                    item -> BuiltInRegistries.ITEM.getId(item.getKey())
            )).toList()) {
                var item = BuiltInRegistries.ITEM.get(entry.getKey());
                for (Ingredient ingredient : unproxiedIngredients) {
                    if (ingredient.test(new ItemStack(item, 1))) continue outer;
                }
                ConcoctiEnergyGenerator.Recipe proxy = new ConcoctiEnergyGenerator.Recipe(
                        Ingredient.of(item),
                        entry.getValue().burnTime(),
                        ConcoctiEnergyGenerator.BlockEntity.DEFAULT_ENERGY_PER_TICK
                );
                proxies.add(proxy);
            }
            registration.addRecipes(ConcoctiMachines.ENERGY_GENERATOR.JEI_RECIPE_TYPE, proxies);
        }
    }

    private <R extends ProcessingRecipe<R, I>, I extends RecipeInput> void registerRecipeCatalystFor(
            IRecipeCatalystRegistration registration,
            ConcoctiMachine<?, ?, ?, I, R, ?, ?, ?, ?> machine
    ) {
        registration.addRecipeCatalyst(new ItemStack(machine.BLOCK.get()), machine.JEI_RECIPE_TYPE);
    }

    private <C extends AbstractConcoctiRecipeCategory<R>, R extends ProcessingRecipe<R, I>, I extends RecipeInput> void registerRecipeCategoryFor(
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
        for (var machine : ConcoctiMachines.MACHINES) {
            machineConsumer.accept(machine);
        }
    }

    private void registerInfos(@NotNull IRecipeRegistration registration) {
        List<String> items = Arrays.asList(
                "concocti_seeds",
                "dirty_concocti_nugget",
                "dirty_concocti_ingot",
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
