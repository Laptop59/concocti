package io.github.laptop59.concocti.datagen.client;

import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluidParent;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluids;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.item.MoldItem;
import io.github.laptop59.concocti.common.machine.ConcoctiMachine;
import io.github.laptop59.concocti.common.machine.ConcoctiMachines;
import io.github.laptop59.concocti.common.machine.ConcoctiMultiBlockMachine;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

public class ConcoctiBlockModelProvider extends BlockModelProvider {
    public ConcoctiBlockModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (ConcoctiFluidParent fluidParent : ConcoctiFluids.FLUIDS) {
            if (fluidParent.BLOCK == null) continue; // Not an actual block to register
            withExistingParent(fluidParent.ID, mcLoc("block/block"))
                    .texture("particle", modLoc("block/" + fluidParent.ID + "_still"));
        }

        ConcoctiMachines.forEach(machine -> {
            if (machine instanceof ConcoctiMultiBlockMachine multiBlockMachine) {
                ResourceLocation OFF = modLoc("block/" + multiBlockMachine.ID + "_front");
                ResourceLocation ON = modLoc("block/" + multiBlockMachine.ID + "_front_on");
                ResourceLocation OTHER = modLoc("block/" + ConcoctiBlocks.TOUGH_CONCOCTI_BRICKS.getId().getPath());

                withExistingParent(machine.ID, mcLoc("block/orientable"))
                        .texture("top", OTHER).texture("front", OFF).texture("side", OTHER);
                withExistingParent(machine.ID + "_on", mcLoc("block/orientable"))
                        .texture("top", OTHER).texture("front", ON).texture("side", OTHER);
            }
        });
    }
}
