package io.github.laptop59.concocti.common.block.entity;

import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluids;
import io.github.laptop59.concocti.common.menu.ConcoctiSolidifierMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConcoctiSolidifierBlockEntity extends AbstractPoweredBlockEntity {
    private static final int OUTPUT_SLOT = 0;
    private static final int MOLD_SLOT = 1;

    int ticksLeft = 0;
    int totalTicks = 0;
    int lastSmeltedItemId = -1;

    public final FluidTank tank = new FluidTank(8000) {

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return stack.is(ConcoctiFluids.SUPPORTED_CONCOCTI_SOLIDIFIER_FLUIDS);
        }
    };

    private int getFluidId() {
        FluidStack stack = tank.getFluid();
        if (stack.is(Fluids.EMPTY)) return 0;
        if (stack.is(Fluids.WATER)) return 1;
        if (stack.is(Fluids.LAVA)) return 2;
        if (stack.is(ConcoctiFluids.MOLTEN_CONCOCTI)) return 3;
        if (stack.is(ConcoctiFluids.MOLTEN_CONCOCTIZED_DIRT)) return 4;

        return -1;
    }

    protected final ContainerData dataAccess = new ContainerData() {

        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> ticksLeft;
                case 1 -> totalTicks;
                case 2 -> energy.getEnergyStored();
                case 3 -> energy.getMaxEnergyStored();
                case 4 -> getFluidId();
                case 5 -> tank.getFluidAmount();
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
        super(ConcoctiBlocks.CONCOCTI_SOLIDIFIER_BLOCK_ENTITY.get(), pos, blockState, 50000, 10000, 3);
    }

    @Override
    protected boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return false;
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        return new int[]{0, 1};
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
    protected @NotNull NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(@NotNull NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory) {
        return new ConcoctiSolidifierMenu(containerId, inventory, this, this.dataAccess);
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.ticksLeft = tag.getInt("ticks_left");
        this.tank.setFluid(tank.readFromNBT(registries, tag).getFluid());
        // Fill in the total ticks.
        this.totalTicks = 0;
        this.lastSmeltedItemId = tag.getInt("last_smelted_item_id");
        /*
        for (ConcoctiMelterBlockEntity.ItemData data : itemDataMap.values()) {
            if (data.id == lastSmeltedItemId) {
                this.totalTicks = data.ticks;
            }
        }*/
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("ticks_left", this.ticksLeft);
        // Fetch the appropriate item ID.
        tag.putInt("last_smelted_item_id", this.lastSmeltedItemId);
        tank.writeToNBT(registries, tag);
    }
}
