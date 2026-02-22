package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.Alignment;
import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.BOScreen;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.PaneParams;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import org.joml.Matrix3x2fStack;

import java.util.Collections;

/**
 * Element used for rendering tooltips.
 */
public class Tooltip extends AbstractTextElement
{
    public static final int DEFAULT_MAX_WIDTH = 208;
    public static final int DEFAULT_MAX_HEIGHT = AbstractTextElement.SIZE_FOR_UNLIMITED_ELEMENTS;

    private static final int CURSOR_BOX_SIZE = 12;
    private static final int Z_OFFSET = 200;
    private static final int BACKGROUND_COLOR = 0xf0100010; // TooltipRenderUtil.BACKGROUND_COLOR;
    private static final int BORDER_COLOR_A = 0x505000ff; // TooltipRenderUtil.BORDER_COLOR_TOP
    private static final int BORDER_COLOR_B = 0x5028007f; // TooltipRenderUtil.BORDER_COLOR_BOTTOM

    public static final int DEFAULT_TEXT_COLOR = 0xffffff; // white

    protected boolean autoWidth = true;
    protected boolean autoHeight = true;
    protected int maxWidth = DEFAULT_MAX_WIDTH;
    protected int maxHeight = DEFAULT_MAX_HEIGHT;

    /**
     * Standard constructor which instantiates the tooltip.
     * 
     * @see PaneBuilders#tooltipBuilder()
     * @deprecated {@link PaneBuilders#tooltipBuilder()}
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
            textWidth = maxWidth - 8 + 1;
        }
        if (autoHeight)
        {
            textHeight = maxHeight - 8;
        }

        super.recalcTextRendering();
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
    public void drawSelf(final BOGuiGraphics ms, final double mx, final double my)
    {
        // draw in last pass, not in main pass
    }

    @Override
    public void drawSelfLast(final BOGuiGraphics target, final double mx, final double my)
    {
        final Matrix3x2fStack ms = target.guiGraphics().pose();

        if (!preparedText.isEmpty() && isEnabled())
        {
            recalcPreparedTextBox();
            if (autoWidth)
            {
                width = renderedTextWidth + 8;
            }
            if (autoHeight)
            {
                height = renderedTextHeight + 8;
            }

            final BOScreen scr = window.getScreen();
            final double renderScale = scr.getRenderScale();
            final int marginOffset = 4;

            x = (int) mx + CURSOR_BOX_SIZE;
            y = Math.max(marginOffset, (int) my - CURSOR_BOX_SIZE);

            // really black box math here: scaled absolute cursor > width - scaled tooltip size
            if ((scr.getAbsoluteMouseX() + CURSOR_BOX_SIZE) * scr.getVanillaGuiScale() >
                scr.getFramebufferWidth() - renderScale * (width + marginOffset))
            {
                // if overflow then flip tooltip to left
                final int guiToWindowOffset = (int) (scr.getFramebufferWidth() - scr.width * renderScale) / 2;
                x = Math.max(marginOffset - guiToWindowOffset, x - 2 * CURSOR_BOX_SIZE - width);
            }

            // same condition (just sign change for CURSOR_BOX_SIZE)
            final int absoluteY = (int) ((scr.getAbsoluteMouseY() - CURSOR_BOX_SIZE) * scr.getVanillaGuiScale());
            final int maxAbsoluteMy = scr.getFramebufferHeight() - (int) (renderScale * (height + marginOffset));
            if (absoluteY > maxAbsoluteMy)
            {
                // but we don't flip here but just move upwards
                y -= (absoluteY - maxAbsoluteMy) / renderScale;
            }

            // modified INLINE: vanilla Screen#renderTooltip(MatrixStack, List<? extends IReorderingProcessor>, int, int, FontRenderer)
            TooltipRenderUtil.renderTooltipBackground(target.guiGraphics(), x, y, width, height, null);

            super.innerDrawSelf(target, mx, my);
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

    /**
     * Class to mark tooltip which has automatically generated content
     */
    public static final class AutomaticTooltip extends Tooltip
    {
    }
}
