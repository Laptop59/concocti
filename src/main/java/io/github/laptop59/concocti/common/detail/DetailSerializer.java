package io.github.laptop59.concocti.common.detail;

public interface DetailSerializer<T> {
    void serialize(DetailContext context, DetailHolder<T> holder);
}
