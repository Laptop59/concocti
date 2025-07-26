package io.github.laptop59.concocti.common.block.entity;

import io.github.laptop59.concocti.client.gui.components.MachineSettings;
import io.github.laptop59.concocti.client.gui.components.MachineSettingsSlots;
import io.github.laptop59.concocti.client.gui.components.SlotFlag;
import io.github.laptop59.concocti.client.gui.components.SlotType;
import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.abstraction.Complexion;
import io.github.laptop59.concocti.common.abstraction.Properties;
import io.github.laptop59.concocti.common.abstraction.Property;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.ConcoctiHatchBlock;
import io.github.laptop59.concocti.common.detail.*;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluidTankHandler;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluidTankSlotTypedHandler;
import io.github.laptop59.concocti.common.machine.FluidTankHolder;
import io.github.laptop59.concocti.common.machine.SettingsHolder;
import io.github.laptop59.concocti.common.menu.ConcoctiEnergyHatchMenu;
import io.github.laptop59.concocti.common.menu.ConcoctiFluidHatchMenu;
import io.github.laptop59.concocti.common.menu.ConcoctiItemHatchMenu;
import io.github.laptop59.concocti.common.util.Lazy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
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

import java.util.*;

public class ConcoctiHatchBlockEntity extends AbstractPoweredBlockEntity implements ItemHandlerBlockEntity, FluidHandlerBlockEntity, Details, SettingsHolder, FluidTankHolder {
    public boolean ejectOn;
    public boolean pullOn;
    public ConcoctiFluidTankHandler fluidHandler;

    public int autoCooldown = 0;

    public DetailHolders detailHolders = new DetailHolders();
    public final MachineSettings machineSettings = new MachineSettings(List.of(SlotType.NONE));

    public static final int AUTO_COOLDOWN = 5;
    public static final int TANK_CAPACITY = 256_000;
    public static final int ENERGY_CAPACITY = 10_000_000;

    // Details
    private final DetailHolder<FluidTank> fluidTank = new DetailHolder<>(
            DetailCodec.FLUID_TANK, "fluid_tank", new FluidTank(0), this
    );

    // Properties
    public final Property<Boolean> EJECT_ON =
            Properties.EJECT_ON.newWithLinker(() -> ejectOn);
    public final Property<Boolean> PULL_ON =
            Properties.PULL_ON.newWithLinker(() -> pullOn);
    public final Property<MachineSettingsSlots> MACHINE_SETTINGS_SLOTS =
            Properties.MACHINE_SETTINGS_SLOTS.newWithLinker(() -> machineSettings.slots);
    public final Property<Integer> ENERGY_STORED =
            Properties.ENERGY_STORED.newWithLinker(() -> energy.getEnergyStored());
    public final Property<Integer> MAX_ENERGY_STORED =
            Properties.MAX_ENERGY_STORED.newWithLinker(() -> energy.getMaxEnergyStored());
    public final Property<FluidStack> FLUID_TANK = Properties.PURE_FLUID_OUTPUT.newWithLinker(() -> fluidTank.get().getFluid());

    protected final Lazy<IItemHandler> inputItemHandler;
    protected final Lazy<IFluidHandler> inputFluidHandler;
    protected final Lazy<IItemHandler> outputItemHandler;
    protected final Lazy<IFluidHandler> outputFluidHandler;

    protected final Complexion dataAccess = new Complexion(
            EJECT_ON.of(false),
            PULL_ON.of(false),
            MACHINE_SETTINGS_SLOTS.of(new MachineSettingsSlots()),
            ENERGY_STORED.of(0),
            MAX_ENERGY_STORED.of(0),
            FLUID_TANK.of(new FluidStack(Fluids.EMPTY, 0))
    );

    /**
     * Gets the item handler from a particular direction.
     */
    public @Nullable IItemHandler getSidedItemHandler(Direction direction) {
        SlotType slotType = machineSettings.getSlot(direction);
        if (slotType != null && !slotType.isSet(SlotFlag.ITEM)) return null;
        List<Integer> slots = getItemSlots();
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
            public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                if (slots.contains(slot) && (slotType == null || slotType.isSet(SlotFlag.INPUT)))
                    return itemHandler.insertItem(slot, stack, simulate);
                return stack;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slots.contains(slot) && (slotType == null || slotType.isSet(SlotFlag.OUTPUT)))
                    return itemHandler.extractItem(slot, amount, simulate);
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

    /**
     * Negates whether eject is on and returns the new value.
     */
    public boolean changeEjectOn() {
        ejectOn = !ejectOn;
        attemptToEject();
        return ejectOn;
    }

    /**
     * Negates whether pull is on and returns the new value.
     */
    public boolean changePullOn() {
        pullOn = !pullOn;
        attemptToPull();
        return pullOn;
    }

    /**
     * Attempts to eject output items, fluids and energy. However, this is a no-op if eject is not enabled.
     */
    public void attemptToEject() {
        if (!ejectOn) return;
        attemptToEjectItems();
        attemptToEjectFluids();
        attemptToEjectEnergy();
    }

    /**
     * Attempts to pull input items and fluids. However, this is a no-op if pull is not enabled.
     */
    public void attemptToPull() {
        if (!pullOn) return;
        attemptToPullItems();
        attemptToPullFluids();
    }

    /**
     * Attempts to eject output items. However, this is <b>NOT</b> a no-op if pull is not enabled - it doesn't care whether pull is on or off.
     */
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

    /**
     * Attempts to eject output fluids. However, this is <b>NOT</b> a no-op if eject is not enabled - it doesn't care whether eject is on or off.
     */
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

    /**
     * Attempts to eject output energy (if applicable). However, this is <b>NOT</b> a no-op if eject is not enabled - it doesn't care whether eject is on or off.
     */
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

    /**
     * Attempts to pull input items. However, this is <b>NOT</b> a no-op if pull is not enabled - it doesn't care whether pull is on or off.
     */
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

    /**
     * Attempts to pull input fluids. However, this is <b>NOT</b> a no-op if pull is not enabled - it doesn't care whether pull is on or off.
     */
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
     *
     * @param from Handler to take items from.
     * @param to   Handler to put items to.
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
     *
     * @param from Handler to take fluids from.
     * @param to   Handler to put fluids to.
     */
    protected static void transfer(@NotNull IFluidHandler from, @NotNull IFluidHandler to) {
        int iterations = 0;
        final int MAX_FLUID_ITERATIONS = 8192;
        while (!transfer(from, to, Integer.MAX_VALUE, false).isEmpty()) {
            if (++iterations > MAX_FLUID_ITERATIONS) {
                Concocti.LOGGER.warn("Iterating fluid transfer took more than {} iterations!", MAX_FLUID_ITERATIONS);
                break;
            }
        }
    }

    /**
     * Transfer energy from one storage to another.
     *
     * @param from Storage to extract energy from.
     * @param to   Storage to insert energy to.
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
     *
     * @param from      Handler to take fluids from.
     * @param to        Handler to put fluids to.
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

    /**
     * Gets the fluid handler from a particular direction.
     */
    public @Nullable IFluidHandler getSidedFluidHandler(Direction direction) {
        SlotType slotType = machineSettings.getSlot(direction);
        if (slotType != null && !slotType.isSet(SlotFlag.FLUID)) return null;
        IFluidTank tank = getFluidTank();
        return new ConcoctiFluidTankSlotTypedHandler(() -> List.of(tank), slotType);
    }

    @Override
    protected final boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return true;
    }

    public ConcoctiHatchBlockEntity(BlockPos pos, BlockState blockState) {
        super(
                ConcoctiBlocks.HATCH_BLOCK_ENTITY.get(),
                pos,
                blockState,
                0,
                0,
                0,
                () -> DynamicEnergyStorage.Mode.NONE
        );

        ConcoctiHatchBlock block = (ConcoctiHatchBlock) blockState.getBlock();

        ConcoctiHatchBlock.Type type = block.getType();
        ConcoctiHatchBlock.Purpose purpose = block.getPurpose();

        if (type == ConcoctiHatchBlock.Type.ITEM) {
            this.resetItemHandler(9);
        }

        if (type == ConcoctiHatchBlock.Type.FLUID)
            fluidTank.set(new FluidTank(TANK_CAPACITY));

        if (type == ConcoctiHatchBlock.Type.ENERGY) {
            energy.setMaxEnergy(ENERGY_CAPACITY);
            energy.setMaxEnergyTransfer(ENERGY_CAPACITY);
        }

        this.machineSettings.availableTypes = new ArrayList<>(getAllowedSlotTypes());
        this.machineSettings.availableTypes.addFirst(SlotType.NONE);

        DynamicEnergyStorage.Mode mode = DynamicEnergyStorage.Mode.NONE;
        if (type == ConcoctiHatchBlock.Type.ENERGY) {
            if (purpose == ConcoctiHatchBlock.Purpose.INPUT)
                mode = DynamicEnergyStorage.Mode.INPUT_ONLY;
            else if (purpose == ConcoctiHatchBlock.Purpose.OUTPUT)
                mode = DynamicEnergyStorage.Mode.OUTPUT_ONLY;
        }

        this.setEnergyModeSupplier(mode.toSupplier());

        this.fluidHandler = new ConcoctiFluidTankHandler(() -> List.of(fluidTank.get()));

        inputItemHandler = new Lazy<>(() -> itemHandler.whitelistSlots(
                type == ConcoctiHatchBlock.Type.ITEM && purpose == ConcoctiHatchBlock.Purpose.INPUT ?
                        List.of(0, 1, 2, 3, 4, 5, 6, 7, 8) : List.of()
        ));
        inputFluidHandler = new Lazy<>(() -> fluidHandler.whitelistTanks(
                purpose == ConcoctiHatchBlock.Purpose.INPUT ? List.of(fluidTank.get()) : List.of()
        ));
        outputItemHandler = new Lazy<>(() -> itemHandler.whitelistSlots(
                type == ConcoctiHatchBlock.Type.ITEM && purpose == ConcoctiHatchBlock.Purpose.OUTPUT ?
                        List.of(0, 1, 2, 3, 4, 5, 6, 7, 8) : List.of()
        ));
        outputFluidHandler = new Lazy<>(() -> fluidHandler.whitelistTanks(
                purpose == ConcoctiHatchBlock.Purpose.OUTPUT ? List.of(fluidTank.get()) : List.of()
        ));
    }

    @Contract(pure = true)
    protected @NotNull List<SlotType> getAllowedSlotTypes() {
        ConcoctiHatchBlock block = (ConcoctiHatchBlock) getBlockState().getBlock();

        ConcoctiHatchBlock.Type type = block.getType();
        ConcoctiHatchBlock.Purpose purpose = block.getPurpose();

        switch (type) {
            case ITEM -> {
                if (purpose == ConcoctiHatchBlock.Purpose.INPUT)
                    return List.of(SlotType.ITEM_INPUT);
                if (purpose == ConcoctiHatchBlock.Purpose.OUTPUT)
                    return List.of(SlotType.ITEM_OUTPUT);
            }
            case FLUID -> {
                if (purpose == ConcoctiHatchBlock.Purpose.INPUT)
                    return List.of(SlotType.FLUID_INPUT);
                if (purpose == ConcoctiHatchBlock.Purpose.OUTPUT)
                    return List.of(SlotType.FLUID_OUTPUT);
            }
            case ENERGY -> {
                if (purpose == ConcoctiHatchBlock.Purpose.OUTPUT)
                    return List.of(SlotType.ENERGY_OUTPUT);
            }
        }
        return List.of();
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

    /**
     * Creates a menu for this block entity.
     */
    @Override
    protected @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory) {
        return switch (getBlock().getType()) {
            case ITEM -> new ConcoctiItemHatchMenu(containerId, inventory, this, dataAccess);
            case FLUID -> new ConcoctiFluidHatchMenu(containerId, inventory, this, dataAccess);
            case ENERGY -> new ConcoctiEnergyHatchMenu(containerId, inventory, this, dataAccess);
        };
    }

    /**
     * Gets the block of this block entity.
     */
    protected ConcoctiHatchBlock getBlock() {
        return (ConcoctiHatchBlock) getBlockState().getBlock();
    }

    /**
     * A basic implementation of a Concocti Machine's server tick.
     */
    public void tick(Level level, BlockPos pos, BlockState state) {
        ConcoctiHatchBlockEntity entity = this;
        if (--entity.autoCooldown <= 0) {
            entity.autoCooldown = AUTO_COOLDOWN;
            entity.attemptToPull();
            entity.attemptToEject();
        }
    }

    /**
     * Loads the last recipe ID, ticks left and total ticks for this block entity, along with items.
     * <p> This method should call {@code super.loadAdditional()} at the beginning and load everything else
     * specific to this block entity.
     */
    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.machineSettings.slots = MachineSettingsSlots.CODEC
                .parse(NbtOps.INSTANCE, tag.get("machine_settings_slots"))
                .getOrThrow();
        this.ejectOn = tag.contains("eject_on") && tag.getBoolean("eject_on");
        this.pullOn = tag.contains("pull_on") && tag.getBoolean("pull_on");
        deserialize(new DetailContext(tag, registries, null));
    }

    /**
     * Saves the last recipe ID, ticks left and total ticks for this block entity, along with items.
     * <p> This method should call {@code super.saveAdditional()} at the beginning and save everything else
     * specific to this block entity.
     */
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("machine_settings_slots",
                MachineSettingsSlots.CODEC.encodeStart(NbtOps.INSTANCE, machineSettings.slots).getOrThrow()
        );
        tag.putBoolean("eject_on", this.ejectOn);
        tag.putBoolean("pull_on", this.pullOn);
        serialize(new DetailContext(tag, registries, null));
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
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

    public IFluidTank getFluidTank() {
        return fluidTank.get();
    }

    @Override
    public <HT> void add(DetailHolder<HT> holder) {
        detailHolders.add(holder);
    }

    @Override
    public void addAll(List<DetailHolder<?>> holderList) {
        detailHolders.addAll(holderList);
    }

    @Override
    public void serialize(DetailContext context) {
        detailHolders.serialize(context);
    }

    @Override
    public void deserialize(DetailContext context) {
        detailHolders.deserialize(context);
    }

    @Override
    public MachineSettings getMachineSettings() {
        return machineSettings;
    }

    @Override
    public List<IFluidHandler> getIndexedFluidHandlers() {
        return List.of(fluidTank.get());
    }
}
