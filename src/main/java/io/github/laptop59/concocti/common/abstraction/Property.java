package io.github.laptop59.concocti.common.abstraction;

/**
 * A wrapper of a codec whose identity cannot be recreated.
 * This object is useful because it helps differentiate
 * from two different properties using the same {@code ComplexionCodec}.
 * <p>
 * Because of this object's special behavior, the same {@code Property}s used
 * to construct a Complexion <b>MUST BE THE ONLY</b> properties used in the complexion's
 * functions, when they require a property.
 * */
public record Property<T>(ComplexionCodec<T> codec) {
    /** Creates a read-only unique codec whose identity cannot be recreated. */
    public Property {}

    /** Get the wrapped codec. */
    public ComplexionCodec<T> get() {
        return codec;
    }

    /** Create a valued codec from this unique codec.  */
    public ValuedComplexionCodec<T> of(T object) {
        return new ValuedComplexionCodec<>(this, object);
    }
}
