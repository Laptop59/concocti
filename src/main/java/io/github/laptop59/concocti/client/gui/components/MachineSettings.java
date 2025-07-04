package io.github.laptop59.concocti.client.gui.components;

import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class MachineSettings {
    public List<SlotType> availableTypes;
    public MachineSettingsSlots slots;

    /**
     * Creates a new {@code MachineSettings} object based only on the available slot types.
     * <p></p>
     * Note: {@code availableTypes} should exclude {@link SlotType#NONE}!
     * @param availableTypes The slot types to include, excluding {@code NONE} which is already included.
     */
    public MachineSettings(List<SlotType> availableTypes) {
        ArrayList<SlotType> list = new ArrayList<>(availableTypes);
        list.addFirst(SlotType.NONE);
        this.availableTypes = List.copyOf(list);
        slots = emptySlots();
    }

    public static MachineSettingsSlots emptySlots() {
        return new MachineSettingsSlots();
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
        int ordinal = availableTypes.indexOf(getSlot(direction)) + 1;
        if (availableTypes.size() <= ordinal) ordinal = 0;
        setSlot(direction, availableTypes.get(ordinal));
    }

    /**
     * Cycles the slot of a particular block face to the previous one.
     * @param direction The direction of the block face.
     */
    public void cycleSlotPrevious(Direction direction) {
        int ordinal = availableTypes.indexOf(getSlot(direction)) - 1;
        if (ordinal < 0) ordinal = availableTypes.size() - 1;
        setSlot(direction, availableTypes.get(ordinal));
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

}
