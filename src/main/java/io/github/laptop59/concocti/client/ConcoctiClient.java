package io.github.laptop59.concocti.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.fluid.ConcoctiFluids;
import io.github.laptop59.concocti.common.machine.ConcoctiMachines;
import io.github.laptop59.concocti.common.machine.ConcoctiMultiBlockMachine;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

import static io.github.laptop59.concocti.common.Concocti.MODID;

@Mod(value = MODID, dist = Dist.CLIENT)
public class ConcoctiClient {
    public static Set<UUID> concoctizedEntities = Set.of();
    public static final Component UNCONSUMED = Component.translatable("screen.concocti.unconsumed").withStyle(style -> style.withItalic(false).withColor(0xC7C7C7));

    // For rendering through walls
    public static final RenderType GHOST_RENDER_TYPE = RenderType.create(
            "concocti:ghost",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true, // needs sorting for translucency
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_TRANSLUCENT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .setLayeringState(RenderStateShard.POLYGON_OFFSET_LAYERING)
                    .createCompositeState(true)
    );

    public static final RenderType GHOST_RENDER_CULL_TYPE = RenderType.create(
            "concocti:ghost_cull",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true, // needs sorting for translucency
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_TRANSLUCENT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setCullState(RenderStateShard.CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .setLayeringState(RenderStateShard.POLYGON_OFFSET_LAYERING)
                    .createCompositeState(true)
    );

    public ConcoctiClient(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.register(ConcoctiClient.class);
    }

    @SubscribeEvent
    public static void onRegisterRenderer(EntityRenderersEvent.RegisterRenderers event) {
        for (var machine : ConcoctiMachines.MACHINES.values()) {
            if (machine instanceof ConcoctiMultiBlockMachine multiBlockMachine) {
                event.registerBlockEntityRenderer(
                        multiBlockMachine.BLOCK_ENTITY.get(),
                        ConcoctiMultiblockBlockEntityRenderer::new
                );
                Concocti.LOGGER.info("Registered block entity renderer for multiblock " + machine.ID + ".");
            }
        }
    }

    @SubscribeEvent
    public static void onClientExtensions(RegisterClientExtensionsEvent event) {
        // Tell Minecraft how to render our custom Concocti fluids.
        ConcoctiFluids.forEach(fluid -> event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public @NotNull ResourceLocation getStillTexture() {
                return ResourceLocation.fromNamespaceAndPath(MODID, "block/" + fluid.ID + "_still");
            }

            @Override
            public @NotNull ResourceLocation getFlowingTexture() {
                return ResourceLocation.fromNamespaceAndPath(MODID, "block/" + fluid.ID + "_flow");
            }
        }, fluid.FLUID_TYPE));
    }

    public static MutableComponent getChanceComponent(float chance) {
        String chanceStr = String.format("%.2f", chance * 100);
        return Component.translatable("screen.concocti.chance", chanceStr).withStyle(style -> style.withItalic(false).withColor(ChatFormatting.GOLD));
    }
}
