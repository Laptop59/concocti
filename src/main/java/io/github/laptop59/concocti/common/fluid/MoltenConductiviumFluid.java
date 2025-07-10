package io.github.laptop59.concocti.common.fluid;

import io.github.laptop59.concocti.common.item.ConcoctiItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

public abstract class MoltenConductiviumFluid extends BaseFlowingFluid {

    protected MoltenConductiviumFluid() {
        super(
                new Properties(
                        ConcoctiFluids.MOLTEN_CONDUCTIVIUM_FLUID_TYPE,
                        ConcoctiFluids.MOLTEN_CONDUCTIVIUM,
                        ConcoctiFluids.FLOWING_MOLTEN_CONDUCTIVIUM
                ).block(ConcoctiFluids.MOLTEN_CONDUCTIVIUM_BLOCK)
                .bucket(ConcoctiItems.MOLTEN_CONDUCTIVIUM_BUCKET)
        );
    }

    @Override
    public @NotNull Item getBucket() {
        return ConcoctiItems.MOLTEN_CONDUCTIVIUM_BUCKET.get();
    }

    @Override
    public @NotNull Fluid getSource() {
        return ConcoctiFluids.MOLTEN_CONDUCTIVIUM.get();
    }

    @Override
    public @NotNull Fluid getFlowing() {
        return ConcoctiFluids.FLOWING_MOLTEN_CONDUCTIVIUM.get();
    }

    @Override
    public @NotNull FluidType getFluidType() {
        return ConcoctiFluids.MOLTEN_CONDUCTIVIUM_FLUID_TYPE.get();
    }

    public static class Flowing extends MoltenConductiviumFluid {

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

    public static class Source extends MoltenConductiviumFluid {
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
