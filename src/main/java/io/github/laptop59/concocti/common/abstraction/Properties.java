package io.github.laptop59.concocti.common.abstraction;

import net.neoforged.neoforge.fluids.FluidStack;

/** This class contains {@link io.github.laptop59.concocti.common.abstraction.Property} constants for convenience. */
public final class Properties {
    public static final Property<Integer> TICKS_LEFT = ComplexionCodec.INTEGER.unique();
    public static final Property<Integer> TOTAL_TICKS = ComplexionCodec.INTEGER.unique();
    public static final Property<Integer> ENERGY_STORED = ComplexionCodec.INTEGER.unique();
    public static final Property<Integer> MAX_ENERGY_STORED = ComplexionCodec.INTEGER.unique();
    public static final Property<FluidStack> PURE_FLUID_OUTPUT = ComplexionCodec.FLUID_STACK.unique();
    public static final Property<FluidStack> BYPRODUCT_FLUID_OUTPUT = ComplexionCodec.FLUID_STACK.unique();

    private Properties() {}
}
