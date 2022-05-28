package com.ldtteam.blockui.mod;

import com.ldtteam.common.CommonLdtTeamInit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(BlockUI.MOD_ID)
public class BlockUI
{
    public static final String MOD_ID = "blockui";
    public static final Logger MOD_LOG = LogManager.getLogger(MOD_ID);

    public BlockUI()
    {
        CommonLdtTeamInit.setup(MOD_ID);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
            () -> () -> Mod.EventBusSubscriber.Bus.MOD.bus().get().register(ClientLifecycleSubscriber.class));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
            () -> () -> Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(ClientEventSubscriber.class));
    }
}
