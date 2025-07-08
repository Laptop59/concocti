package io.github.laptop59.concocti.common.poi;

import com.google.common.collect.ImmutableSet;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.ConductiviumLightningRodBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiPoiTypes {
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, MODID);

    public static final DeferredHolder<PoiType, PoiType> CONDUCTIVIUM_LIGHTNING_ROD = POI_TYPES.register(
            "conductivium_lightning_rod", () -> new PoiType(
                    ImmutableSet.copyOf(ConcoctiBlocks.CONDUCTIVIUM_LIGHTNING_ROD.get().getStateDefinition().getPossibleStates()),
                    0, // No entities are involved in this (except the lightning rod, but that's not directly involved I believe)
                    1 // At this point I just borrowed the original lightning rod code.
            )
    );

}
