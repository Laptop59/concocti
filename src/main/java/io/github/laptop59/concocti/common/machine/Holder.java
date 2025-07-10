package io.github.laptop59.concocti.common.machine;

/** An object that acts as a nullable pointer for another object. */
public final class Holder<T> {
    private T object;

    public Holder() {
        object = null;
    }

    public Holder(T object) {
        this.object = object;
    }

    /* Gets the internal object. */
    public T get() {
        return object;
    }

    /* Populates this holder with an object. */
    public void set(T object) {
        this.object = object;
    }
}
