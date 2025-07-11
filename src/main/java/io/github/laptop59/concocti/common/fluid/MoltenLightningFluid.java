package io.github.laptop59.concocti.common.fluid;

import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

public abstract class MoltenLightningFluid extends BaseFlowingFluid {

    protected MoltenLightningFluid() {
        super(
                new Properties(
                        ConcoctiFluids.MOLTEN_LIGHTNING_FLUID_TYPE,
                        ConcoctiFluids.MOLTEN_LIGHTNING,
                        ConcoctiFluids.FLOWING_MOLTEN_LIGHTNING
                ).block(ConcoctiFluids.MOLTEN_LIGHTNING_BLOCK)
                        .levelDecreasePerBlock(2)
        );
    }

    @Override
    public @NotNull Fluid getSource() {
        return ConcoctiFluids.MOLTEN_LIGHTNING.get();
    }

    @Override
    public @NotNull Fluid getFlowing() {
        return ConcoctiFluids.FLOWING_MOLTEN_LIGHTNING.get();
    }

    @Override
    public @NotNull FluidType getFluidType() {
        return ConcoctiFluids.MOLTEN_LIGHTNING_FLUID_TYPE.get();
    }

    public static class Flowing extends MoltenLightningFluid {

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

    public static class Source extends MoltenLightningFluid {
        @Override
        public int getAmount(@NotNull FluidState state) {
            return 5;
        }

        @Override
        public boolean isSource(@NotNull FluidState state) {
            return true;
        }
    }
}
