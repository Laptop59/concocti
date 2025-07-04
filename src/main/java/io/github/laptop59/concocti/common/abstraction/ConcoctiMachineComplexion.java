package io.github.laptop59.concocti.common.abstraction;

import io.github.laptop59.concocti.client.gui.components.MachineSettingsSlots;
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineBlockEntity;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** This complexion automatically puts the base machine codecs first. */
public class ConcoctiMachineComplexion extends Complexion {
    public static List<ValuedComplexionCodec<?>> getBaseCodecs(AbstractConcoctiMachineBlockEntity<?, ?, ?, ?, ?> blockEntity) {
        return List.of(
                blockEntity.TICKS_LEFT.of(0),
                blockEntity.TOTAL_TICKS.of(0),
                blockEntity.ENERGY_STORED.of(0),
                blockEntity.MAX_ENERGY_STORED.of(0),
                blockEntity.FACING_DIRECTION.of(Direction.DOWN),
                blockEntity.MACHINE_SETTINGS_SLOTS.of(new MachineSettingsSlots()),

                blockEntity.EJECT_ON.of(false),
                blockEntity.PULL_ON.of(false)
        );
    }

    /** Creates a new {@code ConcoctiMachineComplexion} instance with the specified codecs and values. */
    public ConcoctiMachineComplexion(AbstractConcoctiMachineBlockEntity<?, ?, ?, ?, ?> blockEntity, ValuedComplexionCodec<?>... valuedComplexionCodecs) {
        this(blockEntity, List.of(valuedComplexionCodecs));
    }

    /** Creates a new {@code ConcoctiMachineComplexion} instance with the specified codecs and values. */
    public ConcoctiMachineComplexion(AbstractConcoctiMachineBlockEntity<?, ?, ?, ?, ?> blockEntity, List<ValuedComplexionCodec<?>> valuedComplexionCodecs) {
        super(getAllCodecs(blockEntity, valuedComplexionCodecs));
    }

    protected static List<ValuedComplexionCodec<?>> getAllCodecs(AbstractConcoctiMachineBlockEntity<?, ?, ?, ?, ?> blockEntity, List<ValuedComplexionCodec<?>> valuedComplexionCodecs) {
        ArrayList<ValuedComplexionCodec<?>> complexionCodecs = new ArrayList<>(getBaseCodecs(blockEntity));
        complexionCodecs.addAll(valuedComplexionCodecs);
        return complexionCodecs;
    }
}
