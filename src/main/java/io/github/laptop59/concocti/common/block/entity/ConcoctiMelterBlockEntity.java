package io.github.laptop59.concocti.common.block.entity;

import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluids;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.menu.ConcoctiMelterMenu;
import io.github.laptop59.concocti.common.menu.ConcoctiUpgradeSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Supplier;

import static io.github.laptop59.concocti.common.block.ConcoctiMelterBlock.LIT;

public class ConcoctiMelterBlockEntity extends AbstractPoweredBlockEntity {
    private static final int INPUT_SLOT = 0;
    private static final int UPGRADE_SLOT = 1;

    int ticksLeft = 0;
    int totalTicks = 0;

    int lastSmeltedItemId = -1;

    private int moltenConcocti = 0;
    private int moltenConcoctizedDirt = 0;

    public final IFluidHandler fluids = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 2;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return switch (tank) {
                case 0 -> new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, moltenConcocti);
                case 1 -> new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTIZED_DIRT, moltenConcoctizedDirt);
                case 2 -> throw new IllegalArgumentException("Expected tank to be 0 or 1, got " + tank + " instead");
                default -> FluidStack.EMPTY;
            };
        }

        @Override
        public int getTankCapacity(int tank) {
            return 8000;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return switch (tank) {
                case 0 -> stack.is(ConcoctiFluids.MOLTEN_CONCOCTI);
                case 1 -> stack.is(ConcoctiFluids.MOLTEN_CONCOCTIZED_DIRT);
                default -> false;
            };
        }

        @Override
        public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) { return 0; }

        @Override
        public @NotNull FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) {
            int amount;
            if (resource.is(ConcoctiFluids.MOLTEN_CONCOCTI)) {
                amount = Math.min(resource.getAmount(), moltenConcocti);
                if (action != FluidAction.SIMULATE) {
                    moltenConcocti -= amount;
                }
            } else if (resource.is(ConcoctiFluids.MOLTEN_CONCOCTIZED_DIRT)) {
                amount = Math.min(resource.getAmount(), moltenConcoctizedDirt);
                if (action != FluidAction.SIMULATE) {
                    moltenConcoctizedDirt -= amount;
                }
            } else {
                return FluidStack.EMPTY;
            }
            return new FluidStack(resource.getFluid(), amount);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
            if (moltenConcocti > 0) {
                int amount = Math.min(maxDrain, moltenConcocti);
                if (action != FluidAction.SIMULATE) moltenConcocti -= amount;
                return new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTI, amount);
            } else {
                int amount = Math.min(maxDrain, moltenConcoctizedDirt);
                if (action != FluidAction.SIMULATE) moltenConcoctizedDirt -= amount;
                return new FluidStack(ConcoctiFluids.MOLTEN_CONCOCTIZED_DIRT, Math.min(maxDrain, moltenConcoctizedDirt));
            }
        }
    };

    @Override
    protected boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return switch (slot) {
            case 0 -> stack.is(ConcoctiItems.Tags.MELTABLE_CONCOCTI_ITEMS);
            case 1 -> stack.is(ConcoctiItems.Tags.CONCOCTI_UPGRADES);
            default -> false;
        };
    }

    record ItemData(int ticks, int moltenConcoctiMade, int moltenConcoctizedDirtMade, int id) { }

    private static final HashMap<Item, ItemData> itemDataMap = new HashMap<>();

    static {
        registerItemData(ConcoctiItems.DIRTY_CONCOCTI_NUGGET, 10, 12, 3);
        registerItemData(ConcoctiItems.DIRTY_CONCOCTI_INGOT, 80, 12 * 9, 3 * 9);
        registerItemData(ConcoctiItems.DIRTY_CONCOCTI_BLOCK, 600, 12 * 81, 3 * 81);

        registerItemData(ConcoctiItems.PURIFIED_CONCOCTI_NUGGET, 10, 15, 0);
        registerItemData(ConcoctiItems.PURIFIED_CONCOCTI_INGOT, 80, 15 * 9, 0);
        registerItemData(ConcoctiItems.PURIFIED_CONCOCTI_BLOCK, 600, 15 * 81, 0);
    }

    protected final ContainerData dataAccess = new ContainerData() {

        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> ticksLeft;
                case 1 -> totalTicks;
                case 2 -> energy.getEnergyStored();
                case 3 -> energy.getMaxEnergyStored();
                case 4 -> moltenConcocti;
                case 5 -> moltenConcoctizedDirt;
                case 6 -> lastSmeltedItemId;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0: ticksLeft = value;
                case 1: totalTicks = value;
                case 2, 3: break;
                case 4: moltenConcocti = value;
                case 5: moltenConcoctizedDirt = value;
                case 6: lastSmeltedItemId = value;
            }
        }

        @Override
        public int getCount() {
            return 7;
        }
    };

    public ConcoctiMelterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ConcoctiBlocks.CONCOCTI_MELTER_BLOCK_ENTITY.get(), pos, blockState, 50000, 10000, 2);
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("block.concocti.concocti_melter");
    }

    @Override
    protected @NotNull NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(@NotNull NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory) {
        return new ConcoctiMelterMenu(containerId, inventory, this, this.dataAccess);
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
        return this.getItem(0);
    }

    private static void registerItemData(Supplier<? extends Item> item, int ticks, int moltenConcoctiMade, int moltenConcoctizedDirtMade) {
        itemDataMap.put(item.get(), new ItemData(ticks, moltenConcoctiMade, moltenConcoctizedDirtMade, itemDataMap.size()));
    }

    private int getTickEnergyIntake() {
        int s = ConcoctiUpgradeSlot.getUpgradeUnits(getItem(1));
        return Math.toIntExact(Math.round(25.0f * Math.pow(1.2f, s)));
    }

    private int getTotalTicksNeeded(int base) {
        return base;
    }

    private int getTickMultiplier() {
        int s = ConcoctiUpgradeSlot.getUpgradeUnits(getItem(1));
        return (int) (Math.clamp(Math.ceil(Math.pow(0.9f, -s)), 1, 19));
    }

    private boolean canMelt() {
        ItemStack stack = this.getInputStack();
        // Check if enough energy is left.
        if (energy.getEnergyStored() < getTickEnergyIntake()) return false;
        // If there is nothing to melt, we cannot even melt!
        if (stack.isEmpty()) return false;
        // Check for a match between the ID of the stack and the last known stack (via an ID).
        ItemData data = itemDataMap.get(stack.getItem());
        if (this.lastSmeltedItemId != -1 && data.id != this.lastSmeltedItemId) return false;
        // Check whether the fluids obtained from this item will not exceed our fluid limit.
        return moltenConcocti + data.moltenConcoctiMade <= 8000 && moltenConcoctizedDirt + data.moltenConcoctizedDirtMade <= 8000;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ConcoctiMelterBlockEntity entity) {
        if (entity.ticksLeft >= entity.totalTicks) entity.lastSmeltedItemId = -1;
        if (entity.canMelt()) {
            ItemData data = itemDataMap.get(entity.getInputStack().getItem());
            if (entity.lastSmeltedItemId != data.id) {
                entity.lastSmeltedItemId = data.id;
                entity.totalTicks = entity.getTotalTicksNeeded(data.ticks);
                entity.ticksLeft = entity.totalTicks;
            }
            entity.ticksLeft -= entity.getTickMultiplier();
            entity.energy.extractEnergy(entity.getTickEnergyIntake(), false);
            if (entity.ticksLeft <= 0) {
                // Produce the fluids.
                entity.getInputStack().shrink(1);
                entity.moltenConcocti += data.moltenConcoctiMade;
                entity.moltenConcoctizedDirt += data.moltenConcoctizedDirtMade;
                entity.totalTicks = entity.getTotalTicksNeeded(data.ticks);
                entity.ticksLeft = entity.totalTicks;
            }
        } else {
            if (entity.ticksLeft < entity.totalTicks) entity.ticksLeft += entity.getTickMultiplier();;
        }
        if (state.getValue(LIT) != entity.canMelt()) {
            level.setBlock(pos, state.setValue(LIT, true), 3);
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.moltenConcocti = tag.getInt("molten_concocti");
        this.moltenConcoctizedDirt = tag.getInt("molten_concoctized_dirt");
        this.ticksLeft = tag.getInt("ticks_left");
        this.lastSmeltedItemId = tag.getInt("last_smelted_item_id");
        // Fill in the total ticks.
        this.totalTicks = 0;
        for (ItemData data : itemDataMap.values()) {
            if (data.id == lastSmeltedItemId) {
                this.totalTicks = data.ticks;
            }
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("molten_concocti", this.moltenConcocti);
        tag.putInt("molten_concoctized_dirt", this.moltenConcoctizedDirt);
        tag.putInt("ticks_left", this.ticksLeft);
        // Fetch the appropriate item ID.
        tag.putInt("last_smelted_item_id", this.lastSmeltedItemId);
    }
}
