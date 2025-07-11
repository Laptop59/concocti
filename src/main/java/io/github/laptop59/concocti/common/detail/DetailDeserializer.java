package io.github.laptop59.concocti.common.detail;

public interface DetailDeserializer<T> {
    void deserialize(DetailContext context, DetailHolder<T> holder);
}
