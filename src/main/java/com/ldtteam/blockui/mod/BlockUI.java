package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.util.Crash;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(BlockUI.MOD_ID)
public final class BlockUI
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

    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    public static Logger getLogger()
    {
        return getLogger(STACK_WALKER.getCallerClass());
    }

    public static Logger getLogger(final Class<?> clazz)
    {
        final String clazzName = clazz.getSimpleName();
        if (clazzName == null || clazzName.isBlank())
        {
            throw Crash.argument("Given class has no simple name: " + clazz);
        }
        return LoggerFactory.getLogger(MOD_ID + "/" + clazzName);
    }
}
