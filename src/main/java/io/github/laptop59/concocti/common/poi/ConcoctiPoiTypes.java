package io.github.laptop59.concocti.common.poi;

import com.google.common.collect.ImmutableSet;
import io.github.laptop59.concocti.common.ConcoctiRegisters;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.machine.ConcoctiMachines;
import io.github.laptop59.concocti.common.machine.ConcoctiMultiBlockMachine;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.HashSet;
import java.util.Set;

public class ConcoctiPoiTypes {

    public static final DeferredHolder<PoiType, PoiType> CONDUCTIVIUM_LIGHTNING_ROD = ConcoctiRegisters.POI_TYPES.register(
            "conductivium_lightning_rod", () -> new PoiType(
                    ImmutableSet.copyOf(ConcoctiBlocks.CONDUCTIVIUM_LIGHTNING_ROD.get().getStateDefinition().getPossibleStates()),
                    0, // No entities are involved in this (except the lightning, but that's not directly involved I believe)
                    1 // At this point I just borrowed the original lightning rod code.
            )
    );

    public static final DeferredHolder<PoiType, PoiType> MULTIBLOCK_CONTROLLER = ConcoctiRegisters.POI_TYPES.register(
            "multiblock_controller", () -> new PoiType(
                    getMultiblockControllerStates(),
                    0,
                    1
            )
    );

    private static Set<BlockState> getMultiblockControllerStates() {
        Set<BlockState> states = new HashSet<>();
        ConcoctiMachines.forEach(concoctiMachine -> {
            if (concoctiMachine instanceof ConcoctiMultiBlockMachine controller) {
                states.addAll(controller.BLOCK.get().getStateDefinition().getPossibleStates());
            }
        });
        return states;
    }
}
