package io.github.laptop59.concocti.common.block.entity;

import io.github.laptop59.concocti.client.gui.components.MachineSettingsSlots;
import io.github.laptop59.concocti.client.gui.components.SlotType;
import io.github.laptop59.concocti.common.abstraction.Complexion;
import io.github.laptop59.concocti.common.abstraction.Properties;
import io.github.laptop59.concocti.common.abstraction.Property;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.menu.ConcoctiSolidifierMenu;
import io.github.laptop59.concocti.common.recipe.ConcoctiRecipes;
import io.github.laptop59.concocti.common.recipe.ConcoctiSolidifierRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class ConcoctiSolidifierBlockEntity extends AbstractConcoctiMachineBlockEntity
        <ConcoctiSolidifierBlockEntity, ConcoctiSolidifierMenu, ConcoctiSolidifierBlockEntity.InputValue,
                ConcoctiSolidifierRecipe.Input, ConcoctiSolidifierRecipe>
    implements FluidHandlerBlockEntity {

    public record InputValue(ItemStack mold, FluidStack fluid, ItemStack baseItem) {}

    private static final int MOLD_SLOT = 2;
    private static final int BASE_ITEM_SLOT = 3;
    private static final int OUTPUT_SLOT = 4;

    public static final int TANK_CAPACITY = 64000;

    public final FluidTank tank = new FluidTank(TANK_CAPACITY);

    public final Property<FluidStack> FLUID_INPUT = Properties.FLUID_INPUT.newWithLinker(tank::getFluid);

    protected final ContainerData dataAccess = new Complexion(
            TICKS_LEFT.of(0),
            TOTAL_TICKS.of(0),
            ENERGY_STORED.of(0),
            MAX_ENERGY_STORED.of(0),
            FACING_DIRECTION.of(Direction.DOWN),
            MACHINE_SETTINGS_SLOTS.of(new MachineSettingsSlots()),
            FLUID_INPUT.of(FluidStack.EMPTY)
    );

    public ConcoctiSolidifierBlockEntity(BlockPos pos, BlockState blockState) {
        super(
                pos, blockState, 10000, 10000, 5,
                ConcoctiBlocks.CONCOCTI_SOLIDIFIER_BLOCK_ENTITY, 20.0f
        );
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("block.concocti.concocti_solidifier");
    }

    @Override
    protected boolean isItemValidInMachine(int slot, @NotNull ItemStack stack) {
        return slot == MOLD_SLOT || slot == BASE_ITEM_SLOT;
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

    public IFluidHandler getFluidHandler() {
        return tank;
    }

    @Override
    @Contract(pure = true)
    protected @NotNull List<SlotType> getAllowedSlotTypes() {
        return List.of(
                SlotType.FLUID_INPUT,
                SlotType.MOLD_ITEM_INPUT,
                SlotType.BASE_ITEM_INPUT,
                SlotType.ITEM_OUTPUT
        );
    }

    public List<Integer> getItemSlots(SlotType type) {
        switch (type) {
            case ITEM_OUTPUT -> {
                return List.of(OUTPUT_SLOT);
            }
            case MOLD_ITEM_INPUT -> {
                return List.of(MOLD_SLOT);
            }
            case BASE_ITEM_INPUT -> {
                return List.of(BASE_ITEM_SLOT);
            }
        };
        return List.of();
    }

    public List<IFluidTank> getFluidTanks(SlotType type) {
        switch (type) {
            case FLUID_INPUT -> {
                return List.of(tank);
            }
        };
        return List.of();
    }
}
