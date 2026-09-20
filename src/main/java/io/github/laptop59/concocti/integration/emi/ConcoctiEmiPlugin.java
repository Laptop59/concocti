package io.github.laptop59.concocti.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.item.ConcoctiItemsInfo;
import io.github.laptop59.concocti.common.machine.ConcoctiMachine;
import io.github.laptop59.concocti.common.machine.ConcoctiMachines;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import io.github.laptop59.concocti.common.machine.AbstractConcoctiRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;
import java.util.Objects;

@EmiEntrypoint
public class ConcoctiEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        for (var machine : ConcoctiMachines.MACHINES.values()) {
            registerMachine(registry, machine);
        }
        registerInfos(registry);
    }

    private <R extends ProcessingRecipe<R, I>, I extends RecipeInput> void registerMachine(EmiRegistry registry, ConcoctiMachine<?,?,?,I,R,?,?,?,?> machine) {
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

        for (RecipeHolder<R> recipe : machine.getRecipeProxies()) {
            // No indices: indices only exist when there can be multiple different inputs
            // (as given in the ConcoctiEmiRecipe construction documentation) in one recipe.
            // Here, an ingredient does not count as such.
            registry.addRecipe(new ConcoctiEmiRecipe<>(category, recipe, machine.newRecipeCategory(), new int[0]));
        }
    }

    private <R extends ProcessingRecipe<R, I>, I extends RecipeInput> void registerRecipesFor(EmiRegistry registry, RecipeManager recipeManager, ConcoctiMachine<?,?,?,I,R,?,?,?,?> machine, EmiRecipeCategory category) {
        List<RecipeHolder<R>> recipes = recipeManager
            .getAllRecipesFor(machine.RECIPE_TYPE.get())
            .stream()
            .toList();

        recipes.forEach(recipe -> {
            AbstractConcoctiRecipeCategory<R> recipeCategory = machine.newRecipeCategory();
            ConcoctiEmiRecipe<R> templateRecipe = new ConcoctiEmiRecipe<>(category, recipe, recipeCategory, new int[0]);
            EmiRecipeBuilder builder = new EmiRecipeBuilder(null, null, null, null, templateRecipe);
            recipeCategory.set(builder, recipe.value());
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

    private void registerInfos(EmiRegistry registry) {
        for (DeferredItem<? extends Item> deferredItem : ConcoctiItemsInfo.ITEMS_WITH_INGREDIENT_INFO) {
            Item item = deferredItem.get();
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            Objects.requireNonNull(itemId);
            registry.addRecipe(new EmiInfoRecipe(
                List.of(EmiIngredient.of(Ingredient.of(item))),
                List.of(Component.translatable("info.concocti." + itemId.getPath())),
                itemId.withPrefix("/info/")
            ));
        }
    }
}
