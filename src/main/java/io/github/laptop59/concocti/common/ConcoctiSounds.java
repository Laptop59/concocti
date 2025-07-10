package io.github.laptop59.concocti.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiSounds {
    public static final DeferredHolder<SoundEvent, SoundEvent> CONCOCTI_MELTER_FIRE_CRACKLE = ConcoctiRegisters.SOUND_EVENTS.register(
            "block.concocti_melter.fire_crackle", // must match the resource location on the next line
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "block.concocti_melter.fire_crackle"))
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> CONCOCTI_ENERGY_GENERATOR_FIRE_CRACKLE = ConcoctiRegisters.SOUND_EVENTS.register(
            "block.concocti_energy_generator.fire_crackle", // must match the resource location on the next line
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "block.concocti_energy_generator.fire_crackle"))
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> CONCOCTI_MIXER_FIRE_CRACKLE = ConcoctiRegisters.SOUND_EVENTS.register(
            "block.concocti_mixer.fire_crackle", // must match the resource location on the next line
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "block.concocti_mixer.fire_crackle"))
    );
}
