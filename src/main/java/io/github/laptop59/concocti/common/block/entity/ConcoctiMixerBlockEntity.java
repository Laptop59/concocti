package io.github.laptop59.concocti.common.block.entity;

import io.github.laptop59.concocti.client.gui.components.SlotType;
import io.github.laptop59.concocti.common.abstraction.ConcoctiMachineComplexion;
import io.github.laptop59.concocti.common.abstraction.Properties;
import io.github.laptop59.concocti.common.abstraction.Property;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.machine.ConcoctiMachineDetails;
import io.github.laptop59.concocti.common.machine.ConcoctiMachineDetailsBuilder;
import io.github.laptop59.concocti.common.menu.ConcoctiMixerMenu;
import io.github.laptop59.concocti.common.recipe.ConcoctiMixerRecipe;
import io.github.laptop59.concocti.common.recipe.ConcoctiRecipes;
import io.github.laptop59.concocti.common.recipe.ItemsFluidsRecipeInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ConcoctiMixerBlockEntity extends AbstractConcoctiMachineBlockEntity
        <ConcoctiMixerBlockEntity, ConcoctiMixerMenu, ConcoctiMixerBlockEntity.InputValue, ItemsFluidsRecipeInput, ConcoctiMixerRecipe> {

    public record InputValue(
            IItemHandler itemHandler,
            IFluidHandler fluidHandler
    ) {}

    // Tanks
    private final FluidTank fluidInput1 = new FluidTank(TANK_CAPACITY);
    private final FluidTank fluidInput2 = new FluidTank(TANK_CAPACITY);
    private final FluidTank fluidInput3 = new FluidTank(TANK_CAPACITY);
    private final FluidTank fluidInput4 = new FluidTank(TANK_CAPACITY);
    private final FluidTank fluidOutput = new FluidTank(TANK_CAPACITY * 2);

    // Slots
    private static final int OUTPUT_SLOT = 6;
    private static final int INPUT_SLOT_1 = 2;
    private static final int INPUT_SLOT_2 = 3;
    private static final int INPUT_SLOT_3 = 4;
    private static final int INPUT_SLOT_4 = 5;

    private final IItemHandler inputItemHandler = itemHandler.whitelistWrapper(
            INPUT_SLOT_1, INPUT_SLOT_2, INPUT_SLOT_3, INPUT_SLOT_4
    );

    private final IFluidHandler inputFluidHandler = fluidHandler.whitelistTanks(
            fluidInput1, fluidInput2, fluidInput3, fluidInput4
    );

    // Properties
    public final Property<FluidStack> FLUID_INPUT_1 = Properties.FLUID_INPUT_1.newWithLinker(fluidInput1::getFluid);
    public final Property<FluidStack> FLUID_INPUT_2 = Properties.FLUID_INPUT_2.newWithLinker(fluidInput2::getFluid);
    public final Property<FluidStack> FLUID_INPUT_3 = Properties.FLUID_INPUT_3.newWithLinker(fluidInput3::getFluid);
    public final Property<FluidStack> FLUID_INPUT_4 = Properties.FLUID_INPUT_4.newWithLinker(fluidInput4::getFluid);
    public final Property<FluidStack> FLUID_OUTPUT = Properties.FLUID_OUTPUT.newWithLinker(fluidOutput::getFluid);

    // Details
    public final static Supplier<BlockEntityType<ConcoctiMixerBlockEntity>> BLOCK_ENTITY_TYPE =
            ConcoctiBlocks.CONCOCTI_MIXER_BLOCK_ENTITY;

    @Override
    public List<IFluidHandler> getIndexedFluidHandlers() {
        return List.of(fluidInput1, fluidInput2, fluidInput3, fluidInput4, fluidOutput);
    }

    public Supplier<ConcoctiMachineDetails<ConcoctiMixerBlockEntity, ConcoctiMixerMenu, ConcoctiMixerBlockEntity.InputValue, ItemsFluidsRecipeInput, ConcoctiMixerRecipe>> getUncachedMachineDetails() {
        return () -> ConcoctiMachineDetailsBuilder
                .<ConcoctiMixerBlockEntity, ConcoctiMixerMenu, ConcoctiMixerBlockEntity.InputValue, ItemsFluidsRecipeInput, ConcoctiMixerRecipe>create()
                .withMaxEnergy(5_000)
                .withMaxEnergyTransfer(5_000)
                .withSlots(7)
                .withRateConsumption(10.0f)
                .withEnergyMode(() -> DynamicEnergyStorage.Mode.INPUT_ONLY)
                .withRecipeType(ConcoctiRecipes.CONCOCTI_MIXER_RECIPE_TYPE)
                .withDefaultName(Component.translatable("block.concocti.concocti_mixer"))
                .withAllowedSlotTypes(
                        SlotType.ITEM_INPUT_1,
                        SlotType.ITEM_INPUT_2,
                        SlotType.ITEM_INPUT_3,
                        SlotType.ITEM_INPUT_4,
                        SlotType.FLUID_INPUT_1,
                        SlotType.FLUID_INPUT_2,
                        SlotType.FLUID_INPUT_3,
                        SlotType.FLUID_INPUT_4,
                        SlotType.ITEM_OUTPUT,
                        SlotType.FLUID_OUTPUT,
                        SlotType.ALL_ITEM_INPUTS,
                        SlotType.ALL_FLUID_INPUTS
                )
                .withMenuClass(ConcoctiMixerMenu.class)
                .withComplexion(() -> dataAccess)
                .withItemSlotsMap(new EnumMap<>(
                        Map.of(
                                SlotType.ITEM_INPUT_1, List.of(INPUT_SLOT_1),
                                SlotType.ITEM_INPUT_2, List.of(INPUT_SLOT_2),
                                SlotType.ITEM_INPUT_3, List.of(INPUT_SLOT_3),
                                SlotType.ITEM_INPUT_4, List.of(INPUT_SLOT_4),
                                SlotType.ALL_ITEM_INPUTS, List.of(INPUT_SLOT_1, INPUT_SLOT_2, INPUT_SLOT_3, INPUT_SLOT_4),
                                SlotType.ITEM_OUTPUT, List.of(OUTPUT_SLOT)
                        )
                ))
                .withFluidSlotsMap(new EnumMap<>(
                        Map.of(
                                SlotType.FLUID_INPUT_1, List.of(() -> fluidInput1),
                                SlotType.FLUID_INPUT_2, List.of(() -> fluidInput2),
                                SlotType.FLUID_INPUT_3, List.of(() -> fluidInput3),
                                SlotType.FLUID_INPUT_4, List.of(() -> fluidInput4),
                                SlotType.ALL_FLUID_INPUTS, List.of(
                                        () -> fluidInput1,
                                        () -> fluidInput2,
                                        () -> fluidInput3,
                                        () -> fluidInput4
                                ),
                                SlotType.FLUID_OUTPUT, List.of(() -> fluidOutput)
                        )
                ))
                .build();
    }

    @Override
    protected boolean isItemValidInMachine(int slot, @NotNull ItemStack stack) {
        return true;
    }

    protected final ConcoctiMachineComplexion dataAccess = new ConcoctiMachineComplexion(
            this,
            FLUID_INPUT_1.of(FluidStack.EMPTY.copy()),
            FLUID_INPUT_2.of(FluidStack.EMPTY.copy()),
            FLUID_INPUT_3.of(FluidStack.EMPTY.copy()),
            FLUID_INPUT_4.of(FluidStack.EMPTY.copy()),
            FLUID_OUTPUT.of(FluidStack.EMPTY.copy())
    );

    public ConcoctiMixerBlockEntity(BlockPos pos, BlockState blockState) {
        super(
                BLOCK_ENTITY_TYPE, pos, blockState
        );
    }

    @Override
    protected InputValue getInput() {
        return new InputValue(
                inputItemHandler,
                inputFluidHandler
        );
    }

    @Override
    public boolean canProcess() {
        if (!super.canProcess()) return false;
        ConcoctiMixerRecipe recipe = getCurrentRecipe(getInput());
        if (!recipe.matches(new ItemsFluidsRecipeInput(inputItemHandler, inputFluidHandler))) return false;
        // Check whether the fluids obtained from this item will not exceed our fluid limit.
        ItemStack outputSlotItems = getItem(OUTPUT_SLOT);
        ItemStack resultItems = recipe.getOutputItem();
        if (!outputSlotItems.isEmpty() &&
                resultItems != null &&
                outputSlotItems.getCount() + resultItems.getCount() > outputSlotItems.getMaxStackSize())
            return false;
        FluidStack resultFluid = recipe.getOutputFluid();
        if (resultFluid != null && fluidHandler.fill(resultFluid, IFluidHandler.FluidAction.SIMULATE) != resultFluid.getAmount())
            return false;
        return true;
    }

    @Override
    protected ItemsFluidsRecipeInput recipeInputFrom(InputValue input) {
        return new ItemsFluidsRecipeInput(input.itemHandler, input.fluidHandler);
    }

    @Override
    protected ResourceLocation getRecipeIdFrom(InputValue inputValue) {
        RecipeManager recipeManager = getLevel().getRecipeManager();
        return recipeManager
                .getRecipeFor(ConcoctiRecipes.CONCOCTI_MIXER_RECIPE_TYPE.get(), recipeInputFrom(inputValue), getLevel())
                .map(RecipeHolder::id)
                .orElse(null);
    }

    @Override
    protected void onRecipeCompleted(ConcoctiMixerRecipe recipe) {
       recipeInputFrom(getInput()).consume(recipe.getInputItems(), recipe.getInputFluids());
       if (recipe.getOutputItem() != null)
           itemHandler.insertItem(OUTPUT_SLOT, recipe.getOutputItem().copy(), false);
       if (recipe.getOutputFluid() != null)
           fluidOutput.fill(recipe.getOutputFluid().copy(), IFluidHandler.FluidAction.EXECUTE);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        fluidInput1.setFluid(parseFluidStack((CompoundTag) tag.get("fluid_input_1"), registries));
        fluidInput2.setFluid(parseFluidStack((CompoundTag) tag.get("fluid_input_2"), registries));
        fluidInput3.setFluid(parseFluidStack((CompoundTag) tag.get("fluid_input_3"), registries));
        fluidInput4.setFluid(parseFluidStack((CompoundTag) tag.get("fluid_input_4"), registries));
        fluidOutput.setFluid(parseFluidStack((CompoundTag) tag.get("fluid_output"), registries));
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        saveFluidStack("fluid_input_1", tag, fluidInput1, registries);
        saveFluidStack("fluid_input_2", tag, fluidInput2, registries);
        saveFluidStack("fluid_input_3", tag, fluidInput3, registries);
        saveFluidStack("fluid_input_4", tag, fluidInput4, registries);
        saveFluidStack("fluid_output", tag, fluidOutput, registries);
    }

    @Override
    public List<IFluidTank> getFluidTanks() {
        return List.of(
                fluidInput1, fluidInput2, fluidInput3, fluidInput4, fluidOutput
        );
    }
}
