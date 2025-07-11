package io.github.laptop59.concocti.common.abstraction;

public final class ValuedComplexionCodec<T> {
    private final Property<T> property;
    private T object;

    public ValuedComplexionCodec(Property<T> property, T object) {
        this.property = property;
        this.object = object;
    }

    /**
     * Get the property stored within this object.
     */
    public Property<T> complexionCodec() {
        return property;
    }

    /**
     * Get the stored default value of this object.
     */
    public T object() {
        return object;
    }

    /**
     * Set the new default value of this object.
     */
    public void setNewValue(T object) {
        this.object = object;
    }
}
