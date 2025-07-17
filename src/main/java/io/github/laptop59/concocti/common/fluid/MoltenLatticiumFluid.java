package io.github.laptop59.concocti.common.fluid;

import io.github.laptop59.concocti.common.item.ConcoctiItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

public abstract class MoltenLatticiumFluid extends BaseFlowingFluid {

    protected MoltenLatticiumFluid() {
        super(
                new Properties(
                        ConcoctiFluids.MOLTEN_LATTICIUM_FLUID_TYPE,
                        ConcoctiFluids.MOLTEN_LATTICIUM,
                        ConcoctiFluids.FLOWING_MOLTEN_LATTICIUM
                ).block(ConcoctiFluids.MOLTEN_LATTICIUM_BLOCK)
                        .bucket(ConcoctiItems.MOLTEN_LATTICIUM_BUCKET)
        );
    }

    @Override
    public @NotNull Item getBucket() {
        return ConcoctiItems.MOLTEN_LATTICIUM_BUCKET.get();
    }

    @Override
    public @NotNull Fluid getSource() {
        return ConcoctiFluids.MOLTEN_LATTICIUM.get();
    }

    @Override
    public @NotNull Fluid getFlowing() {
        return ConcoctiFluids.FLOWING_MOLTEN_LATTICIUM.get();
    }

    @Override
    public @NotNull FluidType getFluidType() {
        return ConcoctiFluids.MOLTEN_LATTICIUM_FLUID_TYPE.get();
    }

    @Override
    protected void spreadTo(@NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockState blockState, @NotNull Direction direction, @NotNull FluidState fluidState) {
        if (direction == Direction.DOWN) {
            FluidState otherFluidState = level.getFluidState(pos);
            if (this.isSame(ConcoctiFluids.MOLTEN_LATTICIUM.get()) && otherFluidState.is(FluidTags.WATER)) {
                if (blockState.getBlock() instanceof LiquidBlock) {
                    level.setBlock(pos, net.neoforged.neoforge.event.EventHooks.fireFluidPlaceBlockEvent(level, pos, pos, Blocks.BASALT.defaultBlockState()), 3);
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

    public static class Flowing extends MoltenLatticiumFluid {

        @Override
        protected void createFluidStateDefinition(StateDefinition.@NotNull Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(@NotNull FluidState state) {
            return false;
        }
    }

    public static class Source extends MoltenLatticiumFluid {
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
