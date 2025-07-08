package io.github.laptop59.concocti.common.block.entity;

import io.github.laptop59.concocti.client.gui.components.SlotType;
import io.github.laptop59.concocti.common.abstraction.ConcoctiMachineComplexion;
import io.github.laptop59.concocti.common.abstraction.Properties;
import io.github.laptop59.concocti.common.abstraction.Property;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.machine.ConcoctiMachineDetails;
import io.github.laptop59.concocti.common.machine.ConcoctiMachineDetailsBuilder;
import io.github.laptop59.concocti.common.menu.ConcoctiElectronCollectorMenu;
import io.github.laptop59.concocti.common.menu.ConcoctiEnergyGeneratorMenu;
import io.github.laptop59.concocti.common.menu.ConcoctiMelterMenu;
import io.github.laptop59.concocti.common.recipe.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class ConcoctiElectronCollectorBlockEntity extends AbstractConcoctiMachineBlockEntity
    <ConcoctiElectronCollectorBlockEntity, ConcoctiElectronCollectorMenu, LightningState, LightningRecipeInput, ConcoctiElectronCollectorRecipe> {

    private final FluidTank fluidOutput = new FluidTank(TANK_CAPACITY);
    private final LightningState lightningState = new LightningState(false);

    // Slots: NONE

    // Properties
    public final Property<FluidStack> FLUID_OUTPUT = Properties.FLUID_OUTPUT.newWithLinker(fluidOutput::getFluid);

    // Details
    public final static Supplier<BlockEntityType<ConcoctiElectronCollectorBlockEntity>> BLOCK_ENTITY_TYPE =
            ConcoctiBlocks.CONCOCTI_ELECTRON_COLLECTOR_BLOCK_ENTITY;

    @Override
    public List<IFluidHandler> getIndexedFluidHandlers() {
        return List.of(fluidOutput);
    }

    public Supplier<ConcoctiMachineDetails<ConcoctiElectronCollectorBlockEntity, ConcoctiElectronCollectorMenu, LightningState, LightningRecipeInput, ConcoctiElectronCollectorRecipe>> getUncachedMachineDetails() {
        return () -> ConcoctiMachineDetailsBuilder
                .<ConcoctiElectronCollectorBlockEntity, ConcoctiElectronCollectorMenu, LightningState, LightningRecipeInput, ConcoctiElectronCollectorRecipe>create()
                .withMaxEnergy(100_000)
                .withMaxEnergyTransfer(100_000)
                .withSlots(2)
                .withRateConsumption(100.0f)
                .withEnergyMode(() -> DynamicEnergyStorage.Mode.INPUT_ONLY)
                .withRecipeType(ConcoctiRecipes.CONCOCTI_ELECTRON_COLLECTOR_RECIPE_TYPE)
                .withDefaultName(Component.translatable("block.concocti.concocti_electron_collector"))
                .withAllowedSlotTypes(
                        SlotType.FLUID_OUTPUT
                )
                .withMenuClass(ConcoctiElectronCollectorMenu.class)
                .withComplexion(() -> dataAccess)
                .withItemSlotsMap(new EnumMap<>(SlotType.class))
                .withFluidSlotsMap(new EnumMap<>(
                        Map.of(
                                SlotType.FLUID_OUTPUT, List.of(() -> fluidOutput)
                        )
                ))
                .build();
    }

    @Override
    protected boolean isItemValidInMachine(int slot, @NotNull ItemStack stack) {
        return false;
    }

    protected final ConcoctiMachineComplexion dataAccess = new ConcoctiMachineComplexion(
            this,
            FLUID_OUTPUT.of(FluidStack.EMPTY)
    );

    public ConcoctiElectronCollectorBlockEntity(BlockPos pos, BlockState blockState) {
        super(
                BLOCK_ENTITY_TYPE, pos, blockState
        );
        fluidOutput.getFluid().limitSize(TANK_CAPACITY);
    }

    @Override
    public boolean canProcess() {
        if (!super.canProcess()) return false;
        ConcoctiElectronCollectorRecipe recipe = getCurrentRecipe(lightningState);
        // Check whether the fluids obtained from this item will not exceed our fluid limit.
        FluidStack result = recipe.getOutputFluid().copy();
        return fluidOutput.fill(result, IFluidHandler.FluidAction.SIMULATE) == result.getAmount();
    }

    @Override
    protected LightningRecipeInput recipeInputFrom(LightningState lightningState) {
        return new LightningRecipeInput(lightningState);
    }

    @Override
    protected LightningState getInput() {
        return lightningState;
    }

    @Override
    protected ResourceLocation getRecipeIdFrom(LightningState input) {
        return getRecipe(input).getId();
    }

    protected ConcoctiElectronCollectorRecipe getRecipe(LightningState input) {
        Optional<RecipeHolder<ConcoctiElectronCollectorRecipe>> optional = level.getRecipeManager().getRecipeFor(
                // The recipe type.
                getRecipeType().get(),
                recipeInputFrom(input),
                level
        );
        return optional.map(RecipeHolder::value).orElse(null);
    }

    @Override
    protected void onRecipeCompleted(ConcoctiElectronCollectorRecipe recipe) {
        if (Math.random() < recipe.getChance())
            fluidOutput.fill(recipe.getOutputFluid(), IFluidHandler.FluidAction.EXECUTE);
        lightningState.setLightningCollected(false);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        fluidOutput.setFluid(parseFluidStack((CompoundTag) tag.get("fluid_output"), registries));
        lightningState.setLightningCollected(tag.getBoolean("lightning_collected"));
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        saveFluidStack("fluid_output", tag, fluidOutput, registries);
        tag.putBoolean("lightning_collected", lightningState.getLightningCollected());
    }

    @Override
    public List<IFluidTank> getFluidTanks() {
        return List.of(fluidOutput);
    }

    public void markLightningState() {
        lightningState.setLightningCollected(true);
    }
}
