package com.ldtteam.blockui.context;

import com.ldtteam.blockui.mod.ResourceLoader;
import com.ldtteam.blockui.util.Crash;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeRenderTypes;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import java.util.PriorityQueue;
import java.util.function.BiConsumer;

public abstract class RenderContext
{
    public static final int NO_TEXT_BACKGROUND = 0x00_00_00_00;

    // =============== context ===============
    public final Minecraft mc = Minecraft.getInstance();

    public DebugLevel debugLevel = DebugLevel.NONE;
    /**
     * true if F3+T was run before this tick
     */
    public boolean resourceReloaded = false;
    protected PoseStack poseStack = null;
    private boolean taken = false; // this should be volatile at least, but rendering must be done on main thread anyway

    protected void grab()
    {
        if (taken)
        {
            throw Crash.state("Trying to grab context twice");
        }
        taken = true;

        // init
        poseStack = new PoseStack();

        clearRenderState();
    }

    protected void clearRenderState()
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.disableCull();
        RenderSystem.defaultBlendFunc();
    }

    public void tickAndRelease()
    {
        renderDelayed();

        // gc release & reset
        poseStack = null;

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.enableCull();

        taken = false;
    }

    // =============== delayed queue ===============

    private final PriorityQueue<DelayedTask> delayedRenderTasks = new PriorityQueue<>(20);

    public void delayRendering(final DelayedLevel level, final Runnable task)
    {
        if (level == DelayedLevel.NONE)
        {
            task.run();
            return;
        }

        delayedRenderTasks.add(new DelayedTask(task, level));
    }

    protected void renderDelayed()
    {
        while (!delayedRenderTasks.isEmpty())
        {
            delayedRenderTasks.poll().task.run();
        }
    }

    // =============== pose stack ===============

    public void pushTranslate(final int x, final int y)
    {
        poseStack.pushPose();
        poseStack.translate(x, y, 0);
    }

    public void pushScaleNoZ(final float scaleX, final float scaleY)
    {
        poseStack.pushPose();
        poseStack.scale(scaleX, scaleY, 1.0f);
    }

    public abstract void pushTooltipTranslate(final Object renderingAdapter, final int tooltipWidth, final int tooltipHeight);

    public void scaleNoZ(final float scaleX, final float scaleY)
    {
        poseStack.scale(scaleX, scaleY, 1.0f);
    }

    public void pop()
    {
        poseStack.popPose();
    }

    // =============== text texture filtering ===============

    private static final int FILTERING_ROUNDING = 50;
    private static final float FILTERING_THRESHOLD = 0.02f; // should be 1/FILTERING_ROUNDING
    private boolean previousFilteringValue;

    public void enableTextFiltering()
    {
        previousFilteringValue = ForgeRenderTypes.enableTextTextureLinearFiltering;
        ForgeRenderTypes.enableTextTextureLinearFiltering = true;
    }

    public void disableTextFiltering()
    {
        ForgeRenderTypes.enableTextTextureLinearFiltering = previousFilteringValue;
    }

    /**
     * @return true if texture filtering was enabled
     */
    public boolean applyTextScale(final float scale)
    {
        // TODO: math check, sus?
        final Vector4f temp = new Vector4f(1, 1, 0, 0);
        poseStack.last().pose().transform(temp);

        final float oldScaleX = temp.x();
        final float oldScaleY = temp.y();
        final float newScaleX = (float) Math.round(oldScaleX * scale * FILTERING_ROUNDING) / FILTERING_ROUNDING;
        final float newScaleY = (float) Math.round(oldScaleY * scale * FILTERING_ROUNDING) / FILTERING_ROUNDING;

        if (Math.abs(Math.round(newScaleX) - newScaleX) > FILTERING_THRESHOLD ||
            Math.abs(Math.round(newScaleY) - newScaleY) > FILTERING_THRESHOLD)
        {
            // smooth the texture
            // if (newScaleX < window.getScreen().getVanillaGuiScale() || newScaleY < window.getScreen().getVanillaGuiScale())
            // TODO: figure out how to not linear filter when mag filter is used, might just want to use direct ogl call

            enableTextFiltering();
            scaleNoZ(scale, scale);
            return true;
        }
        else
        {
            // round scale if not smoothing
            scaleNoZ(newScaleX / oldScaleX, newScaleY / oldScaleY);
            return false;
        }
    }

    // =============== render state ===============

    public void setColorModifier(final float modifier)
    {
        RenderSystem.setShaderColor(modifier, modifier, modifier, 1.0f);
    }

    public void restoreOneColorModifier()
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    // =============== rendering ===============

    protected Font getFont(@Nullable final ItemStack itemStack)
    {
        if (itemStack != null)
        {
            final Font font = IClientItemExtensions.of(itemStack).getFont(itemStack, IClientItemExtensions.FontContext.ITEM_COUNT);
            if (font != null)
            {
                return font;
            }
        }
        return mc.font;
    }

    public void renderItem(final ItemStack itemStack, final int x, final int y)
    {
        renderItem(itemStack, x, y, null);
    }

    public abstract void renderItem(final ItemStack itemStack, final int x, final int y, @Nullable final String altStackSize);

    /**
     * X and Y are bottom right anchored
     */
    public int drawStringBottomRight(final String text, final float x, final float y, final int color)
    {
        return drawString(text, x - (mc.font.width(text) + 1), y - mc.font.lineHeight, color, false);
    }

    public abstract void drawStringsInBatch(final boolean shadow,
        final int defaultColor,
        final int backgroundColor,
        final Font.DisplayMode mode,
        final BatchStringSupplier textSupplier);

    public abstract int drawString(final String text, final float x, final float y, final int color, final boolean shadow);

    public void drawLineRect(final int x, final int y, final int w, final int h, final int argbColor)
    {
        drawLineRect(x, y, w, h, argbColor, 1);
    }

    public void drawLineRect(final int x, final int y, final int w, final int h, final int argbColor, final int lineWidth)
    {
        drawLineRect(x,
            y,
            w,
            h,
            (argbColor >> 16) & 0xff,
            (argbColor >> 8) & 0xff,
            argbColor & 0xff,
            (argbColor >> 24) & 0xff,
            lineWidth);
    }

    public void drawLineRect(final int x,
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

        final Matrix4f m = poseStack.last().pose();
        final BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        // we do triangle so it respects current gui scale
        buffer.begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(m, x, y, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(m, x, y + h, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(m, x + lineWidth, y + h - lineWidth, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(m, x + lineWidth, y + lineWidth, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(m, x + w - lineWidth, y + lineWidth, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(m, x + w, y, 0).color(red, green, blue, alpha).endVertex();
        Tesselator.getInstance().end();

        buffer.begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(m, x + w, y + h, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(m, x + w, y, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(m, x + w - lineWidth, y + lineWidth, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(m, x + w - lineWidth, y + h - lineWidth, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(m, x + lineWidth, y + h - lineWidth, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(m, x, y + h, 0).color(red, green, blue, alpha).endVertex();
        Tesselator.getInstance().end();

        RenderSystem.disableBlend();
    }

    public void blit(final ResourceLocation rl,
        final int x,
        final int y,
        final int w,
        final int h,
        final float uMin,
        final float vMin,
        final float uMax,
        final float vMax)
    {
        ResourceLoader.INSTANCE.ensureLoadedOutOfJarResLoc(rl);
        mc.getTextureManager().bindForSetup(rl);

        RenderSystem.setShaderTexture(0, rl);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();

        final Matrix4f m = poseStack.last().pose();
        final BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(m, x, y, 0).uv(uMin, vMin).endVertex();
        buffer.vertex(m, x, y + h, 0).uv(uMin, vMax).endVertex();
        buffer.vertex(m, x + w, y + h, 0).uv(uMax, vMax).endVertex();
        buffer.vertex(m, x + w, y, 0).uv(uMax, vMin).endVertex();

        Tesselator.getInstance().end();

        RenderSystem.disableBlend();
    }

    public void blitRepeatable(final ResourceLocation rl,
        final int x,
        final int y,
        final int width,
        final int height,
        final float uMin,
        final float vMin,
        final float uMax,
        final float vMax,
        final RepeatableExtension ext)
    {
        if (ext == null || (ext.countX == 1 && ext.countY == 1))
        {
            blit(rl, x, y, width, height, uMin, vMin, uMax, vMax);
            return;
        }

        ResourceLoader.INSTANCE.ensureLoadedOutOfJarResLoc(rl);
        mc.getTextureManager().bindForSetup(rl);

        RenderSystem.setShaderTexture(0, rl);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();

        final Matrix4f m = poseStack.last().pose();
        final BufferBuilder vbo = Tesselator.getInstance().getBuilder();
        vbo.begin(Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);

        // corners

        final int xLast = x + ext.xAdjust + ext.countX * ext.width;
        final int yLast = y + ext.yAdjust + ext.countY * ext.height;
        final int xLastEnd = x + (ext.countX - 1) * ext.width + width;
        final int yLastEnd = y + (ext.countY - 1) * ext.height + height;

        populateTexTriangle(vbo, m, x, x + ext.xAdjust, y, y + ext.yAdjust, uMin, ext.uRepeatMin, vMin, ext.vRepeatMin);
        populateTexTriangle(vbo, m, x, x + ext.xAdjust, yLast, yLastEnd, uMin, ext.uRepeatMin, ext.vRepeatMax, vMax);
        populateTexTriangle(vbo, m, xLast, xLastEnd, y, y + ext.yAdjust, ext.uRepeatMax, uMax, vMin, ext.vRepeatMin);
        populateTexTriangle(vbo, m, xLast, xLastEnd, yLast, yLastEnd, ext.uRepeatMax, uMax, ext.vRepeatMax, vMax);

        // center and top & bot edges

        int xStart = x + ext.xAdjust;
        int xEnd = xStart + ext.width;
        for (int i = 0; i < ext.countX; i++, xStart += ext.width, xEnd += ext.width)
        {
            populateTexTriangle(vbo, m, xStart, xEnd, y, y + ext.yAdjust, ext.uRepeatMin, ext.uRepeatMax, vMin, ext.vRepeatMin);
            populateTexTriangle(vbo, m, xStart, xEnd, yLast, yLastEnd, ext.uRepeatMin, ext.uRepeatMax, ext.vRepeatMax, vMax);

            int yStart = y + ext.yAdjust;
            for (int j = 0; j < ext.countY; j++, yStart += ext.height)
            {
                populateTexTriangle(vbo,
                    m,
                    xStart,
                    xEnd,
                    yStart,
                    yStart + ext.height,
                    ext.uRepeatMin,
                    ext.uRepeatMax,
                    ext.vRepeatMin,
                    ext.vRepeatMax);
            }
        }

        // left & right edges
        int yStart = y + ext.yAdjust;
        for (int j = 0; j < ext.countY; j++, yStart += ext.height)
        {
            populateTexTriangle(vbo,
                m,
                x,
                x + ext.xAdjust,
                yStart,
                yStart + ext.height,
                uMin,
                ext.uRepeatMin,
                ext.vRepeatMin,
                ext.vRepeatMax);
            populateTexTriangle(vbo,
                m,
                xLast,
                xLastEnd,
                yStart,
                yStart + ext.height,
                ext.uRepeatMax,
                uMax,
                ext.vRepeatMin,
                ext.vRepeatMax);
        }

        Tesselator.getInstance().end();

        RenderSystem.disableBlend();
    }

    private static void populateTexTriangle(final BufferBuilder buffer,
        final Matrix4f m,
        final int xMin,
        final int xMax,
        final int yMin,
        final int yMax,
        final float uMin,
        final float uMax,
        final float vMin,
        final float vMax)
    {
        buffer.vertex(m, xMin, yMin, 0).uv(uMin, vMin).endVertex();
        buffer.vertex(m, xMin, yMax, 0).uv(uMin, vMax).endVertex();
        buffer.vertex(m, xMax, yMin, 0).uv(uMax, vMin).endVertex();
        buffer.vertex(m, xMax, yMin, 0).uv(uMax, vMin).endVertex();
        buffer.vertex(m, xMin, yMax, 0).uv(uMin, vMax).endVertex();
        buffer.vertex(m, xMax, yMax, 0).uv(uMax, vMax).endVertex();
    }

    public void coloredTriangleFan(final boolean shouldUseBlending, final BiConsumer<BufferBuilder, Matrix4f> triangleFanBuilder)
    {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        if (shouldUseBlending)
        {
            RenderSystem.enableBlend();
        }
        else
        {
            RenderSystem.disableBlend();
        }

        final Matrix4f m = poseStack.last().pose();
        final BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        buffer.begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        triangleFanBuilder.accept(buffer, m);
        Tesselator.getInstance().end();

        RenderSystem.disableBlend();
    }

    private record DelayedTask(Runnable task, DelayedLevel level) implements Comparable<DelayedTask>
    {
        @Override
        public int compareTo(final DelayedTask o)
        {
            return o.level.zLevel - level.zLevel;
        }
    }

    public static class RepeatableExtension
    {
        public final int xAdjust;
        public final int yAdjust;
        public final int width;
        public final int height;
        public final float uRepeatMin;
        public final float uRepeatMax;
        public final float vRepeatMin;
        public final float vRepeatMax;

        public int countX;
        public int countY;

        /**
         * @param xAdjust    box in tx
         * @param yAdjust    box in tx
         * @param width      box in tx
         * @param height     box in tx
         * @param uRepeatMin tx float
         * @param uRepeatMax tx float
         * @param vRepeatMin tx float
         * @param vRepeatMax tx float
         */
        public RepeatableExtension(final int xAdjust,
            final int yAdjust,
            final int width,
            final int height,
            final float uRepeatMin,
            final float uRepeatMax,
            final float vRepeatMin,
            final float vRepeatMax)
        {
            this.xAdjust = xAdjust;
            this.yAdjust = yAdjust;
            this.width = width;
            this.height = height;
            this.uRepeatMin = uRepeatMin;
            this.uRepeatMax = uRepeatMax;
            this.vRepeatMin = vRepeatMin;
            this.vRepeatMax = vRepeatMax;
        }
    }
}
