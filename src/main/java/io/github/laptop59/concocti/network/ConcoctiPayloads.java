package io.github.laptop59.concocti.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ConcoctiPayloads {
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        // Sets the current network version
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                ConcoctizedEntitiesPayload.TYPE,
                ConcoctizedEntitiesPayload.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ConcoctizedEntitiesPayloadHandler::handleData,
                        ConcoctizedEntitiesPayloadHandler::handleData
                )
        );
        registrar.playToClient(
                FluidBarSoundPayloadS2C.TYPE,
                FluidBarSoundPayloadS2C.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        FluidBarSoundPayloadS2CHandler::handleData,
                        FluidBarSoundPayloadS2CHandler::handleData
                )
        );
        registrar.playToServer(
                FluidBarInteractionPayloadC2S.TYPE,
                FluidBarInteractionPayloadC2S.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        FluidBarInteractionPayloadC2SHandler::handleData,
                        FluidBarInteractionPayloadC2SHandler::handleData
                )
        );
        registrar.playToServer(
                ConcoctiMachineSettingsSlotChangeC2S.TYPE,
                ConcoctiMachineSettingsSlotChangeC2S.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ConcoctiMachineSettingsSlotChangeC2SHandler::handleData,
                        ConcoctiMachineSettingsSlotChangeC2SHandler::handleData
                )
        );
        registrar.playToServer(
                ConcoctiMachineSettingsEjectOnChangeC2S.TYPE,
                ConcoctiMachineSettingsEjectOnChangeC2S.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ConcoctiMachineSettingsEjectOnChangeC2SHandler::handleData,
                        ConcoctiMachineSettingsEjectOnChangeC2SHandler::handleData
                )
        );
        registrar.playToServer(
                ConcoctiMachineSettingsPullOnChangeC2S.TYPE,
                ConcoctiMachineSettingsPullOnChangeC2S.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ConcoctiMachineSettingsPullOnChangeC2SHandler::handleData,
                        ConcoctiMachineSettingsPullOnChangeC2SHandler::handleData
                )
        );
    }
}
