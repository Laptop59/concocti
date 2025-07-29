package io.github.laptop59.concocti.common.machine;

import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.HatchPurpose;
import io.github.laptop59.concocti.common.block.HatchType;
import io.github.laptop59.concocti.common.machine.impl.*;
import io.github.laptop59.concocti.common.multiblock.MultiblockBlockTagPredicate;
import io.github.laptop59.concocti.common.multiblock.MultiblockSimpleBlockPredicate;
import io.github.laptop59.concocti.common.multiblock.MultiblockStructure;
import io.github.laptop59.concocti.common.multiblock.MultiblockToughConcoctiBrickLikePredicate;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;

public final class ConcoctiMachines {
    public static final ArrayList<ConcoctiMachine<?, ?, ?, ?, ?, ?, ?, ?, ?>> MACHINES = new ArrayList<>();

    public static ConcoctiMelter MELTER;
    public static ConcoctiSolidifier SOLIDIFIER;
    public static ConcoctiEnergyGenerator ENERGY_GENERATOR;
    public static ConcoctiMixer MIXER;
    public static ConcoctiElectronCollector ELECTRON_COLLECTOR;
    public static ConcoctiCrystallizer CRYSTALLIZER;
    public static ConcoctiCompressor COMPRESSOR;

    public static ConcoctiMultiBlockMachine MAGNETIC_SEPARATOR;

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
        CRYSTALLIZER = register(new ConcoctiCrystallizer());
        COMPRESSOR = register(new ConcoctiCompressor());

        // Multiblocks
        // Note: we do not need a predicate in the controller position.

        MAGNETIC_SEPARATOR = register(new ConcoctiMultiBlockMachine(
                "concocti_magnetic_separator",
                100.0f,
                MultiblockStructure.from(MultiblockStructure.Builder.create(-1, -1, 0, 1, 3, 2), builder -> builder.load(
                            Map.of(
                                    'B', new MultiblockToughConcoctiBrickLikePredicate(
                                            ConcoctiBlocks.getDeferredHatch(HatchPurpose.INPUT, HatchType.FLUID),
                                            ConcoctiBlocks.getDeferredHatch(HatchPurpose.OUTPUT, HatchType.ITEM)
                                    ),
                                    'C', new MultiblockSimpleBlockPredicate(ConcoctiBlocks.CONDUCTIVIUM_BLOCK),
                                    'A', new MultiblockBlockTagPredicate(BlockTags.AIR)
                            ),
                            null,
                            "BBB BBB BBB BBB BBB",
                            "BBB BCB BAB BCB BBB",
                            "BBB B B BBB BBB BBB"
                        )
                )
        ));
    }

    public static void forEach(Consumer<ConcoctiMachine<?, ?, ?, ?, ?, ?, ?, ?, ?>> consumer) {
        MACHINES.forEach(consumer);
    }

    private ConcoctiMachines() {
    }
}
