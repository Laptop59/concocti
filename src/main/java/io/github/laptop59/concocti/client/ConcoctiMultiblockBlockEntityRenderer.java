package io.github.laptop59.concocti.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.ConcoctiHatchBlock;
import io.github.laptop59.concocti.common.block.Hatch;
import io.github.laptop59.concocti.common.item.ConcoctiItems;
import io.github.laptop59.concocti.common.machine.ConcoctiMultiBlockMachine;
import io.github.laptop59.concocti.common.multiblock.MultiblockResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public class ConcoctiMultiblockBlockEntityRenderer implements BlockEntityRenderer<ConcoctiMultiBlockMachine.BlockEntity> {
    BlockEntityRendererProvider.Context context;

    public ConcoctiMultiblockBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    public Optional<BlockItem> getHatch(Item item) {
        if (item instanceof BlockItem) {
            return ConcoctiItems.HATCHES
                    .stream()
                    .filter(hatch -> hatch.get() == item)
                    .findFirst()
                    .map(DeferredHolder::get);
        }
        return Optional.empty();
    }

    @Override
    public void render(ConcoctiMultiBlockMachine.BlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.buildPreview) return;
        var unmatching = blockEntity.unmatchingBlockStates;
        if (unmatching == null) return;
        // Now render the blocks.
        LocalPlayer player = Minecraft.getInstance().player;
        Optional<BlockItem> selectedHatchPreview = Optional.empty();
        if (player != null) {
            selectedHatchPreview = getHatch(player.getMainHandItem().getItem());
            if (selectedHatchPreview.isEmpty()) {
                selectedHatchPreview = getHatch(player.getOffhandItem().getItem());
            }
        }
        if (selectedHatchPreview.isPresent() && selectedHatchPreview.get().getBlock() instanceof ConcoctiHatchBlock hatchBlock) {
            // Get all positions for which a hatch is accepted.
            ArrayList<BlockPos> poses = blockEntity.hatchPositionsMap.get(new Hatch(hatchBlock.getType(), hatchBlock.getPurpose()));
            if (poses != null)
                for (BlockPos pos : poses) {
                    BlockState rendered = ConcoctiBlocks.GOOD_BLOCK.get().defaultBlockState();
                    poseStack.pushPose();
                    poseStack.translate(pos.getX() - blockEntity.getBlockPos().getX(), pos.getY() - blockEntity.getBlockPos().getY(), pos.getZ() - blockEntity.getBlockPos().getZ());
                    // Using our ghost render type to render them through walls.
                    context.getBlockRenderDispatcher().renderSingleBlock(
                            rendered,
                            poseStack,
                            bufferSource,
                            LightTexture.FULL_BRIGHT,
                            OverlayTexture.NO_OVERLAY,
                            ModelData.EMPTY,
                            ConcoctiClient.GHOST_RENDER_CULL_TYPE
                    );
                    poseStack.popPose();
                }
        }
        for (Map.Entry<BlockPos, MultiblockResult> entry : unmatching.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState required = entry.getValue().blockState();
            boolean isAir = entry.getValue().insteadWasAir();
            Level level = blockEntity.getLevel();
            assert level != null;
            poseStack.pushPose();
            poseStack.translate(pos.getX() - blockEntity.getBlockPos().getX(), pos.getY() - blockEntity.getBlockPos().getY(), pos.getZ() - blockEntity.getBlockPos().getZ());
            BlockState rendered = isAir ? required : ConcoctiBlocks.BAD_BLOCK.get().defaultBlockState();
            float badScale = 1.01f;
            float badTranslation = (1 - badScale) / 2;
            if (isAir) {
                poseStack.scale(0.5f, 0.5f, 0.5f);
                poseStack.translate(0.5, 0.5, 0.5);
            } else {
                poseStack.scale(badScale, badScale, badScale);
                poseStack.translate(badTranslation, badTranslation, badTranslation);
            }
            // Using our ghost render type to render them through walls.
            context.getBlockRenderDispatcher().renderSingleBlock(
                    rendered,
                    poseStack,
                    bufferSource,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    ModelData.EMPTY,
                    isAir ? ConcoctiClient.GHOST_RENDER_TYPE : ConcoctiClient.GHOST_RENDER_CULL_TYPE
            );
            poseStack.popPose();
        }
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(ConcoctiMultiBlockMachine.@NotNull BlockEntity blockEntity) {
        return AABB.INFINITE;
    }
}
