package io.github.laptop59.concocti.common.abstraction;

public interface Linker<T> {
    /** Get the object loosely associated with this property. */
    T getLinkedObject();
}
