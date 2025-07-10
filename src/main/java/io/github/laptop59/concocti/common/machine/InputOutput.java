package io.github.laptop59.concocti.common.machine;

import java.util.List;
import java.util.function.Function;

public record InputOutput<T>(T inputs, T outputs) {
    /** Get all inputs associated. */
    public T getInputs() { return inputs; }

    /** Get all outputs associated. */
    public T getOutputs() { return outputs; }

    /** Creates an {@code InputOutput} of no inputs nor outputs. */
    public static <E, T> InputOutput<Function<T, List<E>>> empty() {
        return new InputOutput<>(emptyList(), emptyList());
    }

    /** Creates an {@code InputOutput} of both inputs and outputs. */
    public static <E, T> InputOutput<Function<T, List<E>>> of(Function<T, List<E>> inputs, Function<T, List<E>> outputs) {
        return new InputOutput<>(inputs, outputs);
    }

    /** Creates an {@code InputOutput} of ONLY inputs. */
    public static <E, T> InputOutput<Function<T, List<E>>> onlyInputs(Function<T, List<E>> inputs) {
        return new InputOutput<>(inputs, emptyList());
    }

    /** Creates a {@code InputOutput} of ONLY outputs. */
    public static <E, T> InputOutput<Function<T, List<E>>> onlyOutputs(Function<T, List<E>> outputs) {
        return new InputOutput<>(emptyList(), outputs);
    }

    private static <E, T> Function<T, List<E>> emptyList() {
        return object -> List.of();
    }
}
