package com.ldtteam.blockui.util.color;

/**
 * Colour backed by A-RGB format
 */
public record ColourARGB(int argb) implements IColour
{
    @Override
    public int alpha()
    {
        return (argb >> 24) & 0xff;
    }

    @Override
    public int red()
    {
        return (argb >> 16) & 0xff;
    }

    @Override
    public int green()
    {
        return (argb >> 8) & 0xff;
    }

    @Override
    public int blue()
    {
        return (argb >> 0) & 0xff;
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
