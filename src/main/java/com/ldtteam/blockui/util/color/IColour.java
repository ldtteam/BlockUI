package com.ldtteam.blockui.util.color;

import com.mojang.blaze3d.vertex.BufferBuilder;

public interface IColour
{
    /**
     * @return red channel only, range 0-255
     */
    int red();

    /**
     * @return green channel only, range 0-255
     */
    int green();

    /**
     * @return blue channel only, range 0-255
     */
    int blue();

    /**
     * @return alpha channel only, range 0-255
     */
    int alpha();

    /**
     * @return alpha channel only, range 0-1
     */
    default float alphaF()
    {
        return alpha() / 255.0f;
    }

    /**
     * @return A-RGB joined format
     */
    int argb();

    /**
     * @return RGB-A joined format
     */
    int rgba();

    /**
     * @return quartet instance or this instance (if already in quartet format)
     */
    default ColourQuartet asQuartet()
    {
        return new ColourQuartet(red(), green(), blue(), alpha());
    }

    /**
     * @return RGB-A instance or this instance (if already in RGB-A format)
     */
    default ColourRGBA asRGBA()
    {
        return new ColourRGBA(rgba());
    }

    /**
     * @return A-RGB instance or this instance (if already in A-RGB format)
     */
    default ColourARGB asARGB()
    {
        return new ColourARGB(argb());
    }

    /**
     * @see BufferBuilder#color(int, int, int, int)
     */
    default void writeIntoBuffer(final BufferBuilder buffer)
    {
        buffer.color(red(), green(), blue(), alpha());
    }

    /**
     * @see BufferBuilder#defaultColor(int, int, int, int)
     */
    default void setInfoBufferDefaultColor(final BufferBuilder buffer)
    {
        buffer.defaultColor(red(), green(), blue(), alpha());
    }
}
