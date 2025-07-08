package io.github.laptop59.concocti.common.block.entity;

import io.github.laptop59.concocti.client.gui.components.SlotType;
import io.github.laptop59.concocti.common.abstraction.ConcoctiMachineComplexion;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.machine.ConcoctiMachineDetails;
import io.github.laptop59.concocti.common.machine.ConcoctiMachineDetailsBuilder;
import io.github.laptop59.concocti.common.menu.ConcoctiEnergyGeneratorMenu;
import io.github.laptop59.concocti.common.menu.ConcoctiUpgradeSlot;
import io.github.laptop59.concocti.common.recipe.ConcoctiEnergyGeneratorRecipe;
import io.github.laptop59.concocti.common.recipe.ConcoctiRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static io.github.laptop59.concocti.common.block.ConcoctiEnergyGeneratorBlock.LIT;

public class ConcoctiEnergyGeneratorBlockEntity extends AbstractConcoctiMachineBlockEntity
    <ConcoctiEnergyGeneratorBlockEntity, ConcoctiEnergyGeneratorMenu, ItemStack, SingleRecipeInput, ConcoctiEnergyGeneratorRecipe> {

    // Slots
    private static final int INPUT_SLOT = 2;

    // Properties: None

    // Details
    public final static Supplier<BlockEntityType<ConcoctiEnergyGeneratorBlockEntity>> BLOCK_ENTITY_TYPE =
            ConcoctiBlocks.CONCOCTI_ENERGY_GENERATOR_BLOCK_ENTITY;

    public static int energyPerTick = 50;

    public Supplier<ConcoctiMachineDetails<ConcoctiEnergyGeneratorBlockEntity, ConcoctiEnergyGeneratorMenu, ItemStack, SingleRecipeInput, ConcoctiEnergyGeneratorRecipe>> getUncachedMachineDetails() {
        return () -> ConcoctiMachineDetailsBuilder
                .<ConcoctiEnergyGeneratorBlockEntity, ConcoctiEnergyGeneratorMenu, ItemStack, SingleRecipeInput, ConcoctiEnergyGeneratorRecipe>create()
                .withMaxEnergy(100_000)
                .withMaxEnergyTransfer(100_000)
                .withSlots(3)
                .withRateConsumption(50.0f)
                .withEnergyMode(() -> DynamicEnergyStorage.Mode.OUTPUT_ONLY)
                .withRecipeType(ConcoctiRecipes.CONCOCTI_ENERGY_GENERATOR_RECIPE_TYPE)
                .withDefaultName(Component.translatable("block.concocti.concocti_energy_generator"))
                .withAllowedSlotTypes(
                        SlotType.ITEM_INPUT,
                        SlotType.ENERGY_OUTPUT
                )
                .withMenuClass(ConcoctiEnergyGeneratorMenu.class)
                .withComplexion(() -> dataAccess)
                .withItemSlotsMap(new EnumMap<>(
                        Map.of(
                                SlotType.ITEM_INPUT, List.of(INPUT_SLOT)
                        )
                ))
                .withFluidSlotsMap(new EnumMap<>(SlotType.class))
                .build();
    }

    @Override
    protected boolean isItemValidInMachine(int slot, @NotNull ItemStack stack) {
        return slot == INPUT_SLOT;
    }

    protected final ConcoctiMachineComplexion dataAccess = new ConcoctiMachineComplexion(this);

    public ConcoctiEnergyGeneratorBlockEntity(BlockPos pos, BlockState blockState) {
        super(
                BLOCK_ENTITY_TYPE, pos, blockState
        );
    }

    public ItemStack getInputStack() {
        return this.getItem(INPUT_SLOT);
    }

    /** Returns 0 if the input item is not a fuel, otherwise return the number of ticks it would burn for. */
    public int getFuelTicksFromOneInputItem() {
        ItemStack input = getInputStack();
        Holder<Item> holder = input.getItemHolder();
        FurnaceFuel fuel = holder.getData(NeoForgeDataMaps.FURNACE_FUELS);
        if (fuel == null) return 0;
        return fuel.burnTime();
    }

    @Override
    public boolean canProcess() {
        return ticksLeft <= 0 && getFuelTicksFromOneInputItem() > 0 && energy.getEnergyStored() < energy.getMaxEnergyStored();
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state) {
        ConcoctiEnergyGeneratorBlockEntity entity = this;

        int currentUpgradeUnits = ConcoctiUpgradeSlot.getUpgradeUnits(entity.getItem(UPGRADE_SLOT));
        if (currentUpgradeUnits != entity.lastUpgradeUnits) {
            entity.lastUpgradeUnits = currentUpgradeUnits;
            entity.setNewEnergyMultiplier(entity.getInefficientEnergyMultiplier());
        }
        if (entity.ticksLeft >= entity.totalTicks) entity.lastRecipeId = null;
        if (--entity.autoCooldown <= 0) {
            entity.autoCooldown = AUTO_COOLDOWN;
            entity.attemptToPull();
            entity.attemptToEject();
        }
        int consumableTicks = entity.getTickMultiplier();
        while (consumableTicks > 0) {
            if (entity.ticksLeft > 0) {
                int ticksConsumed = consumableTicks;
                if (ticksConsumed > entity.ticksLeft) ticksConsumed = entity.ticksLeft;
                consumableTicks -= ticksConsumed;
                entity.energy.forceReceiveEnergy(energyPerTick * ticksConsumed, false);
                entity.ticksLeft -= ticksConsumed;
            } else {
                entity.lastRecipeId = null;
            }
            if (entity.canProcess()) {
                ItemStack input = entity.getInput();
                ResourceLocation toBeProcessed = entity.getRecipeIdFrom(input);
                int ticksToBurn = getFuelTicksFromOneInputItem();
                if ((entity.lastRecipeId == null || !entity.lastRecipeId.equals(toBeProcessed))) {
                    entity.lastRecipeId = toBeProcessed;
                    entity.totalTicks = ticksToBurn;
                    entity.ticksLeft += entity.totalTicks;
                    getInputStack().shrink(1);
                    entity.attemptToPull();
                    entity.attemptToEject();
                }
            } else break;
        }
        if (state.getValue(LIT) != (entity.ticksLeft > 0)) {
            level.setBlock(pos, state.setValue(LIT, (entity.ticksLeft > 0)), 1 | 2);
        }
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
    protected void onRecipeCompleted(ConcoctiEnergyGeneratorRecipe recipe) {}

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
    }

    @Override
    public List<IFluidTank> getFluidTanks() {
        return List.of();
    }
}
