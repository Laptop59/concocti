package io.github.laptop59.concocti.common.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Acts like a pointer in low-level languages.
 * @param <T> The type of object to hold.
 */
public class Holder<T> implements Supplier<T> {
    protected T object = null;

    public Holder() {}

    public Holder(T object) {
        this.object = object;
    }

    @Override
    public T get() {
        return object;
    }

    public void set(T newObject) {
        this.object = newObject;
    }

    public void edit(Consumer<T> consumer) {
        consumer.accept(object);
    }
}
