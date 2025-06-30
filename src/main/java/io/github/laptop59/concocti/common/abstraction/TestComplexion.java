package io.github.laptop59.concocti.common.abstraction;

import io.github.laptop59.concocti.client.gui.components.MachineSettings;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.EnumMap;

/** This class describes how a custom Complexion is created. */
public class TestComplexion extends Complexion {
    public final static Property<Integer> A = ComplexionCodec.INTEGER.unique();
    public final static Property<Integer> B = ComplexionCodec.INTEGER.unique();
    public final static Property<Byte> C = ComplexionCodec.BYTE.unique();
    public final static Property<Short> D = ComplexionCodec.SHORT.unique();
    public final static Property<Long> E = ComplexionCodec.LONG.unique();
    public final static Property<Float> F = ComplexionCodec.FLOAT.unique();
    public final static Property<Double> G = ComplexionCodec.DOUBLE.unique();
    public final static Property<Character> H = ComplexionCodec.CHARACTER.unique();
    public final static Property<EnumMap<Direction, MachineSettings.SlotType>> I = ComplexionCodec.MACHINE_SETTINGS_SLOTS.unique();

    /* Not required, just for testing purposes. */
    public final static Property<?>[] PROPERTIES = {A, B, C, D, E, F, G, H, I};

    public TestComplexion() {
        super(
                A.of(8),
                B.of(-1),
                C.of((byte) 4),
                D.of((short) -9),
                E.of(-1600000000L),
                F.of(-25.0f),
                G.of(36.0),
                H.of('t'),
                I.of(MachineSettings.emptySlots())
        );
    }

    public TestComplexion(int a, int b, byte c, short d, long e, float f, double g, char h, EnumMap<Direction, MachineSettings.SlotType> i) {
        super(
                A.of(a),
                B.of(b),
                C.of(c),
                D.of(d),
                E.of(e),
                F.of(f),
                G.of(g),
                H.of(h),
                I.of(i)
        );
    }

    public static void main(String[] args) {
        test(new TestComplexion());
    }

    public static void test(TestComplexion testComplexion) {
        // testComplexion.edit(A, i -> 3 * i + 2);
        for (Property<?> property : PROPERTIES) {
            testProperty(testComplexion, property);
        }
        // Testing the complexion with editing here:
        System.out.println("Machine Settings Slots editing TEST: " + testComplexion.get(I));
        System.out.println("Before: " + testComplexion.get(I));
        testComplexion.edit(I, map -> {
            map.put(Direction.UP, MachineSettings.SlotType.BASE_ITEM_INPUT);
            map.put(Direction.EAST, MachineSettings.SlotType.PURIFIED_FLUID_OUTPUT);
        });
        System.out.println("After: " + testComplexion.get(I));
    }

    public static <T> void testProperty(TestComplexion testComplexion, Property<T> property) {
        T test1 = testComplexion.get(property);
        testComplexion.set(property, test1);
        T test2 = testComplexion.get(property);
        if (!test1.equals(test2)) {
            System.out.println("Equality failed for " + test1.getClass() + ":");
            System.out.println("1. " + test1);
            System.out.println("2. " + test2);
        } else {
            System.out.println("Equality succeeded for objects of " + test1.getClass());
        }
    }
}
