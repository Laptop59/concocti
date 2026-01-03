package io.github.laptop59.concocti.common.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/** A type of fluid of Concocti (can be either source or flowing) */
public abstract class ConcoctiFluid extends BaseFlowingFluid {
    public final ConcoctiFluidParent parent;

    public ConcoctiFluid(ConcoctiFluidParent parent) {
        super(properties(parent));
        this.parent = parent;
    }

    protected static Properties properties(ConcoctiFluidParent parent) {
        Properties properties = new Properties(
            parent.FLUID_TYPE,
            parent.SOURCE,
            parent.FLOWING
        );
        if (parent.BLOCK != null)
            properties.block(parent.BLOCK);
        if (parent.BUCKET != null)
            properties.bucket(parent.BUCKET);
        return properties;
    }

    @Override
    public @NotNull Item getBucket() {
        return parent.BUCKET == null ? Items.AIR : parent.BUCKET.get();
    }

    @Override
    public @NotNull Fluid getSource() {
        return parent.SOURCE.get();
    }

    @Override
    public @NotNull Fluid getFlowing() {
        return parent.FLOWING.get();
    }

    @Override
    public @NotNull FluidType getFluidType() {
        return parent.FLUID_TYPE.get();
    }

    @Override
    protected void spreadTo(@NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockState blockState, @NotNull Direction direction, @NotNull FluidState fluidState) {
        if (direction == Direction.DOWN) {
            FluidState otherFluidState = level.getFluidState(pos);
            if (parent.IS_MOLTEN && otherFluidState.is(FluidTags.WATER)) {
                if (blockState.getBlock() instanceof LiquidBlock) {
                    level.setBlock(pos, net.neoforged.neoforge.event.EventHooks.fireFluidPlaceBlockEvent(level, pos, pos, Blocks.COBBLESTONE.defaultBlockState()), 3);
                }
                this.fizz(level, pos);
                return;
            }
        }
        super.spreadTo(level, pos, blockState, direction, fluidState);
    }

    private void fizz(LevelAccessor level, BlockPos pos) {
        level.levelEvent(1501, pos, 0);
    }

    @Override
    protected boolean canConvertToSource(@NotNull Level level) {
        return false;
    }

    @Override
    public boolean canConvertToSource(@NotNull FluidState state, @NotNull Level level, @NotNull BlockPos pos) {
        return this.getFluidType().canConvertToSource(state, level, pos);
    }

    @Override
    protected void beforeDestroyingBlock(@NotNull LevelAccessor worldIn, @NotNull BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? worldIn.getBlockEntity(pos) : null;
        Block.dropResources(state, worldIn, pos, blockEntity);
    }

    @Override
    protected int getSlopeFindDistance(@NotNull LevelReader worldIn) {
        return 4;
    }

    @Override
    protected int getDropOff(@NotNull LevelReader worldIn) {
        return 1;
    }

    @Override
    protected boolean canBeReplacedWith(@NotNull FluidState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Fluid fluidIn, @NotNull Direction direction) {
        return false;
    }

    @Override
    public int getTickDelay(@NotNull LevelReader level) {
        return parent.TICK_RATE;
    }

    @Override
    protected float getExplosionResistance() {
        return 1;
    }

    @Override
    public boolean isSame(@NotNull Fluid fluidIn) {
        return fluidIn == parent.SOURCE.get() || fluidIn == parent.FLOWING.get();
    }

    @Override
    public @NotNull Optional<SoundEvent> getPickupSound() {
        return Optional.ofNullable(getFluidType().getSound(SoundActions.BUCKET_FILL));
    }

    // Source and Flowing logic is below this line.

    protected static class Flowing extends ConcoctiFluid {
        public Flowing(ConcoctiFluidParent parent) {
            super(parent);
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.@NotNull Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(@NotNull FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(@NotNull FluidState state) {
            return false;
        }
    }

    protected static class Source extends ConcoctiFluid {
        public Source(ConcoctiFluidParent parent) {
            super(parent);
        }

        @Override
        public int getAmount(@NotNull FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(@NotNull FluidState state) {
            return true;
        }
    }
}
