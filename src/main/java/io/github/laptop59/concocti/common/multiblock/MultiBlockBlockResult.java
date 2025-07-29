package io.github.laptop59.concocti.common.multiblock;

public enum MultiBlockBlockResult {
    /** Indicates that the predicate COMPLETELY (100%) matches with the queried block. */
    SATISFIED,

    /** Indicates that the predicate does not match with the provided non-{@code Blocks.AIR} block. */
    UNMATCHED,

    /** Indicates that the predicate does not match with the provided {@code Blocks.AIR} block. */
    AIR;

    public boolean isSatisfied() { return this == SATISFIED; }
}
