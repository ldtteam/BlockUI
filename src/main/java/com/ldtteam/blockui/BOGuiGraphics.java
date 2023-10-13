package com.ldtteam.blockui;

import com.ldtteam.blockui.util.SingleBlockNeighborhood;
import com.ldtteam.blockui.util.cursor.Cursor;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class BOGuiGraphics extends GuiGraphics
{
    // Static instance should be fine since gui rendering is on single thread
    private static final SingleBlockNeighborhood NEIGHBORHOOD = new SingleBlockNeighborhood();

    private int cursorMaxDepth = -1;
    private Cursor selectedCursor = Cursor.DEFAULT;

    public BOGuiGraphics(final Minecraft mc, final PoseStack ps, final BufferSource buffers)
    {
        super(mc, ps, buffers);
    }

    private Font getFont(@Nullable final ItemStack itemStack)
    {
        if (itemStack != null)
        {
            final Font font = IClientItemExtensions.of(itemStack).getFont(itemStack, IClientItemExtensions.FontContext.ITEM_COUNT);
            if (font != null)
            {
                return font;
            }
        }
        return minecraft.font;
    }

    public void renderItemDecorations(final ItemStack itemStack, final int x, final int y)
    {
        super.renderItemDecorations(getFont(itemStack), itemStack, x, y);
    }

    public void renderItemDecorations(final ItemStack itemStack, final int x, final int y, @Nullable final String altStackSize)
    {
        super.renderItemDecorations(getFont(itemStack), itemStack, x, y, altStackSize);
    }

    public int drawString(final String text, final float x, final float y, final int color)
    {
        return drawString(text, x, y, color, false);
    }

    public int drawString(final String text, final float x, final float y, final int color, final boolean shadow)
    {
        return super.drawString(minecraft.font, text, x, y, color, shadow);
    }

    public void setCursor(final Cursor cursor)
    {
        if (pose().poseStack.size() >= cursorMaxDepth)
        {
            cursorMaxDepth = pose().poseStack.size();
            selectedCursor = cursor;
        }
    }

    public void applyCursor()
    {
        selectedCursor.apply();
    }

    /**
     * Render given blockState with model just like {@link #renderItem(ItemStack, int, int)}
     *
     * @param blockState blockState
     * @param modelData  model data for given blockState
     * @param itemStack  backing itemStack for given blockState
     */
    public void renderBlockStateAsItem(final BlockState blockState, final ModelData modelData, final ItemStack itemStack)
    {
        final BakedModel itemModel = minecraft.getItemRenderer().getModel(itemStack, null, null, 0);
        final boolean lighting = !itemModel.usesBlockLight();

        pose().pushPose();
        pose().translate(8, 8, 150);
        pose().mulPoseMatrix(new Matrix4f().scaling(1.0F, -1.0F, 1.0F));
        pose().scale(16.0F, 16.0F, 16.0F);
        ForgeHooksClient.handleCameraTransforms(pose(), itemModel, ItemDisplayContext.GUI, false);
        pose().translate(-0.5F, -0.5F, -0.5F);
        pushPvmToShader();

        if (lighting)
        {
            Lighting.setupForFlatItems();
        }
        else
        {
            Lighting.setupLevel(new Matrix4f());
        }

        minecraft.getBlockRenderer().renderSingleBlock(blockState, new PoseStack(), bufferSource(), LightTexture.pack(10, 10), OverlayTexture.NO_OVERLAY, modelData, null);
        flush();

        final FluidState fluidState = blockState.getFluidState();
        if (!fluidState.isEmpty())
        {
            final RenderType renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);

            NEIGHBORHOOD.blockState = blockState;
            minecraft.getBlockRenderer().renderLiquid(BlockPos.ZERO, NEIGHBORHOOD, bufferSource().getBuffer(renderType), blockState, fluidState);

            bufferSource().endBatch(renderType);
        }

        Lighting.setupFor3DItems();

        popPvmFromShader();
        pose().popPose();
    }

    public void pushPvmToShader()
    {
        RenderSystem.getModelViewStack().pushPose();
        RenderSystem.getModelViewStack().setIdentity();
        RenderSystem.getModelViewStack().mulPoseMatrix(pose().last().pose());
        RenderSystem.applyModelViewMatrix();
    }

    public void popPvmFromShader()
    {
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();
    }
}
