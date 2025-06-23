package io.github.laptop59.concocti.common;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static io.github.laptop59.concocti.common.Concocti.MODID;

public class ConcoctiSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> CONCOCTI_MELTER_FIRE_CRACKLE = SOUND_EVENTS.register(
            "block.concocti_melter.fire_crackle", // must match the resource location on the next line
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "block.concocti_melter.fire_crackle"))
    );
}
