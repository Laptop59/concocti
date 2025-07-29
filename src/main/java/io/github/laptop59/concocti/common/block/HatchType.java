package io.github.laptop59.concocti.common.block;

public enum HatchType {
    ITEM("item"),
    FLUID("fluid"),
    ENERGY("energy");

    final String id;

    HatchType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
