package io.github.laptop59.concocti.common.abstraction;

import net.minecraft.world.inventory.ContainerData;

import java.util.function.Consumer;

public class Complexion implements ContainerData {
    protected final int sizeIntegers;
    protected final int[] data;
    protected final Property<?>[] codecs;
    protected int pointer;

    /** Creates a new {@code Complexion} instance with the specified codecs and values. */
    protected Complexion(ValuedComplexionCodec<?>... valuedComplexionCodecs) {
        int totalSize = 0;
        for (ValuedComplexionCodec<?> valuedComplexionCodec : valuedComplexionCodecs) {
            int size = valuedComplexionCodec.complexionCodec().codec().size();
            totalSize += size;
        }
        data = new int[totalSize];
        codecs = new Property[valuedComplexionCodecs.length];
        int i = 0;
        for (ValuedComplexionCodec<?> valuedComplexionCodec : valuedComplexionCodecs) {
            codecs[i] = valuedComplexionCodec.complexionCodec();
            encodeValuedCodec(valuedComplexionCodec);
            i++;
        }
        this.sizeIntegers = totalSize;
    }

    protected Complexion() {
        throw new IllegalStateException("Complexion default constructor was not overridden!");
    }

    protected <T> void encodeValuedCodec(ValuedComplexionCodec<T> valuedComplexionCodec) {
        Property<T> complexionCodec = valuedComplexionCodec.complexionCodec();
        int oldPointer = pointer;
        complexionCodec.codec().serialize(valuedComplexionCodec.object(), this);
        int newPointer = pointer;
        int testedSize = newPointer - oldPointer;
        int expectedSize = valuedComplexionCodec.complexionCodec().codec().size();
        if (testedSize != expectedSize) {
            throw new IllegalStateException("Expected codec " + complexionCodec + " to output " + expectedSize + " integers but instead it output " + testedSize + ".");
        }
    }

    protected int pointerAt(Property<?> complexionCodec) {
        int pointer = 0;
        for (Property<?> c : codecs) {
            if (c == complexionCodec) return pointer;
            pointer += c.codec().size();
        }
        throw new IllegalArgumentException("The codec " + complexionCodec + " provided was not registered within this instance.");
    }

    /** Gets the stored value of this codec using a property.
     * Using this function, the aftermath position of the internal pointer should not be predicted and used in consideration.
     * <p>
     * <b>IMPORTANT:</b> Only the {@code Property}s used to construct this complexion can be used here! Make sure to store the properties used statically in your class!
     * <p>
     * Even two {@code Property}s with the same codec but are actually different instances are totally distinct.
     * @param complexionCodec A property to index the complexion with.
     * @return The value stored in the property in the complexion.
     * */
    public <T> T get(Property<T> complexionCodec) {
        // First, get the pointer right before the codec and set it.
        pointer = pointerAt(complexionCodec);
        // Then decode.
        return complexionCodec.codec().deserialize(this);
    }

    /** Sets the value of this codec, using a property, to a new value.
     * Using this function, the aftermath position of the internal pointer should not be predicted and used in consideration.
     * <p>
     * <b>IMPORTANT:</b> Only the {@code Property}s used to construct this complexion can be used here! Make sure to store the properties used statically in your class!
     * <p>
     * Even two {@code Property}s with the same codec but are actually different instances are totally distinct.
     * @param complexionCodec A property to index the complexion with.
     * @param value The new value.
     * */
    public <T> void set(Property<T> complexionCodec, T value) {
        // First, get the pointer right before the codec and set it.
        pointer = pointerAt(complexionCodec);
        // Then encode.
        complexionCodec.codec().serialize(value, this);
    }

    /** Edits the value of this codec, using a property and a consumer.
     * Using this function, the aftermath position of the internal pointer should not be predicted and used in consideration.
     * <p>
     * <b>IMPORTANT:</b> Only the {@code Property}s used to construct this complexion can be used here! Make sure to store the properties used statically in your class!
     * <p>
     * Even two {@code Property}s with the same codec but are actually different instances are totally distinct.
     * @param complexionCodec A property to index the complexion with.
     * @param consumer Your consumer which will edit the values of the object desired.
     * */
    public <T> void edit(Property<T> complexionCodec, Consumer<T> consumer) {
        // First, get the pointer right before the codec and set it.
        int propertyPointer = pointerAt(complexionCodec);
        pointer = propertyPointer;
        // Then decode.
        T object = complexionCodec.codec().deserialize(this);
        consumer.accept(object);
        // Now re-encode.
        pointer = propertyPointer;
        complexionCodec.codec().serialize(object, this);
    }

    /** Edits the value of this codec, using a property and an editor.
     * Using this function, the aftermath position of the internal pointer should not be predicted and used in consideration.
     * <p>
     * <b>IMPORTANT:</b> Only the {@code Property}s used to construct this complexion can be used here! Make sure to store the properties used statically in your class!
     * <p>
     * Even two {@code Property}s with the same codec but are actually different instances are totally distinct.
     * @param complexionCodec A property to index the complexion with.
     * @param editor Your editor which will edit the values of the object desired and return it.
     * */
    public <T> void edit(Property<T> complexionCodec, Editor<T> editor) {
        // First, get the pointer right before the codec and set it.
        int propertyPointer = pointerAt(complexionCodec);
        pointer = propertyPointer;
        // Then decode.
        T object = complexionCodec.codec().deserialize(this);
        object = editor.editAndReturn(object);
        // Now re-encode.
        pointer = propertyPointer;
        complexionCodec.codec().serialize(object, this);
    }

    /** Overwrites internal data inside the complexion with a pointer that moves to the right. Only use this for {@code ComplexionCodec}s!
     * @param values Integers to encode into the internal buffer.
     * */
    public void encode(int... values) {
        for (int value : values)
            data[pointer++] = value;
    }

    /** Reads internal data inside the complexion with a pointer that moves to the right. Only use this for {@code ComplexionCodec}s!
     * @return The read integer and shifts the pointer after.
     * */
    public int decode() {
        return data[pointer++];
    }

    @Override
    public int get(int index) {
        return data[index];
    }

    @Override
    public void set(int index, int value) {
        data[index] = value;
    }

    @Override
    public int getCount() {
        return data.length;
    }
}
