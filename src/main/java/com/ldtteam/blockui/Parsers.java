package com.ldtteam.blockui;

import com.ldtteam.blockui.mod.Log;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Parsers
{
    private Parsers()
    {
        /* prevent construction */
    }

    public static final Pattern PERCENTAGE_PATTERN = Pattern.compile("([-+]?\\d+)(%|px)?", Pattern.CASE_INSENSITIVE);
    public static final Pattern RGBA_PATTERN = Pattern
        .compile("rgba?\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*(?:,\\s*([01]\\.\\d+)\\s*)?\\)", Pattern.CASE_INSENSITIVE);
    public static final Pattern HEXADECIMAL_PATTERN = Pattern.compile("#([0-9A-F]{6,8})", Pattern.CASE_INSENSITIVE);

    // Primitives
    public static Function<String, Boolean> BOOLEAN = v -> v == null || !v.isEmpty() && !v.equals("disabled") && Boolean.parseBoolean(v);
    public static Function<String, Integer> INT = Integer::parseInt;
    public static Function<String, Float> FLOAT = Float::parseFloat;
    public static Function<String, Double> DOUBLE = Double::parseDouble;

    /** Parses a resource location, include shorthand tricks */
    public static Function<String, ResourceLocation> RESOURCE = ResourceLocation::new;

    /** Parses a potentially translatable portion of text as a component */
    private static Function<String, String> RAW_TEXT = v -> {
        String result = v == null ? "" : v;
        final Matcher m = Pattern.compile("\\$[({](\\S+)[})]").matcher(result);

        while (m.find())
        {
            String translated = I18n.get(m.group(1));
            if (translated.equals(m.group(1)))
            {
                translated = "MISSING: " + m.group(1);
            }
            result = result.replace(m.group(0), translated);
        }

        return result;
    };

    /** Parses a potentially translatable portion of text as a component */
    public static Function<String, MutableComponent> TEXT = v -> {
        final String result = RAW_TEXT.apply(v);
        return result == null ? null : new TextComponent(result);
    };

    /** Applies the TEXT parser across multiple lines */
    public static Function<String, List<MutableComponent>> MULTILINE = v -> Arrays.stream(Parsers.RAW_TEXT.apply(v).split("(\\\\n|\\n)"))
        .map(TextComponent::new)
        .map(MutableComponent.class::cast)
        .toList();

    /** Parses a color from hex, rgba, name, or pure value */
    public static Function<String, Integer> COLOR = v -> {
        Matcher m = HEXADECIMAL_PATTERN.matcher(v);
        if (m.find())
        {
            return Integer.parseInt(m.group(1), 16);
        }

        m = RGBA_PATTERN.matcher(v);
        if (m.find())
        {
            return Color.rgbaToInt(m);
        }

        try
        {
            return Integer.parseUnsignedInt(v);
        }
        catch (final NumberFormatException ex)
        {
            return Color.getByName(v);
        }
    };

    /**
     * Parses a number, which may potentially be marked as a percentage of the given total
     *
     * @param total the number that the parsed value may be a percentage of
     */
    public static Function<String, Integer> SCALED(int total)
    {
        return v -> {
            try
            {
                final Matcher m = PERCENTAGE_PATTERN.matcher(v);
                if (!m.find())
                    return null;

                final int value = Integer.parseInt(m.group(1));
                return m.group(2) != null && m.group(2).equals("%") ? total * Mth.clamp(value, 0, 100) / 100 : value;
            }
            catch (final NumberFormatException | IndexOutOfBoundsException | IllegalStateException ex)
            {
                Log.getLogger().warn(ex);
            }

            return null;
        };
    }

    /**
     * Parses multiple numbers, where each may be a percentage of a given total
     *
     * @param totals a list of totals that correlate with the position in the shorthand string
     */
    public static Function<String, List<Integer>> SCALED(int... totals)
    {
        return v -> {
            if (v == null)
            {
                return null;
            }

            final List<Integer> results = new ArrayList<>(totals.length);

            final String[] values = v.split("\\s*[,\\s]\\s*");
            for (int i = 0; i < totals.length; i++)
            {
                int index = values.length > i ? i : Math.min(i % 2, values.length - 1);
                results.add(Parsers.SCALED(totals[i]).apply(values[index]));
            }

            return results;
        };
    }

    /**
     * Supply an enumeration class and this will parse it
     *
     * @param clazz the enum class to parse against
     * @param <T>   they enum class type
     */
    public static <T extends Enum<T>> Function<String, T> ENUM(Class<T> clazz)
    {
        return v -> {
            try
            {
                return Enum.valueOf(clazz, v);
            }
            catch (IllegalArgumentException | NullPointerException e)
            {
                Log.getLogger().warn("Attempt to access non-existent enumeration '" + v + "'.");
            }
            return null;
        };
    }

    /**
     * A function factory to create a shorthand parser that will determine the number of parts
     * in a string and fill it out to the given number of parts
     *
     * @param parser the parser to used for each individual part
     * @param parts  the max number of parts that the result will have
     * @param <T>    describes the type of each parsed value
     */
    public static <T> Function<String, List<T>> shorthand(Function<String, T> parser, int parts)
    {
        return v -> {
            if (v == null)
            {
                return null;
            }

            final List<T> results = new ArrayList<>(parts);

            for (final String segment : v.split("\\s*[,\\s]\\s*"))
            {
                results.add(parser.apply(segment));
            }

            while (results.size() < parts)
            {
                // Will duplicate in pairs, so a 4-part property defined
                // from "2 8" will become "2 8 2 8"
                // useful for syncing vertical and horizontal for each edge
                results.add(results.get(Math.max(0, results.size() - 2)));
            }

            return results;
        };
    }
}
