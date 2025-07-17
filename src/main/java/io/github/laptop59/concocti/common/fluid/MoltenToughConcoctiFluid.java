package io.github.laptop59.concocti.common.fluid;

import io.github.laptop59.concocti.common.item.ConcoctiItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

public abstract class MoltenToughConcoctiFluid extends BaseFlowingFluid {

    protected MoltenToughConcoctiFluid() {
        super(
                new Properties(
                        ConcoctiFluids.MOLTEN_TOUGH_CONCOCTI_FLUID_TYPE,
                        ConcoctiFluids.MOLTEN_TOUGH_CONCOCTI,
                        ConcoctiFluids.FLOWING_MOLTEN_TOUGH_CONCOCTI
                ).block(ConcoctiFluids.MOLTEN_TOUGH_CONCOCTI_BLOCK)
                        .bucket(ConcoctiItems.MOLTEN_TOUGH_CONCOCTI_BUCKET)
        );
    }

    @Override
    public @NotNull Item getBucket() {
        return ConcoctiItems.MOLTEN_TOUGH_CONCOCTI_BUCKET.get();
    }

    @Override
    public @NotNull Fluid getSource() {
        return ConcoctiFluids.MOLTEN_TOUGH_CONCOCTI.get();
    }

    @Override
    public @NotNull Fluid getFlowing() {
        return ConcoctiFluids.FLOWING_MOLTEN_TOUGH_CONCOCTI.get();
    }

    @Override
    public @NotNull FluidType getFluidType() {
        return ConcoctiFluids.MOLTEN_TOUGH_CONCOCTI_FLUID_TYPE.get();
    }

    @Override
    protected void spreadTo(@NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockState blockState, @NotNull Direction direction, @NotNull FluidState fluidState) {
        super.spreadTo(level, pos, blockState, direction, fluidState);
    }

    private void fizz(LevelAccessor level, BlockPos pos) {
        level.levelEvent(1501, pos, 0);
    }

    public static class Flowing extends MoltenToughConcoctiFluid {

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

    public static class Source extends MoltenToughConcoctiFluid {
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
