package io.github.laptop59.concocti.common.block.entity;

import io.github.laptop59.concocti.client.gui.components.MachineSettings;
import io.github.laptop59.concocti.client.gui.components.MachineSettingsSlots;
import io.github.laptop59.concocti.client.gui.components.SlotFlag;
import io.github.laptop59.concocti.client.gui.components.SlotType;
import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.abstraction.Properties;
import io.github.laptop59.concocti.common.abstraction.Property;
import io.github.laptop59.concocti.common.block.frame.FrameAttributes;
import io.github.laptop59.concocti.common.block.frame.FrameBlock;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluidTankHandler;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluidTankSlotTypedHandler;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.machine.ConcoctiMachineDetails;
import io.github.laptop59.concocti.common.menu.ConcoctiFrameSlot;
import io.github.laptop59.concocti.common.menu.ConcoctiUpgradeSlot;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.github.laptop59.concocti.common.block.AbstractConcoctiMachineBlock.FACING;
import static io.github.laptop59.concocti.common.block.ConcoctiMelterBlock.LIT;

/**
 * A class that serves as a base for Concocti Machines. <p>
 * Any {@link ContainerData} will have to be stored by the extending classes.
 * @param <T> The type of block entity (should be itself.)
 * @param <M> The type of menu of this block entity.
 * @param <V> The recipe input data (items, fluids, ...) this block entity will accept for checking any recipes. <p>
 *            If this involves more than one object, consider making an <i>inner record class</i> to store anything needed.
 * @param <I> The {@link RecipeInput} type.
 * @param <R> The recipe type of this block entity.
 */
public abstract class AbstractConcoctiMachineBlockEntity
        <T extends AbstractConcoctiMachineBlockEntity<T, M, V, I, R>,
                M extends AbstractContainerMenu, V, I extends RecipeInput, R extends ProcessingRecipe<R, I>>
        extends AbstractPoweredBlockEntity
        implements ItemHandlerBlockEntity, FluidHandlerBlockEntity {

    int ticksLeft = 0;
    int totalTicks = 0;
    int lastUpgradeUnits = -1;
    float rateConsumption;
    boolean ejectOn;
    boolean pullOn;
    ResourceLocation lastRecipeId = null;
    protected ConcoctiFluidTankHandler fluidHandler;

    int autoCooldown = 0;

    public ConcoctiMachineDetails<T, M, V, I, R> machineDetails;
    public final MachineSettings machineSettings = new MachineSettings(List.of());

    public static final int AUTO_COOLDOWN = 5;
    public static final int TANK_CAPACITY = 64000;

    // Slots
    public static final int UPGRADE_SLOT = 0;
    public static final int FRAME_SLOT = 1;

    // Properties
    public final Property<Integer> TICKS_LEFT =
            Properties.TICKS_LEFT.newWithLinker(() -> ticksLeft);
    public final Property<Integer> TOTAL_TICKS =
            Properties.TOTAL_TICKS.newWithLinker(() -> totalTicks);
    public final Property<Integer> ENERGY_STORED =
            Properties.ENERGY_STORED.newWithLinker(() -> energy.getEnergyStored());
    public final Property<Integer> MAX_ENERGY_STORED =
            Properties.MAX_ENERGY_STORED.newWithLinker(() -> energy.getMaxEnergyStored());
    public final Property<Boolean> EJECT_ON =
            Properties.EJECT_ON.newWithLinker(() -> ejectOn);
    public final Property<Boolean> PULL_ON =
            Properties.PULL_ON.newWithLinker(() -> pullOn);

    public final Property<Direction> FACING_DIRECTION =
            Properties.FACING_DIRECTION.newWithLinker(() -> getBlockState().getValue(FACING));
    public final Property<MachineSettingsSlots> MACHINE_SETTINGS_SLOTS =
            Properties.MACHINE_SETTINGS_SLOTS.newWithLinker(() -> machineSettings.slots);

    /** Get the machine-specific details of this machine, uncached. Do not use this function for normal use. */
    protected abstract Supplier<ConcoctiMachineDetails<T, M, V, I, R>> getUncachedMachineDetails();

    /** Get the machine-specific details of this machine and caches it if not done yet. */
    protected ConcoctiMachineDetails<T, M, V, I, R> getMachineDetails() {
        if (this.machineDetails != null) return this.machineDetails;
        ConcoctiMachineDetails<T, M, V, I, R> details = getUncachedMachineDetails().get();
        this.machineDetails = details;
        return details;
    }

    /** Gets the item handler from a particular direction. */
    public @Nullable IItemHandler getSidedItemHandler(Direction direction) {
        SlotType slotType = machineSettings.getSlot(direction);
        if (slotType != null && !slotType.isSet(SlotFlag.ITEM)) return null;
        List<Integer> slots = slotType == null ? getItemSlots() : getItemSlots(slotType);
        if (slots.isEmpty()) return null;
        return new IItemHandler() {
            @Override
            public int getSlots() {
                return slotSize;
            }

            @Override
            public @NotNull ItemStack getStackInSlot(int slot) {
                if (slots.contains(slot)) return itemHandler.getStackInSlot(slot);
                return ItemStack.EMPTY.copy();
            }

            @Override
            public @NotNull ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (slots.contains(slot) && (slotType == null || slotType.isSet(SlotFlag.INPUT))) return itemHandler.insertItem(slot, stack, simulate);
                return stack;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slots.contains(slot) && (slotType == null || slotType.isSet(SlotFlag.OUTPUT))) return itemHandler.extractItem(slot, amount, simulate);
                return ItemStack.EMPTY.copy();
            }

            @Override
            public int getSlotLimit(int slot) {
                return itemHandler.getSlotLimit(slot);
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return itemHandler.isItemValid(slot, stack);
            }
        };
    }

    /** Negates whether eject is on and returns the new value. */
    public boolean changeEjectOn() {
        ejectOn = !ejectOn;
        attemptToEject();
        return ejectOn;
    }

    /** Negates whether pull is on and returns the new value. */
    public boolean changePullOn() {
        pullOn = !pullOn;
        attemptToPull();
        return pullOn;
    }

    /** Attempts to eject output items, fluids and energy. However, this is a no-op if eject is not enabled. */
    public void attemptToEject() {
        if (!ejectOn) return;
        attemptToEjectItems();
        attemptToEjectFluids();
        attemptToEjectEnergy();
    }

    /** Attempts to pull input items and fluids. However, this is a no-op if pull is not enabled. */
    public void attemptToPull() {
        if (!pullOn) return;
        attemptToPullItems();
        attemptToPullFluids();
    }

    /** Attempts to eject output items. However, this is <b>NOT</b> a no-op if pull is not enabled - it doesn't care whether pull is on or off. */
    protected void attemptToEjectItems() {
        for (Direction direction : machineSettings.slots.keySet()) {
            IItemHandler input = getSidedItemHandler(direction);
            if (input == null) continue;
            IItemHandler output = getLevel().getCapability(
                    Capabilities.ItemHandler.BLOCK,
                    getBlockPos().relative(direction),
                    direction.getOpposite()
            );
            if (output == null) continue;
            transfer(input, output, false);
        }
    }

    /** Attempts to eject output fluids. However, this is <b>NOT</b> a no-op if eject is not enabled - it doesn't care whether eject is on or off. */
    protected void attemptToEjectFluids() {
        for (Direction direction : machineSettings.slots.keySet()) {
            IFluidHandler input = getSidedFluidHandler(direction);
            if (input == null) continue;
            IFluidHandler output = getLevel().getCapability(
                    Capabilities.FluidHandler.BLOCK,
                    getBlockPos().relative(direction),
                    direction.getOpposite()
            );
            if (output == null) continue;
            transfer(input, output);
        }
    }

    /** Attempts to eject output energy (if applicable). However, this is <b>NOT</b> a no-op if eject is not enabled - it doesn't care whether eject is on or off. */
    protected void attemptToEjectEnergy() {
        for (Direction direction : machineSettings.slots.keySet()) {
            SlotType slotType = machineSettings.getSlot(direction);
            if (!slotType.isSet(SlotFlag.ENERGY | SlotFlag.OUTPUT)) continue;
            IEnergyStorage input = energy;
            if (input == null) continue;
            IEnergyStorage output = getLevel().getCapability(
                    Capabilities.EnergyStorage.BLOCK,
                    getBlockPos().relative(direction),
                    direction.getOpposite()
            );
            if (output == null) continue;
            transfer(input, output);
        }
    }

    /** Attempts to pull input items. However, this is <b>NOT</b> a no-op if pull is not enabled - it doesn't care whether pull is on or off. */
    protected void attemptToPullItems() {
        for (Direction direction : machineSettings.slots.keySet()) {
            IItemHandler output = getSidedItemHandler(direction);
            if (output == null) continue;
            IItemHandler input = getLevel().getCapability(
                    Capabilities.ItemHandler.BLOCK,
                    getBlockPos().relative(direction),
                    direction.getOpposite()
            );
            if (input == null) continue;
            transfer(input, output, true);
        }
    }

    /** Attempts to pull input fluids. However, this is <b>NOT</b> a no-op if pull is not enabled - it doesn't care whether pull is on or off. */
    protected void attemptToPullFluids() {
        for (Direction direction : machineSettings.slots.keySet()) {
            IFluidHandler output = getSidedFluidHandler(direction);
            if (output == null) continue;
            IFluidHandler input = getLevel().getCapability(
                    Capabilities.FluidHandler.BLOCK,
                    getBlockPos().relative(direction),
                    direction.getOpposite()
            );
            if (input == null) continue;
            transfer(input, output);
        }
    }

    /**
     * Transfer items from one handler to another.
     * @param from Handler to take items from.
     * @param to Handler to put items to.
     */
    protected static void transfer(@NotNull IItemHandler from, @NotNull IItemHandler to, boolean fillExistingStacks) {
        // Transfer all the items possible from `from` to `to`.
        // Taken from MI.
        // https://github.com/AztechMC/Modern-Industrialization/blob/c5a997baf3be596049c031bc0b4a7915def7b99d/src/main/java/aztech/modern_industrialization/util/TransferHelper.java#L38
        for (int i = 0; i < from.getSlots(); i++) {
            // First, simulate.
            ItemStack toTake = from.extractItem(i, Integer.MAX_VALUE, true);
            if (toTake.isEmpty()) continue;
            int extractCount = toTake.getCount();
            ItemStack left = fillExistingStacks ?
                    ItemHandlerHelper.insertItemStacked(to, toTake, true) :
                    ItemHandlerHelper.insertItem(to, toTake, true);
            int insertCount = extractCount - left.getCount();
            if (insertCount <= 0) continue;
            // Now we can execute the action.
            toTake = from.extractItem(i, insertCount, false);
            if (toTake.isEmpty()) continue;
            left = fillExistingStacks ?
                    ItemHandlerHelper.insertItemStacked(to, toTake, false) :
                    ItemHandlerHelper.insertItem(to, toTake, false);
            if (!left.isEmpty()) {
                // Try to give the taken items back if possible.
                left = from.insertItem(i, left, false);
                if (!left.isEmpty()) {
                    Concocti.LOGGER.warn("Could not provide back {} to item handler {}, voiding.", left, to);
                }
            }
        }
    }

    /**
     * Transfer fluids from one handler to another.
     * @param from Handler to take fluids from.
     * @param to Handler to put fluids to.
     */
    protected static void transfer(@NotNull IFluidHandler from, @NotNull IFluidHandler to) {
        while (!transfer(from, to, Integer.MAX_VALUE, false).isEmpty());
    }

    /**
     * Transfer energy from one storage to another.
     * @param from Storage to extract energy from.
     * @param to Storage to insert energy to.
     */
    protected static void transfer(@NotNull IEnergyStorage from, @NotNull IEnergyStorage to) {
        if (!from.canExtract() || !to.canReceive()) return;
        int energy = Math.min(from.extractEnergy(Integer.MAX_VALUE, true), to.receiveEnergy(Integer.MAX_VALUE, true));
        // Now try to extract and put.
        to.receiveEnergy(from.extractEnergy(energy, false), false);
    }

    /**
     * Transfer fluids from one handler to another. This function is a fixed version of NeoForge's handler, which does
     * not handle multi-tank to multi-tank transactions properly.
     * @param from Handler to take fluids from.
     * @param to Handler to put fluids to.
     * @param maxAmount The maximum amount of fluid from a tank to take from the {@code from} handler.
     * @param simulated Whether the transfer is simulated or not.
     */
    public static FluidStack transfer(@NotNull IFluidHandler from, @NotNull IFluidHandler to, int maxAmount, boolean simulated) {
        // Taken from MI.
        // https://github.com/AztechMC/Modern-Industrialization/blob/c5a997baf3be596049c031bc0b4a7915def7b99d/src/main/java/aztech/modern_industrialization/util/TransferHelper.java#L147
        int tanks = from.getTanks();
        for (int i = 0; i < tanks; ++i) {
            FluidStack toTry = from.getFluidInTank(i).copy();
            if (toTry.getAmount() > maxAmount) {
                toTry.setAmount(maxAmount);
            }
            FluidStack drainable = from.drain(toTry, IFluidHandler.FluidAction.SIMULATE);
            if (drainable.isEmpty()) {
                continue;
            }
            int fillableAmount = to.fill(drainable, IFluidHandler.FluidAction.SIMULATE);
            if (fillableAmount > 0) {
                drainable.setAmount(fillableAmount);
                if (!simulated) {
                    FluidStack drained = from.drain(drainable, IFluidHandler.FluidAction.EXECUTE);
                    if (!drained.isEmpty()) {
                        drained.setAmount(to.fill(drained, IFluidHandler.FluidAction.EXECUTE));
                         return drained;
                    }
                } else {
                    return drainable;
                }
            }
        }
        return FluidStack.EMPTY;
    }

    /** Gets the fluid handler from a particular direction. */
    public @Nullable IFluidHandler getSidedFluidHandler(Direction direction) {
        SlotType slotType = machineSettings.getSlot(direction);
        if (slotType != null && !slotType.isSet(SlotFlag.FLUID)) return null;
        List<IFluidTank> tanks = slotType == null ? getFluidTanks() : getFluidTanks(slotType);
        if (tanks.isEmpty()) return null;
        return new ConcoctiFluidTankSlotTypedHandler(() -> tanks, slotType);
    }

    @Override
    protected final boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (slot == 0) return stack.is(ConcoctiItems.Tags.CONCOCTI_UPGRADES);
        if (slot == 1) return ConcoctiFrameSlot.getFrameAttributes(stack.getItem()).isPresent();
        return isItemValidInMachine(slot, stack);
    }

    /**
     * Tells whether an item is valid in a specific slot index.
     * @param slot The slot index.
     * @param stack The item stack.
     * @return The item's validity.
     */
    abstract protected boolean isItemValidInMachine(int slot, @NotNull ItemStack stack);

    public AbstractConcoctiMachineBlockEntity(Supplier<BlockEntityType<T>> blockEntityType, BlockPos pos, BlockState blockState) {
        super(
                blockEntityType.get(),
                pos,
                blockState,
                0,
                0,
                0,
                () -> DynamicEnergyStorage.Mode.NONE
        );
        AbstractConcoctiMachineBlockEntity<T, M, V, I, R> blockEntity = this;

        // Now fill up the blank variables.
        ConcoctiMachineDetails<T, M, V, I, R> details = getMachineDetails();

        this.maxEnergy = details.maxEnergy();
        this.maxEnergyTransfer = details.maxEnergyTransfer();
        this.resetItemHandler(details.slots());
        this.rateConsumption = details.rateConsumption();
        this.machineSettings.availableTypes = details.allowedSlotTypes();
        this.setEnergyModeSupplier(details.energyMode());

        this.fluidHandler = new ConcoctiFluidTankHandler(blockEntity::getFluidTanks);

        if (details.slots() < 2) throw new IllegalArgumentException("Expected at least two slots for upgrades and frame.");
    }

    @Contract(pure = true)
    protected @NotNull List<SlotType> getAllowedSlotTypes() {
        return getMachineDetails().allowedSlotTypes();
    }

    @Override
    public final int getContainerSize() {
        return slotSize;
    }

    @Override
    protected @NotNull NonNullList<ItemStack> getItems() {
        return itemHandler.getDirectList();
    }

    @Override
    protected void setItems(@NotNull NonNullList<ItemStack> items) { /* Don't do anything. */ }

    /** Creates a menu for this block entity. */
    @Override
    protected @NotNull M createMenu(int containerId, @NotNull Inventory inventory) {
        try {
            Class<M> menuClass = getMachineDetails().menuClass();
            return menuClass.getConstructor(int.class, Inventory.class, Container.class, ContainerData.class).newInstance(
                    containerId, inventory, this, getMachineDetails().complexion().get()
            );
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /** Gives the amount of energy (in FE) that this block entity consumes per tick. */
    protected int getTickEnergyIntake() {
        return (int) (getTickMultiplier() * rateConsumption);
    }

    /** Gives the amount of energy (in FE) that this block entity consumes per tick. */
    protected float getEnergyMultiplier() {
        return (float) (getTickEnergyIntake()) * (1 - getEfficiency()) / rateConsumption;
    }

    /** Gives the amount of energy (in FE) that this block entity consumes per tick without considering efficiency. */
    protected float getInefficientEnergyMultiplier() {
        return (float) (getTickEnergyIntake()) / rateConsumption;
    }

    /** Gives the tick process multiplier (tells how must faster a recipe is for this block entity). */
    protected int getTickMultiplier() {
        return (int) (getTickMultiplier(ConcoctiUpgradeSlot.getUpgradeUnits(getItem(UPGRADE_SLOT))) * getFrameMultiplier());
    }

    /** Gives the tick process multiplier (tells how must faster a recipe is for this block entity) from upgrade units. */
    public static int getTickMultiplier(int upgradeUnits) {
        double ticks = Math.pow(1.2, upgradeUnits);
        return (int) ticks + upgradeUnits;
    }

    /** Gives the multiplier caused by an upgradable frame in this machine. */
    protected float getFrameMultiplier() {
        Optional<FrameAttributes> optionalFrameAttributes = ConcoctiFrameSlot.getFrameAttributes(getItem(FRAME_SLOT).getItem());
        if (optionalFrameAttributes.isPresent()) {
            FrameAttributes attributes = optionalFrameAttributes.get();
            return attributes.rate();
        }
        return 1.0f;
    }

    /** Gives the energy efficiency caused by an upgradable frame in this machine. */
    protected float getEfficiency() {
        Optional<FrameAttributes> optionalFrameAttributes = ConcoctiFrameSlot.getFrameAttributes(getItem(FRAME_SLOT).getItem());
        if (optionalFrameAttributes.isPresent()) {
            FrameAttributes attributes = optionalFrameAttributes.get();
            return attributes.efficiency();
        }
        return 0.0f;
    }

    /** Gives this block entity's recipe type. This should usually be from
     * {@link io.github.laptop59.concocti.common.recipe.ConcoctiRecipes}. */
    public Supplier<RecipeType<R>> getRecipeType() {
        return getMachineDetails().recipeType();
    }

    /**
     * Tells whether this block entity can process an input. This should check for all conditions. <p>
     * This default implementation only includes general, recipe-specific checks,
     * and does not include checks for overflowing. Use {@code super.canProcess()} to handle
     * everything this implementation does.
     */
    public boolean canProcess() {
        // Check if enough energy is left.
        if (energy.getEnergyStored() < getTickEnergyIntake()) return false;
        // Query the recipe.
        V input = getInput();
        R recipe = getCurrentRecipe(input);
        if (recipe == null) return false;
        // Check for a match between the ID of the stack and the last known one (via an ID).
        ResourceLocation toBeProcessed = getRecipeIdFrom(input);
        return lastRecipeId == null || lastRecipeId.equals(toBeProcessed);
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return getMachineDetails().defaultName();
    }

    /** Creates a {@link RecipeInput} for an input. */
    protected abstract I recipeInputFrom(V input);

    /** Gets the current recipe with an input. */
    protected R getCurrentRecipe(V input) {
        I recipeInput = recipeInputFrom(input);
        Level level = getLevel();
        if (level == null) return null;
        Optional<RecipeHolder<R>> optional = level.getRecipeManager().getRecipeFor(
                // The recipe type.
                getRecipeType().get(),
                recipeInput,
                level
        );
        return optional.map(RecipeHolder::value).orElse(null);
    }

    /** Gets an input (e.g. item) from this block entity. This can rely on a slot, fluid stack or something else. */
    abstract protected V getInput();
    /**
     * Gets a recipe ID from an input (e.g. item). This ID should be unique for each recipe.
     * @param input The input to get a recipe ID from.
     */
    abstract protected ResourceLocation getRecipeIdFrom(V input);

    /**
     * Called when a recipe has finished. This should account for any products being made.
     * @param recipe The recipe that has completed.
     */
    abstract protected void onRecipeCompleted(R recipe);

    /**
     * Gets all the separate handlers of fluid stacks of this machine, which are indexed consistently.
     */
    public List<IFluidHandler> getIndexedFluidHandlers() {
        return List.of();
    }

    /**
     * A basic implementation of a Concocti Machine's server tick.
     */
    public void tick(Level level, BlockPos pos, BlockState state) {
        AbstractConcoctiMachineBlockEntity<T, M, V, I, R> entity = this;
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
        int consumableTicks = getTickMultiplier();
        while (consumableTicks > 0) {
            if (entity.canProcess()) {
                V input = entity.getInput();
                ResourceLocation toBeProcessed = entity.getRecipeIdFrom(input);
                R recipe = entity.getCurrentRecipe(input);
                if (recipe != null && (entity.lastRecipeId == null || !entity.lastRecipeId.equals(toBeProcessed))) {
                    entity.lastRecipeId = toBeProcessed;
                    entity.totalTicks = recipe.getTicks();
                    entity.ticksLeft = entity.totalTicks;
                }
                int ticksConsumed = Math.min(consumableTicks, entity.ticksLeft);
                entity.ticksLeft -= ticksConsumed;
                consumableTicks -= ticksConsumed;
                entity.energy.forceExtractEnergy((int) (rateConsumption * ticksConsumed), false);
                if (recipe != null && entity.ticksLeft <= 0) {
                    // Produce the result.
                    entity.onRecipeCompleted(recipe);
                    entity.attemptToEject();
                    entity.attemptToPull();
                    entity.totalTicks = recipe.getTicks();
                    entity.ticksLeft = entity.totalTicks;
                }
            } else {
                if (entity.ticksLeft < entity.totalTicks) entity.ticksLeft += entity.getTickMultiplier();
                break;
            }
        }
        if (state.getValue(LIT) != entity.canProcess()) {
            level.setBlock(pos, state.setValue(LIT, entity.canProcess()), 1 | 2);
        }
    }

    public static <T extends AbstractConcoctiMachineBlockEntity<T, M, V, I, R>,
            M extends AbstractContainerMenu, V, I extends RecipeInput, R extends ProcessingRecipe<R, I>>
        void serverTick(Level level, BlockPos pos, BlockState state, AbstractConcoctiMachineBlockEntity<T, M, V, I, R> entity) {
        entity.tick(level, pos, state);
    }

    /**
     * Loads the last recipe ID, ticks left and total ticks for this block entity, along with items.
     * <p> This method should call {@code super.loadAdditional()} at the beginning and load everything else
     * specific to this block entity.
     */
    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.ticksLeft = tag.getInt("ticks_left");
        if (tag.contains("last_recipe_id", Tag.STRING_SIZE)) {
            this.lastRecipeId = ResourceLocation.tryParse(tag.getString("last_recipe_id"));
        } else this.lastRecipeId = null;
        // Fill in the total ticks.
        this.totalTicks = tag.getInt("total_ticks");
        this.machineSettings.slots = MachineSettingsSlots.CODEC
                .parse(NbtOps.INSTANCE, tag.get("machine_settings_slots"))
                .getOrThrow();
        this.ejectOn = tag.contains("eject_on") && tag.getBoolean("eject_on");
        this.pullOn = tag.contains("pull_on") && tag.getBoolean("pull_on");
    }

    /**
     * Saves the last recipe ID, ticks left and total ticks for this block entity, along with items.
     * <p> This method should call {@code super.saveAdditional()} at the beginning and save everything else
     * specific to this block entity.
     */
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("ticks_left", this.ticksLeft);
        // Fetch the appropriate item ID.
        if (this.lastRecipeId != null) tag.putString("last_recipe_id", this.lastRecipeId.toString());
        tag.putInt("total_ticks", this.totalTicks);
        tag.put("machine_settings_slots",
                MachineSettingsSlots.CODEC.encodeStart(NbtOps.INSTANCE, machineSettings.slots).getOrThrow()
        );
        tag.putBoolean("eject_on", this.ejectOn);
        tag.putBoolean("pull_on", this.pullOn);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return isItemValid(index, stack);
    }

    @Override
    public boolean canTakeItem(@NotNull Container target, int index, @NotNull ItemStack stack) {
        return isItemValid(index, stack);
    }

    public List<Integer> getItemSlots() {
        ArrayList<Integer> arrayList = new ArrayList<>(slotSize);
        for (int i = 0; i < slotSize; i++) arrayList.add(i);
        return arrayList;
    }

    @Contract(pure = true)
    public List<Integer> getItemSlots(SlotType type) {
        return getItemSlotsMap().getOrDefault(type, List.of());
    }

    @Contract(pure = true)
    public EnumMap<SlotType, List<Integer>> getItemSlotsMap() {
        return getMachineDetails().itemSlotsMap();
    }

    @Contract(pure = true)
    public List<IFluidTank> getFluidTanks(SlotType type) {
        Set<IFluidTank> tanks = new HashSet<>();
        for (Supplier<IFluidTank> listedTanks : getFluidTanksMap().getOrDefault(type, List.of())) {
            tanks.add(listedTanks.get());
        }
        return tanks.stream().toList();
    }


    public EnumMap<SlotType, List<Supplier<IFluidTank>>> getFluidTanksMap() {
        return getMachineDetails().fluidSlotsMap();
    }

    abstract public List<IFluidTank> getFluidTanks();

    protected FluidStack parseFluidStack(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        if (tag != null)
            return FluidStack.parseOptional(registries, tag);
        else
            return FluidStack.EMPTY.copy();
    }

    protected void saveFluidStack(String key, @NotNull CompoundTag tag, FluidTank tank, HolderLookup.@NotNull Provider registries) {
        if (tank.isEmpty()) return;
        tag.put(key, tank.getFluid().save(registries));
    }
}
