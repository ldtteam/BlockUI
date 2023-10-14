package com.ldtteam.blockui;

import com.ldtteam.blockui.mod.Log;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
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
     * @param data      blockState rendering data
     * @param itemStack backing itemStack for given blockState
     */
    public void renderBlockStateAsItem(final BlockStateRenderingData data, final ItemStack itemStack)
    {
        BakedModel itemModel = minecraft.getItemRenderer().getModel(itemStack, null, null, 0);
        if (!itemModel.isGui3d() || data.blockState().getRenderShape() == RenderShape.INVISIBLE)
        {
            // well, some items are bit dumb
            itemModel = minecraft.getItemRenderer().getModel(new ItemStack(Blocks.STONE), null, null, 0);
        }

        pose().pushPose();
        pose().translate(8, 8, 150);
        pose().mulPoseMatrix(new Matrix4f().scaling(1.0F, -1.0F, 1.0F));
        pose().scale(16.0F, 16.0F, 16.0F);
        ForgeHooksClient.handleCameraTransforms(pose(), itemModel, ItemDisplayContext.GUI, false);
        pose().translate(-0.5F, -0.5F, -0.5F);
        pushPvmToShader();

        Lighting.setupLevel(new Matrix4f());

        final int light = LightTexture.pack(10, 10);
        minecraft.getBlockRenderer()
            .renderSingleBlock(data.blockState,
                new PoseStack(),
                bufferSource(),
                light,
                OverlayTexture.NO_OVERLAY,
                data.modelData(),
                null);
        if (data.blockEntity != null)
        {
            try
            {
                minecraft.getBlockEntityRenderDispatcher()
                    .renderItem(data.blockEntity, new PoseStack(), bufferSource(), light, OverlayTexture.NO_OVERLAY);
            }
            catch (final Exception e)
            {
                // well, noop then
            }
        }
        flush();

        final FluidState fluidState = data.blockState.getFluidState();
        if (!fluidState.isEmpty())
        {
            final RenderType renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);

            NEIGHBORHOOD.blockState = data.blockState;
            minecraft.getBlockRenderer()
                .renderLiquid(BlockPos.ZERO, NEIGHBORHOOD, bufferSource().getBuffer(renderType), data.blockState, fluidState);

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

    public static record BlockStateRenderingData(BlockState blockState,
        BlockEntity blockEntity,
        ModelData modelData,
        boolean renderItemDecorations,
        boolean alwaysAddBlockStateTooltip)
    {
        public static final BlockPos ILLEGAL_BLOCK_ENTITY_POS = BlockPos.ZERO.below(1000);

        public static BlockStateRenderingData of(final BlockState blockState, final BlockEntity blockEntity)
        {
            ModelData model = ModelData.EMPTY;
            try
            {
                model = blockEntity.getModelData();
            }
            catch (final Exception e)
            {
                Log.getLogger().warn("Could not get model data for: " + blockState.toString(), e);
            }

            return new BlockStateRenderingData(blockState, blockEntity, model, true, false);
        }

        public static BlockStateRenderingData of(final BlockState blockState)
        {
            if (blockState.hasBlockEntity() && blockState.getBlock() instanceof final EntityBlock entityBlock)
            {
                final BlockEntity be = entityBlock.newBlockEntity(ILLEGAL_BLOCK_ENTITY_POS, blockState);
                if (be != null)
                {
                    return of(blockState, be);
                }
            }
            return new BlockStateRenderingData(blockState, null, null, true, false);
        }

        public BlockStateRenderingData withItemDecorations()
        {
            return renderItemDecorations ? this :
                new BlockStateRenderingData(blockState, blockEntity, modelData, true, alwaysAddBlockStateTooltip);
        }

        public BlockStateRenderingData withoutItemDecorations()
        {
            return !renderItemDecorations ? this :
                new BlockStateRenderingData(blockState, blockEntity, modelData, false, alwaysAddBlockStateTooltip);
        }

        public BlockStateRenderingData withForcedBlockStateTooltip()
        {
            return alwaysAddBlockStateTooltip ? this :
                new BlockStateRenderingData(blockState, blockEntity, modelData, renderItemDecorations, true);
        }

        public BlockStateRenderingData withoutForcedBlockStateTooltip()
        {
            return !alwaysAddBlockStateTooltip ? this :
                new BlockStateRenderingData(blockState, blockEntity, modelData, renderItemDecorations, false);
        }

        public ModelData modelData()
        {
            return modelData == null ? ModelData.EMPTY : modelData;
        }
    }
}
