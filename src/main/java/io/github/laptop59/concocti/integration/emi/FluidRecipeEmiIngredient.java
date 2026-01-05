package io.github.laptop59.concocti.integration.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import io.github.laptop59.concocti.common.recipe.FluidOption;
import io.github.laptop59.concocti.common.recipe.FluidRecipeIngredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class FluidRecipeEmiIngredient {
    private FluidRecipeEmiIngredient() {}

    public static List<EmiIngredient> wrap(FluidRecipeIngredient ingredient) {
        ArrayList<EmiIngredient> all = new ArrayList<>();
        for (FluidOption option : ingredient.options()) {
            List<EmiStack> stacks = Arrays.stream(option.ingredient().getStacks())
                .map(stack -> EmiStack.of(stack.getFluid(), stack.getComponentsPatch(), option.amount()))
                .map(stack -> option.unconsumed() ? new UnconsumedStack(stack) : stack)
                .toList();
            all.add(EmiIngredient.of(stacks));
        }
        return all;
    }
}
