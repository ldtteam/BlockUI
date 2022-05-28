package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.Loader;
import com.ldtteam.common.language.LanguageMapBouncer;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

public class ClientLifecycleSubscriber
{
    @SubscribeEvent
    public static void onRegisterReloadListeners(final RegisterClientReloadListenersEvent event)
    {
        event.registerReloadListener(Loader.INSTANCE);
    }

    @SubscribeEvent
    public static void onLoadComplete(final FMLLoadCompleteEvent event)
    {
        LanguageMapBouncer.unload();
    }
}
