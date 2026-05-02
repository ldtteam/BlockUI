package com.ldtteam.blockui.util.color;

import org.joml.Vector4f;

/**
 * Colour backed by four separated channel values. Generally good choice for rendering related things
 */
public record ColourQuartet4f(float redF, float greenF, float blueF, float alphaF) implements IColour
{
    public ColourQuartet4f(final Vector4f color)
    {
        this(color.x, color.y, color.z, color.w);
    }

    @Override
    public int red()
    {
        return IColour.asInt(redF);
    }

    @Override
    public int green()
    {
        return IColour.asInt(greenF);
    }

    @Override
    public int blue()
    {
        return IColour.asInt(blueF);
    }

    @Override
    public int alpha()
    {
        return IColour.asInt(alphaF);
    }

    @Override
    public float alphaF()
    {
        return alphaF;
    }

    @Override
    public int argb()
    {
        return (alpha() << 24) | (red() << 16) | (green() << 8) | (blue() << 0);
    }

    @Override
    public int rgba()
    {
        return (red() << 24) | (green() << 16) | (blue() << 8) | (alpha() << 0);
    }

    @Override
    public ColourQuartet4f asFloatQuartet()
    {
        return this;
    }
}
