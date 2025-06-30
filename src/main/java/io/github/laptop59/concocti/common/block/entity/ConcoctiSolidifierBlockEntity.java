package io.github.laptop59.concocti.common.block.entity;

import io.github.laptop59.concocti.client.gui.components.MachineSettings;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.menu.ConcoctiSolidifierMenu;
import io.github.laptop59.concocti.common.recipe.ConcoctiRecipes;
import io.github.laptop59.concocti.common.recipe.ConcoctiSolidifierRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class ConcoctiSolidifierBlockEntity extends AbstractConcoctiMachineBlockEntity
        <ConcoctiSolidifierBlockEntity, ConcoctiSolidifierMenu, ConcoctiSolidifierBlockEntity.InputValue,
                ConcoctiSolidifierRecipe.Input, ConcoctiSolidifierRecipe> {

    public record InputValue(ItemStack mold, FluidStack fluid, ItemStack baseItem) {}

    private static final int MOLD_SLOT = 2;
    private static final int BASE_ITEM_SLOT = 3;
    private static final int OUTPUT_SLOT = 4;

    public static final int TANK_CAPACITY = 64000;

    public final FluidTank tank = new FluidTank(TANK_CAPACITY);

    protected final ContainerData dataAccess = new ContainerData() {

        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> ticksLeft;
                case 1 -> totalTicks;
                case 2 -> energy.getEnergyStored();
                case 3 -> energy.getMaxEnergyStored();
                case 4 -> BuiltInRegistries.FLUID.getId(tank.getFluid().getFluid());
                case 5 -> tank.getFluid().getAmount();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0: ticksLeft = value;
                case 1: totalTicks = value;
                case 2, 3, 4, 5: break;
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    };

    public ConcoctiSolidifierBlockEntity(BlockPos pos, BlockState blockState) {
        super(
                pos, blockState, 10000, 10000, 5,
                ConcoctiBlocks.CONCOCTI_SOLIDIFIER_BLOCK_ENTITY, 20.0f,
                new MachineSettings(List.of(
                        MachineSettings.SlotType.FLUID_INPUT,
                        MachineSettings.SlotType.MOLD_ITEM_INPUT,
                        MachineSettings.SlotType.BASE_ITEM_INPUT,
                        MachineSettings.SlotType.ITEM_OUTPUT
                ))
        );
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        return new int[]{0, 1, MOLD_SLOT, BASE_ITEM_SLOT, OUTPUT_SLOT};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack itemStack, @Nullable Direction direction) {
        return index == MOLD_SLOT;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
        return index == MOLD_SLOT;
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("block.concocti.concocti_solidifier");
    }

    @Override
    protected boolean isItemValidInMachine(int slot, @NotNull ItemStack stack) {
        return true;
    }

    @Override
    protected @NotNull ConcoctiSolidifierMenu createMenu(int containerId, @NotNull Inventory inventory) {
        return new ConcoctiSolidifierMenu(containerId, inventory, this, this.dataAccess);
    }

    @Override
    public Supplier<RecipeType<ConcoctiSolidifierRecipe>> getRecipeType() {
        return ConcoctiRecipes.CONCOCTI_SOLIDIFIER_RECIPE_TYPE;
    }

    @Override
    protected ConcoctiSolidifierRecipe.Input recipeInputFrom(InputValue input) {
        return new ConcoctiSolidifierRecipe.Input(input.mold, input.baseItem, input.fluid);
    }

    @Override
    protected InputValue getInput() {
        return new InputValue(getItem(MOLD_SLOT), tank.getFluid(), getItem(BASE_ITEM_SLOT));
    }

    @Override
    protected ResourceLocation getRecipeIdFrom(InputValue input) {
        return ConcoctiSolidifierRecipe.makeResourceLocation(input.baseItem, input.mold, input.fluid);
    }

    @Override
    public boolean canProcess() {
        if (!super.canProcess()) return false;
        ConcoctiSolidifierRecipe recipe = getCurrentRecipe(getInput());
        // Check whether the resulting item can be placed in the slot.
        ItemStack output = getItem(OUTPUT_SLOT);
        if (!output.isEmpty()) {
            return output.getCount() + recipe.getOutputItem().getCount() <= output.getMaxStackSize();
        }
        return true;
    }

    @Override
    protected void onRecipeCompleted(ConcoctiSolidifierRecipe recipe) {
        // Consume a base item.
        getItem(BASE_ITEM_SLOT).shrink(1);
        ItemStack mold = getItem(MOLD_SLOT);
        // Give a solidified item.
        ItemStack output = getItem(OUTPUT_SLOT);
        if (output.isEmpty()) {
            setItem(OUTPUT_SLOT, recipe.getOutputItem().copy());
        } else {
            output.setCount(output.getCount() + recipe.getOutputItem().getCount());
        }
        // Damage the mold.
        if (mold.isDamageableItem()) {
            mold.setDamageValue(mold.getDamageValue() + 1);
            if (mold.getDamageValue() >= mold.getMaxDamage()) mold.shrink(1);
        }
        // Reduce the fluids.
        tank.drain(recipe.getInputFluid().amount(), IFluidHandler.FluidAction.EXECUTE);
    }

    @Override
    public List<IFluidHandler> getIndexedFluidHandlers() {
        return List.of(tank);
    }

    @Override
    public List<IFluidHandler> getInputFluidHandlers() {
        return List.of(tank);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.tank.setFluid(FluidStack.EMPTY.copy());
        CompoundTag fluidStack = (CompoundTag) tag.get("fluid_input");
        if (fluidStack != null)
            this.tank.setFluid(FluidStack
                    .parseOptional(registries, fluidStack));
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (!tank.isEmpty())
            tag.put("fluid_input", tank.getFluid().save(registries));
    }

    @Override
    public IFluidHandler getFluidTank() {
        return tank;
    }
}
