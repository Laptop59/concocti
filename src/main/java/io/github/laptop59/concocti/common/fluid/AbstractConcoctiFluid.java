package io.github.laptop59.concocti.common.fluid;

import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class AbstractConcoctiFluid extends BaseFlowingFluid {
    protected AbstractConcoctiFluid(BaseFlowingFluid.Properties props) {
        super(props);
    }

    public int getAmount(FluidState state) {
        return state.getValue(LEVEL);
    }
}
