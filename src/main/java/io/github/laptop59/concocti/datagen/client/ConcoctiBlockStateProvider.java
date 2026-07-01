package io.github.laptop59.concocti.datagen.client;

import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.ConcoctiHatchBlock;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluidParent;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluids;
import io.github.laptop59.concocti.common.machine.ConcoctiMachine;
import io.github.laptop59.concocti.common.machine.ConcoctiMachines;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ConcoctiBlockStateProvider extends BlockStateProvider {
    public ConcoctiBlockStateProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        this.simpleBlock(ConcoctiBlocks.DIAMETHYST_BLOCK.get());
        this.simpleBlock(ConcoctiBlocks.DIRTY_CONCOCTI_BLOCK.get());
        this.simpleBlock(ConcoctiBlocks.PURIFIED_CONCOCTI_BLOCK.get());
        this.simpleBlock(ConcoctiBlocks.TOUGH_CONCOCTI_BLOCK.get());
        this.simpleBlock(ConcoctiBlocks.COMPRESSED_CONCOCTI_BLOCK.get());
        this.simpleBlock(ConcoctiBlocks.CONDUCTIVIUM_BLOCK.get());
        this.simpleBlock(ConcoctiBlocks.CRYSTALIUM_ORE.get());
        this.simpleBlock(ConcoctiBlocks.LATTICIUM_BLOCK.get());
        this.simpleBlock(ConcoctiBlocks.SOLARIUM_BLOCK.get());

        this.simpleBlock(ConcoctiBlocks.LATTICIUM_FRAME.get());
        this.simpleBlock(ConcoctiBlocks.AUTOCLAVE_FRAME.get());

        this.simpleBlock(ConcoctiBlocks.BASIC_CONCOCTI_FRAME.get());
        this.simpleBlock(ConcoctiBlocks.ADVANCED_CONCOCTI_FRAME.get());

        this.simpleBlock(ConcoctiBlocks.CONCOCTI_BRICKS.get());
        this.simpleBlock(ConcoctiBlocks.TOUGH_CONCOCTI_BRICKS.get());

        for (DeferredBlock<? extends ConcoctiHatchBlock> deferredBlock : ConcoctiBlocks.HATCHES_LIST) {
            ConcoctiHatchBlock block = deferredBlock.get();
            this.simpleBlock(block);
        }

        for (ConcoctiMachine<?, ?, ?, ?, ?, ?, ?, ?, ?> machine : ConcoctiMachines.MACHINES) {
            Block block = machine.BLOCK.get();
            var builder = getVariantBuilder(block);
            builder.forAllStates(blockState -> getModelForConcoctiMachine(machine, blockState));
        }

        for (ConcoctiFluidParent fluidParent : ConcoctiFluids.FLUIDS) {
            if (fluidParent.BLOCK == null) continue; // Not an actual block to register.
            this.simpleBlock(fluidParent.BLOCK.get(), models().getExistingFile(modLoc("block/" + fluidParent.ID)));
        }
    }

    protected ConfiguredModel[] getModelForConcoctiMachine(ConcoctiMachine<?, ?, ?, ?, ?, ?, ?, ?, ?> machine, BlockState blockState) {
        String modelId = machine.ID;
        if (blockState.getValue(BlockStateProperties.LIT)) modelId += "_on";
        ModelFile.ExistingModelFile file = models().getExistingFile(this.modLoc(modelId));
        int yRotation = switch (blockState.getValue(HorizontalDirectionalBlock.FACING)) {
            case NORTH -> 0;
            case SOUTH -> 180;
            case WEST -> 270;
            case EAST -> 90;
            default -> throw new IllegalStateException("Unreachable statement - horizontal direction was expected, got UP or DOWN");
        };
        return ConfiguredModel.builder()
                .modelFile(file)
                .rotationY(yRotation)
                .build();
    }
}
