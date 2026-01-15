package io.github.laptop59.concocti.common.block.entity;

import io.github.laptop59.concocti.common.detail.DetailCodec;
import io.github.laptop59.concocti.common.detail.DetailHolder;
import io.github.laptop59.concocti.common.machine.ConcoctiMachineDetails;
import io.github.laptop59.concocti.common.machine.ItemsFluidsSolarInputValue;
import io.github.laptop59.concocti.common.machine.SolarStorage;
import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import io.github.laptop59.concocti.common.recipe.ItemsFluidsSolarRecipeInput;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import io.github.laptop59.concocti.common.recipe.SolarState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.function.Supplier;

public abstract class AbstractConcoctiMachineOnlyItemsFluidsSolarBlockEntity<
        T extends AbstractConcoctiMachineBlockEntity<T, M, ItemsFluidsSolarInputValue, ItemsFluidsSolarRecipeInput, R>,
        M extends AbstractConcoctiMachineMenu<M>,
        R extends ProcessingRecipe<R, ItemsFluidsSolarRecipeInput>
        > extends AbstractConcoctiMachineBlockEntity<T, M, ItemsFluidsSolarInputValue, ItemsFluidsSolarRecipeInput, R> {

    protected final DetailHolder<SolarState> solar = new DetailHolder<>(
            DetailCodec.SOLAR_STATE, "solar", new SolarState(0L, 100_000L), this
    );

    public AbstractConcoctiMachineOnlyItemsFluidsSolarBlockEntity(Supplier<BlockEntityType<T>> blockEntityType, BlockPos pos, BlockState blockState, Object... extraData) {
        super(blockEntityType, pos, blockState, extraData);
    }

    @Override
    protected ItemsFluidsSolarInputValue getInput() {
        return new ItemsFluidsSolarInputValue(inputItemHandler.get(), inputFluidHandler.get(), solar.get());
    }

    @Override
    protected ItemsFluidsSolarRecipeInput recipeInputFrom(ItemsFluidsSolarInputValue input) {
        return new ItemsFluidsSolarRecipeInput(input.getItemHandler(), input.getFluidHandler(), input.solarStorage());
    }

    @Override
    public Supplier<ConcoctiMachineDetails<T, M, ItemsFluidsSolarInputValue, ItemsFluidsSolarRecipeInput, R>> getUncachedMachineDetails() {
        return getMachineInstance().getDetails();
    }
}
