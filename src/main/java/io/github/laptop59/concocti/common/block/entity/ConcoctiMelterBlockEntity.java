package io.github.laptop59.concocti.common.block.entity;

import io.github.laptop59.concocti.client.gui.components.MachineSettingsSlots;
import io.github.laptop59.concocti.client.gui.components.SlotType;
import io.github.laptop59.concocti.common.abstraction.Complexion;
import io.github.laptop59.concocti.common.abstraction.Properties;
import io.github.laptop59.concocti.common.abstraction.Property;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.menu.ConcoctiMelterMenu;
import io.github.laptop59.concocti.common.recipe.ConcoctiMelterRecipe;
import io.github.laptop59.concocti.common.recipe.ConcoctiRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class ConcoctiMelterBlockEntity extends AbstractConcoctiMachineBlockEntity
    <ConcoctiMelterBlockEntity, ConcoctiMelterMenu, ItemStack, SingleRecipeInput, ConcoctiMelterRecipe> {
    private static final int INPUT_SLOT = 2;
    public static final int TANK_CAPACITY = 64000;
    private final FluidTank pureFluidOutput = new FluidTank(TANK_CAPACITY);
    private final FluidTank byproductFluidOutput = new FluidTank(TANK_CAPACITY);

    // Properties

    public final Property<FluidStack> PURE_FLUID_OUTPUT = Properties.PURE_FLUID_OUTPUT.newWithLinker(pureFluidOutput::getFluid);
    public final Property<FluidStack> BYPRODUCT_FLUID_OUTPUT = Properties.BYPRODUCT_FLUID_OUTPUT.newWithLinker(byproductFluidOutput::getFluid);

    @Override
    public List<IFluidHandler> getIndexedFluidHandlers() {
        return List.of(pureFluidOutput, byproductFluidOutput);
    }

    @Override
    public List<IFluidHandler> getOutputFluidHandlers() {
        return List.of(pureFluidOutput, byproductFluidOutput);
    }

    public final IFluidHandler fluids = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 2;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return switch (tank) {
                case 0 -> pureFluidOutput.getFluidInTank(0);
                case 1 -> byproductFluidOutput.getFluidInTank(0);
                case 2 -> throw new IllegalArgumentException("Expected tank to be 0 or 1, got " + tank + " instead");
                default -> FluidStack.EMPTY;
            };
        }

        @Override
        public int getTankCapacity(int tank) {
            return TANK_CAPACITY;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return true;
        }

        @Override
        public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) { return 0; }

        @Override
        public @NotNull FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) {
            if (!pureFluidOutput.isEmpty() && resource.is(pureFluidOutput.getFluid().getFluid())) {
                return pureFluidOutput.drain(resource, action);
            } else if (!byproductFluidOutput.isEmpty() && resource.is(byproductFluidOutput.getFluid().getFluid())) {
                return byproductFluidOutput.drain(resource, action);
            } else {
                return FluidStack.EMPTY;
            }
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
            if (!pureFluidOutput.isEmpty()) {
                return pureFluidOutput.drain(maxDrain, action);
            } else if (!byproductFluidOutput.isEmpty()) {
                return byproductFluidOutput.drain(maxDrain, action);
            }
            return new FluidStack(Fluids.EMPTY, 0);
        }
    };

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

    protected final Complexion dataAccess = new Complexion(
            TICKS_LEFT.of(0),
            TOTAL_TICKS.of(0),
            ENERGY_STORED.of(0),
            MAX_ENERGY_STORED.of(0),
            FACING_DIRECTION.of(Direction.DOWN),
            MACHINE_SETTINGS_SLOTS.of(new MachineSettingsSlots()),
            PURE_FLUID_OUTPUT.of(FluidStack.EMPTY),
            BYPRODUCT_FLUID_OUTPUT.of(FluidStack.EMPTY)
    );

    public ConcoctiMelterBlockEntity(BlockPos pos, BlockState blockState) {
        super(
                pos, blockState, 10000, 10000, 3,
                ConcoctiBlocks.CONCOCTI_MELTER_BLOCK_ENTITY, 25.0f
        );
        pureFluidOutput.getFluid().limitSize(TANK_CAPACITY);
        byproductFluidOutput.getFluid().limitSize(TANK_CAPACITY);
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("block.concocti.concocti_melter");
    }

    @Override
    protected @NotNull ConcoctiMelterMenu createMenu(int containerId, @NotNull Inventory inventory) {
        return new ConcoctiMelterMenu(containerId, inventory, this, this.dataAccess);
    }

    @Override
    public Supplier<RecipeType<ConcoctiMelterRecipe>> getRecipeType() {
        return ConcoctiRecipes.CONCOCTI_MELTER_RECIPE_TYPE;
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
        FluidStack pure = FluidStack.EMPTY.copy();
        FluidStack byproduct = FluidStack.EMPTY.copy();

        CompoundTag fluidStack1 = (CompoundTag) tag.get("pure_fluid_output");
        CompoundTag fluidStack2 = (CompoundTag) tag.get("byproduct_fluid_output");
        if (fluidStack1 != null)
            pure = FluidStack
                    .parseOptional(registries, fluidStack1);
        if (fluidStack2 != null)
            byproduct = FluidStack
                    .parseOptional(registries, (CompoundTag) tag.get("byproduct_fluid_output"));
        pureFluidOutput.setFluid(pure);
        byproductFluidOutput.setFluid(byproduct);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (!pureFluidOutput.isEmpty())
            tag.put("pure_fluid_output", pureFluidOutput.getFluid().save(registries));
        if (!byproductFluidOutput.isEmpty())
            tag.put("byproduct_fluid_output", byproductFluidOutput.getFluid().save(registries));
    }

    public IFluidHandler getFluidTank() {
        return fluids;
    }

    @Override
    @Contract(pure = true)
    protected @NotNull List<SlotType> getAllowedSlotTypes() {
        return List.of(
                SlotType.ITEM_INPUT,
                SlotType.PURE_FLUID_OUTPUT,
                SlotType.BYPRODUCT_FLUID_OUTPUT,
                SlotType.BOTH_FLUIDS_OUTPUT
        );
    }

    public List<Integer> getItemSlots(SlotType type) {
        switch (type) {
            case ITEM_INPUT -> {
                return List.of(INPUT_SLOT);
            }
        }
        return List.of();
    }

    public List<IFluidTank> getFluidTanks(SlotType type) {
        switch (type) {
            case PURE_FLUID_OUTPUT -> {
                return List.of(pureFluidOutput);
            }
            case BYPRODUCT_FLUID_OUTPUT -> {
                return List.of(byproductFluidOutput);
            }
            case BOTH_FLUIDS_OUTPUT -> {
                return List.of(pureFluidOutput, byproductFluidOutput);
            }
        }
        return List.of();
    }
}
