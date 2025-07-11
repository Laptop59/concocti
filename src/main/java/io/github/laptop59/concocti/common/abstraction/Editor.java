package io.github.laptop59.concocti.common.abstraction;

public interface Editor<T> {
    /**
     * Edit and return the object.
     */
    T editAndReturn(T object);
}
