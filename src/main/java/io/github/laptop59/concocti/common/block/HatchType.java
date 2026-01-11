package io.github.laptop59.concocti.common.block;

import java.util.Objects;

public enum HatchType {
    ITEM("item"),
    FLUID("fluid"),
    ENERGY("energy");

    final String id;

    HatchType(String id) {
        this.id = id;
    }

    public static HatchType of(String hatchType) {
        for (HatchType type : values())
            if (Objects.equals(type.id, hatchType)) return type;
        return null;
    }

    public String getId() {
        return id;
    }
}
