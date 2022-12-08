package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.Loader;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientLifecycleSubscriber
{
    @SubscribeEvent
    public static void onRegisterReloadListeners(final RegisterClientReloadListenersEvent event)
    {
        event.registerReloadListener(Loader.INSTANCE);
    }
}
