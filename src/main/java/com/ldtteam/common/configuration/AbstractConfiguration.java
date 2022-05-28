package com.ldtteam.common.configuration;

import com.ldtteam.blockui.mod.BlockUI;
import com.ldtteam.common.language.LangUtils;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.common.ForgeConfigSpec.LongValue;
import java.util.List;
import java.util.function.Predicate;

public class AbstractConfiguration
{
    protected void createCategory(final Builder builder, final String key)
    {
        builder.comment(makeComment(key, Type.NONE)).push(key);
    }

    protected void swapToCategory(final Builder builder, final String key)
    {
        finishCategory(builder);
        createCategory(builder, key);
    }

    protected void finishCategory(final Builder builder)
    {
        builder.pop();
    }

    private static String nameTKey(final String key)
    {
        return BlockUI.MOD_ID + ".config." + key;
    }

    private static String makeComment(final String key, final Type type, final Object... defaultArguments)
    {
        final String base = LangUtils.translate(nameTKey(key) + ".comment");
        if (type != Type.NONE)
        {
            return base + " " + LangUtils.translate(nameTKey("default") + "." + type.name().toLowerCase(), defaultArguments);
        }
        return base;
    }

    private static Builder buildBase(final Builder builder, final String key, final Type type, final Object... defaultArguments)
    {
        return builder.comment(makeComment(key, type, defaultArguments)).translation(nameTKey(key));
    }

    protected static BooleanValue defineBoolean(final Builder builder, final String key, final boolean defaultValue)
    {
        return buildBase(builder, key, Type.BOOLEAN, defaultValue).define(key, defaultValue);
    }

    protected static IntValue defineInteger(final Builder builder, final String key, final int defaultValue)
    {
        return defineInteger(builder, key, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    protected static IntValue defineInteger(final Builder builder,
        final String key,
        final int defaultValue,
        final int min,
        final int max)
    {
        return buildBase(builder, key, Type.LONG, defaultValue, min, max).defineInRange(key, defaultValue, min, max);
    }

    protected static LongValue defineLong(final Builder builder, final String key, final long defaultValue)
    {
        return defineLong(builder, key, defaultValue, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    protected static LongValue defineLong(final Builder builder,
        final String key,
        final long defaultValue,
        final long min,
        final long max)
    {
        return buildBase(builder, key, Type.LONG, defaultValue, min, max).defineInRange(key, defaultValue, min, max);
    }

    protected static DoubleValue defineDouble(final Builder builder, final String key, final double defaultValue)
    {
        return defineDouble(builder, key, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    protected static DoubleValue defineDouble(final Builder builder,
        final String key,
        final double defaultValue,
        final double min,
        final double max)
    {
        return buildBase(builder, key, Type.DOUBLE, defaultValue, min, max).defineInRange(key, defaultValue, min, max);
    }

    protected static <T> ConfigValue<List<? extends T>> defineList(final Builder builder,
        final String key,
        final List<? extends T> defaultValue,
        final Predicate<Object> elementValidator)
    {
        return buildBase(builder, key, Type.NONE).defineList(key, defaultValue, elementValidator);
    }

    protected static <V extends Enum<V>> EnumValue<V> defineEnum(final Builder builder, final String key, final V defaultValue)
    {
        return buildBase(builder, key, Type.ENUM, defaultValue.name().toLowerCase()).defineEnum(key, defaultValue);
    }

    private enum Type
    {
        NONE,
        BOOLEAN,
        LONG,
        DOUBLE,
        ENUM;
    }
}
