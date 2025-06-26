package io.github.laptop59.concocti.common.block.entity;

import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.menu.ConcoctiMelterMenu;
import io.github.laptop59.concocti.common.menu.ConcoctiUpgradeSlot;
import io.github.laptop59.concocti.common.recipe.ConcoctiMelterRecipe;
import io.github.laptop59.concocti.common.recipe.ConcoctiRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

import static io.github.laptop59.concocti.common.block.ConcoctiMelterBlock.LIT;

public class ConcoctiMelterBlockEntity extends AbstractConcoctiMachineBlockEntity
    <ConcoctiMelterBlockEntity, ConcoctiMelterMenu, ItemStack, SingleRecipeInput, ConcoctiMelterRecipe> {
    private static final int INPUT_SLOT = 2;
    public static final int TANK_CAPACITY = 8000;

    private FluidStack pureFluidOutput = FluidStack.EMPTY.copy();
    private FluidStack byproductFluidOutput = FluidStack.EMPTY.copy();

    public final IFluidHandler fluids = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 2;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return switch (tank) {
                case 0 -> pureFluidOutput;
                case 1 -> byproductFluidOutput;
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
            int amount;
            if (resource.is(pureFluidOutput.getFluidType()) && !pureFluidOutput.isEmpty()) {
                amount = Math.min(resource.getAmount(), pureFluidOutput.getAmount());
                if (action != FluidAction.SIMULATE) {
                    pureFluidOutput.setAmount(pureFluidOutput.getAmount() - amount);
                }
            } else if (resource.is(byproductFluidOutput.getFluidType()) && !byproductFluidOutput.isEmpty()) {
                amount = Math.min(resource.getAmount(), byproductFluidOutput.getAmount());
                if (action != FluidAction.SIMULATE) {
                    byproductFluidOutput.setAmount(byproductFluidOutput.getAmount() - amount);
                }
            } else {
                return FluidStack.EMPTY;
            }
            return new FluidStack(resource.getFluid(), amount);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
            if (pureFluidOutput.getAmount() > 0) {
                int amount = Math.min(maxDrain, pureFluidOutput.getAmount());
                if (action != FluidAction.SIMULATE) pureFluidOutput.setAmount(pureFluidOutput.getAmount() - amount);
                return new FluidStack(pureFluidOutput.getFluid(), amount);
            } else if (byproductFluidOutput.getAmount() > 0) {
                int amount = Math.min(maxDrain, byproductFluidOutput.getAmount());
                if (action != FluidAction.SIMULATE) byproductFluidOutput.setAmount(byproductFluidOutput.getAmount() - amount);
                return new FluidStack(byproductFluidOutput.getFluid(), amount);
            }
            return new FluidStack(Fluids.EMPTY, 0);
        }
    };

    private int recipeFill(@NotNull FluidStack resource) {
        int filled = 0;
        if (pureFluidOutput.isEmpty() || (resource.is(pureFluidOutput.getFluidType()))) {
            filled += Math.min(TANK_CAPACITY - pureFluidOutput.getAmount(), resource.getAmount());
        }
        if (byproductFluidOutput.isEmpty() || (resource.is(byproductFluidOutput.getFluidType()))) {
            filled += Math.min(TANK_CAPACITY - byproductFluidOutput.getAmount(), resource.getAmount());
        }
        return filled;
    }

    private void recipeResultFill(@NotNull FluidStack resource) {
        int filled;
        if (pureFluidOutput.isEmpty()) {
            pureFluidOutput = resource;
            return;
        } else if (pureFluidOutput.getFluid().isSame(resource.getFluid())) {
            filled = Math.min(resource.getAmount(), TANK_CAPACITY - pureFluidOutput.getAmount());
            pureFluidOutput.setAmount(pureFluidOutput.getAmount() + filled);
            resource.setAmount(resource.getAmount() - filled);
        }
        if (resource.isEmpty()) return;
        // No need to check for overflowing.
        if (byproductFluidOutput.isEmpty()) {
            byproductFluidOutput = resource;
            return;
        } else if (byproductFluidOutput.getFluid().isSame(resource.getFluid())) {
            filled = Math.min(resource.getAmount(), TANK_CAPACITY - byproductFluidOutput.getAmount());
            byproductFluidOutput.setAmount(byproductFluidOutput.getAmount() + filled);
            resource.setAmount(resource.getAmount() - filled);
        }
    }

    @Override
    protected boolean isItemValidInMachine(int slot, @NotNull ItemStack stack) {
        return slot == INPUT_SLOT;
    }

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> ticksLeft;
                case 1 -> totalTicks;
                case 2 -> energy.getEnergyStored();
                case 3 -> energy.getMaxEnergyStored();
                case 4 -> BuiltInRegistries.FLUID.getId(pureFluidOutput.getFluid());
                case 5 -> pureFluidOutput.getAmount();
                case 6 -> BuiltInRegistries.FLUID.getId(byproductFluidOutput.getFluid());
                case 7 -> byproductFluidOutput.getAmount();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> ticksLeft = value;
                case 1 -> totalTicks = value;
                case 5 -> pureFluidOutput.setAmount(value);
                case 7 -> byproductFluidOutput.setAmount(value);
            }
        }

        @Override
        public int getCount() {
            return 8;
        }
    };

    public ConcoctiMelterBlockEntity(BlockPos pos, BlockState blockState) {
        super(pos, blockState, 50000, 10000, 3, ConcoctiBlocks.CONCOCTI_MELTER_BLOCK_ENTITY, 25.0f);
        pureFluidOutput.limitSize(TANK_CAPACITY);
        byproductFluidOutput.limitSize(TANK_CAPACITY);
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

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        return new int[]{INPUT_SLOT};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack itemStack, @Nullable Direction direction) {
        return index == INPUT_SLOT;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
        return index == INPUT_SLOT;
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
        this.pureFluidOutput = FluidStack.EMPTY.copy();
        this.byproductFluidOutput = FluidStack.EMPTY.copy();

        CompoundTag fluidStack1 = (CompoundTag) tag.get("pure_fluid_output");
        CompoundTag fluidStack2 = (CompoundTag) tag.get("byproduct_fluid_output");
        if (fluidStack1 != null)
            this.pureFluidOutput = FluidStack
                    .parseOptional(registries, fluidStack1);
        if (fluidStack2 != null)
            this.byproductFluidOutput = FluidStack
                    .parseOptional(registries, (CompoundTag) tag.get("byproduct_fluid_output"));
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (!pureFluidOutput.isEmpty())
            tag.put("pure_fluid_output", pureFluidOutput.save(registries));
        if (!byproductFluidOutput.isEmpty())
            tag.put("byproduct_fluid_output", byproductFluidOutput.save(registries));
    }
}
