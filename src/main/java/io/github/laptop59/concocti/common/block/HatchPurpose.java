package io.github.laptop59.concocti.common.block;

import java.util.Objects;

public enum HatchPurpose {
    INPUT("input"),
    OUTPUT("output");

    final String id;

    HatchPurpose(String id) {
        this.id = id;
    }

    public static HatchPurpose of(String hatchPurpose) {
        for (HatchPurpose purpose : values())
            if (Objects.equals(purpose.id, hatchPurpose)) return purpose;
        return null;
    }

    public String getId() {
        return id;
    }
}
