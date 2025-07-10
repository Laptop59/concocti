package io.github.laptop59.concocti.common;

import com.mojang.logging.LogUtils;
import io.github.laptop59.concocti.client.gui.*;
import io.github.laptop59.concocti.common.block.AbstractConcoctiMachineBlock;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.entity.AbstractConcoctiMachineBlockEntity;
import io.github.laptop59.concocti.common.block.entity.AbstractPoweredBlockEntity;
import io.github.laptop59.concocti.common.block.entity.FluidHandlerBlockEntity;
import io.github.laptop59.concocti.common.block.entity.ItemHandlerBlockEntity;
import io.github.laptop59.concocti.common.effect.ConcoctizedMobEffect;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluids;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.machine.*;
import io.github.laptop59.concocti.common.menu.AbstractConcoctiMachineMenu;
import io.github.laptop59.concocti.common.poi.ConcoctiPoiTypes;
import io.github.laptop59.concocti.common.recipe.ProcessingRecipe;
import io.github.laptop59.concocti.integration.jei.AbstractConcoctiRecipeCategory;
import io.github.laptop59.concocti.network.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Concocti.MODID)
public class Concocti {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "concocti";

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // Creates a creative tab with the id "concocti:concocti" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CONCOCTI_TAB = ConcoctiRegisters.CREATIVE_MODE_TABS.register("concocti", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.concocti")) // The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ConcoctiItems.PURIFIED_CONCOCTI_INGOT.get().getDefaultInstance())
            .displayItems((parameters, output) -> ConcoctiItems.addItemsToCreativeTab(output)).build()
    );

    public static final DeferredHolder<MobEffect, MobEffect> CONCOCTIZED = ConcoctiRegisters.MOB_EFFECTS.register("concoctized",
            () -> new ConcoctizedMobEffect(MobEffectCategory.NEUTRAL, 0x9d57db)
    );

    @SubscribeEvent
    private static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        ConcoctiPayloads.registerPayloads(event);
    }

    @SubscribeEvent
    private static void registerScreens(RegisterMenuScreensEvent event) {
        for (var machine : ConcoctiMachines.MACHINES) {
            registerScreen(event, machine);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static <
            T extends AbstractConcoctiMachineBlockEntity<T, M, V, I, R>,
            M extends AbstractConcoctiMachineMenu<M>,
            V,
            I extends RecipeInput,
            R extends ProcessingRecipe<R, I>,
            Z extends RecipeSerializer<R>,
            B extends AbstractConcoctiMachineBlock,
            S extends AbstractConcoctiMachineScreen<M>,
            C extends AbstractConcoctiRecipeCategory<R>
    > void registerScreen(RegisterMenuScreensEvent event, ConcoctiMachine<T, M, V, I, R, Z, B, S, C> machine) {
        event.register(machine.MENU.get(), machine.getScreenConstructor());
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

        ConcoctiRegisters.BLOCKS.register(modEventBus);
        ConcoctiRegisters.ITEMS.register(modEventBus);
        ConcoctiRegisters.BLOCK_ENTITY_TYPES.register(modEventBus);
        ConcoctiMachines.register();
        ConcoctiRegisters.SOUND_EVENTS.register(modEventBus);
        ConcoctiRegisters.MENUS.register(modEventBus);
        ConcoctiRegisters.FLUIDS.register(modEventBus);
        ConcoctiRegisters.FLUID_TYPES.register(modEventBus);
        ConcoctiRegisters.RECIPE_SERIALIZERS.register(modEventBus);
        ConcoctiRegisters.RECIPE_TYPES.register(modEventBus);
        ConcoctiRegisters.POI_TYPES.register(modEventBus);

        ConcoctiRegisters.MOB_EFFECTS.register(modEventBus);
        ConcoctiRegisters.CREATIVE_MODE_TABS.register(modEventBus);

        initializeUninitializedStaticVariables(
                ConcoctiBlocks.class,
                ConcoctiItems.class,
                ConcoctiSounds.class,
                ConcoctiFluids.class,
                ConcoctiPoiTypes.class
        );

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Concocti) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        modEventBus.register(Concocti.class);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    /** As a hack for initializing static variables of a class, we can use this method. */
    private static void initializeUninitializedStaticVariables(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            Constructor<?>[] constructors = clazz.getConstructors();
            Constructor<?> constructor = Arrays.stream(constructors)
                    .filter(c -> c.getParameterCount() == 0)
                    .findFirst()
                    .orElse(null);
            if (constructor == null) throw new IllegalStateException(clazz + " has no non-parameterized constructor (should be created by the virtual machine by default).");
            try {
                // By doing this should also initialize static variables.
                constructor.newInstance();
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SubscribeEvent
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        ConcoctiRegisters.BLOCK_ENTITY_TYPES.getEntries().forEach(blockEntityTypeDeferredHolder -> {
            BlockEntityType<?> type = blockEntityTypeDeferredHolder.get();
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, type,
                    (o, direction) -> ((AbstractPoweredBlockEntity) o).energy);
            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, type, (o, direction) -> {
                if (o instanceof FluidHandlerBlockEntity fluidHandlerBlockEntity)
                    return fluidHandlerBlockEntity.getSidedFluidHandler(direction);
                return null; // Nothing happens if null is returned, at least that's what I think.
            });
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (o, direction) -> {
                if (o instanceof ItemHandlerBlockEntity itemHandlerBlockEntity)
                    return itemHandlerBlockEntity.getSidedItemHandler(direction);
                return null; // Nothing happens if null is returned, at least that's what I think.
            });
        });
    }

}
