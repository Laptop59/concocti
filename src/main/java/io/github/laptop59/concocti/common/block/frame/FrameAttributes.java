package io.github.laptop59.concocti.common.block.frame;

/**
 * Creates a new set of attributes for a Frame Block.
 * @param rate The multiplier for speed of a machine not reducing the amount of energy required for a faster one.
 * @param efficiency How much of energy provided is not consumed. Value of 1 means the machine does not consume any energy (not intended).
 */
public record FrameAttributes(float rate, float efficiency) {}
