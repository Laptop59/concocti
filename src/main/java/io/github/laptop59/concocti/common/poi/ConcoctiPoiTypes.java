package io.github.laptop59.concocti.common.poi;

import com.google.common.collect.ImmutableSet;
import io.github.laptop59.concocti.common.ConcoctiRegisters;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ConcoctiPoiTypes {

    public static final DeferredHolder<PoiType, PoiType> CONDUCTIVIUM_LIGHTNING_ROD = ConcoctiRegisters.POI_TYPES.register(
            "conductivium_lightning_rod", () -> new PoiType(
                    ImmutableSet.copyOf(ConcoctiBlocks.CONDUCTIVIUM_LIGHTNING_ROD.get().getStateDefinition().getPossibleStates()),
                    0, // No entities are involved in this (except the lightning rod, but that's not directly involved I believe)
                    1 // At this point I just borrowed the original lightning rod code.
            )
    );

}
