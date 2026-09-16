package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.mod.config.BlockUIClientConfiguration;
import com.ldtteam.common.config.AbstractConfiguration;
import com.ldtteam.common.config.Configurations;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.common.NeoForge;

@Mod(BlockUI.MOD_ID)
public class BlockUI
{
    public static final String MOD_ID = "blockui";
    public static Configurations<BlockUIClientConfiguration, AbstractConfiguration, AbstractConfiguration> configuration;

    public BlockUI(final FMLModContainer modContainer, final Dist dist)
    {
        final IEventBus modBus = modContainer.getEventBus();
        final IEventBus forgeBus = NeoForge.EVENT_BUS;

        configuration = new Configurations<>(
            modContainer,
            modBus,
            BlockUIClientConfiguration::new,
            null,
            null);

        if (dist.isClient())
        {
            modBus.register(ClientLifecycleSubscriber.class);
            forgeBus.register(ClientEventSubscriber.class);
        }
    }

    public static boolean isHighContrastCountEnabled()
    {
        return configuration != null && configuration.getClient() != null && configuration.getClient().highContrastCount.get();
    }
}
