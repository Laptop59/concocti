package io.github.laptop59.concocti.common.block.entity;

import io.github.laptop59.concocti.common.machine.ConcoctiMachineDetails;
import io.github.laptop59.concocti.common.machine.ItemsFluidsInputValue;
import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import io.github.laptop59.concocti.common.recipe.ItemsFluidsRecipeInput;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public abstract class AbstractConcoctiMachineOnlyItemsFluidsBlockEntity<
        T extends AbstractConcoctiMachineBlockEntity<T, M, ItemsFluidsInputValue, ItemsFluidsRecipeInput, R>,
        M extends AbstractConcoctiMachineMenu<M>,
        R extends ProcessingRecipe<R, ItemsFluidsRecipeInput>
        > extends AbstractConcoctiMachineBlockEntity<T, M, ItemsFluidsInputValue, ItemsFluidsRecipeInput, R> {
    public AbstractConcoctiMachineOnlyItemsFluidsBlockEntity(Supplier<BlockEntityType<T>> blockEntityType, BlockPos pos, BlockState blockState, Object... extraData) {
        super(blockEntityType, pos, blockState, extraData);
    }

    @Override
    protected ItemsFluidsInputValue getInput() {
        return new ItemsFluidsInputValue(inputItemHandler.get(), inputFluidHandler.get());
    }

    @Override
    protected ItemsFluidsRecipeInput recipeInputFrom(ItemsFluidsInputValue input) {
        return new ItemsFluidsRecipeInput(input.getItemHandler(), input.getFluidHandler());
    }

    @Override
    public Supplier<ConcoctiMachineDetails<T, M, ItemsFluidsInputValue, ItemsFluidsRecipeInput, R>> getUncachedMachineDetails() {
        return getMachineInstance().getDetails();
    }
}
