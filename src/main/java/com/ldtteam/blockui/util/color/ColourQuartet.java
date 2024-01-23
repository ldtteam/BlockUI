package com.ldtteam.blockui.util.color;

/**
 * Colour backed by four separated channel values. Generally good choice for rendering related things
 */
public record ColourQuartet(int red, int green, int blue, int alpha) implements IColour
{
    @Override
    public int argb()
    {
        return (alpha << 24) | (red << 16) | (green << 8) | (blue << 0);
    }

    @Override
    public int rgba()
    {
        return (red << 24) | (green << 16) | (blue << 8) | (alpha << 0);
    }

    @Override
    public ColourQuartet asQuartet()
    {
        return this;
    }
}
