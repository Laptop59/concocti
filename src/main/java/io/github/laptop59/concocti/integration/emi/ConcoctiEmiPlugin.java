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
import io.github.laptop59.concocti.common.machine.AbstractConcoctiRecipeCategory;
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
                // No indices: indices only exist when there can be multiple different inputs
                // (as given in the ConcoctiEmiRecipe construction documentation) in one recipe.
                // Here, an ingredient does not count as such.
                registry.addRecipe(new ConcoctiEmiRecipe<>(category, recipe, ConcoctiMachines.ENERGY_GENERATOR.newRecipeCategory(), new int[0]));
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

        recipes.forEach(recipe -> {
            AbstractConcoctiRecipeCategory<R> recipeCategory = machine.newRecipeCategory();
            ConcoctiEmiRecipe<R> templateRecipe = new ConcoctiEmiRecipe<>(category, recipe, recipeCategory, new int[0]);
            EmiRecipeBuilder builder = new EmiRecipeBuilder(null, null, null, null, templateRecipe);
            recipeCategory.set(builder, recipe);
            // This will give us the dimension of indices and the number of possibilities to consider:
            // Number of recipes to make = n1 * n2 * ... * nN
            Integer[] numberOfWays = builder.numberOfIndices.toArray(new Integer[0]);
            // Loop through all the possible ways
            int length = numberOfWays.length;
            int[] way = new int[length];
            outer:
            while (true) {
                registry.addRecipe(new ConcoctiEmiRecipe<>(category, recipe, machine.newRecipeCategory(), way.clone()));

                int i = length - 1;
                if (i < 0) break;
                while (++way[i] >= numberOfWays[i]) {
                    way[i] = 0;
                    if (--i < 0) break outer;
                }
            }
        });
    }
}
