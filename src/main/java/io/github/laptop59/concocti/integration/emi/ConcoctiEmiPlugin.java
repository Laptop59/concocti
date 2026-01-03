package io.github.laptop59.concocti.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import io.github.laptop59.concocti.common.machine.ConcoctiMachine;
import io.github.laptop59.concocti.common.machine.ConcoctiMachines;
import io.github.laptop59.concocti.common.machine.impl.ConcoctiEnergyGenerator;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

@EmiEntrypoint
public class ConcoctiEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        for (var machine : ConcoctiMachines.MACHINES) {
            registerMachine(registry, machine);
        }
    }

    private void registerMachine(EmiRegistry registry, ConcoctiMachine<?,?,?,?,?,?,?,?,?> machine) {
        // First, we need to create the category.
        EmiStack workstation = EmiStack.of(machine.ITEM);
        EmiRecipeCategory category = new EmiRecipeCategory(
            machine.ITEM.getId(),
            workstation
        );

        registry.addCategory(category);
        registry.addWorkstation(category, workstation);

        // Create actual recipes.
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        registerRecipesFor(registry, recipeManager, machine, category);

        if (machine == ConcoctiMachines.ENERGY_GENERATOR) {
            for (ConcoctiEnergyGenerator.Recipe recipe : ConcoctiEnergyGenerator.getRecipeProxies()) {
                registry.addRecipe(new ConcoctiEmiRecipe<>(category, recipe, ConcoctiMachines.ENERGY_GENERATOR.newRecipeCategory(null)));
            }
        }
    }

    private <R extends ProcessingRecipe<R, I>, I extends RecipeInput> void registerRecipesFor(EmiRegistry registry, RecipeManager recipeManager, ConcoctiMachine<?,?,?,I,R,?,?,?,?> machine, EmiRecipeCategory category) {
        List<R> recipes = recipeManager
            .getAllRecipesFor(machine.RECIPE_TYPE.get())
            .stream()
            .map(RecipeHolder::value)
            .sorted()
            .toList();
        recipes.forEach(recipe -> registry.addRecipe(new ConcoctiEmiRecipe<>(category, recipe, machine.newRecipeCategory(null))));
    }
}
