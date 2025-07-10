package io.github.laptop59.concocti.common.machine;

import io.github.laptop59.concocti.common.machine.impl.*;

import java.util.ArrayList;
import java.util.function.Consumer;

public final class ConcoctiMachines {
    public static final ArrayList<ConcoctiMachine<?, ?, ?, ?, ?, ?, ?, ?, ?>> MACHINES = new ArrayList<>();

    public static ConcoctiMelter MELTER;
    public static ConcoctiSolidifier SOLIDIFIER;
    public static ConcoctiEnergyGenerator ENERGY_GENERATOR;
    public static ConcoctiMixer MIXER;
    public static ConcoctiElectronCollector ELECTRON_COLLECTOR;

    private static <T extends ConcoctiMachine<?, ?, ?, ?, ?, ?, ?, ?, ?>> T register(T machine) {
        MACHINES.add(machine);
        return machine;
    }

    public static void register() {
        MELTER = register(new ConcoctiMelter());
        SOLIDIFIER = register(new ConcoctiSolidifier());
        ENERGY_GENERATOR = register(new ConcoctiEnergyGenerator());
        MIXER = register(new ConcoctiMixer());
        ELECTRON_COLLECTOR = register(new ConcoctiElectronCollector());
    }

    public static void forEach(Consumer<ConcoctiMachine<?, ?, ?, ?, ?, ?, ?, ?, ?>> consumer) {
        MACHINES.forEach(consumer);
    }

    private ConcoctiMachines() {}
}
