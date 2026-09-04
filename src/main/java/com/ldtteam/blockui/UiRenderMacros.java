package com.ldtteam.blockui;

import com.ldtteam.blockui.mod.BlockUI;
import com.ldtteam.blockui.util.color.IColour;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling.NineSlice;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling.Tile;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.fml.loading.FMLEnvironment;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Our replacement for GuiComponent.
 */
public class UiRenderMacros
{
    public static final double HALF_BIAS = 0.5;
    /** alpha/blending enabled by default */
    public static final RenderPipeline GUI_POS_COLOR_TRIANGLES = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
        .withLocation(BlockUI.resLoc("gui_pos_color_triangles"))
        .withVertexShader("core/position_color")
        .withFragmentShader("core/position_color")
        .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
        .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
        .build();
    /** alpha/blending enabled by default */
    public static final RenderPipeline GUI_POS_TEX_TRIANGLES = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
        .withLocation(BlockUI.resLoc("gui_pos_tex_triangles"))
        .withVertexShader("core/position_tex")
        .withFragmentShader("core/position_tex")
        .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
        .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
        .build();
    /** alpha/blending enabled by default */
    public static final RenderPipeline GUI_POS_TEX_COLOR_TRIANGLES = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
        .withLocation(BlockUI.resLoc("gui_pos_tex_color_triangles"))
        .withVertexShader("core/position_tex_color")
        .withFragmentShader("core/position_tex_color")
        .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
        .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
        .build();
    /** alpha/blending enabled by default */
    public static final RenderPipeline GUI_POS_COLOR_LINES = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
        .withLocation(BlockUI.resLoc("gui_pos_color_lines"))
        .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
        .withPrimitiveTopology(PrimitiveTopology.LINES)
        .build();

    public static void drawLineRectGradient(final GuiGraphicsExtractor ps,
        final int x,
        final int y,
        final int w,
        final int h,
        final int argbColorStart,
        final int argbColorEnd)
    {
        drawLineRectGradient(ps, x, y, w, h, argbColorStart, argbColorEnd, 1);
    }

    public static void drawLineRectGradient(final GuiGraphicsExtractor ps,
        final int x,
        final int y,
        final int w,
        final int h,
        final int argbColorStart,
        final int argbColorEnd,
        final int lineWidth)
    {
        drawLineRectGradient(ps,
            x,
            y,
            w,
            h,
            (argbColorStart >> 16) & 0xff,
            (argbColorEnd >> 16) & 0xff,
            (argbColorStart >> 8) & 0xff,
            (argbColorEnd >> 8) & 0xff,
            argbColorStart & 0xff,
            argbColorEnd & 0xff,
            (argbColorStart >> 24) & 0xff,
            (argbColorEnd >> 24) & 0xff,
            lineWidth);
    }

    public static void drawLineRectGradient(final GuiGraphicsExtractor ps,
        final int x,
        final int y,
        final int w,
        final int h,
        final int redStart,
        final int redEnd,
        final int greenStart,
        final int greenEnd,
        final int blueStart,
        final int blueEnd,
        final int alphaStart,
        final int alphaEnd,
        final int lineWidth)
    {
        if (lineWidth < 1 || (alphaStart == 0 && alphaEnd == 0))
        {
            return;
        }

        submitNoTex(ps, GUI_POS_COLOR_TRIANGLES, x, y, w, h, (m, buffer) -> {
            populateFillTriangles(m, buffer, x, y, w, lineWidth, redStart, greenStart, blueStart, alphaStart);
            populateFillGradientTriangles(m,
                buffer,
                x,
                y + lineWidth,
                lineWidth,
                h - 2 * lineWidth,
                redStart,
                redEnd,
                greenStart,
                greenEnd,
                blueStart,
                blueEnd,
                alphaStart,
                alphaEnd);
            populateFillGradientTriangles(m,
                buffer,
                x + w - lineWidth,
                y + lineWidth,
                lineWidth,
                h - 2 * lineWidth,
                redStart,
                redEnd,
                greenStart,
                greenEnd,
                blueStart,
                blueEnd,
                alphaStart,
                alphaEnd);
            populateFillTriangles(m, buffer, x, y + h - lineWidth, w, lineWidth, redEnd, greenEnd, blueEnd, alphaEnd);
        });
    }

    public static void drawLineRect(final GuiGraphicsExtractor ps,
        final int x,
        final int y,
        final int w,
        final int h,
        final int argbColor)
    {
        drawLineRect(ps, x, y, w, h, argbColor, 1);
    }

    public static void drawLineRect(final GuiGraphicsExtractor ps,
        final int x,
        final int y,
        final int w,
        final int h,
        final int argbColor,
        final int lineWidth)
    {
        drawLineRect(ps,
            x,
            y,
            w,
            h,
            (argbColor >> 16) & 0xff,
            (argbColor >> 8) & 0xff,
            argbColor & 0xff,
            (argbColor >> 24) & 0xff,
            lineWidth);
    }

    public static void drawLineRect(final GuiGraphicsExtractor ps,
        final int x,
        final int y,
        final int w,
        final int h,
        final int red,
        final int green,
        final int blue,
        final int alpha,
        final int lineWidth)
    {
        if (lineWidth < 1 || alpha == 0)
        {
            return;
        }

        submitNoTex(ps, GUI_POS_COLOR_TRIANGLES, x, y, w, h, (m, buffer) -> {
            populateFillTriangles(m, buffer, x, y, w, lineWidth, red, green, blue, alpha);
            populateFillTriangles(m, buffer, x, y + lineWidth, lineWidth, h - 2 * lineWidth, red, green, blue, alpha);
            populateFillTriangles(m, buffer, x + w - lineWidth, y + lineWidth, lineWidth, h - 2 * lineWidth, red, green, blue, alpha);
            populateFillTriangles(m, buffer, x, y + h - lineWidth, w, lineWidth, red, green, blue, alpha);
        });
    }

    public static void fill(final GuiGraphicsExtractor ps, final int x, final int y, final int w, final int h, final int argbColor)
    {
        fill(ps, x, y, w, h, (argbColor >> 16) & 0xff, (argbColor >> 8) & 0xff, argbColor & 0xff, (argbColor >> 24) & 0xff);
    }

    public static void fill(final GuiGraphicsExtractor ps,
        final int x,
        final int y,
        final int w,
        final int h,
        final int red,
        final int green,
        final int blue,
        final int alpha)
    {
        if (alpha == 0)
        {
            return;
        }

        submitNoTex(ps,
            GUI_POS_COLOR_TRIANGLES,
            x,
            y,
            w,
            h,
            (m, buffer) -> populateFillTriangles(m, buffer, x, y, w, h, red, green, blue, alpha));
    }

    public static void fillGradient(final GuiGraphicsExtractor ps,
        final int x,
        final int y,
        final int w,
        final int h,
        final int argbColorStart,
        final int argbColorEnd)
    {
        fillGradient(ps,
            x,
            y,
            w,
            h,
            (argbColorStart >> 16) & 0xff,
            (argbColorEnd >> 16) & 0xff,
            (argbColorStart >> 8) & 0xff,
            (argbColorEnd >> 8) & 0xff,
            argbColorStart & 0xff,
            argbColorEnd & 0xff,
            (argbColorStart >> 24) & 0xff,
            (argbColorEnd >> 24) & 0xff);
    }

    public static void fillGradient(final GuiGraphicsExtractor ps,
        final int x,
        final int y,
        final int w,
        final int h,
        final int redStart,
        final int redEnd,
        final int greenStart,
        final int greenEnd,
        final int blueStart,
        final int blueEnd,
        final int alphaStart,
        final int alphaEnd)
    {
        if (alphaStart == 0 && alphaEnd == 0)
        {
            return;
        }

        submitNoTex(ps,
            GUI_POS_COLOR_TRIANGLES,
            x,
            y,
            w,
            h,
            (m, buffer) -> populateFillGradientTriangles(m,
                buffer,
                x,
                y,
                w,
                h,
                redStart,
                redEnd,
                greenStart,
                greenEnd,
                blueStart,
                blueEnd,
                alphaStart,
                alphaEnd));
    }

    public static void hLine(final GuiGraphicsExtractor ps, final int x, final int xEnd, final int y, final int argbColor)
    {
        line(ps, x, y, xEnd, y, (argbColor >> 16) & 0xff, (argbColor >> 8) & 0xff, argbColor & 0xff, (argbColor >> 24) & 0xff);
    }

    public static void hLine(final GuiGraphicsExtractor ps,
        final int x,
        final int xEnd,
        final int y,
        final int red,
        final int green,
        final int blue,
        final int alpha)
    {
        line(ps, x, y, xEnd, y, red, green, blue, alpha);
    }

    public static void vLine(final GuiGraphicsExtractor ps, final int x, final int y, final int yEnd, final int argbColor)
    {
        line(ps, x, y, x, yEnd, (argbColor >> 16) & 0xff, (argbColor >> 8) & 0xff, argbColor & 0xff, (argbColor >> 24) & 0xff);
    }

    public static void vLine(final GuiGraphicsExtractor ps,
        final int x,
        final int y,
        final int yEnd,
        final int red,
        final int green,
        final int blue,
        final int alpha)
    {
        line(ps, x, y, x, yEnd, red, green, blue, alpha);
    }

    public static void line(final GuiGraphicsExtractor ps,
        final int x,
        final int y,
        final int xEnd,
        final int yEnd,
        final int argbColor)
    {
        line(ps, x, y, xEnd, yEnd, (argbColor >> 16) & 0xff, (argbColor >> 8) & 0xff, argbColor & 0xff, (argbColor >> 24) & 0xff);
    }

    public static void line(final GuiGraphicsExtractor ps,
        final int x,
        final int y,
        final int xEnd,
        final int yEnd,
        final int red,
        final int green,
        final int blue,
        final int alpha)
    {
        if (alpha == 0)
        {
            return;
        }

        submitNoTex(ps, GUI_POS_COLOR_LINES, x, y, xEnd - x, yEnd - y, (m, buffer) -> {
            buffer.addVertexWith2DPose(m, x, y).setColor(red, green, blue, alpha);
            buffer.addVertexWith2DPose(m, xEnd, yEnd).setColor(red, green, blue, alpha);
        });
    }

    public static void blit(final GuiGraphicsExtractor ps,
        final Identifier rl,
        final int x,
        final int y,
        final int w,
        final int h,
        final int u,
        final int v,
        final int mapW,
        final int mapH)
    {
        blit(ps, rl, x, y, w, h, (float) u / mapW, (float) v / mapH, (float) (u + w) / mapW, (float) (v + h) / mapH, null);
    }

    public static void blit(final GuiGraphicsExtractor ps,
        final Identifier rl,
        final int x,
        final int y,
        final int w,
        final int h,
        final int u,
        final int v,
        final int uW,
        final int vH,
        final int mapW,
        final int mapH)
    {
        blit(ps, rl, x, y, w, h, (float) u / mapW, (float) v / mapH, (float) (u + uW) / mapW, (float) (v + vH) / mapH, null);
    }

    public static void blitSprite(final GuiGraphicsExtractor ps,
        final TextureAtlasSprite sprite,
        final GuiSpriteScaling guiScaling,
        final int x,
        final int y,
        final int w,
        final int h)
    {
        resolveSprite(sprite, guiScaling).blit(ps, x, y, w, h);
    }

    public static void blitSprite(final GuiGraphicsExtractor ps,
        final TextureAtlasSprite sprite,
        final int x,
        final int y,
        final int w,
        final int h)
    {
        blit(ps, sprite.atlasLocation(), x, y, w, h, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), null);
    }

    public static void blit(final GuiGraphicsExtractor ps,
        final Identifier rl,
        final int x,
        final int y,
        final int w,
        final int h,
        @Nullable final IColour colorModulation)
    {
        blit(ps, rl, x, y, w, h, 0.0f, 0.0f, 1.0f, 1.0f, colorModulation);
    }

    public static void blit(final GuiGraphicsExtractor ps,
        final Identifier rl,
        final int x,
        final int y,
        final int w,
        final int h,
        final float uMin,
        final float vMin,
        final float uMax,
        final float vMax,
        @Nullable final IColour colorModulation)
    {
        if (colorModulation == null)
        {
            submitBlit(ps,
                GUI_POS_TEX_TRIANGLES,
                x,
                y,
                w,
                h,
                rl,
                (m, buffer) -> populateBlitTriangles(buffer, m, x, x + w, y, y + h, uMin, uMax, vMin, vMax));
        }
        else
        {
            // TODO: this would normally use uniform 'colorModulator', but vanilla doesn't expose it to gui yet
            submitBlit(ps,
                GUI_POS_TEX_COLOR_TRIANGLES,
                x,
                y,
                w,
                h,
                rl,
                (m, buffer) -> populateBlitTriangles(buffer, m, x, x + w, y, y + h, uMin, uMax, vMin, vMax, colorModulation));
        }
    }

    /**
     * Draws texture without scaling so one texel is one pixel, using repeatable texture center.
     *
     * @param ps              MatrixStack
     * @param rl              image ResLoc
     * @param x               start target coords [pixels]
     * @param y               start target coords [pixels]
     * @param width           target rendering box [pixels]
     * @param height          target rendering box [pixels]
     * @param uMin            texture start offset [normalized texels]
     * @param vMin            texture start offset [normalized texels]
     * @param uMax            texture end offset [normalized texels]
     * @param vMax            texture end offset [normalized texels]
     * @param nineSlice       repeatable box definition [texels]
     * @param colorModulation texture color modulation
     */
    public static void blitRepeatable(final GuiGraphicsExtractor ps,
        final Identifier rl,
        final int x,
        final int y,
        final int width,
        final int height,
        final float uMin,
        final float vMin,
        final float uMax,
        final float vMax,
        final NineSlice nineSlice,
        final IColour colorModulation)
    {
        if (nineSlice.border().left() < 0 || nineSlice.border().right() < 0 ||
            nineSlice.border().top() < 0 ||
            nineSlice.border().bottom() < 0)
        {
            throw new IllegalArgumentException("Negative nineSlice borders");
        }
        if (nineSlice.border().left() + nineSlice.border().right() > nineSlice.width() ||
            nineSlice.border().top() + nineSlice.border().bottom() > nineSlice.height())
        {
            throw new IllegalArgumentException("NineSlice borders greater than box");
        }

        if (nineSlice.width() == width && nineSlice.height() == height)
        {
            blit(ps, rl, x, y, width, height, uMin, vMin, uMax, vMax, colorModulation);
            return;
        }

        submitBlit(ps, GUI_POS_TEX_TRIANGLES, x, y, width, height, rl, (m, b) -> {
            final IColour c = Objects.requireNonNullElse(colorModulation, NOOP_COLOUR);

            // nineSlice w/h is in UV [0,1]
            // nineSlice assumes texel = pixel

            final int lrBorder = nineSlice.border().left() + nineSlice.border().right();
            final int tbBorder = nineSlice.border().top() + nineSlice.border().bottom();

            // pixels
            final int xAdjust = nineSlice.border().left();
            final int yAdjust = nineSlice.border().top();
            final int pixWidth = nineSlice.width() - lrBorder;
            final int pixHeight = nineSlice.height() - tbBorder;

            final int repeatCountX = Math.max(0, width - lrBorder) / pixWidth;
            final int repeatCountY = Math.max(0, height - tbBorder) / pixHeight;

            // corners

            final int x0 = x;
            final int x1 = x + xAdjust;
            final int x2normal = xAdjust + repeatCountX * pixWidth;
            final int x2stretched = width - xAdjust;
            final int x2 = x + (nineSlice.stretchInner() ? x2stretched : x2normal);
            final int x3 = x + width;

            final int y0 = y;
            final int y1 = y + yAdjust;
            final int y2normal = yAdjust + repeatCountY * pixHeight;
            final int y2stretched = height - yAdjust;
            final int y2 = y + (nineSlice.stretchInner() ? y2stretched : y2normal);
            final int y3 = y + height;

            final float u0 = uMin;
            final float u1 = Mth.lerp((float) nineSlice.border().left() / nineSlice.width(), uMin, uMax);
            final float u2stretchFix = nineSlice.stretchInner() ? 0 : x2stretched - x2normal;
            final float u2 = Mth.lerp(1.0f - (nineSlice.border().right() + u2stretchFix) / nineSlice.width(), uMin, uMax);
            final float u3 = uMax;

            final float v0 = vMin;
            final float v1 = Mth.lerp((float) nineSlice.border().top() / nineSlice.height(), vMin, vMax);
            final float v2stretchFix = nineSlice.stretchInner() ? 0 : y2stretched - y2normal;
            final float v2 = Mth.lerp(1.0f - (nineSlice.border().bottom() + v2stretchFix) / nineSlice.height(), vMin, vMax);
            final float v3 = vMax;

            populateBlitTriangles(b, m, x0, x1, y0, y1, u0, u1, v0, v1, c);
            populateBlitTriangles(b, m, x0, x1, y2, y3, u0, u1, v2, v3, c);
            populateBlitTriangles(b, m, x2, x3, y0, y1, u2, u3, v0, v1, c);
            populateBlitTriangles(b, m, x2, x3, y2, y3, u2, u3, v2, v3, c);

            // tiles

            final float uS = u1;
            final float uE = Mth.lerp(1.0f - (float) nineSlice.border().right() / nineSlice.width(), uMin, uMax);
            final float vS = v1;
            final float vE = Mth.lerp(1.0f - (float) nineSlice.border().bottom() / nineSlice.height(), vMin, vMax);

            // stretch single tile
            if (nineSlice.stretchInner())
            {
                final int xS = x1, xE = x2;
                final int yS = y1, yE = y2;

                // in same order as fori
                populateBlitTriangles(b, m, xS, xE, y0, y1, uS, uE, v0, v1, c);
                populateBlitTriangles(b, m, xS, xE, y2, y3, uS, uE, v2, v3, c);

                populateBlitTriangles(b, m, xS, xE, yS, yE, uS, uE, vS, vE, c);

                populateBlitTriangles(b, m, x0, x1, yS, yE, u0, u1, vS, vE, c);
                populateBlitTriangles(b, m, x2, x3, yS, yE, u2, u3, vS, vE, c);
                return;
            }
            // else draw tiling

            // center and top & bot edges
            for (int i = 0; i < repeatCountX; i++)
            {
                final int xS = x1 + i * pixWidth;
                final int xE = xS + pixWidth;

                populateBlitTriangles(b, m, xS, xE, y0, y1, uS, uE, v0, v1, c);
                populateBlitTriangles(b, m, xS, xE, y2, y3, uS, uE, v2, v3, c);

                for (int j = 0; j < repeatCountY; j++)
                {
                    final int yS = y1 + j * pixHeight;
                    final int yE = yS + pixHeight;

                    populateBlitTriangles(b, m, xS, xE, yS, yE, uS, uE, vS, vE, c);
                }
            }

            // left & right edges
            for (int j = 0; j < repeatCountY; j++)
            {
                final int yS = y1 + j * pixHeight;
                final int yE = yS + pixHeight;

                populateBlitTriangles(b, m, x0, x1, yS, yE, u0, u1, vS, vE, c);
                populateBlitTriangles(b, m, x2, x3, yS, yE, u2, u3, vS, vE, c);
            }
        });
    }

    public static void populateFillTriangles(final Matrix3x2f m,
        final VertexConsumer buffer,
        final int x,
        final int y,
        final int w,
        final int h,
        final int red,
        final int green,
        final int blue,
        final int alpha)
    {
        if (w == 0 || h == 0)
        {
            return;
        }

        buffer.addVertexWith2DPose(m, x, y).setColor(red, green, blue, alpha);
        buffer.addVertexWith2DPose(m, x, y + h).setColor(red, green, blue, alpha);
        buffer.addVertexWith2DPose(m, x + w, y).setColor(red, green, blue, alpha);
        buffer.addVertexWith2DPose(m, x + w, y).setColor(red, green, blue, alpha);
        buffer.addVertexWith2DPose(m, x, y + h).setColor(red, green, blue, alpha);
        buffer.addVertexWith2DPose(m, x + w, y + h).setColor(red, green, blue, alpha);
    }

    public static void populateFillGradientTriangles(final Matrix3x2f m,
        final VertexConsumer buffer,
        final int x,
        final int y,
        final int w,
        final int h,
        final int redStart,
        final int redEnd,
        final int greenStart,
        final int greenEnd,
        final int blueStart,
        final int blueEnd,
        final int alphaStart,
        final int alphaEnd)
    {
        if (w == 0 || h == 0)
        {
            return;
        }

        buffer.addVertexWith2DPose(m, x, y).setColor(redStart, greenStart, blueStart, alphaStart);
        buffer.addVertexWith2DPose(m, x, y + h).setColor(redEnd, greenEnd, blueEnd, alphaEnd);
        buffer.addVertexWith2DPose(m, x + w, y).setColor(redStart, greenStart, blueStart, alphaStart);
        buffer.addVertexWith2DPose(m, x + w, y).setColor(redStart, greenStart, blueStart, alphaStart);
        buffer.addVertexWith2DPose(m, x, y + h).setColor(redEnd, greenEnd, blueEnd, alphaEnd);
        buffer.addVertexWith2DPose(m, x + w, y + h).setColor(redEnd, greenEnd, blueEnd, alphaEnd);
    }

    public static void populateBlitTriangles(final VertexConsumer buffer,
        final Matrix3x2f mat,
        final float xStart,
        final float xEnd,
        final float yStart,
        final float yEnd,
        final float uMin,
        final float uMax,
        final float vMin,
        final float vMax)
    {
        if (xStart == xEnd || yStart == yEnd)
        {
            return;
        }

        buffer.addVertexWith2DPose(mat, xStart, yStart).setUv(uMin, vMin);
        buffer.addVertexWith2DPose(mat, xStart, yEnd).setUv(uMin, vMax);
        buffer.addVertexWith2DPose(mat, xEnd, yStart).setUv(uMax, vMin);
        buffer.addVertexWith2DPose(mat, xEnd, yStart).setUv(uMax, vMin);
        buffer.addVertexWith2DPose(mat, xStart, yEnd).setUv(uMin, vMax);
        buffer.addVertexWith2DPose(mat, xEnd, yEnd).setUv(uMax, vMax);
    }

    public static void populateBlitTriangles(final VertexConsumer buffer,
        final Matrix3x2f mat,
        final float xStart,
        final float xEnd,
        final float yStart,
        final float yEnd,
        final float uMin,
        final float uMax,
        final float vMin,
        final float vMax,
        final IColour color)
    {
        if (xStart == xEnd || yStart == yEnd)
        {
            return;
        }

        buffer.addVertexWith2DPose(mat, xStart, yStart).setUv(uMin, vMin);
        color.writeIntoBuffer(buffer);
        buffer.addVertexWith2DPose(mat, xStart, yEnd).setUv(uMin, vMax);
        color.writeIntoBuffer(buffer);
        buffer.addVertexWith2DPose(mat, xEnd, yStart).setUv(uMax, vMin);
        color.writeIntoBuffer(buffer);
        buffer.addVertexWith2DPose(mat, xEnd, yStart).setUv(uMax, vMin);
        color.writeIntoBuffer(buffer);
        buffer.addVertexWith2DPose(mat, xStart, yEnd).setUv(uMin, vMax);
        color.writeIntoBuffer(buffer);
        buffer.addVertexWith2DPose(mat, xEnd, yEnd).setUv(uMax, vMax);
        color.writeIntoBuffer(buffer);
    }

    /**
     * @return rendering lambda detached from sprite and guiScaling instances
     */
    public static ResolvedBlit resolveSprite(final TextureAtlasSprite sprite, final GuiSpriteScaling guiScaling)
    {
        final Identifier atlasLocation = sprite.atlasLocation();
        final float u0 = sprite.getU0();
        final float v0 = sprite.getV0();
        final float u1 = sprite.getU1();
        final float v1 = sprite.getV1();
        if (guiScaling.type() == Type.STRETCH)
        {
            return (ps, x, y, w, h, c) -> blit(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1, c);
        }
        else if (guiScaling instanceof final NineSlice nineSlice)
        {
            return (ps, x, y, w, h, c) -> blitRepeatable(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1, nineSlice, c);
        }
        else if (guiScaling instanceof final Tile tile)
        {
            final NineSlice nineSlice = new NineSlice(tile.width(), tile.height(), new NineSlice.Border(0, 0, 0, 0), false);
            return (ps, x, y, w, h, c) -> blitRepeatable(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1, nineSlice, c);
        }
        if (!FMLEnvironment.isProduction())
        {
            throw new UnsupportedOperationException("Missing resolver for gui scaling: " + guiScaling.type());
        }
        return ResolvedBlit.EMPTY;
    }

    /**
     * Used for precompiling math around rendering
     */
    @FunctionalInterface
    public static interface ResolvedBlit
    {
        public static final ResolvedBlit EMPTY = (ps, x, y, w, h, c) -> {};

        void blit(GuiGraphicsExtractor ps, int x, int y, int w, int h, @Nullable IColour colorModulation);

        default void blit(final GuiGraphicsExtractor ps, final int x, final int y, final int w, final int h)
        {
            blit(ps, x, y, w, h, colorModulation());
        }

        @Nullable
        default IColour colorModulation()
        {
            return null;
        }

        default ResolvedBlitWithColorModulation withColorModulation(final IColour colorModulation)
        {
            return new ResolvedBlitWithColorModulation(this, colorModulation);
        }
    }

    public static record ResolvedBlitWithColorModulation(ResolvedBlit blit, IColour colorModulation) implements ResolvedBlit
    {
        @Override
        public void blit(final GuiGraphicsExtractor ps,
            final int x,
            final int y,
            final int w,
            final int h,
            @Nullable final IColour colorModulation)
        {
            blit.blit(ps, x, y, w, h, colorModulation);
        }
    }

    public static void submitNoTex(final GuiGraphicsExtractor target,
        final RenderPipeline pipeline,
        final int x,
        final int y,
        final int w,
        final int h,
        final BiConsumer<Matrix3x2f, VertexConsumer> task)
    {
        innerSubmit(target,
            x,
            y,
            w,
            h,
            (pose, bounds, scissors) -> target.submitGuiElementRenderState(
                new UiRenderMacrosGuiElementRenderState(pose, task, pipeline, TextureSetup.noTexture(), bounds, scissors)));
    }

    public static void submitBlit(final GuiGraphicsExtractor target,
        final RenderPipeline pipeline,
        final int x,
        final int y,
        final int w,
        final int h,
        final Identifier texResLoc,
        final BiConsumer<Matrix3x2f, VertexConsumer> task)
    {
        innerSubmit(target, x, y, w, h, (pose, bounds, scissors) -> {
            final AbstractTexture texture = target.minecraft.getTextureManager().getTexture(texResLoc);
            final TextureSetup textureSetup = TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler());
            target.submitGuiElementRenderState(
                new UiRenderMacrosGuiElementRenderState(pose, task, pipeline, textureSetup, bounds, scissors));
        });
    }

    public static <T> void innerSubmit(final GuiGraphicsExtractor target,
        final int x,
        final int y,
        final int w,
        final int h,
        final SubmitTask task)
    {
        final Matrix3x2f pose = new Matrix3x2f(target.pose());
        final ScreenRectangle scissors = target.peekScissorStack();

        ScreenRectangle bounds = new ScreenRectangle(x, y, w, h);
        bounds = bounds.transformMaxBounds(pose);
        bounds = scissors == null ? bounds : scissors.intersection(bounds);

        if (bounds != null)
        {
            task.submit(pose, bounds, scissors);
        }
    }

    @FunctionalInterface
    public static interface SubmitTask
    {
        void submit(Matrix3x2f pose, ScreenRectangle bounds, ScreenRectangle scissors);
    }

    public record UiRenderMacrosGuiElementRenderState(Matrix3x2f pose,
        BiConsumer<Matrix3x2f, VertexConsumer> task,
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        @Nullable ScreenRectangle bounds,
        @Nullable ScreenRectangle scissorArea) implements GuiElementRenderState
    {
        @Override
        public void buildVertices(final VertexConsumer vertexConsumer)
        {
            task.accept(pose(), vertexConsumer);
        }
    }

    public static final IColour NOOP_COLOUR = new IColour()
    {
        @Override
        public int red()
        {
            return 0;
        }

        @Override
        public int green()
        {
            return 0;
        }

        @Override
        public int blue()
        {
            return 0;
        }

        @Override
        public int alpha()
        {
            return 0;
        }

        @Override
        public int argb()
        {
            return 0;
        }

        @Override
        public int rgba()
        {
            return 0;
        }

        @Override
        public void writeIntoBuffer(VertexConsumer buffer)
        {
            // intentionally skip
        }
    };
}
