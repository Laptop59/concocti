package io.github.laptop59.concocti.common.abstraction;

import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import org.jetbrains.annotations.Contract;

import java.util.List;

public abstract class ComplexionViewer implements ContainerData {
    public final int[] data;

    @Contract(pure = true)
    abstract public List<Property<?>> getProperties();
    protected final List<Property<?>> properties;
    protected final Complexion complexion;

    public ComplexionViewer(SimpleContainerData containerData) {
        this.data = containerData.ints;
        this.properties = getProperties();
        this.complexion = new Complexion(data, properties);
    }

    public ComplexionViewer(Complexion containerData) {
        this.data = containerData.data;
        this.properties = getProperties();
        this.complexion = new Complexion(data, properties);
    }

    @Override
    public int get(int index) {
        return data[index];
    }

    public <T> T get(Property<T> property) {
        return complexion.get(property);
    }

    @Override
    public void set(int index, int value) {
        throw new UnsupportedOperationException("This view is read-only.");
    }

    @Override
    public int getCount() {
        return data.length;
    }
}
