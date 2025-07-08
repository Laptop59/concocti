package io.github.laptop59.concocti.common.block.entity;

import io.github.laptop59.concocti.client.gui.components.SlotType;
import io.github.laptop59.concocti.common.abstraction.ConcoctiMachineComplexion;
import io.github.laptop59.concocti.common.abstraction.Properties;
import io.github.laptop59.concocti.common.abstraction.Property;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.machine.ConcoctiMachineDetails;
import io.github.laptop59.concocti.common.machine.ConcoctiMachineDetailsBuilder;
import io.github.laptop59.concocti.common.menu.ConcoctiMelterMenu;
import io.github.laptop59.concocti.common.recipe.ConcoctiMelterRecipe;
import io.github.laptop59.concocti.common.recipe.ConcoctiRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
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
import java.util.function.Supplier;

public class ConcoctiMelterBlockEntity extends AbstractConcoctiMachineBlockEntity
    <ConcoctiMelterBlockEntity, ConcoctiMelterMenu, ItemStack, SingleRecipeInput, ConcoctiMelterRecipe> {

    private final FluidTank pureFluidOutput = new FluidTank(TANK_CAPACITY);
    private final FluidTank byproductFluidOutput = new FluidTank(TANK_CAPACITY);

    // Slots
    private static final int INPUT_SLOT = 2;

    // Properties
    public final Property<FluidStack> PURE_FLUID_OUTPUT = Properties.PURE_FLUID_OUTPUT.newWithLinker(pureFluidOutput::getFluid);
    public final Property<FluidStack> BYPRODUCT_FLUID_OUTPUT = Properties.BYPRODUCT_FLUID_OUTPUT.newWithLinker(byproductFluidOutput::getFluid);

    // Details
    public final static Supplier<BlockEntityType<ConcoctiMelterBlockEntity>> BLOCK_ENTITY_TYPE =
            ConcoctiBlocks.CONCOCTI_MELTER_BLOCK_ENTITY;

    @Override
    public List<IFluidHandler> getIndexedFluidHandlers() {
        return List.of(pureFluidOutput, byproductFluidOutput);
    }

    public Supplier<ConcoctiMachineDetails<ConcoctiMelterBlockEntity, ConcoctiMelterMenu, ItemStack, SingleRecipeInput, ConcoctiMelterRecipe>> getUncachedMachineDetails() {
        return () -> ConcoctiMachineDetailsBuilder
                .<ConcoctiMelterBlockEntity, ConcoctiMelterMenu, ItemStack, SingleRecipeInput, ConcoctiMelterRecipe>create()
                .withMaxEnergy(10_000)
                .withMaxEnergyTransfer(10_000)
                .withSlots(3)
                .withRateConsumption(25.0f)
                .withEnergyMode(() -> DynamicEnergyStorage.Mode.INPUT_ONLY)
                .withRecipeType(ConcoctiRecipes.CONCOCTI_MELTER_RECIPE_TYPE)
                .withDefaultName(Component.translatable("block.concocti.concocti_melter"))
                .withAllowedSlotTypes(
                        SlotType.ITEM_INPUT,
                        SlotType.PURE_FLUID_OUTPUT,
                        SlotType.BYPRODUCT_FLUID_OUTPUT,
                        SlotType.BOTH_FLUIDS_OUTPUT
                )
                .withMenuClass(ConcoctiMelterMenu.class)
                .withComplexion(() -> dataAccess)
                .withItemSlotsMap(new EnumMap<>(
                        Map.of(
                                SlotType.ITEM_INPUT, List.of(INPUT_SLOT)
                        )
                ))
                .withFluidSlotsMap(new EnumMap<>(
                        Map.of(
                                SlotType.PURE_FLUID_OUTPUT, List.of(() -> pureFluidOutput),
                                SlotType.BYPRODUCT_FLUID_OUTPUT, List.of(() -> byproductFluidOutput),
                                SlotType.BOTH_FLUIDS_OUTPUT, List.of(() -> pureFluidOutput, () -> byproductFluidOutput)
                        )
                ))
                .build();
    }

    private int recipeFill(@NotNull FluidStack resource) {
        int filled = 0;
        if (pureFluidOutput.isEmpty() || resource.is(pureFluidOutput.getFluid().getFluid())) {
            filled += pureFluidOutput.fill(resource, IFluidHandler.FluidAction.SIMULATE);
        }
        if (byproductFluidOutput.isEmpty() || (resource.is(byproductFluidOutput.getFluid().getFluid()))) {
            filled += byproductFluidOutput.fill(resource, IFluidHandler.FluidAction.SIMULATE);
        }
        return filled;
    }

    private void recipeResultFillSingle(boolean isPureOutput, @NotNull FluidStack resource) {
        int filled;
        FluidTank output = isPureOutput ? pureFluidOutput : byproductFluidOutput;
        if (output.isEmpty() || output.getFluid().getFluid().isSame(resource.getFluid())) {
            // Set the fluid to be the resource's fluid.
            int amount = output.isEmpty() ? 0 : output.getFluidAmount();
            FluidStack copy = resource.copy();
            if (isPureOutput)
                pureFluidOutput.setFluid(copy);
            else
                byproductFluidOutput.setFluid(copy);
            filled = Math.min(resource.getAmount(), TANK_CAPACITY - amount);
            copy.setAmount(amount + filled);
            resource.setAmount(resource.getAmount() - filled);
        }
    }

    private void recipeResultFill(@NotNull FluidStack resource) {
        recipeResultFillSingle(true, resource);
        if (resource.isEmpty()) return;
        // No need to check for overflowing.
        recipeResultFillSingle(false, resource);
    }

    @Override
    protected boolean isItemValidInMachine(int slot, @NotNull ItemStack stack) {
        return slot == INPUT_SLOT;
    }

    protected final ConcoctiMachineComplexion dataAccess = new ConcoctiMachineComplexion(
            this,
            PURE_FLUID_OUTPUT.of(FluidStack.EMPTY),
            BYPRODUCT_FLUID_OUTPUT.of(FluidStack.EMPTY)
    );

    public ConcoctiMelterBlockEntity(BlockPos pos, BlockState blockState) {
        super(
                BLOCK_ENTITY_TYPE, pos, blockState
        );
        pureFluidOutput.getFluid().limitSize(TANK_CAPACITY);
        byproductFluidOutput.getFluid().limitSize(TANK_CAPACITY);
    }

    public ItemStack getInputStack() {
        return this.getItem(INPUT_SLOT);
    }

    @Override
    public boolean canProcess() {
        if (!super.canProcess()) return false;
        ConcoctiMelterRecipe recipe = getCurrentRecipe(getInput());
        // Check whether the fluids obtained from this item will not exceed our fluid limit.
        FluidStack resultPureFluid = recipe.getOutputPureFluid().copy();
        FluidStack resultByproductFluid = recipe.getOutputByproductFluid().copy();
        if (resultPureFluid.getAmount() > recipeFill(resultPureFluid))
            return false;
        if (!resultByproductFluid.isEmpty() &&
                resultByproductFluid.getAmount() > recipeFill(resultByproductFluid))
            return false;
        return true;
    }

    @Override
    protected SingleRecipeInput recipeInputFrom(ItemStack input) {
        return new SingleRecipeInput(input);
    }

    @Override
    protected ItemStack getInput() {
        return getInputStack();
    }

    @Override
    protected ResourceLocation getRecipeIdFrom(ItemStack input) {
        return BuiltInRegistries.ITEM.getKey(input.getItem());
    }

    @Override
    protected void onRecipeCompleted(ConcoctiMelterRecipe recipe) {
        getInputStack().shrink(1);
        recipeResultFill(recipe.getOutputPureFluid().copy());
        recipeResultFill(recipe.getOutputByproductFluid().copy());
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        pureFluidOutput.setFluid(parseFluidStack((CompoundTag) tag.get("pure_fluid_output"), registries));
        byproductFluidOutput.setFluid(parseFluidStack((CompoundTag) tag.get("byproduct_fluid_output"), registries));
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        saveFluidStack("pure_fluid_output", tag, pureFluidOutput, registries);
        saveFluidStack("byproduct_fluid_output", tag, byproductFluidOutput, registries);
    }

    @Override
    public List<IFluidTank> getFluidTanks() {
        return List.of(
                pureFluidOutput, byproductFluidOutput
        );
    }
}
