package io.github.laptop59.concocti.common.machine;

import io.github.laptop59.concocti.client.gui.components.SlotType;
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineBlockEntity;
import io.github.laptop59.concocti.common.block.entity.DynamicEnergyStorage;
import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import io.github.laptop59.concocti.common.menu.MenuServerConstructor;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.IFluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public record ConcoctiMachineDetails<
        T extends AbstractConcoctiMachineBlockEntity<T, M, V, I, R>,
        M extends AbstractConcoctiMachineMenu<M>,
        V,
        I extends RecipeInput,
        R extends ProcessingRecipe<R, I>
        >(
        Class<T> blockEntityClass,

        int maxEnergy,
        int maxEnergyTransfer,
        int slots,
        float rateConsumption,
        Supplier<DynamicEnergyStorage.Mode> energyMode,

        Supplier<RecipeType<R>> recipeType,

        Component defaultName,
        List<SlotType> allowedSlotTypes,
        MenuServerConstructor<M> menuServerConstructor,

        EnumMap<SlotType, List<Integer>> itemSlotsMap,
        EnumMap<SlotType, List<Function<T, IFluidTank>>> fluidSlotsMap,

        InputOutput<Function<T, List<Integer>>> inputOutputSlots,
        InputOutput<Function<T, List<IFluidTank>>> inputOutputFluids,

        @Nullable SoundEvent cracklingSoundEvent
) {
}