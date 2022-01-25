package com.ldtteam.blockui;

import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Color utility methods.
 */
public final class Color
{
    private static final Map<String, Integer> nameToColorMap = new HashMap<>();
    static
    {
        // Would love to load these from a file
        putRGB("aqua", 0x00FFFF);
        putRGB("black", 0x000000);
        putRGB("blue", 0x0000FF);
        putRGB("cyan", 0x00FFFF);
        putRGB("fuchsia", 0xFF00FF);
        putRGB("green", 0x008000);
        putRGB("ivory", 0xFFFFF0);
        putRGB("lime", 0x00FF00);
        putRGB("magenta", 0xFF00FF);
        putRGB("orange", 0xFFA500);
        putRGB("orangered", 0xFF4500);
        putRGB("purple", 0x800080);
        putRGB("red", 0xFF0000);
        putRGB("white", 0xFFFFFF);
        putRGB("yellow", 0xFFFF00);
        putRGB("gray", 0x808080);
        putRGB("darkgray", 0xA9A9A9);
        putRGB("dimgray", 0x696969);
        putRGB("lightgray", 0xD3D3D3);
        putRGB("slategray", 0x708090);
        putRGB("darkgreen", 0x006400);
    }

    private static void putRGB(final String colorName, final int colorRGB)
    {
        nameToColorMap.put(colorName, 0xff000000 | colorRGB);
    }

    private Color()
    {
        // Hides default constructor.
    }

    /**
     * Parses a color or returns the default
     *
     * @param color a string representation of the color, in rgba, hex, or int
     * @param def   the fallback value
     * @return the parsed or defaulted color integer
     */
    public static int parse(final String color, final int def)
    {
        final Integer result = Parsers.COLOR.apply(color);
        return result != null ? result : def;
    }

    /**
     * Get a color integer from its name.
     *
     * @param name name of the color.
     * @param def  default to use if the name doesn't exist.
     * @return the color as an integer.
     */
    public static int getByName(final String name, final int def)
    {
        final Integer color = getByName(name);
        return color != null ? color : def;
    }

    /**
     * Get a color integer from its name.
     *
     * @param name name of the color.
     * @return the color as an integer.
     */
    @Nullable
    public static Integer getByName(final String name)
    {
        return nameToColorMap.get(name.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Get the int from rgba.
     *
     * @param r the red value from 0-255.
     * @param g the green value from 0-255.
     * @param b the blue value from 0-255.
     * @param a the transparency value from 0-255.
     * @return the accumulated int.
     */
    public static int rgbaToInt(final int r, final int g, final int b, final int a)
    {
        return ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
    }

    public static int rgbaToInt(final Matcher m)
    {
        final int r = Mth.clamp(Integer.parseInt(m.group(1)), 0, 255);
        final int g = Mth.clamp(Integer.parseInt(m.group(2)), 0, 255);
        final int b = Mth.clamp(Integer.parseInt(m.group(3)), 0, 255);
        final int a = m.groupCount() > 4 ? Mth.clamp((int) Double.parseDouble(m.group(4)) * 255, 0, 255) : 255;

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static TextColor toVanilla(final int color)
    {
        return TextColor.fromRgb(color);
    }
}
