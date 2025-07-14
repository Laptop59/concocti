package io.github.laptop59.concocti.common.fluid;

import io.github.laptop59.concocti.common.item.ConcoctiItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

public abstract class SupersaturatedCrystaliumSolutionFluid extends BaseFlowingFluid {

    protected SupersaturatedCrystaliumSolutionFluid() {
        super(
                new Properties(
                        ConcoctiFluids.SUPERSATURATED_CRYSTALIUM_SOLUTION_FLUID_TYPE,
                        ConcoctiFluids.SUPERSATURATED_CRYSTALIUM_SOLUTION,
                        ConcoctiFluids.FLOWING_SUPERSATURATED_CRYSTALIUM_SOLUTION
                ).block(ConcoctiFluids.SUPERSATURATED_CRYSTALIUM_SOLUTION_BLOCK)
                        .bucket(ConcoctiItems.SUPERSATURATED_CRYSTALIUM_SOLUTION_BUCKET)
        );
    }

    @Override
    public @NotNull Item getBucket() {
        return ConcoctiItems.SUPERSATURATED_CRYSTALIUM_SOLUTION_BUCKET.get();
    }

    @Override
    public @NotNull Fluid getSource() {
        return ConcoctiFluids.SUPERSATURATED_CRYSTALIUM_SOLUTION.get();
    }

    @Override
    public @NotNull Fluid getFlowing() {
        return ConcoctiFluids.FLOWING_SUPERSATURATED_CRYSTALIUM_SOLUTION.get();
    }

    @Override
    public @NotNull FluidType getFluidType() {
        return ConcoctiFluids.SUPERSATURATED_CRYSTALIUM_SOLUTION_FLUID_TYPE.get();
    }

    public static class Flowing extends SupersaturatedCrystaliumSolutionFluid {

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

    public static class Source extends SupersaturatedCrystaliumSolutionFluid {
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
