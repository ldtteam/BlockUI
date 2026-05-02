package com.ldtteam.blockui.util.color;

/**
 * Colour backed by A-RGB format
 */
public record ColourARGB(int argb) implements IColour
{
    public ColourARGB(int rgb, int alpha)
    {
        this((rgb & 0x00ffffff) | ((alpha & MAX_INT_VALUE) << 24));
    }

    @Override
    public int alpha()
    {
        return (argb >> 24) & MAX_INT_VALUE;
    }

    @Override
    public int red()
    {
        return (argb >> 16) & MAX_INT_VALUE;
    }

    @Override
    public int green()
    {
        return (argb >> 8) & MAX_INT_VALUE;
    }

    @Override
    public int blue()
    {
        return (argb >> 0) & MAX_INT_VALUE;
    }

    @Override
    public int rgba()
    {
        return (argb << 8) | (alpha());
    }

    @Override
    public ColourARGB asARGB()
    {
        return this;
    }
}
