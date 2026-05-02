package com.ldtteam.blockui.util.color;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.network.chat.TextColor;

public interface IColour
{
    // channel transformations
    public static final float MAX_FLOAT_VALUE = 255.0f;
    public static final int MAX_INT_VALUE = 255;

    public static float asFloat(final int value)
    {
        return value / MAX_FLOAT_VALUE;
    }

    public static int asInt(final float value)
    {
        return (int) Math.floor(value * MAX_FLOAT_VALUE);
    }

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
        return alpha() / MAX_FLOAT_VALUE;
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
    default ColourQuartet4i asIntQuartet()
    {
        return new ColourQuartet4i(red(), green(), blue(), alpha());
    }

    /**
     * @return quartet instance or this instance (if already in quartet format)
     */
    default ColourQuartet4f asFloatQuartet()
    {
        return new ColourQuartet4f(red(), green(), blue(), alpha());
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
     * @see VertexConsumer#setColor(int, int, int, int)
     */
    default void writeIntoBuffer(final VertexConsumer buffer)
    {
        buffer.setColor(red(), green(), blue(), alpha());
    }

    default TextColor toTextColor()
    {
        return TextColor.fromRgb(argb());
    }

    public static final IColour ZERO = new IColour()
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
