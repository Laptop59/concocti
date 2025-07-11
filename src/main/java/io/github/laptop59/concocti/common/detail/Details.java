package io.github.laptop59.concocti.common.detail;

import java.util.Arrays;
import java.util.List;

/**
 * This interface represents an object
 * that manages with multiple different
 * {@code DetailHolder}s which may be of
 * different types.
 */
public interface Details {
    /**
     * Adds a {@code DetailHolder} to the list.
     * @param holder The holder to add
     * @param <T> The type of holder provided.
     */
    <T> void add(DetailHolder<T> holder);

    /**
     * Adds all {@code DetailHolder}s provided to the list.
     * @param holders The holders to add.
     */
    default void addAll(List<DetailHolder<?>> holders) {
        holders.forEach(this::add);
    }

    /**
     * Adds all {@code DetailHolder}s provided to the list.
     * @param holders The holders to add.
     */
    default void addAll(DetailHolder<?>... holders) {
        addAll(Arrays.asList(holders));
    }

    /**
     * Serializes all {@code DetailHolder}s provided into NBT.
     * @param context The context to use.
     */
    void serialize(DetailContext context);

    /**
     * Deserializes all {@code DetailHolder}s provided from NBT.
     * @param context The context to use.
     */
    void deserialize(DetailContext context);
}
