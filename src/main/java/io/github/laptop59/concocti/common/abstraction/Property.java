package io.github.laptop59.concocti.common.abstraction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper of a codec whose identity cannot be recreated.
 * This object is useful because it helps differentiate
 * from two different properties using the same {@code ComplexionCodec}.
 * <p>
 * Because of this object's special behavior, the same {@code Property}s used
 * to construct a Complexion <b>MUST BE THE ONLY</b> properties used in the complexion's
 * functions, when they require a property.
 */
public record Property<T>(ComplexionCodec<T> codec, @Nullable Linker<T> linker) {
    /**
     * Creates a read-only unique codec whose identity cannot be recreated, without any linked object.
     */
    public Property(ComplexionCodec<T> codec) {
        this(codec, null);
    }

    /**
     * Creates a read-only unique codec whose identity cannot be recreated, linking to an object.
     */
    public Property {
    }

    /**
     * Get the wrapped codec.
     */
    public ComplexionCodec<T> codec() {
        return codec;
    }

    /**
     * Get the wrapped linker.
     */
    public Linker<T> linker() {
        return linker;
    }

    /**
     * Create a valued codec from this unique codec.
     */
    public ValuedComplexionCodec<T> of(@NotNull T object) {
        return new ValuedComplexionCodec<>(this, object);
    }

    /**
     * Create another property by editing the linker, which doesn't mutate this object. The returned object is another property.
     */
    public Property<T> newWithLinker(Linker<T> linker) {
        return new Property<>(codec(), linker);
    }

    @Override
    public @NotNull String toString() {
        return "Property[codec=" + codec + ",linker=" + linker + "]";
    }
}
