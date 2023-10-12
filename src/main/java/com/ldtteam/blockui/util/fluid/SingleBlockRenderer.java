package com.ldtteam.blockui.util.fluid;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

/**
 * Utility class to render a single block.
 * Inspired by https://gist.github.com/XFactHD/337be1471ca4b60c1959dc02691a24fc
 * Loosely related to vanilla RebuildTask.compile in {@link net.minecraft.client.renderer.chunk.ChunkRenderDispatcher}
 */
public final class SingleBlockRenderer
{
    /**
     * Render a single block.
     *
     * @param block     the block to render.
     * @param modelData the model data.
     * @param poseStack pose stack.
     */
    public static void render(final BlockState block, final ModelData modelData, final PoseStack poseStack)
    {
        final Minecraft mc = Minecraft.getInstance();
        final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (!block.getFluidState().isEmpty())
        {
            final SingleBlockLevel level = new SingleBlockLevel(mc.level, block);
            final PoseStack.Pose pose = poseStack.last();
            final VertexConsumer consumer = new FluidVertexConsumer(buffers, block.getFluidState(), pose.pose(), pose.normal());
            mc.getBlockRenderer().renderLiquid(BlockPos.ZERO, level, consumer, block, block.getFluidState());
        }

        //noinspection ConstantConditions [renderType is nullable but not annotated]
        mc.getBlockRenderer().renderSingleBlock(block, poseStack, buffers, LightTexture.FULL_BLOCK, OverlayTexture.NO_OVERLAY, modelData, null);
        buffers.endBatch();
    }
}