package com.ldtteam.blockui;

import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import org.lwjgl.opengl.GL11;

/**
 * Render utility functions.
 */
public final class Render
{
    private Render()
    {
        // Hide default constructor
    }

    /**
     * Draw an outlined untextured rectangle.
     * 
     * @param color argb
     */
    public static void drawOutlineRect(final PoseStack ms, final int x, final int y, final int w, final int h, final int color)
    {
        drawOutlineRect(ms, x, y, w, h, color, 1.0f);
    }

    /**
     * Draw an outlined untextured rectangle.
     * 
     * @param color argb
     */
    public static void drawOutlineRect(final PoseStack ms,
        final int x,
        final int y,
        final int w,
        final int h,
        final int color,
        final float lineWidth)
    {
        drawOutlineRect(ms.last()
            .pose(), x, y, w, h, (color >> 16) & 0xff, (color >> 8) & 0xff, color & 0xff, (color >> 24) & 0xff, lineWidth);
    }

    /**
     * Draw an outlined untextured rectangle.
     */
    public static void drawOutlineRect(final Matrix4f matrix,
        final int x,
        final int y,
        final int w,
        final int h,
        final int red,
        final int green,
        final int blue,
        final int alpha,
        final float lineWidth)
    {
        if (lineWidth <= 0.0F)
        {
            // If lineWidth is less than or equal to 0, a GL Error occurs
            return;
        }

        final BufferBuilder vertexBuffer = Tesselator.getInstance().getBuilder();

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        RenderSystem.lineWidth(lineWidth);
        // TODO: forge pr LINES_LOOP
        vertexBuffer.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        vertexBuffer.vertex(matrix, x, y, 0.0f).color(red, green, blue, alpha).endVertex();
        vertexBuffer.vertex(matrix, x + w, y, 0.0f).color(red, green, blue, alpha).endVertex();
        vertexBuffer.vertex(matrix, x + w, y + h, 0.0f).color(red, green, blue, alpha).endVertex();
        vertexBuffer.vertex(matrix, x, y + h, 0.0f).color(red, green, blue, alpha).endVertex();
        vertexBuffer.vertex(matrix, x, y, 0.0f).color(red, green, blue, alpha).endVertex();
        vertexBuffer.end();
        BufferUploader.end(vertexBuffer);

        RenderSystem.lineWidth(1.0F);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
