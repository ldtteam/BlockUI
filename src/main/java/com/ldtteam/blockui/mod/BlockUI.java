package com.ldtteam.blockui.mod;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.common.NeoForge;
import java.util.HashMap;
import java.util.Map;

@Mod(BlockUI.MOD_ID)
public class BlockUI
{
    public static final String MOD_ID = "blockui";

    /**
     * If your mod is using GUI atlas register it here so we know it exists.
     */
    public static final Map<String, Identifier> NAMESPACE_TO_ATLAS_MAP = new HashMap<>();

    public BlockUI(final FMLModContainer modContainer, final Dist dist)
    {
        final IEventBus modBus = modContainer.getEventBus();
        final IEventBus forgeBus = NeoForge.EVENT_BUS;

        if (dist.isClient())
        {
            modBus.register(ClientLifecycleSubscriber.class);
            forgeBus.register(ClientEventSubscriber.class);
        }
    }

    public static Identifier resLoc(final String path)
    {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
