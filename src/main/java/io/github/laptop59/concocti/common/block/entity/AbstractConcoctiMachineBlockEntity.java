package io.github.laptop59.concocti.common.block.entity;

import io.github.laptop59.concocti.client.gui.components.MachineSettings;
import io.github.laptop59.concocti.client.gui.components.MachineSettingsComponent;
import io.github.laptop59.concocti.common.block.frame.FrameAttributes;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.menu.ConcoctiFrameSlot;
import io.github.laptop59.concocti.common.menu.ConcoctiUpgradeSlot;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static io.github.laptop59.concocti.common.block.ConcoctiMelterBlock.LIT;

/**
 * A class that serves as a base for Concocti Machines. <p>
 * Any {@link ContainerData} will have to be stored by the extending classes. Indices:<p>
 * {@code 0} - should give {@code ticksLeft}.<p>
 * {@code 1} - should give {@code totalTicks}.
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
        extends AbstractPoweredBlockEntity implements StackedContentsCompatible {
    public static final int UPGRADE_SLOT = 0;
    public static final int FRAME_SLOT = 1;

    ResourceLocation lastRecipeId = null;

    int ticksLeft = 0;
    int totalTicks = 0;
    int lastUpgradeUnits = -1;
    float rateConsumption;

    public final MachineSettings machineSettings;

    @Override
    protected final boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (slot == 0) return stack.is(ConcoctiItems.Tags.CONCOCTI_UPGRADES);
        return isItemValidInMachine(slot, stack);
    }

    /**
     * Tells whether an item is valid in a specific slot index.
     * @param slot The slot index.
     * @param stack The item stack.
     * @return The item's validity.
     */
    abstract protected boolean isItemValidInMachine(int slot, @NotNull ItemStack stack);

    public AbstractConcoctiMachineBlockEntity(BlockPos pos, BlockState blockState, int maxEnergy,
                                              int maxEnergyTransfer, int slotSize, Supplier<BlockEntityType<T>> typeSupplier,
                                              float rateConsumption, MachineSettings machineSettings) {
        super(typeSupplier.get(), pos, blockState, maxEnergy, maxEnergyTransfer, slotSize);
        this.rateConsumption = rateConsumption;
        this.machineSettings = machineSettings;
        if (slotSize < 2) throw new IllegalArgumentException("Expected at least two slots for upgrades and frame.");
    }

    @Override
    public final int getContainerSize() {
        return SIZE;
    }

    @Override
    protected @NotNull NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(@NotNull NonNullList<ItemStack> items) {
        this.items = items;
    }

    /** Creates a menu for this block entity. */
    @Override
    abstract protected @NotNull M createMenu(int containerId, @NotNull Inventory inventory);

    /** Gives the amount of energy (in FE) that this block entity consumes per tick. */
    protected int getTickEnergyIntake() {
        int s = ConcoctiUpgradeSlot.getUpgradeUnits(getItem(UPGRADE_SLOT));
        return Math.toIntExact(Math.round(rateConsumption * (1 - getEfficiency()) * Math.pow(1.2f, s)));
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
        int s = ConcoctiUpgradeSlot.getUpgradeUnits(getItem(UPGRADE_SLOT));
        return (int) (Math.clamp(Math.ceil(Math.pow(0.9f, -s)), 1, 19) * getFrameMultiplier());
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
    public abstract Supplier<RecipeType<R>> getRecipeType();

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
     * Gets only the input handlers of fluid stacks of this machine.
     */
    public List<IFluidHandler> getInputFluidHandlers() {
        return List.of();
    }

    /**
     * Gets only the output handlers of fluid stacks of this machine.
     */
    public List<IFluidHandler> getOutputFluidHandlers() {
        return List.of();
    }

    /**
     * A basic implementation of a Concocti Machine's server tick.
     */
    public static <T extends AbstractConcoctiMachineBlockEntity<T, M, V, I, R>,
            M extends AbstractContainerMenu, V, I extends RecipeInput, R extends ProcessingRecipe<R, I>>
        void serverTick(Level level, BlockPos pos, BlockState state, AbstractConcoctiMachineBlockEntity<T, M, V, I, R> entity) {
        int currentUpgradeUnits = ConcoctiUpgradeSlot.getUpgradeUnits(entity.getItem(UPGRADE_SLOT));
        if (currentUpgradeUnits != entity.lastUpgradeUnits) {
            entity.lastUpgradeUnits = currentUpgradeUnits;
            entity.setNewEnergyMultiplier(entity.getInefficientEnergyMultiplier());
        }
        if (entity.ticksLeft >= entity.totalTicks) entity.lastRecipeId = null;
        if (entity.canProcess()) {
            V input = entity.getInput();
            ResourceLocation toBeProcessed = entity.getRecipeIdFrom(input);
            R recipe = entity.getCurrentRecipe(input);
            if (recipe != null && (entity.lastRecipeId == null || !entity.lastRecipeId.equals(toBeProcessed))) {
                entity.lastRecipeId = toBeProcessed;
                entity.totalTicks = recipe.getTicks();
                entity.ticksLeft = entity.totalTicks;
            }
            entity.ticksLeft -= entity.getTickMultiplier();
            entity.energy.extractEnergy(entity.getTickEnergyIntake(), false);
            if (recipe != null && entity.ticksLeft <= 0) {
                // Produce the result.
                entity.onRecipeCompleted(recipe);
                entity.totalTicks = recipe.getTicks();
                entity.ticksLeft = entity.totalTicks;
            }
        } else {
            if (entity.ticksLeft < entity.totalTicks) entity.ticksLeft += entity.getTickMultiplier();
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
        if (tag.contains("last_recipe_id", Tag.STRING_SIZE)) {
            this.lastRecipeId = ResourceLocation.tryParse(tag.getString("last_recipe_id"));
        } else this.lastRecipeId = null;
        // Fill in the total ticks.
        this.totalTicks = tag.getInt("total_ticks");
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
    }

    @Override
    public void fillStackedContents(@NotNull StackedContents helper) {
        for (ItemStack itemstack : this.items) {
            helper.accountStack(itemstack);
        }
    }

    public abstract IFluidHandler getFluidTank();
}
