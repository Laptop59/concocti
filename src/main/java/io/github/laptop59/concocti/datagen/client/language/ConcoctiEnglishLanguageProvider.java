package io.github.laptop59.concocti.datagen.client.language;

import io.github.laptop59.concocti.client.gui.components.SlotType;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.ConcoctiHatchBlock;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluid;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluidParent;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluids;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.item.MoldItem;
import io.github.laptop59.concocti.common.machine.ConcoctiMachine;
import io.github.laptop59.concocti.common.machine.ConcoctiMachines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ConcoctiEnglishLanguageProvider extends LanguageProvider {
    public ConcoctiEnglishLanguageProvider(PackOutput output, String modid) {
        super(
                output,
                modid,
                "en_us"
        );
    }

    @Override
    protected void addTranslations() {
        // Normal translation keys go here.
        add("itemGroup.concocti", "Concocti");

        // Machines go here.
        add(ConcoctiMachines.MELTER, "Concocti Melter");
        add(ConcoctiMachines.SOLIDIFIER, "Concocti Solidifier");
        add(ConcoctiMachines.ENERGY_GENERATOR, "Concocti Energy Generator");
        add(ConcoctiMachines.MIXER, "Concocti Mixer");
        add(ConcoctiMachines.ELECTRON_COLLECTOR, "Concocti Electron Collector");
        add(ConcoctiMachines.CRYSTALLIZER, "Concocti Crystallizer");
        add(ConcoctiMachines.COMPRESSOR, "Concocti Compressor");
        add(ConcoctiMachines.MAGNETIC_SEPARATOR, "Concocti Magnetic Separator");
        add(ConcoctiMachines.CENTRIFUGE, "Concocti Centrifuge");

        // Blocks go here.
        add(ConcoctiBlocks.DIAMETHYST_BLOCK, "Block of Diamethyst");
        add(ConcoctiBlocks.PURIFIED_CONCOCTI_BLOCK, "Block of Purified Concocti");
        add(ConcoctiBlocks.DIRTY_CONCOCTI_BLOCK, "Block of Dirty Concocti");
        add(ConcoctiBlocks.TOUGH_CONCOCTI_BLOCK, "Block of Tough Concocti");
        add(ConcoctiBlocks.COMPRESSED_CONCOCTI_BLOCK, "Block of Compressed Concocti");
        add(ConcoctiBlocks.CONDUCTIVIUM_BLOCK, "Block of Conductivium");
        add(ConcoctiBlocks.BASIC_CONCOCTI_FRAME, "Basic Concocti Frame");
        add(ConcoctiBlocks.ADVANCED_CONCOCTI_FRAME, "Advanced Concocti Frame");
        add(ConcoctiBlocks.CONCOCTI_BRICKS, "Concocti Bricks");
        add(ConcoctiBlocks.TOUGH_CONCOCTI_BRICKS, "Tough Concocti Bricks");
        add(ConcoctiBlocks.CONDUCTIVIUM_LIGHTNING_ROD, "Conductivium Lightning Rod");
        add(ConcoctiBlocks.CRYSTALIUM_BLOCK, "Block of Crystalium");
        add(ConcoctiBlocks.CRYSTALIUM_ORE, "Crystalium Ore");
        add(ConcoctiBlocks.LATTICIUM_BLOCK, "Latticium Block");

        // Items go here.
        add(ConcoctiItems.DIAMETHYST_CRYSTAL, "Diamethyst Crystal");
        add(ConcoctiItems.CONCOCTI_SEEDS, "Concocti Seeds");
        add(ConcoctiItems.INFINITY_CONCOCTI_SEEDS, "Infinity Concocti Seeds");
        add(ConcoctiItems.DIRTY_CONCOCTI_INGOT, "Dirty Concocti Ingot");
        add(ConcoctiItems.DIRTY_CONCOCTI_NUGGET, "Dirty Concocti Nugget");
        add(ConcoctiItems.PURIFIED_CONCOCTI_INGOT, "Purified Concocti Ingot");
        add(ConcoctiItems.PURIFIED_CONCOCTI_NUGGET, "Purified Concocti Nugget");
        add(ConcoctiItems.TOUGH_CONCOCTI_INGOT, "Tough Concocti Ingot");
        add(ConcoctiItems.TOUGH_CONCOCTI_NUGGET, "Tough Concocti Nugget");
        add(ConcoctiItems.COMPRESSED_CONCOCTI_INGOT, "Compressed Concocti Ingot");
        add(ConcoctiItems.COMPRESSED_CONCOCTI_NUGGET, "Compressed Concocti Nugget");
        add(ConcoctiItems.CONDUCTIVIUM_NUGGET, "Conductivium Nugget");
        add(ConcoctiItems.CONDUCTIVIUM_INGOT, "Conductivium Ingot");

        add(ConcoctiFluids.MOLTEN_CONCOCTI.BUCKET, "Bucket of Molten Concocti");
        add(ConcoctiFluids.MOLTEN_TOUGH_CONCOCTI.BUCKET, "Bucket of Molten Tough Concocti");
        add(ConcoctiFluids.MOLTEN_CONCOCTIZED_DIRT.BUCKET, "Bucket of Molten Concoctized Dirt");
        add(ConcoctiFluids.MOLTEN_COPPER.BUCKET, "Bucket of Molten Copper");
        add(ConcoctiFluids.MOLTEN_CONDUCTIVIUM.BUCKET, "Bucket of Molten Conductivium");
        add(ConcoctiFluids.CRYSTALIUM_SOLUTION.BUCKET, "Bucket of Crystalium Solution");
        add(ConcoctiFluids.SUPERSATURATED_CRYSTALIUM_SOLUTION.BUCKET, "Bucket of Supersaturated Crystalium Solution");
        add(ConcoctiFluids.MOLTEN_LATTICIUM.BUCKET, "Bucket of Molten Latticium");

        add(ConcoctiItems.COMPACT_CONCOCTI_UPGRADE, "Compact Concocti Upgrade");
        add(ConcoctiItems.COMPACTER_CONCOCTI_UPGRADE, "Compacter Concocti Upgrade");
        add(ConcoctiItems.COMPACTEST_CONCOCTI_UPGRADE, "Compactest Concocti Upgrade");
        add(ConcoctiItems.ELECTROSTATIC_CONDUCTIVIUM_NUGGET, "Electrostatic Conductivium Nugget");
        add(ConcoctiItems.ELECTROSTATIC_CONDUCTIVIUM_INGOT, "Electrostatic Conductivium Ingot");
        add(ConcoctiItems.CRYSTALIUM_NUGGET, "Crystalium Nugget");
        add(ConcoctiItems.CRYSTALIUM_INGOT, "Crystalium Ingot");
        add(ConcoctiItems.RAW_CRYSTALIUM, "Raw Crystalium");
        add(ConcoctiItems.LATTICIUM_NUGGET, "Latticium Nugget");
        add(ConcoctiItems.LATTICIUM_INGOT, "Latticium Ingot");
        add(ConcoctiItems.DENSE_CONCOCTI_PELLET, "Dense Concocti Pellet");

        add(ConcoctiItems.Tags.CONCOCTI_UPGRADES, "Concocti Upgrades");
        add(ConcoctiItems.Tags.CONCOCTI_HATCHES, "Concocti Hatches");
        for (Map.Entry<MoldItem.Type, TagKey<Item>> moldType : ConcoctiItems.Tags.MOLDS.entrySet()) {
            add(moldType.getValue(), capitalize(moldType.getKey().id + "_molds"));
        }

        // Fluid types go here.
        add("fluid.concocti.empty", "Empty");

        add(ConcoctiFluids.MOLTEN_CONCOCTI, "Molten Concocti");
        add(ConcoctiFluids.MOLTEN_TOUGH_CONCOCTI, "Molten Tough Concocti");
        add(ConcoctiFluids.MOLTEN_CONCOCTIZED_DIRT, "Molten Concoctized Dirt");
        add(ConcoctiFluids.MOLTEN_COPPER, "Molten Copper");
        add(ConcoctiFluids.MOLTEN_CONDUCTIVIUM, "Molten Conductivium");
        add(ConcoctiFluids.MOLTEN_LIGHTNING, "Molten Lightning");
        add(ConcoctiFluids.CRYSTALIUM_SOLUTION, "Crystalium Solution");
        add(ConcoctiFluids.SUPERSATURATED_CRYSTALIUM_SOLUTION, "Supersaturated Crystalium Solution");
        add(ConcoctiFluids.MOLTEN_LATTICIUM, "Molten Latticium");
        add(ConcoctiFluids.MOLTEN_IRON, "Molten Iron");
        add(ConcoctiFluids.MOLTEN_REDSTONE, "Molten Redstone");

        // Miscellaneous things go here.
        add("effect.concocti.concoctized", "Concoctized");
        add("subtitles.concocti.block.concocti_melter.fire_crackle", "Concocti Melter crackles");
        add("subtitles.concocti.block.concocti_energy_generator.fire_crackle", "Concocti Energy Generator crackles");
        add("subtitles.concocti.block.concocti_mixer.fire_crackle", "Concocti Mixer crackles");

        addTag("dirty_concocti", "Dirty Concocti");
        addTag("purified_concocti", "Purified Concocti");
        addTag("tough_concocti", "Tough Concocti");
        addTag("compressed_concocti", "Compressed Concocti");

        addCommonTag("ingots.concocti", "Concocti Ingots");
        addCommonTag("nuggets.concocti", "Concocti Nuggets");
        addCommonTag("storage_blocks.concocti", "Concocti Storage Blocks");

        // GUI elements go here.
        add("screen.concocti.energy_bar", "Energy: %s (%s%%)");
        add("screen.concocti.fluid_bar", "%s: %s/%s mB");
        add("screen.concocti.fluid_amount", "%s mB");
        add("screen.concocti.no_upgrade", "Put a Concocti Upgrade here to speed up this block.");
        add("screen.concocti.upgrade_info", "Upgrade units: +%s");
        add("screen.concocti.duration", "Duration: %ss");
        add("screen.concocti.chance", "Chance: %s%%");
        add("screen.concocti.no_frame", "Put a Concocti Frame here to improve this block.");
        add("screen.concocti.frame_info_rate", "Additional Rate: +%s%%");
        add("screen.concocti.frame_info_efficiency", "Efficiency: %s%%");
        add("screen.concocti.single_upgrade_info", "(+%s each)");
        add("screen.concocti.open_machine_settings", "Open Machine Settings");
        add("screen.concocti.close_machine_settings", "Close Machine Settings");
        add("screen.concocti.machine_settings", "Settings");
        add("screen.concocti.durability_info", "Durability: %s/%s");
        add("screen.concocti.infinite_durability_info", "Durability: %s");
        add("screen.concocti.mold_base_durability_info", "Available Durability: %s");
        add("screen.concocti.rate", "Rate: x%s");
        add("screen.concocti.requirements", "Requirements:");
        add("screen.concocti.directly_on_top_of_machine", "Directly on top of machine");
        add("screen.concocti.struck_by_lightning", "Struck by lightning");
        add("screen.concocti.unconsumed", "Unconsumed");
        add("screen.concocti.valid", "Valid");
        add("screen.concocti.invalid", "Invalid");

        add("screen.concocti.slot_compound", "%s (%s)");
        add("screen.concocti.slot_up", "Up");
        add("screen.concocti.slot_down", "Down");
        add("screen.concocti.slot_left", "Left");
        add("screen.concocti.slot_right", "Right");
        add("screen.concocti.slot_front", "Front");
        add("screen.concocti.slot_back", "Back");
        add("screen.concocti.slot_east", "East");
        add("screen.concocti.slot_west", "West");
        add("screen.concocti.slot_north", "North");
        add("screen.concocti.slot_south", "South");

        add("screen.concocti.eject_off", "Eject: OFF");
        add("screen.concocti.eject_on", "Eject: ON");
        add("screen.concocti.pull_off", "Pull: OFF");
        add("screen.concocti.pull_on", "Pull: ON");
        add("screen.concocti.build_preview_off", "Build Preview: OFF");
        add("screen.concocti.build_preview_on", "Build Preview: ON");

        // Information of items go here.
        add("info.concocti.concocti_seeds", "Hitting a mob with this item usually provides it a status effect of Concoctized, which allows Dirty Concocti Nuggets and Ingots to start dropping upon taking damage while losing less health.");
        add("info.concocti.infinity_concocti_seeds", "A superior version to the generic Concocti Seeds which provide an infinite effect and are not consumed upon use.");
        add("info.concocti.dirty_concocti_nugget", "Drop from hitting a mob with the Concoctized status effect, induced by hitting it with Concoctized Seeds.");
        add("info.concocti.dirty_concocti_ingot", "Lucky drop from hitting a mob with the Concoctized status effect, induced by hitting it with Concoctized Seeds.");
        add("info.concocti.conductivium_lightning_rod", "Being more conductive than the copper variant of this block, even without thunderstorms or rain, lightning strikes can still hit this rod.");
        add("info.concocti.concocti_electron_collector", "Collects electrons from lightning strikes struck on the Conductivium Lightning Rod above it and converts them into a fluid called Molten Lightning.");

        // Translation key for molds.
        for (Map.Entry<MoldItem.Material, Map<MoldItem.Type, DeferredItem<? extends Item>>> entry : ConcoctiItems.MOLDS.entrySet()) {
            add(ConcoctiItems.MOLD_BASES.get(entry.getKey()).get(), getMoldBaseName(entry.getKey()));
            for (Map.Entry<MoldItem.Type, DeferredItem<? extends Item>> entry2 : entry.getValue().entrySet()) {
                MoldItem item = (MoldItem) entry2.getValue().get();
                add(item, getName(item));
            }
        }

        for (DeferredBlock<? extends ConcoctiHatchBlock> deferredBlock : ConcoctiBlocks.HATCHES_LIST) {
            ConcoctiHatchBlock block = deferredBlock.get();
            add(block, getName(block));
        }

        // Translation key for tank types.
        for (SlotType slotType : SlotType.values()) {
            String key = "screen.concocti.slot_type." + slotType.name().toLowerCase(Locale.ROOT);
            add(key, capitalize(slotType.name()));
        }
    }

    // ADDING REGISTERED ITEMS/BLOCKS/FLUIDS

    private void add(DeferredBlock<Block> block, String translation) {
        this.add(block.get(), translation);
    }

    private void add(ConcoctiMachine<?, ?, ?, ?, ?, ?, ?, ?, ?> concoctiMachine, String translation) {
        this.add(concoctiMachine.BLOCK.get(), translation);
        this.add("emi.category.concocti." + concoctiMachine.ID, translation);
    }

    private void add(DeferredItem<? extends Item> item, String translation) {
        this.add(item.get(), translation);
    }

    private void add(ConcoctiFluidParent fluid, String translation) {
        this.add("block.concocti." + fluid.ID, translation);
        this.add("fluid_type.concocti." + fluid.ID, translation);
    }

    private void addCommonTag(String tag, String translation) {
        this.add("tag.c." + tag, translation);
    }

    // ADDING TAGS
    // For EMI.
    private void addTag(String tag, String translation) {
        this.add("tag.concocti." + tag, translation);
    }

    protected String getName(MoldItem item) {
        return capitalize(item.getMaterial().prefix) + " " + capitalize(item.getType().id) + " Mold";
    }

    protected String getName(ConcoctiHatchBlock block) {
        return "Concocti " + capitalize(block.getType().getId()) + " " + capitalize(block.getPurpose().getId()) + " Hatch";
    }

    protected String getName(String str) {
        return capitalize(str);
    }

    protected String getMoldBaseName(MoldItem.Material material) {
        return capitalize(material.prefix) + " Mold Base";
    }

    protected static String capitalize(String name) {
        if (name.isEmpty()) return name;
        return Arrays
                .stream(name.split("_"))
                .map(str -> str.substring(0, 1).toUpperCase(Locale.ROOT) + str.substring(1).toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(" "));
    }
}