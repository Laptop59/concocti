package io.github.laptop59.concocti.client.gui.components;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class MachineSettingsSlots extends EnumMap<Direction, SlotType> {
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
        for (Direction direction : Direction.values())
            put(direction, SlotType.NONE);
    }

    public static final Codec<MachineSettingsSlots> CODEC = SlotType.CODEC.listOf().xmap(
            (list) -> {
                MachineSettingsSlots slots = new MachineSettingsSlots();
                int i = 0;
                for (Direction direction : SLOTS_ORDER) {
                    slots.put(direction, list.get(i++));
                }
                return slots;
            },
            (slots) -> {
                ArrayList<SlotType> list = new ArrayList<>(6);
                for (Direction direction : SLOTS_ORDER) {
                    list.add(slots.get(direction));
                }
                return list;
            }
    );

    public static final StreamCodec<ByteBuf, MachineSettingsSlots> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull MachineSettingsSlots decode(@NotNull ByteBuf buffer) {
            MachineSettingsSlots slots = new MachineSettingsSlots();

            for (Direction direction : SLOTS_ORDER)
                slots.put(direction, SlotType.STREAM_CODEC.decode(buffer));

            return slots;
        }

        @Override
        public void encode(@NotNull ByteBuf buffer, @NotNull MachineSettingsSlots value) {
            for (Direction direction : SLOTS_ORDER)
                SlotType.STREAM_CODEC.encode(buffer, value.get(direction));
        }
    };
}
