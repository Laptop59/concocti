package io.github.laptop59.concocti.common.util;

import java.util.function.Supplier;

/**
 * A class that evaluates a value <b>ONLY ONCE</b> the first time it is asked to get it.
 */
public final class Lazy<T> implements Supplier<T> {
    private T innerValue = null;
    private final Supplier<T> innerSupplier;
    private boolean hasEvaluated = false;

    public Lazy(Supplier<T> supplier) {
        innerSupplier = supplier;
    }

    @Override
    public T get() {
        if (!hasEvaluated) {
            innerValue = innerSupplier.get();
            hasEvaluated = true;
        }
        return innerValue;
    }
}
