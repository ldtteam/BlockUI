package com.ldtteam.blockui.util.color;

/**
 * Colour backed by RGB-A format
 */
public record ColourRGBA(int rgba) implements IColour
{
    @Override
    public int red()
    {
        return (rgba >> 24) & 0xff;
    }

    @Override
    public int green()
    {
        return (rgba >> 16) & 0xff;
    }

    @Override
    public int blue()
    {
        return (rgba >> 8) & 0xff;
    }

    @Override
    public int alpha()
    {
        return (rgba >> 0) & 0xff;
    }

    @Override
    public int argb()
    {
        return (rgba >> 8) | (alpha() << 24);
    }

    @Override
    public ColourRGBA asRGBA()
    {
        return this;
    }
}
