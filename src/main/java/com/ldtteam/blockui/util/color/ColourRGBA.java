package com.ldtteam.blockui.util.color;

/**
 * Colour backed by RGB-A format
 */
public record ColourRGBA(int rgba) implements IColour
{
    public ColourRGBA(int rgb, int alpha)
    {
        this((rgb << 8) | (alpha & MAX_INT_VALUE));
    }

    @Override
    public int red()
    {
        return (rgba >> 24) & MAX_INT_VALUE;
    }

    @Override
    public int green()
    {
        return (rgba >> 16) & MAX_INT_VALUE;
    }

    @Override
    public int blue()
    {
        return (rgba >> 8) & MAX_INT_VALUE;
    }

    @Override
    public int alpha()
    {
        return (rgba >> 0) & MAX_INT_VALUE;
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
