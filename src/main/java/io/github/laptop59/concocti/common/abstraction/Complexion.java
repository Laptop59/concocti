package io.github.laptop59.concocti.common.abstraction;

import net.minecraft.world.inventory.ContainerData;

import java.util.List;
import java.util.function.Consumer;

/**
 * This class acts as an abstraction over normal {@link ContainerData}s.
 * An instance of this class allows you to only care about the object's {@link Property} rather than arbitrary indexes.
 * <p></p>
 * To extend this class and create your own {@code Complexion}, you can either look at {@link TestComplexion} or follow
 * these steps:
 * <p>
 * 1. Create your own class that extends {@code Complexion}.
 * <p>
 * 2. Inside your class put multiple of the following, each being a distinct property as shown below:
 * <p>
 *      {@code public static final Property<T> yourPropertyName = ComplexionCodec.CODEC_NAME.unique()}
 * <p>
 *    This requires a particular codec and object type. Example:
 * <p>
 *     {@code T} = {@code Integer}, {@code CODEC_NAME} = {@code INTEGER}.
 * <p>
 * 3. Create your constructors where you will construct your own complexion using its vararg constructor.
 * <p>
 * 4. Make sure to override the default constructor.
 * <p>
 * 5. Your {@code Complexion} can now act as a ContainerData
 *    and abstract over its integers!
 */
public class Complexion implements ContainerData {
    protected final int sizeIntegers;
    protected final int[] data;
    protected final Property<?>[] properties;
    protected int pointer;

    /** Creates a new {@code Complexion} instance with the specified codecs and values. */
    public Complexion(ValuedComplexionCodec<?>... valuedComplexionCodecs) {
        int totalSize = 0;
        for (ValuedComplexionCodec<?> valuedComplexionCodec : valuedComplexionCodecs) {
            int size = valuedComplexionCodec.complexionCodec().codec().size();
            totalSize += size;
        }
        data = new int[totalSize];
        properties = new Property[valuedComplexionCodecs.length];
        int i = 0;
        for (ValuedComplexionCodec<?> valuedComplexionCodec : valuedComplexionCodecs) {
            properties[i] = valuedComplexionCodec.complexionCodec();
            encodeValuedCodec(valuedComplexionCodec);
            i++;
        }
        this.sizeIntegers = totalSize;
    }

    public Complexion(int[] data, List<Property<?>> properties) {
        this.data = data;
        this.properties = new Property[properties.size()];
        int i = 0;
        for (Property<?> property : properties) {
            this.properties[i] = property;
            i++;
        }
        this.sizeIntegers = data.length;
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
        for (Property<?> c : properties) {
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
        Linker<T> linker = complexionCodec.linker();
        if (linker != null)
            return linker.getLinkedObject();
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
        Linker<T> linker = complexionCodec.linker();
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
        // Get the property associated with this integer.
        int pointer = 0;
        for (Property<?> property : properties) {
            if (pointer >= index) {
                if (serializeIfLinked(property)) {
                    return data[index];
                }
            }
            pointer += property.codec().size();
        }
        return data[index];
    }

    /** Serializes an object if linked and returns whether it was serialized or not. */
    protected <T> boolean serializeIfLinked(Property<T> property) {
        // Check if this property is linked.
        Linker<T> linker = property.linker();
        if (linker != null) {
            this.pointer = pointerAt(property);
            property.codec().serialize(linker.getLinkedObject(), this);
            return true;
        }
        return false;
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
