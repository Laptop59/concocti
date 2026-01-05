package io.github.laptop59.concocti.common.machine;

import io.github.laptop59.concocti.client.gui.AbstractConcoctiMachineScreen;
import io.github.laptop59.concocti.common.block.AbstractConcoctiMachineBlock;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineOnlyItemsFluidsBlockEntity;
import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import io.github.laptop59.concocti.common.recipe.ItemsFluidsRecipeInput;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.state.BlockBehaviour;

public abstract class ConcoctiMachineOnlyItemsFluids<
        T extends AbstractConcoctiMachineOnlyItemsFluidsBlockEntity<T, M, R>,
        M extends AbstractConcoctiMachineMenu<M>,
        R extends ProcessingRecipe<R, ItemsFluidsRecipeInput>,
        Z extends RecipeSerializer<R>,
        B extends AbstractConcoctiMachineBlock<B>,
        S extends AbstractConcoctiMachineScreen<M>,
        C extends AbstractConcoctiRecipeCategory<R>
        > extends ConcoctiMachine<T, M, ItemsFluidsInputValue, ItemsFluidsRecipeInput, R, Z, B, S, C> {
    protected ConcoctiMachineOnlyItemsFluids(String id, BlockBehaviour.Properties properties, ConcoctiBlocks.BlockData blockData) {
        super(id, properties, blockData);
    }
}
