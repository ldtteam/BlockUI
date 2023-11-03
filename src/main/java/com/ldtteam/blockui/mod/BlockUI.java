package com.ldtteam.blockui.mod;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(BlockUI.MOD_ID)
public class BlockUI
{
    public static final String MOD_ID = "blockui";

    public BlockUI()
    {
        if (FMLEnvironment.dist.isClient())
        {
            Mod.EventBusSubscriber.Bus.MOD.bus().get().register(ClientLifecycleSubscriber.class);
            Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(ClientEventSubscriber.class);
        }
    }
}
