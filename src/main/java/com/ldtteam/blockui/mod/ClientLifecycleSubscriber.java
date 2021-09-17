package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.Loader;
import com.ldtteam.blockui.mod.container.ContainerHook;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class ClientLifecycleSubscriber
{
    @SubscribeEvent
    public static void onClientInit(final FMLClientSetupEvent event)
    {
        ContainerHook.container_guis = ForgeTagHandler.makeWrapperTag(ForgeRegistries.BLOCK_ENTITIES,
            new ResourceLocation(BlockUI.MOD_ID, "container_gui"));
    }

    @SubscribeEvent
    public static void onRegisterReloadListeners(final RegisterClientReloadListenersEvent event)
    {
        event.registerReloadListener(Loader.INSTANCE);
    }
}
