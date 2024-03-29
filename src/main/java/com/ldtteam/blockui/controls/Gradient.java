package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.Color;
import com.ldtteam.blockui.PaneParams;

/**
 * BlockOut gradient pane. Used to render a gradient.
 */
public class Gradient extends AbstractTextElement
{
    /**
     * Default Gradients. Some transparent gray value.
     */
    private int gradientStart = -1072689136;
    private int gradientEnd = -804253680;

    /**
     * Standard constructor which instantiates a new label.
     */
    public Gradient()
    {
        super(DEFAULT_TEXT_ALIGNMENT, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_SHADOW, true);
        recalcTextRendering();
    }

    /**
     * Create a label from xml.
     *
     * @param params xml parameters.
     */
    public Gradient(final PaneParams params)
    {
        super(params, DEFAULT_TEXT_ALIGNMENT, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_SHADOW, true);
        gradientStart = params.getColor("gradientstart", gradientStart);
        gradientEnd = params.getColor("gradientend", gradientEnd);
        recalcTextRendering();
    }

    /**
     * Set the gradient start color.
     * @param red the red.
     * @param blue the blue.
     * @param green the green.
     * @param alpha the alpha value.
     */
    public void setGradientStart(final int red, final int green, final int blue, final int alpha)
    {
        this.gradientStart = Color.rgbaToInt(red, green, blue, alpha);
    }

    /**
     * Set the gradient end color.
     * @param red the red.
     * @param blue the blue.
     * @param green the green.
     * @param alpha the alpha value.
     */
    public void setGradientEnd(final int red, final int green, final int blue, final int alpha)
    {
        this.gradientEnd = Color.rgbaToInt(red, green, blue, alpha);
    }

    @Override
    public void drawSelf(final BOGuiGraphics target, final double mx, final double my)
    {
        fillGradient(target.pose(), x, y, width, height, gradientStart, gradientEnd);
        super.drawSelf(target, mx, my);
    }
}
