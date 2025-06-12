package com.ldtteam.blockui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling.NineSlice;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling.Tile;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.loading.FMLEnvironment;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Our replacement for GuiComponent.
 */
public class UiRenderMacros
{
    public static final double HALF_BIAS = 0.5;

    public static void drawLineRectGradient(final PoseStack ps,
        final int x,
        final int y,
        final int w,
        final int h,
        final int argbColorStart,
        final int argbColorEnd)
    {
        drawLineRectGradient(ps, x, y, w, h, argbColorStart, argbColorEnd, 1);
    }

    public static void drawLineRectGradient(final PoseStack ps,
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

    public static void drawLineRectGradient(final PoseStack ps,
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

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        if (alphaStart != 255 || alphaEnd != 255)
        {
            RenderSystem.enableBlend();
        }
        else
        {
            RenderSystem.disableBlend();
        }

        final Matrix4f m = ps.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(m, x, y, 0).setColor(redStart, greenStart, blueStart, alphaStart);
        buffer.addVertex(m, x, y + h, 0).setColor(redEnd, greenEnd, blueEnd, alphaEnd);
        buffer.addVertex(m, x + lineWidth, y + h - lineWidth, 0).setColor(redEnd, greenEnd, blueEnd, alphaEnd);
        buffer.addVertex(m, x + lineWidth, y + lineWidth, 0).setColor(redStart, greenStart, blueStart, alphaStart);
        buffer.addVertex(m, x + w - lineWidth, y + lineWidth, 0).setColor(redStart, greenStart, blueStart, alphaStart);
        buffer.addVertex(m, x + w, y, 0).setColor(redStart, greenStart, blueStart, alphaStart);
        BufferUploader.drawWithShader(buffer.build());

        buffer = Tesselator.getInstance().begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(m, x + w, y + h, 0).setColor(redEnd, greenEnd, blueEnd, alphaEnd);
        buffer.addVertex(m, x + w, y, 0).setColor(redStart, greenStart, blueStart, alphaStart);
        buffer.addVertex(m, x + w - lineWidth, y + lineWidth, 0).setColor(redStart, greenStart, blueStart, alphaStart);
        buffer.addVertex(m, x + w - lineWidth, y + h - lineWidth, 0).setColor(redEnd, greenEnd, blueEnd, alphaEnd);
        buffer.addVertex(m, x + lineWidth, y + h - lineWidth, 0).setColor(redEnd, greenEnd, blueEnd, alphaEnd);
        buffer.addVertex(m, x, y + h, 0).setColor(redEnd, greenEnd, blueEnd, alphaEnd);
        BufferUploader.drawWithShader(buffer.build());

        RenderSystem.disableBlend();
    }

    public static void drawLineRect(final PoseStack ps, final int x, final int y, final int w, final int h, final int argbColor)
    {
        drawLineRect(ps, x, y, w, h, argbColor, 1);
    }

    public static void drawLineRect(final PoseStack ps,
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

    public static void drawLineRect(final PoseStack ps,
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

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        if (alpha != 255)
        {
            RenderSystem.enableBlend();
        }
        else
        {
            RenderSystem.disableBlend();
        }

        final Matrix4f m = ps.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(m, x, y, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x, y + h, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x + lineWidth, y + h - lineWidth, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x + lineWidth, y + lineWidth, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x + w - lineWidth, y + lineWidth, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x + w, y, 0).setColor(red, green, blue, alpha);
        BufferUploader.drawWithShader(buffer.build());

        buffer = Tesselator.getInstance().begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(m, x + w, y + h, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x + w, y, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x + w - lineWidth, y + lineWidth, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x + w - lineWidth, y + h - lineWidth, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x + lineWidth, y + h - lineWidth, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x, y + h, 0).setColor(red, green, blue, alpha);
        BufferUploader.drawWithShader(buffer.build());

        RenderSystem.disableBlend();
    }

    public static void fill(final PoseStack ps, final int x, final int y, final int w, final int h, final int argbColor)
    {
        fill(ps, x, y, w, h, (argbColor >> 16) & 0xff, (argbColor >> 8) & 0xff, argbColor & 0xff, (argbColor >> 24) & 0xff);
    }

    public static void fill(final PoseStack ps,
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

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        if (alpha != 255)
        {
            RenderSystem.enableBlend();
        }
        else
        {
            RenderSystem.disableBlend();
        }

        final Matrix4f m = ps.last().pose();
        final BufferBuilder buffer = Tesselator.getInstance().begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(m, x, y, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x, y + h, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x + w, y + h, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x + w, y, 0).setColor(red, green, blue, alpha);
        BufferUploader.drawWithShader(buffer.build());

        RenderSystem.disableBlend();
    }

    public static void fillGradient(final PoseStack ps,
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

    public static void fillGradient(final PoseStack ps,
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

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        if (alphaStart != 255 || alphaEnd != 255)
        {
            RenderSystem.enableBlend();
        }
        else
        {
            RenderSystem.disableBlend();
        }

        final Matrix4f m = ps.last().pose();
        final BufferBuilder buffer = Tesselator.getInstance().begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(m, x, y, 0).setColor(redStart, greenStart, blueStart, alphaStart);
        buffer.addVertex(m, x, y + h, 0).setColor(redEnd, greenEnd, blueEnd, alphaEnd);
        buffer.addVertex(m, x + w, y + h, 0).setColor(redEnd, greenEnd, blueEnd, alphaEnd);
        buffer.addVertex(m, x + w, y, 0).setColor(redStart, greenStart, blueStart, alphaStart);
        BufferUploader.drawWithShader(buffer.build());

        RenderSystem.disableBlend();
    }

    public static void hLine(final PoseStack ps, final int x, final int xEnd, final int y, final int argbColor)
    {
        line(ps, x, y, xEnd, y, (argbColor >> 16) & 0xff, (argbColor >> 8) & 0xff, argbColor & 0xff, (argbColor >> 24) & 0xff);
    }

    public static void hLine(final PoseStack ps,
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

    public static void vLine(final PoseStack ps, final int x, final int y, final int yEnd, final int argbColor)
    {
        line(ps, x, y, x, yEnd, (argbColor >> 16) & 0xff, (argbColor >> 8) & 0xff, argbColor & 0xff, (argbColor >> 24) & 0xff);
    }

    public static void vLine(final PoseStack ps,
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

    public static void line(final PoseStack ps, final int x, final int y, final int xEnd, final int yEnd, final int argbColor)
    {
        line(ps, x, y, xEnd, yEnd, (argbColor >> 16) & 0xff, (argbColor >> 8) & 0xff, argbColor & 0xff, (argbColor >> 24) & 0xff);
    }

    public static void line(final PoseStack ps,
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

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        if (alpha != 255)
        {
            RenderSystem.enableBlend();
        }
        else
        {
            RenderSystem.disableBlend();
        }

        final Matrix4f m = ps.last().pose();
        final BufferBuilder buffer = Tesselator.getInstance().begin(Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(m, x, y, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, xEnd, yEnd, 0).setColor(red, green, blue, alpha);
        BufferUploader.drawWithShader(buffer.build());

        RenderSystem.disableBlend();
    }

    public static void blit(final PoseStack ps,
        final ResourceLocation rl,
        final int x,
        final int y,
        final int w,
        final int h,
        final int u,
        final int v,
        final int mapW,
        final int mapH)
    {
        blit(ps, rl, x, y, w, h, (float) u / mapW, (float) v / mapH, (float) (u + w) / mapW, (float) (v + h) / mapH);
    }

    public static void blit(final PoseStack ps,
        final ResourceLocation rl,
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
        blit(ps, rl, x, y, w, h, (float) u / mapW, (float) v / mapH, (float) (u + uW) / mapW, (float) (v + vH) / mapH);
    }

    public static void blitSprite(final PoseStack ps,
        final TextureAtlasSprite sprite,
        final GuiSpriteScaling guiScaling,
        final int x,
        final int y,
        final int w,
        final int h)
    {
        final ResourceLocation atlasLocation = sprite.atlasLocation();
        final float u0 = sprite.getU0();
        final float v0 = sprite.getV0();
        final float u1 = sprite.getU1();
        final float v1 = sprite.getV1();
        if (guiScaling.type() == Type.STRETCH)
        {
            blit(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1);
        }
        else if (guiScaling instanceof final NineSlice nineSlice)
        {
            final int rbW = nineSlice.width();
            final int rbH = nineSlice.height();

            if (rbW == w && rbH == h)
            {
                blit(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1);
            }
            else
            {
                final int uR = nineSlice.border().left();
                final int vR = nineSlice.border().top();
                final int rW = rbW - uR - nineSlice.border().right();
                final int rH = rbH - vR - nineSlice.border().bottom();
                blitRepeatable(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1, uR, vR, rW, rH, rbW, rbH);
            }
        }
        else if (guiScaling instanceof final Tile tile)
        {
            final int tW = tile.width();
            final int tH = tile.height();

            if (tW == w && tH == h)
            {
                blit(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1);
            }
            else
            {
                blitRepeatable(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1, 0, 0, tW, tH, tW, tH);
            }
        }
    }

    public static void blitSprite(final PoseStack ps,
        final TextureAtlasSprite sprite,
        final int x,
        final int y,
        final int w,
        final int h)
    {
        blit(ps, sprite.atlasLocation(), x, y, w, h, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
    }

    public static void blit(final PoseStack ps, final ResourceLocation rl, final int x, final int y, final int w, final int h)
    {
        blit(ps, rl, x, y, w, h, 0.0f, 0.0f, 1.0f, 1.0f);
    }

    public static void blit(final PoseStack ps,
        final ResourceLocation rl,
        final int x,
        final int y,
        final int w,
        final int h,
        final float uMin,
        final float vMin,
        final float uMax,
        final float vMax)
    {
        RenderSystem.setShaderTexture(0, rl);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        final Matrix4f m = ps.last().pose();
        final BufferBuilder buffer = Tesselator.getInstance().begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(m, x, y, 0).setUv(uMin, vMin);
        buffer.addVertex(m, x, y + h, 0).setUv(uMin, vMax);
        buffer.addVertex(m, x + w, y + h, 0).setUv(uMax, vMax);
        buffer.addVertex(m, x + w, y, 0).setUv(uMax, vMin);
        BufferUploader.drawWithShader(buffer.build());
    }

    /**
     * Draws texture without scaling so one texel is one pixel, using repeatable texture center. TODO: Nightenom - rework to better
     * algoritm from pgr, also texture extensions?
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
     * @param uRepeat         offset relative to u, v [texels], smaller than uWidth
     * @param vRepeat         offset relative to u, v [texels], smaller than vHeight
     * @param repeatWidth     size of repeatable part in texture [texels], smaller than or equal repeatBoxWidth - uRepeat
     * @param repeatHeight    size of repeatable part in texture [texels], smaller than or equal repeatBoxHeight - vRepeat
     * @param repeatBoxWidth  size of entire repeatable box (borders + repeat part) [texels]
     * @param repeatBoxHeight size of entire repeatable box (borders + repeat part) [texels]
     */
    protected static void blitRepeatable(final PoseStack ps,
        final ResourceLocation rl,
        final int x,
        final int y,
        final int width,
        final int height,
        final float uMin,
        final float vMin,
        final float uMax,
        final float vMax,
        final int uRepeat,
        final int vRepeat,
        final int repeatWidth,
        final int repeatHeight,
        final int repeatBoxWidth,
        final int repeatBoxHeight)
    {
        if (uRepeat < 0 || vRepeat < 0 ||
            uRepeat >= repeatBoxWidth ||
            vRepeat >= repeatBoxHeight ||
            repeatWidth < 1 ||
            repeatHeight < 1 ||
            repeatWidth > repeatBoxWidth - uRepeat ||
            repeatHeight > repeatBoxHeight - vRepeat)
        {
            throw new IllegalArgumentException("Repeatable box is outside of texture box");
        }

        final int repeatCountX = Math.max(1, Math.max(0, width - (repeatBoxWidth - repeatWidth)) / repeatWidth);
        final int repeatCountY = Math.max(1, Math.max(0, height - (repeatBoxHeight - repeatHeight)) / repeatHeight);
        final float uTexelWidth = (uMax - uMin) / repeatBoxWidth;
        final float vTexelHeight = (vMax - vMin) / repeatBoxHeight;

        final Matrix4f mat = ps.last().pose();
        final BufferBuilder buffer = Tesselator.getInstance().begin(Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);

        // main
        for (int i = 0; i < repeatCountX; i++)
        {
            final int uAdjust = i == 0 ? 0 : uRepeat;
            final int xStart = x + uAdjust + i * repeatWidth;
            final int w = Math.min(repeatWidth + uRepeat - uAdjust, width - (repeatBoxWidth - uRepeat - repeatWidth));
            final float minU = uMin + uTexelWidth * uAdjust;
            final float maxU = minU + uTexelWidth * w;

            for (int j = 0; j < repeatCountY; j++)
            {
                final int vAdjust = j == 0 ? 0 : vRepeat;
                final int yStart = y + vAdjust + j * repeatHeight;
                final int h = Math.min(repeatHeight + vRepeat - vAdjust, height - (repeatBoxHeight - vRepeat - repeatHeight));
                final float minV = vMin + vTexelHeight * vAdjust;
                final float maxV = minV + vTexelHeight * h;

                populateBlitTriangles(buffer, mat, xStart, xStart + w, yStart, yStart + h, minU, maxU, minV, maxV);
            }
        }

        final int xEnd = x + Math.min(uRepeat + repeatCountX * repeatWidth, width - (repeatBoxWidth - uRepeat - repeatWidth));
        final int yEnd = y + Math.min(vRepeat + repeatCountY * repeatHeight, height - (repeatBoxHeight - vRepeat - repeatHeight));
        final int uLeft = width - (xEnd - x);
        final int vBot = height - (yEnd - y);
        final float restMinU = uMax - uLeft * uTexelWidth;
        final float restMinV = vMax - vBot * vTexelHeight;

        // bot border
        for (int i = 0; i < repeatCountX; i++)
        {
            final int uAdjust = i == 0 ? 0 : uRepeat;
            final int xStart = x + uAdjust + i * repeatWidth;
            final int w = Math.min(repeatWidth + uRepeat - uAdjust, width - uLeft);
            final float minU = uMin + uTexelWidth * uAdjust;
            final float maxU = minU + uTexelWidth * w;

            populateBlitTriangles(buffer, mat, xStart, xStart + w, yEnd, yEnd + vBot, minU, maxU, restMinV, vMax);
        }

        // left border
        for (int j = 0; j < repeatCountY; j++)
        {
            final int vAdjust = j == 0 ? 0 : vRepeat;
            final int yStart = y + vAdjust + j * repeatHeight;
            final int h = Math.min(repeatHeight + vRepeat - vAdjust, height - vBot);
            final float minV = vMin + vTexelHeight * vAdjust;
            final float maxV = minV + vTexelHeight * h;

            populateBlitTriangles(buffer, mat, xEnd, xEnd + uLeft, yStart, yStart + h, restMinU, uMax, minV, maxV);
        }

        // bot left corner
        populateBlitTriangles(buffer, mat, xEnd, xEnd + uLeft, yEnd, yEnd + vBot, restMinU, uMax, restMinV, vMax);

        RenderSystem.setShaderTexture(0, rl);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        BufferUploader.drawWithShader(buffer.build());
    }

    public static void populateFillTriangles(final Matrix4f m,
        final BufferBuilder buffer,
        final int x,
        final int y,
        final int w,
        final int h,
        final int red,
        final int green,
        final int blue,
        final int alpha)
    {
        buffer.addVertex(m, x, y, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x, y + h, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x + w, y, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x + w, y, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x, y + h, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x + w, y + h, 0).setColor(red, green, blue, alpha);
    }

    public static void populateFillGradientTriangles(final Matrix4f m,
        final BufferBuilder buffer,
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
        buffer.addVertex(m, x, y, 0).setColor(redStart, greenStart, blueStart, alphaStart);
        buffer.addVertex(m, x, y + h, 0).setColor(redEnd, greenEnd, blueEnd, alphaEnd);
        buffer.addVertex(m, x + w, y, 0).setColor(redStart, greenStart, blueStart, alphaStart);
        buffer.addVertex(m, x + w, y, 0).setColor(redStart, greenStart, blueStart, alphaStart);
        buffer.addVertex(m, x, y + h, 0).setColor(redEnd, greenEnd, blueEnd, alphaEnd);
        buffer.addVertex(m, x + w, y + h, 0).setColor(redEnd, greenEnd, blueEnd, alphaEnd);
    }

    public static void populateBlitTriangles(final BufferBuilder buffer,
        final Matrix4f mat,
        final float xStart,
        final float xEnd,
        final float yStart,
        final float yEnd,
        final float uMin,
        final float uMax,
        final float vMin,
        final float vMax)
    {
        buffer.addVertex(mat, xStart, yStart, 0).setUv(uMin, vMin);
        buffer.addVertex(mat, xStart, yEnd, 0).setUv(uMin, vMax);
        buffer.addVertex(mat, xEnd, yStart, 0).setUv(uMax, vMin);
        buffer.addVertex(mat, xEnd, yStart, 0).setUv(uMax, vMin);
        buffer.addVertex(mat, xStart, yEnd, 0).setUv(uMin, vMax);
        buffer.addVertex(mat, xEnd, yEnd, 0).setUv(uMax, vMax);
    }

    /**
     * Render an entity on a GUI.
     * 
     * @param poseStack matrix
     * @param x         horizontal center position
     * @param y         vertical bottom position
     * @param scale     scaling factor
     * @param headYaw   adjusts look rotation
     * @param yaw       adjusts body rotation
     * @param pitch     adjusts look rotation
     * @param entity    the entity to render
     */
    public static void drawEntity(final PoseStack poseStack,
        final int x,
        final int y,
        final double scale,
        final float headYaw,
        final float yaw,
        final float pitch,
        final Entity entity)
    {
        // INLINE: vanilla from InventoryScreen
        final LivingEntity livingEntity = (entity instanceof LivingEntity) ? (LivingEntity) entity : null;
        final Minecraft mc = Minecraft.getInstance();
        if (entity.level() == null) return; // this was entity.setLevel, not sure why cuz sus, dont care if entity has no level
        poseStack.pushPose();
        poseStack.translate((float) x, (float) y, 1050.0F);
        poseStack.scale(1.0F, 1.0F, -1.0F);
        poseStack.translate(0.0D, 0.0D, 1000.0D);
        poseStack.scale((float) scale, (float) scale, (float) scale);
        final Quaternionf pitchRotation = Axis.XP.rotationDegrees(pitch);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(pitchRotation);
        final float oldYaw = entity.getYRot();
        final float oldPitch = entity.getXRot();
        final float oldYawOffset = livingEntity == null ? 0F : livingEntity.yBodyRot;
        final float oldPrevYawHead = livingEntity == null ? 0F : livingEntity.yHeadRotO;
        final float oldYawHead = livingEntity == null ? 0F : livingEntity.yHeadRot;
        entity.setYRot(180.0F + (float) headYaw);
        entity.setXRot(-pitch);
        if (livingEntity != null)
        {
            livingEntity.yBodyRot = 180.0F + yaw;
            livingEntity.yHeadRot = entity.getYRot();
            livingEntity.yHeadRotO = entity.getYRot();
        }
        Lighting.setupForEntityInInventory();
        final EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        pitchRotation.conjugate();
        dispatcher.overrideCameraOrientation(pitchRotation);
        dispatcher.setRenderShadow(false);
        final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> dispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, poseStack, buffers, 0x00F000F0));
        buffers.endBatch();
        dispatcher.setRenderShadow(true);
        entity.setYRot(oldYaw);
        entity.setXRot(oldPitch);
        if (livingEntity != null)
        {
            livingEntity.yBodyRot = oldYawOffset;
            livingEntity.yHeadRotO = oldPrevYawHead;
            livingEntity.yHeadRot = oldYawHead;
        }
        poseStack.popPose();
        Lighting.setupFor3DItems();
    }

    /**
     * @return rendering lambda detached from sprite and guiScaling instances
     * @implNote same as logic {@link #blitSprite(PoseStack, TextureAtlasSprite, GuiSpriteScaling, int, int, int, int)}
     */
    public static ResolvedBlit resolveSprite(final TextureAtlasSprite sprite, final GuiSpriteScaling guiScaling)
    {
        final ResourceLocation atlasLocation = sprite.atlasLocation();
        final float u0 = sprite.getU0();
        final float v0 = sprite.getV0();
        final float u1 = sprite.getU1();
        final float v1 = sprite.getV1();
        if (guiScaling.type() == Type.STRETCH)
        {
            return (ps, x, y, w, h) -> blit(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1);
        }
        else if (guiScaling instanceof final NineSlice nineSlice)
        {
            final int rbW = nineSlice.width();
            final int rbH = nineSlice.height();
            final int uR = nineSlice.border().left();
            final int vR = nineSlice.border().top();
            final int rW = rbW - uR - nineSlice.border().right();
            final int rH = rbH - vR - nineSlice.border().bottom();

            return (ps, x, y, w, h) -> {
                if (rbW == w && rbH == h)
                {
                    blit(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1);
                }
                else
                {
                    blitRepeatable(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1, uR, vR, rW, rH, rbW, rbH);
                }
            };
        }
        else if (guiScaling instanceof final Tile tile)
        {
            final int tW = tile.width();
            final int tH = tile.height();

            return (ps, x, y, w, h) -> {
                if (tW == w && tH == h)
                {
                    blit(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1);
                }
                else
                {
                    blitRepeatable(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1, 0, 0, tW, tH, tW, tH);
                }
            };
        }
        if (!FMLEnvironment.production)
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
        public static final ResolvedBlit EMPTY = (ps, x, y, w, h) -> {};

        void blit(PoseStack ps, int x, int y, int w, int h);
    }
}
