package io.github.laptop59.concocti.common.abstraction;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import org.jetbrains.annotations.Contract;

import java.util.List;

/**
 * This class provides a read-only view of a {@link Complexion}-like object.
 */
public abstract class ComplexionViewer implements ContainerData {
    public final int[] data;

    @Contract(pure = true)
    abstract public List<Property<?>> getProperties();

    @Contract(pure = true)
    public int getPropertiesSize() {
        int size = 0;
        for (Property<?> property : getProperties())
            size += property.codec().size();
        return size;
    }

    protected final List<Property<?>> properties;
    protected final Complexion complexion;

    public ComplexionViewer(SimpleContainerData containerData) {
        this.data = containerData.ints;
        this.properties = getProperties();
        if (getPropertiesSize() != containerData.getCount()) {
            throwPropertiesMismatchException(containerData);
        }
        this.complexion = new Complexion(data, properties);
    }

    public ComplexionViewer(Complexion containerData) {
        // Sanity check!
        this.data = containerData.data;
        this.properties = getProperties();
        if (containerData.properties.length != properties.size() ||
                getPropertiesSize() != containerData.data.length) {
            throwPropertiesMismatchException(containerData);
        }
        this.complexion = new Complexion(data, properties);
    }

    protected void throwPropertiesMismatchException(Complexion containerData) {
        StringBuilder error = new StringBuilder("There is definitely a mismatch of properties. Debug information is provided below:");
        error.append("\nCopied Complexion's properties:");
        for (Property<?> property : containerData.properties) {
            error.append("\n").append(property.toString());
        }
        error.append("\nProvided properties:");
        for (Property<?> property : properties) {
            error.append("\n").append(property.toString());
        }
        error.append("\nPlease report this to the Concocti devs.");
        throw new ReportedException(new CrashReport("Mismatch of properties detected!", new PropertiesMismatchException(error.toString())));
    }

    protected void throwPropertiesMismatchException(SimpleContainerData containerData) {
        String error = "There is definitely a mismatch of properties. Debug information is provided below:" + "\nCopied container data's size: " + containerData.getCount() +
                "\nProvided properties' size: " + getPropertiesSize() +
                "\nPlease report this to the Concocti devs.";
        throw new ReportedException(new CrashReport("Mismatch of properties detected!", new PropertiesMismatchException(error)));
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
