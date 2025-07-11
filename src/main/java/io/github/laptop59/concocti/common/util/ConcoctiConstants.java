package io.github.laptop59.concocti.common.util;

public final class ConcoctiConstants {
    /**
     * Number of mB of metal obtained from melting a nugget.
     */
    public static int MOLTEN_NUGGET = 16;

    /**
     * Number of mB of metal obtained from melting an ingot.
     */
    public static int MOLTEN_INGOT = MOLTEN_NUGGET * 9;

    /**
     * Number of mB of metal obtained from melting a block.
     */
    public static int MOLTEN_BLOCK = MOLTEN_INGOT * 9;

    /**
     * Number of mB of metal obtained from purifying a nugget via melting.
     */
    public static int MOLTEN_NUGGET_PURIFIED = 12;

    /**
     * Number of mB of metal obtained from purifying an ingot via melting.
     */
    public static int MOLTEN_INGOT_PURIFIED = MOLTEN_NUGGET_PURIFIED * 9;

    /**
     * Number of mB of metal obtained from purifying a block via melting.
     */
    public static int MOLTEN_BLOCK_PURIFIED = MOLTEN_INGOT_PURIFIED * 9;

    private ConcoctiConstants() {
    }
}
