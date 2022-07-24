package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.BOScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(BlockUI.MOD_ID)
public class BlockUI
{
    public static final String MOD_ID = "blockui";

    public BlockUI()
    {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Mod.EventBusSubscriber.Bus.MOD.bus().get().register(ClientLifecycleSubscriber.class));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(ClientEventSubscriber.class));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(BOScreen.class));
    }
}
