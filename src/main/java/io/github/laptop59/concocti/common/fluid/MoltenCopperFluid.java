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

public abstract class MoltenCopperFluid extends BaseFlowingFluid {

    protected MoltenCopperFluid() {
        super(
                new Properties(
                        ConcoctiFluids.MOLTEN_COPPER_FLUID_TYPE,
                        ConcoctiFluids.MOLTEN_COPPER,
                        ConcoctiFluids.FLOWING_MOLTEN_COPPER
                ).block(ConcoctiFluids.MOLTEN_COPPER_BLOCK)
                .bucket(ConcoctiItems.MOLTEN_COPPER_BUCKET)
        );
    }

    @Override
    public @NotNull Item getBucket() {
        return ConcoctiItems.MOLTEN_COPPER_BUCKET.get();
    }

    @Override
    public @NotNull Fluid getSource() {
        return ConcoctiFluids.MOLTEN_COPPER.get();
    }

    @Override
    public @NotNull Fluid getFlowing() {
        return ConcoctiFluids.FLOWING_MOLTEN_COPPER.get();
    }

    @Override
    public @NotNull FluidType getFluidType() {
        return ConcoctiFluids.MOLTEN_COPPER_FLUID_TYPE.get();
    }

    public static class Flowing extends MoltenCopperFluid {

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

    public static class Source extends MoltenCopperFluid {
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
