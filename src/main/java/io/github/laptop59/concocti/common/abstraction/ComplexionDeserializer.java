package io.github.laptop59.concocti.common.abstraction;

@FunctionalInterface
public interface ComplexionDeserializer<T> {
    /**
     * Deserialize data from a complexion's internal buffer to reconstruct a new object.
     */
    T deserialize(Complexion instance);
}
