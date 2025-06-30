package io.github.laptop59.concocti.common.abstraction;

import io.github.laptop59.concocti.client.gui.components.MachineSettings;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;

/** A blueprint for the conversion of a Java object to and fro integers. */
public record ComplexionCodec<T>(
        int size,
        ComplexionSerializer<T> serializer,
        ComplexionDeserializer<T> deserializer,
        String type
) implements ComplexionSerializer<T>, ComplexionDeserializer<T> {
    /* Ere Be Co Decs */
    /* (here be codecs) */

    public static final ComplexionCodec<Boolean> BOOLEAN = new ComplexionCodec<>(
            1,
            (object, instance) -> {
                instance.encode(object ? 1 : 0);
            },
            instance -> instance.decode() != 0,
            "BOOLEAN"
    );

    public static final ComplexionCodec<Byte> BYTE = new ComplexionCodec<>(
            1,
            (object, instance) -> {
                instance.encode((byte) (int) object);
            },
            instance -> (byte) instance.decode(),
            "BYTE"
    );

    public static final ComplexionCodec<Short> SHORT = new ComplexionCodec<>(
            1,
            (object, instance) -> {
                instance.encode((short) (int) object);
            },
            instance -> (short) instance.decode(),
            "SHORT"
    );

    public static final ComplexionCodec<Integer> INTEGER = new ComplexionCodec<>(
        1,
            (object, instance) -> {
                instance.encode(object);
            },
            Complexion::decode,
            "INTEGER"
    );

    public static final ComplexionCodec<Long> LONG = new ComplexionCodec<>(
            2,
            (object, instance) -> {
                long number = object;
                int int1 = (int) (number & 0xFFFFFFFFL);
                int int2 = (int) (number >> 32);
                instance.encode(int1, int2);
            },
            instance -> {
                int int1 = instance.decode();
                int int2 = instance.decode();
                return (((long) int2) << 32) | int1;
            },
            "LONG"
    );

    public static final ComplexionCodec<Float> FLOAT = new ComplexionCodec<>(
            1,
            (object, instance) -> {
                instance.encode(Float.floatToRawIntBits(object));
            },
            instance -> Float.intBitsToFloat(instance.decode()),
            "FLOAT"
    );

    public static final ComplexionCodec<Double> DOUBLE = new ComplexionCodec<>(
            2,
            (object, instance) -> {
                long bits = Double.doubleToRawLongBits(object);
                LONG.serialize(bits, instance);
            },
            instance -> {
                long bits = LONG.deserialize(instance);
                return Double.longBitsToDouble(bits);
            },
            "DOUBLE"
    );

    public static final ComplexionCodec<Character> CHARACTER = new ComplexionCodec<>(
            1,
            (object, instance) -> {
                instance.encode((char) (int) object);
            },
            instance -> (char) instance.decode(),
            "CHARACTER"
    );

    public static final ComplexionCodec<FluidStack> FLUID_STACK = new ComplexionCodec<>(
            1 + 1, // one for type and one for fluid amount
            (object, instance) -> {
                int fluidId = BuiltInRegistries.FLUID.getId(object.getFluid());
                int amount = object.getAmount();
                instance.encode(fluidId, amount);
            },
            instance -> {
                int fluidId = instance.decode();
                int amount = instance.decode();
                return new FluidStack(
                        BuiltInRegistries.FLUID.byId(fluidId),
                        amount
                );
            },
            "FLUID_STACK"
    );

    public static final ComplexionCodec<MachineSettings.SlotType> SLOT_TYPE = new ComplexionCodec<>(
            1,
            (object, instance) -> {
                instance.encode(object.getId());
            },
            instance -> MachineSettings.SlotType.byId(instance.decode()),
            "SLOT_TYPE"
    );

    // This can be any arbitrary ordered list, but we do need a constant standard.
    private static final List<Direction> slotsOrder = List.of(
            Direction.NORTH,
            Direction.SOUTH,
            Direction.UP,
            Direction.DOWN,
            Direction.WEST,
            Direction.EAST
    );

    public static final ComplexionCodec<EnumMap<Direction, MachineSettings.SlotType>> MACHINE_SETTINGS_SLOTS = new ComplexionCodec<>(
        6, // each slot occupies 1 integer.
            (object, instance) -> {
                for (Direction direction : slotsOrder) {
                    SLOT_TYPE.serialize(
                            object.get(direction),
                            instance
                    );
                }
            },
            instance -> {
                EnumMap<Direction, MachineSettings.SlotType> map = new EnumMap<>(Direction.class);
                for (Direction direction : slotsOrder) {
                    map.put(direction, SLOT_TYPE.deserialize(instance));
                }
                return map;
            },
            "MACHINE_SETTINGS_SLOTS"
    );

    /** Creates a read-only codec that allows the conversion that a complexion codec would do. */
    public ComplexionCodec {}

    /** Deserializes an object from a {@code Complexion} */
    @Override
    public T deserialize(Complexion instance) {
        return deserializer.deserialize(instance);
    }

    /** Serializes an object from a {@code Complexion} */
    @Override
    public void serialize(T object, Complexion instance) {
        serializer.serialize(object, instance);
    }

    /** Creates a unique complexion codec whose identity cannot be recreated. */
    public Property<T> unique() {
        return new Property<>(this);
    }

    @Override
    public @NotNull String toString() {
        return "Codec[" + type + "]";
    }
}
