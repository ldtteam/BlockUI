package com.ldtteam.common.config;

import com.ldtteam.common.language.LanguageHandler;
import net.minecraft.server.TickTask;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;
import net.neoforged.neoforge.common.ModConfigSpec.LongValue;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractConfiguration
{
    public static final String DEFAULT_KEY_PREFIX = "structurize.config.default.";
    public static final String COMMENT_SUFFIX = ".comment";

    final List<ConfigWatcher<?>> watchers = new ArrayList<>();
    private final Builder builder;
    private final String modId;

    private boolean nextDefineWorldRestart = false;

    protected AbstractConfiguration(final Builder builder, final String modId)
    {
        this.builder = builder;
        this.modId = modId;
    }

    protected void createCategory(final String key)
    {
        if (nextDefineWorldRestart)
        {
            throw new IllegalStateException("Categories cannot have worldRestart flag!");
        }
        buildBase(key, null).push(key);
    }

    protected void swapToCategory(final String key)
    {
        finishCategory();
        createCategory(key);
    }

    protected void finishCategory()
    {
        builder.pop();
    }

    private String nameTKey(final String key)
    {
        return modId + ".config." + key;
    }

    private String commentTKey(final String key)
    {
        return nameTKey(key) + COMMENT_SUFFIX;
    }

    /**
     * Everything must call this class in the end
     */
    private Builder buildBase(final String key, @Nullable final String defaultDesc)
    {
        if (nextDefineWorldRestart)
        {
            nextDefineWorldRestart = false;
            builder.worldRestart();
        }

        String comment = translate(commentTKey(key));
        if (defaultDesc != null && !defaultDesc.isBlank())
        {
            comment += " " + defaultDesc;
        }

        return builder.comment(comment).translation(nameTKey(key));
    }

    private static String translate(final String key, final Object... args)
    {
        return LanguageHandler.translateKey(key).formatted(args);
    }

    protected AbstractConfiguration requiresWorldRestart()
    {
        nextDefineWorldRestart = true;
        return this;
    }

    protected BooleanValue defineBoolean(final String key, final boolean defaultValue)
    {
        return buildBase(key, translate(DEFAULT_KEY_PREFIX + "boolean", defaultValue)).define(key, defaultValue);
    }

    protected IntValue defineInteger(final String key, final int defaultValue)
    {
        return defineInteger(key, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    protected IntValue defineInteger(final String key, final int defaultValue, final int min, final int max)
    {
        return buildBase(key, translate(DEFAULT_KEY_PREFIX + "number", defaultValue, min, max))
            .defineInRange(key, defaultValue, min, max);
    }

    protected ConfigValue<String> defineString(final String key, final String defaultValue)
    {
        return buildBase(key, translate(DEFAULT_KEY_PREFIX + "string", defaultValue)).define(key, defaultValue);
    }

    protected LongValue defineLong(final String key, final long defaultValue)
    {
        return defineLong(key, defaultValue, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    protected LongValue defineLong(final String key, final long defaultValue, final long min, final long max)
    {
        return buildBase(key, translate(DEFAULT_KEY_PREFIX + "number", defaultValue, min, max))
            .defineInRange(key, defaultValue, min, max);
    }

    protected DoubleValue defineDouble(final String key, final double defaultValue)
    {
        return defineDouble(key, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    protected DoubleValue defineDouble(final String key, final double defaultValue, final double min, final double max)
    {
        return buildBase(key, translate(DEFAULT_KEY_PREFIX + "number", defaultValue, min, max))
            .defineInRange(key, defaultValue, min, max);
    }

    protected <T> ConfigValue<List<? extends T>> defineList(final String key,
        final List<? extends T> defaultValue,
        final Predicate<Object> elementValidator)
    {
        return buildBase(key, null).defineList(key, defaultValue, elementValidator);
    }

    protected <T> ConfigValue<List<? extends T>> defineListAllowEmpty(final String key,
        final List<? extends T> defaultValue,
        final Predicate<Object> elementValidator)
    {
        return buildBase(key, null).defineListAllowEmpty(key, defaultValue, elementValidator);
    }

    protected <V extends Enum<V>> EnumValue<V> defineEnum(final String key, final V defaultValue)
    {
        return buildBase(key,
            translate(DEFAULT_KEY_PREFIX + "enum",
                defaultValue,
                Arrays.stream(defaultValue.getDeclaringClass().getEnumConstants()).map(Enum::name).collect(Collectors.joining(", "))))
            .defineEnum(key, defaultValue);
    }

    protected <T> void addWatcher(final ConfigValue<T> configValue, final ConfigListener<T> listener)
    {
        watchers.add(new ConfigWatcher<>(listener, configValue));
    }

    @SuppressWarnings("unchecked")
    protected void addWatcher(final Runnable listener, final ConfigValue<?>... configValues)
    {
        final ConfigListener<Object> typedListener = (o, n) -> listener.run();
        for (final ConfigValue<?> c : configValues)
        {
            watchers.add(new ConfigWatcher<>(typedListener, (ConfigValue<Object>) c));
        }
    }

    @FunctionalInterface
    public static interface ConfigListener<T>
    {
        /**
         * @param oldValue old config value
         * @param newValue new/current config value
         */
        void onChange(T oldValue, T newValue);
    }

    /**
     * synchronized due to nature of config events
     */
    static class ConfigWatcher<T>
    {
        private final ConfigListener<T> listener;
        private final ConfigValue<T> forgeConfig;

        @Nullable
        private T lastValue;

        private ConfigWatcher(final ConfigListener<T> listener, final ConfigValue<T> forgeConfig)
        {
            this.listener = listener;
            this.forgeConfig = forgeConfig;
        }

        boolean isSameForgeConfig(final ConfigValue<?> other)
        {
            return other == forgeConfig;
        }

        synchronized void cacheLastValue()
        {
            lastValue = forgeConfig.get();
        }

        synchronized void compareAndFireChangeEvent()
        {
            final T newValue = forgeConfig.get();

            if (!Objects.equals(newValue, lastValue))
            {
                LogicalSidedProvider.WORKQUEUE.get(FMLEnvironment.dist.isClient() ? LogicalSide.CLIENT : LogicalSide.SERVER)
                    .tell(new TickTask(0, () -> listener.onChange(lastValue, newValue)));
                lastValue = newValue;
            }
        }
    }
}
