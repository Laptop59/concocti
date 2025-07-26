package io.github.laptop59.concocti.datagen.client;

import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

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

        this.simpleBlock(ConcoctiBlocks.BASIC_CONCOCTI_FRAME.get());
        this.simpleBlock(ConcoctiBlocks.ADVANCED_CONCOCTI_FRAME.get());

        this.simpleBlock(ConcoctiBlocks.CONCOCTI_BRICKS.get());
        this.simpleBlock(ConcoctiBlocks.TOUGH_CONCOCTI_BRICKS.get());
    }
}
