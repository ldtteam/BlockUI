package com.ldtteam.blockui.controls;

import java.util.Collections;
import com.ldtteam.blockui.Alignment;
import com.ldtteam.blockui.PaneParams;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;

import net.minecraft.client.renderer.GameRenderer;

/**
 * Element used for rendering tooltips.
 */
public class Tooltip extends AbstractTextElement
{
    public static final int DEFAULT_MAX_WIDTH = 208;
    public static final int DEFAULT_MAX_HEIGHT = AbstractTextElement.SIZE_FOR_UNLIMITED_ELEMENTS;

    private static final int CURSOR_BOX_SIZE = 12;
    private static final int Z_OFFSET = 200;
    private static final int BACKGROUND_COLOR = 0xf0100010;
    private static final int BORDER_COLOR_A = 0x505000ff;
    private static final int BORDER_COLOR_B = 0x5028007f;

    public static final int DEFAULT_TEXT_COLOR = 0xffffff; // white

    protected boolean autoWidth = true;
    protected boolean autoHeight = true;
    protected int maxWidth = DEFAULT_MAX_WIDTH;
    protected int maxHeight = DEFAULT_MAX_HEIGHT;

    /**
     * Standard constructor which instantiates the tooltip.
     * 
     * @see com.ldtteam.blockui.PaneBuilders#tooltipBuilder()
     * @deprecated {@link com.ldtteam.blockui.PaneBuilders#tooltipBuilder()}
     */
    @Deprecated
    public Tooltip()
    {
        super(Alignment.TOP_LEFT, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, true, true);
        init();
    }

    /**
     * Create text from xml.
     *
     * @param params xml parameters.
     */
    public Tooltip(final PaneParams params)
    {
        super(params, Alignment.TOP_LEFT, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, true, true);

        autoWidth = width == 0;
        autoHeight = height == 0;
        init();
    }

    protected void init()
    {
        textLinespace = 1;
        textOffsetX = 4;
        textOffsetY = 4;
        hide();
        recalcTextRendering();
    }

    @Override
    protected void recalcTextRendering()
    {
        if (textScale <= 0.0d || isTextEmpty())
        {
            preparedText = Collections.emptyList();
            return;
        }

        // we have wrap enabled, so we want to create as small bouding box as possible
        if (autoWidth)
        {
            // +1 for shadow
            textWidth = Math.min(text.stream().mapToInt(mc.font::width).max().orElse(Integer.MAX_VALUE), maxWidth - 8) + 1;
        }
        if (autoHeight)
        {
            textHeight = maxHeight - 8;
        }

        super.recalcTextRendering();

        if (autoWidth)
        {
            width = renderedTextWidth + 8;
        }
        if (autoHeight)
        {
            height = renderedTextHeight + 8;
        }
    }

    /**
     * Set the size of a pane.
     * If either of sizes equals zero automatical calculation for that size is enabled.
     *
     * @param w the width.
     * @param h the height.
     */
    @Override
    public void setSize(int w, int h)
    {
        autoWidth = w == 0;
        autoHeight = h == 0;
        super.setSize(w, h);
    }

    @Override
    public void drawSelf(final PoseStack ms, final double mx, final double my)
    {
        // draw in last pass, not in main pass
    }

    @Override
    public void drawSelfLast(final PoseStack ms, final double mx, final double my)
    {
        if (!preparedText.isEmpty() && enabled)
        {
            x = (int) mx + CURSOR_BOX_SIZE - 4;
            y = (int) my - CURSOR_BOX_SIZE - 4;

            if (x + width + 3 > window.getScreen().width)
            {
                x = window.getScreen().width - width - 4;
            }

            if (y + height + 3 > window.getScreen().height)
            {
                y = window.getScreen().height - height - 4;
            }

            // modified INLINE: vanilla Screen#renderTooltip(MatrixStack, List<? extends IReorderingProcessor>, int, int, FontRenderer)
            ms.pushPose();
            ms.translate(x, y, Z_OFFSET);

            final BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            final Matrix4f mat = ms.last().pose();

            buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

            final int bg_a = (BACKGROUND_COLOR >> 24) & 0xff;
            final int bg_r = (BACKGROUND_COLOR >> 16) & 0xff;
            final int bg_g = (BACKGROUND_COLOR >> 8) & 0xff;
            final int bg_b = BACKGROUND_COLOR & 0xff;

            buffer.vertex(mat, 1, 1, 0).color(bg_r, bg_g, bg_b, bg_a).endVertex();
            buffer.vertex(mat, 0, 1, 0).color(bg_r, bg_g, bg_b, bg_a).endVertex();
            buffer.vertex(mat, 0, height - 1, 0).color(bg_r, bg_g, bg_b, bg_a).endVertex();
            buffer.vertex(mat, 1, height - 1, 0).color(bg_r, bg_g, bg_b, bg_a).endVertex();
            buffer.vertex(mat, 1, height, 0).color(bg_r, bg_g, bg_b, bg_a).endVertex();
            buffer.vertex(mat, width - 1, height, 0).color(bg_r, bg_g, bg_b, bg_a).endVertex();
            buffer.vertex(mat, width - 1, height - 1, 0).color(bg_r, bg_g, bg_b, bg_a).endVertex();
            buffer.vertex(mat, width, height - 1, 0).color(bg_r, bg_g, bg_b, bg_a).endVertex();
            buffer.vertex(mat, width, 1, 0).color(bg_r, bg_g, bg_b, bg_a).endVertex();
            buffer.vertex(mat, width - 1, 1, 0).color(bg_r, bg_g, bg_b, bg_a).endVertex();
            buffer.vertex(mat, width - 1, 0, 0).color(bg_r, bg_g, bg_b, bg_a).endVertex();
            buffer.vertex(mat, 1, 0, 0).color(bg_r, bg_g, bg_b, bg_a).endVertex();

            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            Tesselator.getInstance().end();
            drawLineRectGradient(ms, 1, 1, width - 2, height - 2, BORDER_COLOR_A, BORDER_COLOR_B, 1);

            RenderSystem.disableBlend();
            RenderSystem.enableTexture();

            ms.translate(-x, -y, 0.0d);
            super.drawSelf(ms, mx, my);
            ms.popPose();
        }
    }

    public int getMaxWidth()
    {
        return maxWidth;
    }

    /**
     * If width is set to 0 then the smallest possible width is used based on the displayed text.
     * This value can be used to cap such behaviour [default: 208 px].
     */
    public void setMaxWidth(final int maxWidth)
    {
        this.maxWidth = maxWidth;
    }

    public int getMaxHeight()
    {
        return maxHeight;
    }

    /**
     * If height is set to 0 then the smallest possible height is used based on the displayed text.
     * This value can be used to cap such behaviour [default: {@link AbstractTextElement#SIZE_FOR_UNLIMITED_ELEMENTS}].
     */
    public void setMaxHeight(final int maxHeight)
    {
        this.maxHeight = maxHeight;
    }

    @Override
    public boolean isPointInPane(final double mx, final double my)
    {
        // untargetable element
        return false;
    }
}
