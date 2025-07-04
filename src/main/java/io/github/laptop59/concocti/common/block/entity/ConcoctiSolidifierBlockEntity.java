package io.github.laptop59.concocti.common.block.entity;

import io.github.laptop59.concocti.client.gui.components.SlotType;
import io.github.laptop59.concocti.common.abstraction.ConcoctiMachineComplexion;
import io.github.laptop59.concocti.common.abstraction.Properties;
import io.github.laptop59.concocti.common.abstraction.Property;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.machine.ConcoctiMachineDetails;
import io.github.laptop59.concocti.common.machine.ConcoctiMachineDetailsBuilder;
import io.github.laptop59.concocti.common.menu.ConcoctiMelterMenu;
import io.github.laptop59.concocti.common.menu.ConcoctiSolidifierMenu;
import io.github.laptop59.concocti.common.recipe.ConcoctiRecipes;
import io.github.laptop59.concocti.common.recipe.ConcoctiSolidifierRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
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

public class ConcoctiSolidifierBlockEntity extends AbstractConcoctiMachineBlockEntity
        <ConcoctiSolidifierBlockEntity, ConcoctiSolidifierMenu, ConcoctiSolidifierBlockEntity.InputValue,
                ConcoctiSolidifierRecipe.Input, ConcoctiSolidifierRecipe>
    implements FluidHandlerBlockEntity {

    public record InputValue(ItemStack mold, FluidStack fluid, ItemStack baseItem) {}

    public final FluidTank tank = new FluidTank(TANK_CAPACITY);

    // Slots
    private static final int MOLD_SLOT = 2;
    private static final int BASE_ITEM_SLOT = 3;
    private static final int OUTPUT_SLOT = 4;

    // Properties
    public final Property<FluidStack> FLUID_INPUT = Properties.FLUID_INPUT.newWithLinker(tank::getFluid);

    // Details
    public final static Supplier<BlockEntityType<ConcoctiSolidifierBlockEntity>> BLOCK_ENTITY_TYPE =
            ConcoctiBlocks.CONCOCTI_SOLIDIFIER_BLOCK_ENTITY;

    protected final ConcoctiMachineComplexion dataAccess = new ConcoctiMachineComplexion(
            this,
            FLUID_INPUT.of(FluidStack.EMPTY)
    );

    public Supplier<ConcoctiMachineDetails<ConcoctiSolidifierBlockEntity, ConcoctiSolidifierMenu, InputValue, ConcoctiSolidifierRecipe.Input, ConcoctiSolidifierRecipe>> getUncachedMachineDetails() {
        return () -> ConcoctiMachineDetailsBuilder
                .<ConcoctiSolidifierBlockEntity, ConcoctiSolidifierMenu, ConcoctiSolidifierBlockEntity.InputValue, ConcoctiSolidifierRecipe.Input, ConcoctiSolidifierRecipe>create()
                .withMaxEnergy(10_000)
                .withMaxEnergyTransfer(10_000)
                .withSlots(5)
                .withRateConsumption(20.0f)
                .withRecipeType(ConcoctiRecipes.CONCOCTI_SOLIDIFIER_RECIPE_TYPE)
                .withDefaultName(Component.translatable("block.concocti.concocti_solidifier"))
                .withAllowedSlotTypes(
                        SlotType.FLUID_INPUT,
                        SlotType.MOLD_ITEM_INPUT,
                        SlotType.BASE_ITEM_INPUT,
                        SlotType.ITEM_OUTPUT
                )
                .withMenuClass(ConcoctiSolidifierMenu.class)
                .withComplexion(() -> dataAccess)
                .withItemSlotsMap(new EnumMap<>(
                        Map.of(
                                SlotType.ITEM_OUTPUT, List.of(OUTPUT_SLOT),
                                SlotType.MOLD_ITEM_INPUT, List.of(MOLD_SLOT),
                                SlotType.BASE_ITEM_INPUT, List.of(BASE_ITEM_SLOT)
                        )
                ))
                .withFluidSlotsMap(new EnumMap<>(
                        Map.of(
                                SlotType.FLUID_INPUT, List.of(() -> tank)
                        )
                ))
                .build();
    }

    public ConcoctiSolidifierBlockEntity(BlockPos pos, BlockState blockState) {
        super(
                BLOCK_ENTITY_TYPE, pos, blockState
        );
    }

    @Override
    protected boolean isItemValidInMachine(int slot, @NotNull ItemStack stack) {
        return slot == MOLD_SLOT || slot == BASE_ITEM_SLOT;
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
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        tank.setFluid(parseFluidStack((CompoundTag) tag.get("fluid_input"), registries));
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        saveFluidStack("fluid_input", tag, tank, registries);
    }

    @Override
    public List<IFluidTank> getFluidTanks() {
        return List.of(
                tank
        );
    }
}
