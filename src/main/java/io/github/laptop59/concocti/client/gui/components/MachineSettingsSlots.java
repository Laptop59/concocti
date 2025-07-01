package io.github.laptop59.concocti.client.gui.components;

import net.minecraft.core.Direction;

import java.util.EnumMap;
import java.util.List;

public class MachineSettingsSlots extends EnumMap<Direction, MachineSettings.SlotType> {

    // This can be any arbitrary ordered list, but we do need a constant standard.
    public static final List<Direction> SLOTS_ORDER = List.of(
            Direction.NORTH,
            Direction.SOUTH,
            Direction.UP,
            Direction.DOWN,
            Direction.WEST,
            Direction.EAST
    );

    public MachineSettingsSlots() {
        super(Direction.class);
    }
}
