package io.github.laptop59.concocti.datagen;

import io.github.laptop59.concocti.datagen.client.ConcoctiBlockStateProvider;
import io.github.laptop59.concocti.datagen.client.ConcoctiItemModelProvider;
import io.github.laptop59.concocti.datagen.client.ConcoctiItemTextureProvider;
import io.github.laptop59.concocti.datagen.client.language.ConcoctiEnglishLanguageProvider;
import io.github.laptop59.concocti.datagen.server.ConcoctiBlockLootSubProvider;
import io.github.laptop59.concocti.datagen.server.ConcoctiBlockTagsProvider;
import io.github.laptop59.concocti.datagen.server.ConcoctiItemTagsProvider;
import io.github.laptop59.concocti.datagen.server.ConcoctiRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static io.github.laptop59.concocti.common.Concocti.MODID;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = "concocti")
public class ConcoctiDatagenHandler {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        // Data generators may require some of these as constructor parameters.
        // See below for more details on each of these.
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Register the client providers.
        generator.addProvider(event.includeClient(), new ConcoctiItemTextureProvider(output, MODID, existingFileHelper));
        generator.addProvider(event.includeClient(), new ConcoctiBlockStateProvider(output, MODID, existingFileHelper));
        generator.addProvider(event.includeClient(), new ConcoctiItemModelProvider(output, MODID, existingFileHelper));

        generator.addProvider(event.includeClient(), new ConcoctiEnglishLanguageProvider(output, MODID));

        // Register the server providers.
        ConcoctiBlockTagsProvider blockTagsProvider = new ConcoctiBlockTagsProvider(output, lookupProvider, MODID, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new ConcoctiItemTagsProvider(output, lookupProvider,
                blockTagsProvider.contentsGetter(), MODID, existingFileHelper
        ));
        generator.addProvider(event.includeServer(), new ConcoctiRecipeProvider(output, lookupProvider));

        // Add sub-providers for loot table generation.
        List<LootTableProvider.SubProviderEntry> subProviders = new ArrayList<>();
        subProviders.add(new LootTableProvider.SubProviderEntry(ConcoctiBlockLootSubProvider::new, LootContextParamSets.BLOCK));

        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(), subProviders, lookupProvider));
    }
}