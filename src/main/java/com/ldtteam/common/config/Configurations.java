package com.ldtteam.common.config;

import com.ldtteam.common.config.AbstractConfiguration.ConfigWatcher;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.ValueSpec;
import org.apache.commons.lang3.tuple.Pair;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Mod root configuration.
 */
public class Configurations<CLIENT extends AbstractConfiguration,
    SERVER extends AbstractConfiguration,
    COMMON extends AbstractConfiguration>
{
    /**
     * Loaded clientside, not synced
     */
    private final ModConfig client;
    private final CLIENT clientConfig;

    /**
     * Loaded serverside (per world), synced on connection
     */
    private final ModConfig server;
    private final SERVER serverConfig;

    /**
     * Loaded both sides, not synced
     */
    private final ModConfig common;
    private final COMMON commonConfig;

    private final ModConfig[] activeModConfigs;
    private final AbstractConfiguration[] activeConfigs;

    /**
     * Builds configuration tree.
     *
     * @param modContainer from event
     */
    public Configurations(final ModContainer modContainer,
        final IEventBus modBus,
        final Function<Builder, CLIENT> clientFactory,
        final Function<Builder, SERVER> serverFactory,
        final Function<Builder, COMMON> commonFactory)
    {
        final List<ModConfig> modConfigs = new ArrayList<>();
        final List<AbstractConfiguration> configs = new ArrayList<>();

        final Pair<CLIENT, ModConfig> cli = createConfig(clientFactory, Type.CLIENT, modContainer, modConfigs, configs);
        client = cli.getRight();
        clientConfig = cli.getLeft();

        final Pair<SERVER, ModConfig> ser = createConfig(serverFactory, Type.SERVER, modContainer, modConfigs, configs);
        server = ser.getRight();
        serverConfig = ser.getLeft();

        final Pair<COMMON, ModConfig> com = createConfig(commonFactory, Type.COMMON, modContainer, modConfigs, configs);
        common = com.getRight();
        commonConfig = com.getLeft();

        activeModConfigs = modConfigs.toArray(ModConfig[]::new);
        activeConfigs = configs.toArray(AbstractConfiguration[]::new);

        // register events for watchers
        modBus.addListener(ModConfigEvent.Loading.class, event -> onConfigLoad(event.getConfig()));
        modBus.addListener(ModConfigEvent.Reloading.class, event -> onConfigReload(event.getConfig()));
    }

    private <T extends AbstractConfiguration> Pair<T, ModConfig> createConfig(final Function<Builder, T> factory,
        final Type type,
        final ModContainer modContainer,
        final List<ModConfig> modConfigs,
        final List<AbstractConfiguration> configs)
    {
        // dont create client classes on server to avoid class loading issues
        if (factory == null || (type == Type.CLIENT && !FMLEnvironment.dist.isClient()))
        {
            return Pair.of(null, null);
        }

        final Pair<T, ModConfigSpec> builtConfig = new ModConfigSpec.Builder().configure(factory);
        final ModConfig modConfig = new ModConfig(type, builtConfig.getRight(), modContainer);
        final T config = builtConfig.getLeft();

        modContainer.addConfig(modConfig);
        modConfigs.add(modConfig);
        configs.add(config);

        return Pair.of(config, modConfig);
    }

    public CLIENT getClient()
    {
        return clientConfig;
    }

    public SERVER getServer()
    {
        return serverConfig;
    }

    public COMMON getCommon()
    {
        return commonConfig;
    }

    /**
     * cache starting values for watchers
     */
    private void onConfigLoad(final ModConfig modConfig)
    {
        if (client != null && modConfig.getSpec() == client.getSpec())
        {
            clientConfig.watchers.forEach(ConfigWatcher::cacheLastValue);
        }
        else if (server != null && modConfig.getSpec() == server.getSpec())
        {
            serverConfig.watchers.forEach(ConfigWatcher::cacheLastValue);
        }
        else if (common != null && modConfig.getSpec() == common.getSpec())
        {
            commonConfig.watchers.forEach(ConfigWatcher::cacheLastValue);
        }
    }

    /**
     * iterate watchers and fire changes if needed
     */
    private void onConfigReload(final ModConfig modConfig)
    {
        if (client != null && modConfig.getSpec() == client.getSpec())
        {
            clientConfig.watchers.forEach(ConfigWatcher::compareAndFireChangeEvent);
        }
        else if (server != null && modConfig.getSpec() == server.getSpec())
        {
            serverConfig.watchers.forEach(ConfigWatcher::compareAndFireChangeEvent);
        }
        else if (common != null && modConfig.getSpec() == common.getSpec())
        {
            commonConfig.watchers.forEach(ConfigWatcher::compareAndFireChangeEvent);
        }
    }

    /**
     * Setter wrapper so watchers are fine. This should be called from any code that manually changes ConfigValues using set functions.
     * (Mostly done by settings UIs)
     */
    public <T> void set(final ConfigValue<T> configValue, final T value)
    {
        configValue.set(value);
        onConfigValueEdit(configValue);
    }

    /**
     * This should be called from any code that manually changes ConfigValues using set functions. (Mostly done by settings UIs)
     *
     * @param configValue which config value was changed
     */
    public void onConfigValueEdit(final ConfigValue<?> configValue)
    {
        for (final AbstractConfiguration cfg : activeConfigs)
        {
            for (final ConfigWatcher<?> configWatcher : cfg.watchers)
            {
                if (configWatcher.isSameForgeConfig(configValue))
                {
                    configWatcher.compareAndFireChangeEvent();
                }
            }
        }
    }

    private final Map<ConfigValue<?>, Optional<ValueSpec>> valueSpecCache = new IdentityHashMap<>();

    /**
     * @param  value config value from this mod
     * @return       value spec, crashes in dev if not found
     */
    public Optional<ValueSpec> getSpecFromValue(final ConfigValue<?> value)
    {
        return valueSpecCache.computeIfAbsent(value, key -> {
            for (final ModConfig cfg : activeModConfigs)
            {
                if (cfg.getSpec().get(value.getPath()) instanceof final ValueSpec valueSpec)
                {
                    return Optional.of(valueSpec);
                }
            }

            if (!FMLEnvironment.production)
            {
                throw new RuntimeException("Cannot find backing ValueSpec for: " + value.getPath());
            }

            return Optional.empty();
        });
    }
}
