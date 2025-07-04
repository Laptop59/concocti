package io.github.laptop59.concocti.common.machine;

import io.github.laptop59.concocti.client.gui.components.SlotType;
import io.github.laptop59.concocti.common.abstraction.ConcoctiMachineComplexion;
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineBlockEntity;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.fluids.IFluidTank;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public record ConcoctiMachineDetails<
        T extends AbstractConcoctiMachineBlockEntity<T, M, V, I, R>,
        M extends AbstractContainerMenu,
        V,
        I extends RecipeInput,
        R extends ProcessingRecipe<R, I>
>(
    int maxEnergy,
    int maxEnergyTransfer,
    int slots,
    float rateConsumption,

    Supplier<RecipeType<R>> recipeType,

    Component defaultName,
    List<SlotType> allowedSlotTypes,
    Class<M> menuClass,

    Supplier<ConcoctiMachineComplexion> complexion,
    EnumMap<SlotType, List<Integer>> itemSlotsMap,
    EnumMap<SlotType, List<Supplier<IFluidTank>>> fluidSlotsMap
) {
    public ConcoctiMachineDetails {}
}