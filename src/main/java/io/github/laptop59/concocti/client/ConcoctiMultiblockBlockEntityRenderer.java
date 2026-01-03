package io.github.laptop59.concocti.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.laptop59.concocti.common.Concocti;
import io.github.laptop59.concocti.common.machine.ConcoctiMultiBlockMachine;
import io.github.laptop59.concocti.common.multiblock.MultiblockResult;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.Map;

public class ConcoctiMultiblockBlockEntityRenderer implements BlockEntityRenderer<ConcoctiMultiBlockMachine.BlockEntity> {
    BlockEntityRendererProvider.Context context;

    public ConcoctiMultiblockBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(ConcoctiMultiBlockMachine.BlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.buildPreview) return;
        var unmatching = blockEntity.unmatchingBlockStates;
        if (unmatching == null) return;
        // Now render the blocks.
        for (Map.Entry<BlockPos, MultiblockResult> entry : unmatching.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState required = entry.getValue().blockState();
            boolean isAir = entry.getValue().insteadWasAir();
            Level level = blockEntity.getLevel();
            assert level != null;
            poseStack.pushPose();
            poseStack.translate(pos.getX() - blockEntity.getBlockPos().getX(), pos.getY() - blockEntity.getBlockPos().getY(), pos.getZ() - blockEntity.getBlockPos().getZ());
            BlockState rendered = isAir ? required : Blocks.RED_STAINED_GLASS.defaultBlockState();
            float badScale = 1.01f;
            float badTranslation = (1 - badScale) / 2;
            int repeats = 1;
            if (isAir) {
                poseStack.scale(0.5f, 0.5f, 0.5f);
                poseStack.translate(0.5, 0.5, 0.5);
            } else {
                poseStack.scale(badScale, badScale, badScale);
                poseStack.translate(badTranslation, badTranslation, badTranslation);
                repeats = 2;
            }
            for (int i = 0; i < repeats; i++)
                // Using our ghost render type to render them through walls.
                context.getBlockRenderDispatcher().renderSingleBlock(
                        rendered,
                        poseStack,
                        bufferSource,
                        LightTexture.FULL_BRIGHT,
                        OverlayTexture.NO_OVERLAY,
                        ModelData.EMPTY,
                        ConcoctiClient.GHOST_RENDER_TYPE
                );
            poseStack.popPose();
        }
    }
}
