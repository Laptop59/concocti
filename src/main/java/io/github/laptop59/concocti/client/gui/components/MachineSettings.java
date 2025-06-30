package io.github.laptop59.concocti.client.gui.components;

import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class MachineSettings {
    final List<SlotType> availableTypes;
    public final EnumMap<Direction, SlotType> slots;

    /**
     * Creates a new {@code MachineSettings} object based only on the available slot types.
     * <p></p>
     * Note: {@code availableTypes} should exclude {@link SlotType}{@code .NONE}!
     * @param availableTypes The slot types to include, excluding {@code NONE} which is already included.
     */
    public MachineSettings(List<SlotType> availableTypes) {
        ArrayList<SlotType> list = new ArrayList<>(availableTypes);
        list.addFirst(SlotType.NONE);
        this.availableTypes = List.copyOf(list);
        slots = emptySlots();
    }

    public static EnumMap<Direction, SlotType> emptySlots() {
        EnumMap<Direction, SlotType> slots = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values())
            slots.put(direction, SlotType.NONE);
        return slots;
    }

    /**
     * Gets the slot of a particular block face.
     * @param direction The direction of the block face.
     * @return The slot type associated with the provided direction.
     */
    public SlotType getSlot(Direction direction) {
        return slots.get(direction);
    }

    /**
     * Sets the slot of a particular block face to a particular slot type.
     * @param direction The direction of the block face.
     * @param slotType The slot type to fill the block face with.
     */
    void setSlot(Direction direction, SlotType slotType) {
        if (!availableTypes.contains(slotType)) throw new IllegalArgumentException("This MachineSettings does not accept the " + slotType.name() + " slot type.");
        slots.put(direction, slotType);
    }

    /**
     * Sets the slots of the provided full map to these settings.
     * @param map The map to use to fill these settings.
     */
    void setSlots(EnumMap<Direction, SlotType> map) {
        this.slots.putAll(map);
    }

    /**
     * Cycles the slot of a particular block face to the next one.
     * @param direction The direction of the block face.
     */
    public void cycleSlotNext(Direction direction) {
        int ordinal = getSlot(direction).ordinal() + 1;
        if (SlotType.values().length <= ordinal) ordinal = 0;
        setSlot(direction, SlotType.values()[ordinal]);
    }

    /**
     * Cycles the slot of a particular block face to the previous one.
     * @param direction The direction of the block face.
     */
    public void cycleSlotPrevious(Direction direction) {
        int ordinal = getSlot(direction).ordinal() - 1;
        if (ordinal < 0) ordinal = SlotType.values().length - 1;
        setSlot(direction, SlotType.values()[ordinal]);
    }

    /**
     * Cycles the slot of a particular block face based on the provided direction.
     * @param direction The direction of the block face.
     * @param isNext Whether the cycle is to the next slot type or to the previous one.
     */
    public void cycleSlot(Direction direction, boolean isNext) {
        if (isNext)
            cycleSlotNext(direction);
        else
            cycleSlotPrevious(direction);
    }

    public enum SlotType {
        NONE(0, 0xFFE7E7E7),
        ITEM_INPUT(100, 0xFFFFC9C9),
        ITEM_OUTPUT(101, 0xFFFFA1A1),
        FLUID_INPUT(201, 0xFFC9C9FF),
        FLUID_OUTPUT(202, 0xFFFFA1FF),
        PURIFIED_FLUID_OUTPUT(301, 0xFFF6D5FF),
        BYPRODUCT_FLUID_OUTPUT(302, 0xFFE9D5FF),
        BOTH_FLUIDS_OUTPUT(303, 0xFFFFD5F4),
        BASE_ITEM_INPUT(401, 0xFFFFC9C9),
        MOLD_ITEM_INPUT(402, 0xFFFFEDB5);

        final int id;
        final int color;

        SlotType(int id, int color) {
            this.id = id;
            this.color = color;
        }

        /** Gets the internal integral ID of this slot type. */
        public int getId() {
            return id;
        }

        /**
         * Returns a {@code SlotType} by its internal integral ID.
         * @param id The internal integral ID to search for.
         * @return The requested {@code SlotType}.
         * @throws IllegalArgumentException If invalid ID was given.
         */
        public static SlotType byId(int id) {
            for (SlotType type : SlotType.values())
                if (type.id == id)
                    return type;
            throw new IllegalArgumentException("Invalid SlotType ID was given: " + id);
        }
    }
}
