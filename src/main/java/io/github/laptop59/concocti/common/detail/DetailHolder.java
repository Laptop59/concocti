package io.github.laptop59.concocti.common.detail;

import io.github.laptop59.concocti.common.util.Holder;

/**
 * A class which holds the detail of a machine.
 * @param <T> The type of object to hold.
 * */
public class DetailHolder<T> extends Holder<T> {
    final DetailCodec<T> codec;
    final String id;

    public DetailHolder(DetailCodec<T> codec, String id, Details registrar) {
        super();
        this.codec = codec;
        this.id = id;
        register(registrar);
    }

    public DetailHolder(DetailCodec<T> codec, String id, T object, Details registrar) {
        super(object);
        this.codec = codec;
        this.id = id;
        register(registrar);
    }

    public void serialize(DetailContext detailContext) {
        codec.serialize(detailContext, this);
    }

    public void deserialize(DetailContext detailContext) {
        codec.deserialize(detailContext, this);
    }

    protected DetailHolder<T> register(Details registrar) {
        registrar.add(this);
        return this;
    }
}
