package io.github.laptop59.concocti.common.machine;

import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.HatchPurpose;
import io.github.laptop59.concocti.common.block.HatchType;
import io.github.laptop59.concocti.common.machine.impl.*;
import io.github.laptop59.concocti.common.multiblock.*;
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
    public static ConcoctiMultiBlockMachine CENTRIFUGE;

    public static int MAX_RADIUS_SEARCHABLE = 0;
    public static int MAX_SQ_RADIUS_SEARCHABLE = 0;
    public static int MAX_X_RADIUS_SEARCHABLE = 0;
    public static int MAX_Y_RADIUS_SEARCHABLE = 0;
    public static int MAX_Z_RADIUS_SEARCHABLE = 0;

    private static <T extends ConcoctiMachine<?, ?, ?, ?, ?, ?, ?, ?, ?>> T register(T machine) {
        MACHINES.add(machine);
        return machine;
    }

    private static <T extends ConcoctiMultiBlockMachine> T registerMultiblock(T machine) {
        updateRadii(machine.STRUCTURE);

        return register(machine);
    }

    private static void updateRadii(MultiblockStructure structure) {
        int xLen = structure.xLength(), yLen = structure.yLength(), zLen = structure.zLength();

        if (xLen > MAX_X_RADIUS_SEARCHABLE)
            MAX_X_RADIUS_SEARCHABLE = xLen;
        if (zLen > MAX_Z_RADIUS_SEARCHABLE)
            MAX_Z_RADIUS_SEARCHABLE = zLen;
        if (yLen > MAX_Y_RADIUS_SEARCHABLE)
            MAX_Y_RADIUS_SEARCHABLE = yLen;

        MAX_SQ_RADIUS_SEARCHABLE =
                MAX_X_RADIUS_SEARCHABLE * MAX_X_RADIUS_SEARCHABLE +
                MAX_Y_RADIUS_SEARCHABLE * MAX_Y_RADIUS_SEARCHABLE +
                MAX_Z_RADIUS_SEARCHABLE * MAX_Z_RADIUS_SEARCHABLE;

        MAX_RADIUS_SEARCHABLE = (int) Math.ceil(Math.sqrt(MAX_SQ_RADIUS_SEARCHABLE));
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

        MAGNETIC_SEPARATOR = registerMultiblock(new ConcoctiMultiBlockMachine(
                "concocti_magnetic_separator",
                100.0f,
                MultiblockStructure.from(MultiblockStructure.Builder.create(-1, -1, -2, 2, 4, 1), builder -> builder.load(
                            Map.of(
                                    'B', new MultiblockHatchAllowedPredicate(
                                            ConcoctiBlocks.getDeferredHatch(HatchPurpose.INPUT, HatchType.FLUID),
                                            ConcoctiBlocks.getDeferredHatch(HatchPurpose.OUTPUT, HatchType.ITEM),
                                            ConcoctiBlocks.getDeferredHatch(HatchPurpose.INPUT, HatchType.ENERGY)
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

        CENTRIFUGE = registerMultiblock(new ConcoctiMultiBlockMachine(
            "concocti_centrifuge",
            400.0f,
            MultiblockStructure.from(MultiblockStructure.Builder.create(-2, -1, -4, 3, 2, 1), builder -> builder.load(
                    Map.of(
                        'B', new MultiblockSimpleBlockPredicate(ConcoctiBlocks.TOUGH_CONCOCTI_BRICKS),
                        'H', new MultiblockHatchAllowedPredicate(
                            ConcoctiBlocks.getDeferredHatch(HatchPurpose.INPUT, HatchType.ITEM),
                            ConcoctiBlocks.getDeferredHatch(HatchPurpose.INPUT, HatchType.FLUID),
                            ConcoctiBlocks.getDeferredHatch(HatchPurpose.OUTPUT, HatchType.ITEM),
                            ConcoctiBlocks.getDeferredHatch(HatchPurpose.OUTPUT, HatchType.FLUID),
                            ConcoctiBlocks.getDeferredHatch(HatchPurpose.INPUT, HatchType.ENERGY)
                        ),
                        'C', new MultiblockSimpleBlockPredicate(ConcoctiBlocks.CONDUCTIVIUM_BLOCK),
                        'A', new MultiblockBlockTagPredicate(BlockTags.AIR),
                        'U', new MultiblockSimpleBlockPredicate(Blocks.CAULDRON),
                        '+', new MultiblockSimpleBlockPredicate(Blocks.GRINDSTONE)
                    ),
                    null,
                    "BBBBB BHHHB BBBBB",
                    "BBBBB HAUAH BBBBB",
                    "BBHBB HU+UH BBHBB",
                    "BBBBB HAUAH BBBBB",
                    "BBBBB BH HB BBBBB"
                )
            )
        ));

        Concocti.LOGGER.debug("Max radii searchable are: X = {}, Y = {}, Z = {} | Max searchable squared radius = {} | Max searchable radius = {}",
                MAX_X_RADIUS_SEARCHABLE,
                MAX_Y_RADIUS_SEARCHABLE,
                MAX_Z_RADIUS_SEARCHABLE,
                MAX_SQ_RADIUS_SEARCHABLE,
                MAX_RADIUS_SEARCHABLE
        );
    }

    public static void forEach(Consumer<ConcoctiMachine<?, ?, ?, ?, ?, ?, ?, ?, ?>> consumer) {
        MACHINES.forEach(consumer);
    }

    private ConcoctiMachines() {
    }
}
