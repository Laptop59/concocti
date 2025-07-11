package io.github.laptop59.concocti.common.detail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetailHolders implements Details {
    protected final ArrayList<DetailHolder<?>> detailHolders;

    public DetailHolders() {
        this.detailHolders = new ArrayList<>();
    }

    public DetailHolders(List<DetailHolder<?>> detailHolderList) {
        this.detailHolders = new ArrayList<>(detailHolderList);
    }

    public DetailHolders(DetailHolder<?>... detailHolders) {
        this.detailHolders = new ArrayList<>(Arrays.asList(detailHolders));
    }

    @Override
    public <T> void add(DetailHolder<T> holder) {
        detailHolders.add(holder);
    }

    @Override
    public void addAll(List<DetailHolder<?>> holders) {
        detailHolders.addAll(holders);
    }

    @Override
    public void serialize(DetailContext context) {
        detailHolders.forEach(detailHolder -> detailHolder.serialize(new DetailContext(
                context.tag(),
                context.registries(),
                detailHolder.id
        )));
    }

    @Override
    public void deserialize(DetailContext context) {
        detailHolders.forEach(detailHolder -> detailHolder.deserialize(new DetailContext(
                context.tag(),
                context.registries(),
                detailHolder.id
        )));
    }
}
