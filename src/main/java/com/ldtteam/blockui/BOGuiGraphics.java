package com.ldtteam.blockui;

import com.ldtteam.blockui.mod.item.BlockStateRenderingData;
import com.ldtteam.blockui.util.SingleBlockGetter.SingleBlockNeighborhood;
import com.ldtteam.blockui.util.cursor.Cursor;
import com.mojang.blaze3d.platform.cursor.CursorType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

public class BOGuiGraphics extends GuiGraphicsExtractor
{
    // Static instance should be fine since gui rendering is on single thread
    private static final SingleBlockNeighborhood NEIGHBORHOOD = new SingleBlockNeighborhood();

    private int cursorMaxDepth = -1;
    private CursorType selectedCursor = Cursor.DEFAULT;

    public BOGuiGraphics(final Minecraft mc, final CountingMatrix3x2fStack ps, final GuiRenderState renderState, final int mx, final int my)
    {
        super(mc, ps, renderState, mx, my);
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
        super.itemDecorations(getFont(itemStack), itemStack, x, y);
    }

    public void renderItemDecorations(final ItemStack itemStack, final int x, final int y, @Nullable final String altStackSize)
    {
        super.itemDecorations(getFont(itemStack), itemStack, x, y, altStackSize);
    }

    public int drawString(final String text, final int x, final int y, final int color)
    {
        return drawString(text, x, y, color, false);
    }

    public int drawString(final String text, final int x, final int y, final int color, final boolean shadow)
    {
        super.text(minecraft.font, text, x, y, color, shadow);
        return x + minecraft.font.width(text); // should return end pos
    }

    public void setCursor(final CursorType cursor)
    {
        final int size = ((CountingMatrix3x2fStack) pose()).size;
        if (size >= cursorMaxDepth)
        {
            cursorMaxDepth = size;
            selectedCursor = cursor;
        }
    }

    /**
     * @param debugXoffset debug string x offset
     */
    public CursorType applyCursor(final int debugXoffset)
    {
        if (Pane.debugging)
        {
            drawString(selectedCursor.toString(), debugXoffset, -minecraft.font.lineHeight, Color.getByName("white"));
        }

        // requestCursor(selectedCursor);
        // need to direct this to vanilla gui
        return selectedCursor;
    }

    /**
     * Render given blockState with model just like {@link #item(ItemStack, int, int)}
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

        // prepare pose just like itemStack rendering would do

        pose().pushPose();
        pose().last().normal().identity(); // reset normals cuz lighting
        pose().translate(8, 8, 150);
        pose().scale(16.0F, -16.0F, 16.0F);
        ClientHooks.handleCameraTransforms(pose(), itemModel, ItemDisplayContext.GUI, false);

        if (data.modelNeedsRotationFix())
        {
            final Matrix3f oldNormal = pose().last().normal();
            pose().pushPose();
            pose().rotateAround(Axis.YP.rotationDegrees(45), 0.0f, 0.5f, 0.0f);
            pose().last().normal().set(oldNormal.rotate(Axis.YP.rotationDegrees(-45)));
        }

        pose().translate(-0.5F, -0.5F, -0.5F);

        // render block and BE

        final int light = LightTexture.pack(15, 15);
        minecraft.getBlockRenderer()
            .renderSingleBlock(data.blockState(), pose(), bufferSource(), light, OverlayTexture.NO_OVERLAY, data.modelData(), null);
        if (data.blockEntity() != null)
        {
            try
            {
                minecraft.getBlockEntityRenderDispatcher()
                    .getRenderer(data.blockEntity())
                    .render(data.blockEntity(), 0, pose(), bufferSource(), light, OverlayTexture.NO_OVERLAY);
            }
            catch (final Exception e)
            {
                // well, noop then
            }
        }
        flush();

        if (data.modelNeedsRotationFix())
        {
            pose().popPose();
            pose().translate(-0.5F, -0.5F, -0.5F);
        }

        // render fluid

        final FluidState fluidState = data.blockState().getFluidState();
        if (!fluidState.isEmpty())
        {
            final RenderType renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);
            pushMvApplyPose();

            NEIGHBORHOOD.blockState = data.blockState();
            minecraft.getBlockRenderer()
                .renderLiquid(BlockPos.ZERO, NEIGHBORHOOD, bufferSource().getBuffer(renderType), data.blockState(), fluidState);

            bufferSource().endBatch(renderType);
            popMvPose();
        }

        pose().popPose();
    }

    public static double getAltSpeedFactor(final Minecraft mc)
    {
        return mc.hasAltDown() ? 5 : 1;
    }

    public ScreenRectangle calcTransformedPaneBounds(final Pane pane)
    {
        return new ScreenRectangle(0, 0, pane.getWidth(), pane.getHeight()).transformAxisAligned(pose());
    }

    public static class CountingMatrix3x2fStack extends Matrix3x2fStack
    {
        private int size = 0;

        public CountingMatrix3x2fStack(final int stackSize)
        {
            super(stackSize);
        }

        @Override
        public Matrix3x2fStack clear()
        {
            size = 0;
            return super.clear();
        }

        @Override
        public Matrix3x2fStack popMatrix()
        {
            size--;
            return super.popMatrix();
        }

        @Override
        public Matrix3x2fStack pushMatrix()
        {
            size++;
            return super.pushMatrix();
        }
    }
}
