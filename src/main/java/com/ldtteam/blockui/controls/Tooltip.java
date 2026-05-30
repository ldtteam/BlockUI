package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.Alignment;
import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.BOScreen;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.util.records.SizeI;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.Nullable;
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
    private static final int CONTENT_PADDING = TooltipRenderUtil.PADDING; // one direction

    public static final int DEFAULT_TEXT_COLOR = 0xFFffffff; // white

    protected boolean autoWidth = true;
    protected boolean autoHeight = true;
    protected int maxWidth = DEFAULT_MAX_WIDTH;
    protected int maxHeight = DEFAULT_MAX_HEIGHT;

    // TODO: make AbstractTextElement accept this as part of text - requires big type changes
    @Nullable
    protected ClientTooltipComponent tooltipComponent = null;
    private SizeI tooltipComponentSize = null;

    @Nullable
    protected Identifier style = null;
    private ResolvedBlit backgroundBlit = null;
    private ResolvedBlit frameBlit = null;

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
        textOffsetX = CONTENT_PADDING;
        textOffsetY = CONTENT_PADDING;
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
            textWidth = maxWidth - 2 * CONTENT_PADDING + 1;
        }
        if (autoHeight)
        {
            textHeight = maxHeight - 2 * CONTENT_PADDING;
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

    public void setStyle(@Nullable final Identifier style)
    {
        this.style = style;
        backgroundBlit = null;
        frameBlit = null;
    }

    public Identifier getStyle()
    {
        return style;
    }

    public void setTooltipComponent(@Nullable final TooltipComponent tooltipComponent)
    {
        setTooltipComponent(tooltipComponent == null ? null : ClientTooltipComponent.create(tooltipComponent));
    }

    public void setTooltipComponent(@Nullable final ClientTooltipComponent tooltipComponent)
    {
        this.tooltipComponent = tooltipComponent;
        tooltipComponentSize = null;
    }

    public ClientTooltipComponent getTooltipComponent()
    {
        return tooltipComponent;
    }

    @Override
    public void drawSelf(final BOGuiGraphics ms, final double mx, final double my)
    {
        // draw in last pass, not in main pass
    }

    @Override
    public void drawSelfLast(final BOGuiGraphics target, final double mx, final double my)
    {
        final Matrix3x2fStack ms = target.pose();

        if (!preparedText.isEmpty() && isEnabled())
        {
            if (backgroundBlit == null)
            {
                backgroundBlit = Image.resolveBlit(TooltipRenderUtil.getBackgroundSprite(style), 0, 0, 0, 0);
            }
            if (frameBlit == null)
            {
                frameBlit = Image.resolveBlit(TooltipRenderUtil.getFrameSprite(style), 0, 0, 0, 0);
            }
            if (tooltipComponentSize == null)
            {
                // -2 for manual adjustment of terrible mojang math
                tooltipComponentSize = tooltipComponent == null ? new SizeI(0, 0) :
                    new SizeI(tooltipComponent.getWidth(mc.font), tooltipComponent.getHeight(mc.font) - 2);
            }

            recalcPreparedTextBox();
            if (autoWidth)
            {
                width = Math.max(renderedTextWidth, tooltipComponentSize.width()) + 2 * CONTENT_PADDING;
            }
            if (autoHeight)
            {
                height = renderedTextHeight + tooltipComponentSize.height() + 2 * CONTENT_PADDING;
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
            // INLINE: update from net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil
            ms.pushMatrix();

            final int shift = TooltipRenderUtil.MARGIN;
            backgroundBlit.blit(target, x - shift, y - shift, width + 2 * shift, height + 2 * shift);
            frameBlit.blit(target, x - shift, y - shift, width + 2 * shift, height + 2 * shift);

            super.innerDrawSelf(target, mx, my);

            if (tooltipComponent != null)
            {
                final int adjustedY = y + CONTENT_PADDING + renderedTextHeight + textLinespace;
                tooltipComponent.extractText(target, mc.font, x + CONTENT_PADDING, adjustedY);
                tooltipComponent.extractImage(mc.font,
                    x + CONTENT_PADDING,
                    adjustedY,
                    Math.min(tooltipComponentSize.width(), width - 2 * CONTENT_PADDING),
                    Math.min(tooltipComponentSize.height(), height - 2 * CONTENT_PADDING),
                    target);
            }

            ms.popMatrix();
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
