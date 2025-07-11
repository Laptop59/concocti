package io.github.laptop59.concocti.common.abstraction;

@FunctionalInterface
public interface Linker<T> {
    /**
     * Get the object loosely associated with this property.
     */
    T getLinkedObject();
}
