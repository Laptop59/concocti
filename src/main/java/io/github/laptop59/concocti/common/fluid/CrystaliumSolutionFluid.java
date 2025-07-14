package io.github.laptop59.concocti.common.fluid;

import io.github.laptop59.concocti.common.item.ConcoctiItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

public abstract class CrystaliumSolutionFluid extends BaseFlowingFluid {

    protected CrystaliumSolutionFluid() {
        super(
                new Properties(
                        ConcoctiFluids.CRYSTALIUM_SOLUTION_FLUID_TYPE,
                        ConcoctiFluids.CRYSTALIUM_SOLUTION,
                        ConcoctiFluids.FLOWING_CRYSTALIUM_SOLUTION
                ).block(ConcoctiFluids.CRYSTALIUM_SOLUTION_BLOCK)
                        .bucket(ConcoctiItems.CRYSTALIUM_SOLUTION_BUCKET)
        );
    }

    @Override
    public @NotNull Item getBucket() {
        return ConcoctiItems.CRYSTALIUM_SOLUTION_BUCKET.get();
    }

    @Override
    public @NotNull Fluid getSource() {
        return ConcoctiFluids.CRYSTALIUM_SOLUTION.get();
    }

    @Override
    public @NotNull Fluid getFlowing() {
        return ConcoctiFluids.FLOWING_CRYSTALIUM_SOLUTION.get();
    }

    @Override
    public @NotNull FluidType getFluidType() {
        return ConcoctiFluids.CRYSTALIUM_SOLUTION_FLUID_TYPE.get();
    }

    public static class Flowing extends CrystaliumSolutionFluid {

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

    public static class Source extends CrystaliumSolutionFluid {
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
