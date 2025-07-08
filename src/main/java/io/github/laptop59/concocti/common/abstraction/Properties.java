package io.github.laptop59.concocti.common.abstraction;

import io.github.laptop59.concocti.client.gui.components.MachineSettingsSlots;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;

/** This class contains {@link io.github.laptop59.concocti.common.abstraction.Property} constants for convenience. */
public final class Properties {
    public static final Property<Integer> TICKS_LEFT = ComplexionCodec.INTEGER.unique();
    public static final Property<Integer> TOTAL_TICKS = ComplexionCodec.INTEGER.unique();
    public static final Property<Integer> ENERGY_STORED = ComplexionCodec.INTEGER.unique();
    public static final Property<Integer> MAX_ENERGY_STORED = ComplexionCodec.INTEGER.unique();
    public static final Property<Direction> FACING_DIRECTION = ComplexionCodec.DIRECTION.unique();
    public static final Property<MachineSettingsSlots> MACHINE_SETTINGS_SLOTS = ComplexionCodec.MACHINE_SETTINGS_SLOTS.unique();
    public static final Property<Boolean> EJECT_ON = ComplexionCodec.BOOLEAN.unique();
    public static final Property<Boolean> PULL_ON = ComplexionCodec.BOOLEAN.unique();

    public static final Property<FluidStack> PURE_FLUID_OUTPUT = ComplexionCodec.FLUID_STACK.unique();
    public static final Property<FluidStack> BYPRODUCT_FLUID_OUTPUT = ComplexionCodec.FLUID_STACK.unique();

    public static final Property<FluidStack> FLUID_INPUT = ComplexionCodec.FLUID_STACK.unique();

    public static final Property<FluidStack> FLUID_INPUT_1 = ComplexionCodec.FLUID_STACK.unique();
    public static final Property<FluidStack> FLUID_INPUT_2 = ComplexionCodec.FLUID_STACK.unique();
    public static final Property<FluidStack> FLUID_INPUT_3 = ComplexionCodec.FLUID_STACK.unique();
    public static final Property<FluidStack> FLUID_INPUT_4 = ComplexionCodec.FLUID_STACK.unique();
    public static final Property<FluidStack> FLUID_OUTPUT = ComplexionCodec.FLUID_STACK.unique();

    private Properties() {}
}
