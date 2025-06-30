package io.github.laptop59.concocti.common.abstraction;

public interface ComplexionSerializer<T> {
    /** Serialize this object's information into a complexion's internal buffer. */
    void serialize(T object, Complexion instance);
}
