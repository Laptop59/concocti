package io.github.laptop59.concocti.common.block;

public enum HatchPurpose {
    INPUT("input"),
    OUTPUT("output");

    final String id;

    HatchPurpose(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
