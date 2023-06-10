package com.ldtteam.blockui.views;

import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.Color;
import com.ldtteam.blockui.PaneParams;

/**
 * Simple box element.
 */
public class Box extends View
{
    private int lineWidth = 1;
    private int color = 0xff000000;

    /**
     * Required default constructor.
     */
    public Box()
    {
        super();
    }

    /**
     * Loads box from xml.
     *
     * @param params xml parameters.
     */
    public Box(final PaneParams params)
    {
        super(params);
        lineWidth = params.getInteger("linewidth", lineWidth);
        color = params.getColor("color", color);
    }

    /**
     * Set the color of the box.
     * @param red the red.
     * @param green the green.
     * @param blue the blue.
     */
    public void setColor(final int red, final int green, final int blue)
    {
        this.color = Color.rgbaToInt(red, green, blue, 255);
    }

    /**
     * Setter for the line width property.
     * @param lineWidth
     */
    public void setLineWidth(final int lineWidth) {
        this.lineWidth = lineWidth;
    }

    @Override
    public void drawSelf(final BOGuiGraphics ms, final double mx, final double my)
    {
        drawLineRect(ms.pose(), x, y, width, height, color, lineWidth);

        super.drawSelf(ms, mx, my);
    }

    @Override
    public void drawSelfLast(final BOGuiGraphics ms, final double mx, final double my)
    {
        super.drawSelfLast(ms, mx, my);
    }
}
