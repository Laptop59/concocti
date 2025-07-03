package io.github.laptop59.concocti.client.gui.components;

import com.mojang.serialization.Codec;

public enum SlotType {
    NONE(0, 0xe8e8e8),
    ITEM_INPUT(100, 0xff7d7d, SlotFlag.ITEM | SlotFlag.INPUT),
    ITEM_OUTPUT(101, 0xde6859, SlotFlag.ITEM | SlotFlag.OUTPUT),
    FLUID_INPUT(201, 0x78aaff, SlotFlag.FLUID | SlotFlag.INPUT),
    FLUID_OUTPUT(202, 0xff80ff, SlotFlag.FLUID | SlotFlag.OUTPUT),
    PURE_FLUID_OUTPUT(301, 0x8262cc, SlotFlag.FLUID | SlotFlag.OUTPUT),
    BYPRODUCT_FLUID_OUTPUT(302, 0x6b81b3, SlotFlag.FLUID | SlotFlag.OUTPUT),
    BOTH_FLUIDS_OUTPUT(303, 0xdb5eb0, SlotFlag.FLUID | SlotFlag.OUTPUT),
    BASE_ITEM_INPUT(401, 0xd95571, SlotFlag.ITEM | SlotFlag.INPUT),
    MOLD_ITEM_INPUT(402, 0xadff8c, SlotFlag.ITEM | SlotFlag.INPUT);

    public static final Codec<SlotType> CODEC = Codec.INT.xmap(
            SlotType::byId,
            SlotType::getId
    );

    final int id;
    final int color;
    final int flags;

    SlotType(int id, int color) {
        this(id, color, 0);
    }

    SlotType(int id, int color, int flags) {
        this.id = id;
        this.color = 0xff000000 + color;
        this.flags = flags;
    }

    /**
     * Gets the internal integral ID of this slot type.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns a {@code SlotType} by its internal integral ID.
     *
     * @param id The internal integral ID to search for.
     * @return The requested {@code SlotType}.
     * @throws IllegalArgumentException If an invalid ID was given.
     */
    public static SlotType byId(int id) {
        for (SlotType type : SlotType.values())
            if (type.id == id)
                return type;
        throw new IllegalArgumentException("Invalid SlotType ID was given: " + id);
    }

    public int getFlags() {
        return flags;
    }

    public boolean isSet(int flag) {
        return (flags & flag) != 0;
    }
}
