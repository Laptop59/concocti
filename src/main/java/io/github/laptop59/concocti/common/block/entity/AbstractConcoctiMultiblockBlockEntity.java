package io.github.laptop59.concocti.common.block.entity;

import io.github.laptop59.concocti.common.block.ConcoctiHatchBlock;
import io.github.laptop59.concocti.common.block.Hatch;
import io.github.laptop59.concocti.common.block.HatchPurpose;
import io.github.laptop59.concocti.common.block.HatchType;
import io.github.laptop59.concocti.common.block.frame.FrameAttributes;
import io.github.laptop59.concocti.common.detail.DetailContext;
import io.github.laptop59.concocti.common.detail.DetailHolder;
import io.github.laptop59.concocti.common.detail.DetailHolders;
import io.github.laptop59.concocti.common.detail.Details;
import io.github.laptop59.concocti.common.energy.MergedEnergyStorage;
import io.github.laptop59.concocti.common.energy.ViewOnlyEnergyStorage;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluidTank;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluidTankHandler;
import io.github.laptop59.concocti.common.fluid.MergedViewOnlyFluidHandler;
import io.github.laptop59.concocti.common.fluid.ViewOnlyFluidHandler;
import io.github.laptop59.concocti.common.item.MergedItemHandler;
import io.github.laptop59.concocti.common.item.ViewOnlyItemHandler;
import io.github.laptop59.concocti.common.machine.ConcoctiMultiBlockMachine;
import io.github.laptop59.concocti.common.machine.FluidTankHolder;
import io.github.laptop59.concocti.common.machine.ItemsFluidsInputValue;
import io.github.laptop59.concocti.common.machine.SettingsHolder;
import io.github.laptop59.concocti.common.menu.ConcoctiFrameSlot;
import io.github.laptop59.concocti.common.menu.ConcoctiMultiblockMenu;
import io.github.laptop59.concocti.common.menu.ConcoctiUpgradeSlot;
import io.github.laptop59.concocti.common.multiblock.MultiblockBlockPredicate;
import io.github.laptop59.concocti.common.multiblock.MultiblockHatchAllowedPredicate;
import io.github.laptop59.concocti.common.multiblock.MultiblockResult;
import io.github.laptop59.concocti.common.multiblock.MultiblockStructure;
import io.github.laptop59.concocti.common.recipe.AbstractConcoctiMultiblockRecipe;
import io.github.laptop59.concocti.common.recipe.FluidOutput;
import io.github.laptop59.concocti.common.recipe.ItemOutput;
import io.github.laptop59.concocti.common.recipe.ItemsFluidsRecipeInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import static io.github.laptop59.concocti.common.block.AbstractConcoctiMachineBlock.LIT;

/**
 * A class that serves as a base for Concocti Machines. <p>
 * Any {@link ContainerData} will have to be stored by the extending classes.
 *
 * @param <T> The type of block entity (should be itself.)
 * @param <R> The recipe type of this block entity.
 */
public abstract class AbstractConcoctiMultiblockBlockEntity
        <T extends AbstractConcoctiMultiblockBlockEntity<T, R>, R extends AbstractConcoctiMultiblockRecipe<R>>
        extends AbstractConcoctiMachineOnlyItemsFluidsBlockEntity<T, ConcoctiMultiblockMenu, R>
        implements Details, SettingsHolder, FluidTankHolder {
    public int autoCooldown = -100000000;
    public static final int AUTO_COOLDOWN = 20;
    public boolean valid = false;
    public boolean buildPreview = false;
    public int ticksProcessed = 0;

    public DetailHolders detailHolders = new DetailHolders();
    public MultiblockStructure structure;
    public Map<BlockPos, MultiblockResult> unmatchingBlockStates = null;
    public Map<Hatch, ArrayList<BlockPos>> hatchPositionsMap = new HashMap<>();
    public ListTag hatchPositionsMapListTag = new ListTag();

    // Slots
    public static final int UPGRADE_SLOT = 0;
    public static final int FRAME_SLOT = 1;

    public IItemHandler itemStackInputHandler = null;
    public IFluidHandler fluidStackInputHandler = null;
    public IEnergyStorage energyInputStorage = null;
    public IItemHandler itemStackOutputHandler = null;
    public IFluidHandler fluidStackOutputHandler = null;
    public IEnergyStorage energyOutputStorage = null;

    /**
     * Tells whether an item is valid in a specific tank index.
     *
     * @param slot  The tank index.
     * @param stack The item stack.
     * @return The item's validity.
     */
    abstract protected boolean isItemValidInMachine(int slot, @NotNull ItemStack stack);

    public AbstractConcoctiMultiblockBlockEntity(Supplier<BlockEntityType<T>> blockEntityType, BlockPos pos, BlockState blockState, Object... extraData) {
        super(
                blockEntityType,
                pos,
                blockState,
                extraData
        );
    }

    /**
     * Creates a menu for this block entity.
     */
    @Override
    protected @NotNull ConcoctiMultiblockMenu createMenu(int containerId, @NotNull Inventory inventory) {
        return new ConcoctiMultiblockMenu(getMachineInstance().MENU, containerId, inventory, this);
    }

    /**
     * Gives the amount of energy (in FE) that this block entity consumes per tick.
     */
    protected int getTickEnergyIntake() {
        return (int) (getTickMultiplier() * rateConsumption);
    }

    /**
     * Gives the amount of energy (in FE) that this block entity consumes per tick.
     */
    protected float getEnergyMultiplier() {
        return (float) (getTickEnergyIntake()) * (1 - getEfficiency()) / rateConsumption;
    }

    /**
     * Gives the amount of energy (in FE) that this block entity consumes per tick without considering efficiency.
     */
    protected float getInefficientEnergyMultiplier() {
        return (float) (getTickEnergyIntake()) / rateConsumption;
    }

    /**
     * Gives the tick process multiplier (tells how must faster a recipe is for this block entity).
     */
    protected int getTickMultiplier() {
        return (int) (getTickMultiplier(ConcoctiUpgradeSlot.getUpgradeUnits(getItem(UPGRADE_SLOT))) * getFrameMultiplier());
    }

    /**
     * Gives the tick process multiplier (tells how must faster a recipe is for this block entity) from upgrade units.
     */
    public static int getTickMultiplier(int upgradeUnits) {
        double ticks = Math.pow(1.2, upgradeUnits);
        return (int) ticks + upgradeUnits;
    }

    /**
     * Gives the multiplier caused by an upgradable frame in this machine.
     */
    protected float getFrameMultiplier() {
        Optional<FrameAttributes> optionalFrameAttributes = ConcoctiFrameSlot.getFrameAttributes(getItem(FRAME_SLOT).getItem());
        if (optionalFrameAttributes.isPresent()) {
            FrameAttributes attributes = optionalFrameAttributes.get();
            return attributes.rate();
        }
        return 1.0f;
    }

    /**
     * Gives the energy efficiency caused by an upgradable frame in this machine.
     */
    protected float getEfficiency() {
        Optional<FrameAttributes> optionalFrameAttributes = ConcoctiFrameSlot.getFrameAttributes(getItem(FRAME_SLOT).getItem());
        if (optionalFrameAttributes.isPresent()) {
            FrameAttributes attributes = optionalFrameAttributes.get();
            return attributes.efficiency();
        }
        return 0.0f;
    }

    /**
     * Gets the energy storage from a particular direction.
     */
    @Override
    public @Nullable IEnergyStorage getSidedEnergyStorage(Direction direction) {
        return new ViewOnlyEnergyStorage(() -> new MergedEnergyStorage(valid ? List.of(energyInputStorage, energyOutputStorage) : List.of()));
    }

    /**
     * Gets the item handler from a particular direction.
     */
    @Override
    public @Nullable IItemHandler getSidedItemHandler(Direction direction) {
        return new ViewOnlyItemHandler(() -> new MergedItemHandler(valid ? List.of(itemStackInputHandler, itemStackOutputHandler) : List.of()));
    }

    /**
     * Gets the fluid handler from a particular direction.
     */
    @Override
    public @Nullable IFluidHandler getSidedFluidHandler(Direction direction) {
        return new ViewOnlyFluidHandler(() -> new MergedViewOnlyFluidHandler(valid ? List.of(fluidStackInputHandler, fluidStackOutputHandler) : List.of()));
    }

    /**
     * Gives this block entity's recipe type.
     */
    public abstract Supplier<RecipeType<R>> getRecipeType();

    /**
     * Tells whether this block entity can process an input. This should check for all conditions. <p>
     * This default implementation only includes general, recipe-specific checks,
     * and does not include checks for overflowing. Use {@code super.canProcess()} to handle
     * everything this implementation does.
     */
    public boolean canProcess() {
        if (!valid) return false;
        // Check if enough energy is left.
        int fakeExtracted = energyInputStorage.extractEnergy(getTickEnergyIntake(), true);
        int required = getTickEnergyIntake();
        if (fakeExtracted < required) return false;
        // Query the recipe.
        ItemsFluidsInputValue input = getInput();
        RecipeHolder<R> recipe = getRecipe(input);
        if (recipe == null) return false;
        if (!canInsertOutputsSeparately(recipe.value())) return false;
        return lastRecipe == null || lastRecipe == recipe;
    }

    public void updateHatchPositions() {
        this.hatchPositionsMap = new HashMap<>();

        for (HatchType type : HatchType.values())
            for (HatchPurpose purpose : HatchPurpose.values())
                hatchPositionsMap.put(new Hatch(type, purpose), new ArrayList<>());

        BlockPos blockPos = getBlockPos();
        Direction direction = getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        for (int dz = structure.zStart(); dz < structure.zEnd(); dz++)
            for (int dy = structure.yStart(); dy < structure.yEnd(); dy++)
                for (int dx = structure.xStart(); dx < structure.xEnd(); dx++) {
                    BlockPos rotatedRelativePos = MultiblockStructure.rotateAccordingToNorth(new BlockPos(dx, dy, dz), direction);
                    BlockPos hatchBlockPos = blockPos.offset(rotatedRelativePos);
                    MultiblockBlockPredicate predicate = structure.at(dx, dy, dz);
                    if (predicate instanceof MultiblockHatchAllowedPredicate predicate1) {
                        for (Hatch hatch : predicate1.getAllowed())
                            hatchPositionsMap.get(hatch).add(hatchBlockPos);
                    }
                }

    }

    public void updateHatchPositionsToNbt() {
        hatchPositionsMapListTag = new ListTag();
        for (Map.Entry<Hatch, ArrayList<BlockPos>> entry : this.hatchPositionsMap.entrySet()) {
            CompoundTag compoundTag = new CompoundTag();
            HatchType hatchType = entry.getKey().type();
            HatchPurpose hatchPurpose = entry.getKey().purpose();
            compoundTag.putString("hatch_type", hatchType.getId());
            compoundTag.putString("hatch_purpose", hatchPurpose.getId());
            ListTag positions = new ListTag();
            for (BlockPos pos : entry.getValue())
                positions.add(NbtUtils.writeBlockPos(pos));
            compoundTag.put("positions", positions);

            hatchPositionsMapListTag.add(compoundTag);
        };
    }

    public void loadHatchPositionsFromNbt() {
        for (Tag entry : hatchPositionsMapListTag) {
            if (entry instanceof CompoundTag tag) {
                HatchType hatchType = HatchType.of(tag.getString("hatch_type"));
                HatchPurpose hatchPurpose = HatchPurpose.of(tag.getString("hatch_purpose"));
                Hatch hatch = new Hatch(hatchType, hatchPurpose);
                ListTag positions = tag.getList("positions", Tag.TAG_INT_ARRAY);
                ArrayList<BlockPos> positionsList = new ArrayList<>();
                for (int i = 0; i < positions.size(); i++) {
                    int[] blockPosTag = positions.getIntArray(i);
                    positionsList.add(new BlockPos(blockPosTag[0], blockPosTag[1], blockPosTag[2]));
                }
                hatchPositionsMap.put(hatch, positionsList);
            }
        }
    }

    @Override
    protected ItemsFluidsInputValue getInput() {
        return new ItemsFluidsInputValue(itemStackInputHandler, fluidStackInputHandler);
    }

    @Override
    protected abstract @NotNull Component getDefaultName();

    protected boolean canInsertOutputsSeparately(R recipe) {
        for (ItemOutput result : recipe.getOutputItems())
            if (!ItemHandlerHelper.insertItem(itemStackOutputHandler, result.stack().copy(), true).isEmpty()) return false;
        for (FluidOutput result : recipe.getOutputFluids())
            if (fluidStackOutputHandler.fill(result.stack(), IFluidHandler.FluidAction.SIMULATE) < result.stack().getAmount()) return false;
        return true;
    }

    /**
     * Called when a recipe has finished. This should account for any products being made.
     *
     * @param recipe The recipe that has completed.
     */
    protected void onRecipeCompleted(R recipe) {
        // Check for state again.
        updateMultiblockState();
        if (!valid) return;
        ItemsFluidsInputValue input = getInput();
        ItemsFluidsRecipeInput recipeInput = recipeInputFrom(input);
        recipeInput.consume(recipe.getInputItems(), recipe.getInputFluids());
        // Now add the finished products.
        for (ItemOutput result : recipe.getOutputItems())
            if (result.roll()) ItemHandlerHelper.insertItem(itemStackOutputHandler, result.stack().copy(), false);
        for (FluidOutput result : recipe.getOutputFluids())
            if (result.roll()) fluidStackOutputHandler.fill(result.stack(), IFluidHandler.FluidAction.EXECUTE);
    }

    /**
     * Gets all the separate handlers of fluid stacks of this machine, which are indexed consistently.
     */
    public List<ConcoctiFluidTank> getIndexedFluidHandlers() {
        return List.of();
    }

    protected void nullifyHandlers() {
        itemStackInputHandler = null;
        fluidStackInputHandler = null;
        energyInputStorage = null;
        itemStackOutputHandler = null;
        fluidStackOutputHandler = null;
        energyOutputStorage = null;
        invalidateCapabilities();
    }

    public void updateMultiblockState() {
        Direction direction = getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        valid = structure.match(getLevel(), getBlockPos(), direction);
        if (!valid) {
            ticksLeft = totalTicks;
            lastRecipe = null;
            unmatchingBlockStates = structure.getUnmatched(getLevel(), getBlockPos(), direction);

            nullifyHandlers();
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            return;
        }
        unmatchingBlockStates = null;
        // Create the handlers.
        Map<BlockPos, Object> dataMap = structure.getExtraData(getLevel(), getBlockPos(), direction);
        List<IItemHandler> itemInputHandlers = new ArrayList<>();
        List<IFluidTank> fluidInputTanks = new ArrayList<>();
        List<IEnergyStorage> energyInputStorages = new ArrayList<>();
        List<IItemHandler> itemOutputHandlers = new ArrayList<>();
        List<IFluidTank> fluidOutputTanks = new ArrayList<>();
        List<IEnergyStorage> energyOutputStorages = new ArrayList<>();
        for (Map.Entry<BlockPos, Object> entry : dataMap.entrySet()) {
            // BlockPos pos = entry.getKey();
            Object data = entry.getValue();
            if (data instanceof MultiblockHatchAllowedPredicate.Data(
                    ConcoctiHatchBlockEntity entity, Supplier<? extends ConcoctiHatchBlock>[] allowedHatches
            )) {
                // Get the entity.
                ConcoctiHatchBlock block = entity.getBlock();
                HatchType type = block.getType();
                HatchPurpose purpose = block.getPurpose();
                switch (type) {
                    case ITEM -> {
                        if (purpose == HatchPurpose.INPUT)
                            itemInputHandlers.add(entity.inputItemHandler.get());
                        else if (purpose == HatchPurpose.OUTPUT)
                            itemOutputHandlers.add(entity.outputItemHandler.get());
                    }
                    case FLUID -> {
                        if (purpose == HatchPurpose.INPUT)
                            fluidInputTanks.add(entity.getFluidTank());
                        else if (purpose == HatchPurpose.OUTPUT)
                            fluidOutputTanks.add(entity.getFluidTank());
                    }
                    case ENERGY -> {
                        if (purpose == HatchPurpose.INPUT)
                            energyInputStorages.add(entity.energy);
                        else if (purpose == HatchPurpose.OUTPUT)
                            energyOutputStorages.add(entity.energy);
                    }
                }
            }
        }

        ConcoctiFluidTankHandler fluidInputHandler = new ConcoctiFluidTankHandler(() -> fluidInputTanks);
        ConcoctiFluidTankHandler fluidOutputHandler = new ConcoctiFluidTankHandler(() -> fluidOutputTanks);
        MergedItemHandler itemInputHandler = new MergedItemHandler(itemInputHandlers);
        MergedItemHandler itemOutputHandler = new MergedItemHandler(itemOutputHandlers);
        MergedEnergyStorage energyInputStorageLocal = new MergedEnergyStorage(energyInputStorages);
        MergedEnergyStorage energyOutputStorageLocal = new MergedEnergyStorage(energyOutputStorages);

        itemStackInputHandler = itemInputHandler;
        itemStackOutputHandler = itemOutputHandler;
        fluidStackInputHandler = fluidInputHandler;
        fluidStackOutputHandler = fluidOutputHandler;
        energyInputStorage = energyInputStorageLocal;
        energyOutputStorage = energyOutputStorageLocal;

        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        invalidateCapabilities();
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    /**
     * A basic implementation of a Concocti Machine's server tick.
     */
    protected void tickMachineSpecific(Level level, BlockPos pos, BlockState state) {
        ticksProcessed = 0;

        AbstractConcoctiMultiblockBlockEntity<T, R> entity = this;
        if (entity.ticksLeft >= entity.totalTicks) entity.lastRecipe = null;
        int consumableTicks = getTickMultiplier();
        // int subticks = 0;
        autoCooldown--;
        if (autoCooldown <= 0) {
            autoCooldown = AUTO_COOLDOWN;
            updateMultiblockState();
        }
        while (consumableTicks > 0) {
            if (entity.canProcess()) {
                ItemsFluidsInputValue input = entity.getInput();
                RecipeHolder<R> recipe = entity.getRecipe(input);
                if (recipe != null && (entity.lastRecipe == null || !entity.lastRecipe.equals(recipe))) {
                    entity.lastRecipe = recipe;
                    entity.totalTicks = recipe.value().getTicks();
                    entity.ticksLeft = entity.totalTicks;
                }
                int ticksConsumed = Math.min(consumableTicks, entity.ticksLeft);
                entity.ticksLeft -= ticksConsumed;
                consumableTicks -= ticksConsumed;
                ticksProcessed += ticksConsumed;
                entity.energyInputStorage.extractEnergy((int) (rateConsumption * ticksConsumed), false);
                if (recipe != null && entity.ticksLeft <= 0) {
                    // Produce the result.
                    entity.onRecipeCompleted(recipe.value());
                    // subticks++;
                    entity.totalTicks = recipe.value().getTicks();
                    entity.ticksLeft += entity.totalTicks;
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

    /**
     * Loads the last recipe ID, ticks left and total ticks for this block entity, along with items.
     * <p> This method should call {@code super.loadAdditional()} at the beginning and load everything else
     * specific to this block entity.
     */
    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.ticksLeft = tag.getInt("ticks_left");
        this.lastRecipe = null;
        if (tag.contains("last_recipe_id", Tag.STRING_SIZE)) {
            ResourceLocation id = ResourceLocation.tryParse(tag.getString("last_recipe_id"));
            if (id != null) {
                Recipe<?> ungenericRecipe = getLevel()
                        .getRecipeManager()
                        .byKey(id)
                        .map(RecipeHolder::value)
                        .orElse(null);
                Class<R> recipeClass = getRecipeClass();
                if (recipeClass.isInstance(ungenericRecipe)) {
                    this.lastRecipe = new RecipeHolder<>(
                            id,
                            recipeClass.cast(ungenericRecipe)
                    );
                }
            }
        } else this.lastRecipe = null;
        // Fill in the total ticks.
        this.totalTicks = tag.getInt("total_ticks");
        this.buildPreview = tag.contains("build_preview") && tag.getBoolean("build_preview");

        deserialize(new DetailContext(tag, registries, null));
    }

    /** Get the recipe class of this multiblock. */
    protected abstract Class<R> getRecipeClass();

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
        if (this.lastRecipe != null) tag.putString("last_recipe_id", this.lastRecipe.toString());
        tag.putInt("total_ticks", this.totalTicks);
        tag.putBoolean("build_preview", this.buildPreview);
        serialize(new DetailContext(tag, registries, null));
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        super.getUpdateTag(registries);
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        ListTag listTagKeys = new ListTag();
        ListTag listTagValues = new ListTag();
        byte[] listTagInsteadWasAirBytes = new byte[0];
        if (unmatchingBlockStates != null) {
            listTagInsteadWasAirBytes = new byte[unmatchingBlockStates.size()];
            int i = 0;
            for (Map.Entry<BlockPos, MultiblockResult> entry : unmatchingBlockStates.entrySet()) {
                listTagKeys.add(NbtUtils.writeBlockPos(entry.getKey()));
                listTagValues.add(NbtUtils.writeBlockState(entry.getValue().blockState()));
                listTagInsteadWasAirBytes[i] = entry.getValue().insteadWasAir() ? (byte) 1 : (byte) 0;

                i++;
            }
        }
        tag.put("unmatched_block_states_keys", listTagKeys);
        tag.put("unmatched_block_states_values", listTagValues);
        tag.put("unmatched_block_states_instead_was_air", new ByteArrayTag(listTagInsteadWasAirBytes));

        updateHatchPositions();
        updateHatchPositionsToNbt();
        tag.put("allowed_hatch_positions", hatchPositionsMapListTag);
        return tag;
    }

    // Return our packet here. This method returning a non-null result tells the game to use this packet for syncing.
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // The packet uses the CompoundTag returned by #getUpdateTag. An alternative overload of #create exists
        // that allows you to specify a custom update tag, including the ability to omit data the client might not need.
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // Optionally: Run some custom logic when the packet is received.
    // The super/default implementation forwards to #loadAdditional.
    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
        super.onDataPacket(connection, packet, registries);
        // Do whatever you need to do here.
        CompoundTag tag = packet.getTag();
        if (tag.contains("unmatched_block_states_keys") && tag.contains("unmatched_block_states_values")) {
            ListTag listTagKeys = tag.getList("unmatched_block_states_keys", Tag.TAG_INT_ARRAY);
            ListTag listTagValues = tag.getList("unmatched_block_states_values", Tag.TAG_COMPOUND);
            byte[] listTagInsteadWasAirBytes = tag.getByteArray("unmatched_block_states_instead_was_air");
            Map<BlockPos, MultiblockResult> map = new HashMap<>();
            for (int i = 0; i < listTagKeys.size(); i++) {
                int[] blockPosTag = listTagKeys.getIntArray(i);
                CompoundTag blockStateTag = listTagValues.getCompound(i);
                BlockPos blockPos = new BlockPos(blockPosTag[0], blockPosTag[1], blockPosTag[2]);
                BlockState blockState = NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), blockStateTag);
                boolean insteadWasAir = listTagInsteadWasAirBytes[i] != (byte) 0;
                map.put(blockPos, new MultiblockResult(blockState, insteadWasAir));
            }
            this.unmatchingBlockStates = map;
        }
        if (tag.contains("allowed_hatch_positions")) {
            hatchPositionsMapListTag = tag.getList("allowed_hatch_positions", Tag.TAG_COMPOUND);
            loadHatchPositionsFromNbt();
        }
    }

    @Override
    public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
        return isItemValid(index, stack);
    }

    @Override
    public boolean canTakeItem(@NotNull Container target, int index, @NotNull ItemStack stack) {
        return isItemValid(index, stack);
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

    public void changeBuildPreview() {
        buildPreview = !buildPreview;
        updateMultiblockState();
    }

    @Override
    public Object getExtraData() {
        return new ConcoctiMultiBlockMachine.Extra(valid, buildPreview);
    }
}
