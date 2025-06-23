package io.github.laptop59.concocti.common;

import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluids;
import io.github.laptop59.concocti.client.gui.ConcoctiMelterScreen;
import io.github.laptop59.concocti.client.gui.ConcoctiSolidifierScreen;
import io.github.laptop59.concocti.common.effect.ConcoctizedMobEffect;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.menu.ConcoctiMenus;
import io.github.laptop59.concocti.network.ConcoctizedEntitiesPayload;
import io.github.laptop59.concocti.network.ConcoctizedEntitiesPayloadHandler;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Concocti.MODID)
public class Concocti {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "concocti";

    // Directly reference a slf4j logger
    static final Logger LOGGER = LogUtils.getLogger();

    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "concocti" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a creative tab with the id "concocti:concocti" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CONCOCTI_TAB = CREATIVE_MODE_TABS.register("concocti", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.concocti")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ConcoctiItems.PURIFIED_CONCOCTI_INGOT.get().getDefaultInstance())
            .displayItems((parameters, output) -> ConcoctiItems.addItemsToCreativeTab(output)).build()
    );

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MODID);
    public static final DeferredHolder<MobEffect, MobEffect> CONCOCTIZED = MOB_EFFECTS.register("concoctized",
            () -> new ConcoctizedMobEffect(MobEffectCategory.NEUTRAL, 0x9d57db)
    );

    @SubscribeEvent
    private static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        // Sets the current network version
        final PayloadRegistrar registrar = event.registrar("1");
        // When a piglin is zombified, update all players' concoctized entities.
        registrar.playToClient(
                ConcoctizedEntitiesPayload.TYPE,
                ConcoctizedEntitiesPayload.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ConcoctizedEntitiesPayloadHandler::handleData, ConcoctizedEntitiesPayloadHandler::handleData
                )
        );
    }

    @SubscribeEvent
    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ConcoctiMenus.CONCOCTI_MELTER_MENU.get(), ConcoctiMelterScreen::new);
        event.register(ConcoctiMenus.CONCOCTI_SOLIDIFIER_MENU.get(), ConcoctiSolidifierScreen::new);
    }

    @SubscribeEvent
    private static void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        Concocti.LOGGER.info("Concocti is loading!");
    }

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Concocti(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the commonSetup method for modloading
        NeoForge.EVENT_BUS.register(ConcoctiEventHandler.class);

        ConcoctiItems.ITEMS.register(modEventBus);
        ConcoctiBlocks.BLOCKS.register(modEventBus);
        ConcoctiBlocks.BLOCK_ENTITY_TYPES.register(modEventBus);
        ConcoctiSounds.SOUND_EVENTS.register(modEventBus);
        ConcoctiMenus.MENUS.register(modEventBus);
        ConcoctiFluids.FLUIDS.register(modEventBus);
        ConcoctiFluids.FLUID_TYPES.register(modEventBus);

        MOB_EFFECTS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Concocti) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        modEventBus.register(Concocti.class);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ConcoctiBlocks.CONCOCTI_MELTER_BLOCK_ENTITY.get(), (o, direction) -> o.energy);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ConcoctiBlocks.CONCOCTI_MELTER_BLOCK_ENTITY.get(), (o, direction) -> o.fluids);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ConcoctiBlocks.CONCOCTI_MELTER_BLOCK_ENTITY.get(), (o, direction) -> o.itemHandler);

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ConcoctiBlocks.CONCOCTI_SOLIDIFIER_BLOCK_ENTITY.get(), (o, direction) -> o.energy);
        // event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ConcoctiBlocks.CONCOCTI_MELTER_BLOCK_ENTITY.get(), (o, direction) -> o.fluids);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ConcoctiBlocks.CONCOCTI_SOLIDIFIER_BLOCK_ENTITY.get(), (o, direction) -> o.itemHandler);
    }

}
