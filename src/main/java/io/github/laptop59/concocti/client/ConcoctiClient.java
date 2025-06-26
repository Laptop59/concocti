package io.github.laptop59.concocti.client;

import io.github.laptop59.concocti.common.fluid.ConcoctiFluids;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

import static io.github.laptop59.concocti.common.Concocti.MODID;

@Mod(value = MODID, dist = Dist.CLIENT)
public class ConcoctiClient {
    public static Set<UUID> concoctizedEntities = Set.of();

    public ConcoctiClient(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.register(ConcoctiClient.class);
    }

    @SubscribeEvent
    public static void onClientExtensions(RegisterClientExtensionsEvent event) {
        // Tell Minecraft how to render our custom Concocti fluids.
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public @NotNull ResourceLocation getStillTexture() {
                return ResourceLocation.fromNamespaceAndPath(MODID, "block/molten_concocti_still");
            }
            @Override
            public @NotNull ResourceLocation getFlowingTexture() {
                return ResourceLocation.fromNamespaceAndPath(MODID, "block/molten_concocti_flow");
            }
        }, ConcoctiFluids.MOLTEN_CONCOCTI_FLUID_TYPE);
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public @NotNull ResourceLocation getStillTexture() {
                return ResourceLocation.fromNamespaceAndPath(MODID, "block/molten_concoctized_dirt_still");
            }
            @Override
            public @NotNull ResourceLocation getFlowingTexture() {
                return ResourceLocation.fromNamespaceAndPath(MODID, "block/molten_concoctized_dirt_flow");
            }
        }, ConcoctiFluids.MOLTEN_CONCOCTIZED_DIRT_FLUID_TYPE);
    }
}
