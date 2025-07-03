package io.github.laptop59.concocti.datagen.client.language;

import io.github.laptop59.concocti.client.gui.components.SlotType;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluids;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.item.MoldItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
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

        // Blocks go here.
        add(ConcoctiBlocks.DIAMETHYST_BLOCK, "Block of Diamethyst");
        add(ConcoctiBlocks.PURIFIED_CONCOCTI_BLOCK, "Block of Purified Concocti");
        add(ConcoctiBlocks.DIRTY_CONCOCTI_BLOCK, "Block of Dirty Concocti");
        add(ConcoctiBlocks.TOUGH_CONCOCTI_BLOCK, "Block of Tough Concocti");
        add(ConcoctiBlocks.COMPRESSED_CONCOCTI_BLOCK, "Block of Compressed Concocti");
        add(ConcoctiBlocks.CONCOCTI_MELTER, "Concocti Melter");
        add(ConcoctiBlocks.CONCOCTI_SOLIDIFIER, "Concocti Solidifier");
        add(ConcoctiBlocks.BASIC_CONCOCTI_FRAME, "Basic Concocti Frame");
        add(ConcoctiBlocks.ADVANCED_CONCOCTI_FRAME, "Advanced Concocti Frame");

        // Items go here.
        add(ConcoctiItems.DIAMETHYST_CRYSTAL, "Diamethyst Crystal");
        add(ConcoctiItems.CONCOCTI_SEEDS, "Concocti Seeds");
        add(ConcoctiItems.DIRTY_CONCOCTI_INGOT, "Dirty Concocti Ingot");
        add(ConcoctiItems.DIRTY_CONCOCTI_NUGGET, "Dirty Concocti Nugget");
        add(ConcoctiItems.PURIFIED_CONCOCTI_INGOT, "Purified Concocti Ingot");
        add(ConcoctiItems.PURIFIED_CONCOCTI_NUGGET, "Purified Concocti Nugget");
        add(ConcoctiItems.TOUGH_CONCOCTI_INGOT, "Tough Concocti Ingot");
        add(ConcoctiItems.TOUGH_CONCOCTI_NUGGET, "Tough Concocti Nugget");
        add(ConcoctiItems.COMPRESSED_CONCOCTI_INGOT, "Compressed Concocti Ingot");
        add(ConcoctiItems.COMPRESSED_CONCOCTI_NUGGET, "Compressed Concocti Nugget");
        add(ConcoctiItems.MOLTEN_CONCOCTI_BUCKET, "Bucket of Molten Concocti");
        add(ConcoctiItems.MOLTEN_CONCOCTIZED_DIRT_BUCKET, "Bucket of Molten Concoctized Dirt");
        add(ConcoctiItems.COMPACT_CONCOCTI_UPGRADE, "Compact Concocti Upgrade");
        add(ConcoctiItems.COMPACTER_CONCOCTI_UPGRADE, "Compacter Concocti Upgrade");
        add(ConcoctiItems.COMPACTEST_CONCOCTI_UPGRADE, "Compactest Concocti Upgrade");

        // Fluid types go here.
        add(ConcoctiFluids.MOLTEN_CONCOCTI, "Molten Concocti");
        add(ConcoctiFluids.MOLTEN_CONCOCTIZED_DIRT, "Molten Concoctized Dirt");

        // Miscellaneous things go here.
        add("effect.concocti.concoctized", "Concoctized");
        add("subtitles.concocti.block.concocti_melter.fire_crackle", "Concocti Melter crackles");

        addCommonTag("ingots.concocti", "Concocti Ingots");
        addCommonTag("nuggets.concocti", "Concocti Nuggets");
        addCommonTag("storage_blocks.concocti", "Concocti Storage Blocks");

        // GUI elements go here.
        add("screen.concocti.energy_bar", "Energy: %s/%s FE");
        add("screen.concocti.fluid_bar", "%s: %s/%s mB");
        add("screen.concocti.fluid_amount", "%s mB");
        add("screen.concocti.no_upgrade", "Put a Concocti Upgrade here to speed up this block.");
        add("screen.concocti.upgrade_info", "Upgrade units: +%s");
        add("screen.concocti.duration", "Duration: %ss");
        add("screen.concocti.no_frame", "Put a Concocti Frame here to improve this block.");
        add("screen.concocti.frame_info_rate", "Additional Rate: +%s%%");
        add("screen.concocti.frame_info_efficiency", "Efficiency: %s%%");
        add("screen.concocti.single_upgrade_info", "(+%s each)");
        add("screen.concocti.open_machine_settings", "Open Machine Settings");
        add("screen.concocti.close_machine_settings", "Close Machine Settings");
        add("screen.concocti.machine_settings", "Settings");
        add("screen.concocti.durability_info", "Durability: %s/%s");

        add("screen.concocti.slot_compound", "%s (%s)");
        add("screen.concocti.slot_up", "Up");
        add("screen.concocti.slot_down", "Down");
        add("screen.concocti.slot_left", "Left");
        add("screen.concocti.slot_right", "Right");
        add("screen.concocti.slot_front", "Front");
        add("screen.concocti.slot_back", "Back");

        // Information of items go here.
        add("info.concocti.concocti_seeds", "Hitting a mob with this item usually provides it a status effect of Concoctized, which allows Dirty Concocti Nuggets to start dropping upon taking damage while losing less health.");
        add("info.concocti.dirty_concocti_nugget", "Dropped from hitting a mob with the Concoctized status effect, which is induced by hitting it with Concoctized Seeds.");

        // Translation key for molds.
        for (Map.Entry<MoldItem.Material, Map<MoldItem.Type, DeferredItem<? extends Item>>> entry : ConcoctiItems.MOLDS.entrySet()) {
            add(ConcoctiItems.MOLD_BASES.get(entry.getKey()).get(), getMoldBaseName(entry.getKey()));
            for (Map.Entry<MoldItem.Type, DeferredItem<? extends Item>> entry2 : entry.getValue().entrySet()) {
                MoldItem item = (MoldItem) entry2.getValue().get();
                add(item, getName(item));
            }
        }

        // Translation key for slot types.
        for (SlotType slotType : SlotType.values()) {
            String key = "screen.concocti.slot_type." + slotType.name().toLowerCase(Locale.ROOT);
            add(key, capitalize(slotType.name()));
        }
    }

    // ADDING REGISTERED ITEMS/BLOCKS/FLUIDS

    private void add(DeferredBlock<Block> block, String translation) {
        this.add(block.get(), translation);
    }

    private void add(DeferredItem<? extends Item> item, String translation) {
        this.add(item.get(), translation);
    }

    private void add(DeferredHolder<Fluid, FlowingFluid> fluid, String translation) {
        String name = BuiltInRegistries.FLUID.getKey(fluid.get()).getPath();
        this.add("block.concocti." + name, translation);
        this.add("fluid_type.concocti." + name, translation);
    }

    private void addCommonTag(String tag, String translation) {
        this.addTag("c." + tag, translation);
    }

    // ADDING TAGS
    // For EMI.
    private void addTag(String tag, String translation) {
        this.add("tag.concocti." + tag, translation);
    }

    protected String getName(MoldItem item) {
        return capitalize(item.getMaterial().prefix) + " " + capitalize(item.getType().id) + " Mold";
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