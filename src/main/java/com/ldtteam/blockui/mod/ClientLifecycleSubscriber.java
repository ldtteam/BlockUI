package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.Loader;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientLifecycleSubscriber
{
    @SubscribeEvent
    public static void onRegisterReloadListeners(final RegisterClientReloadListenersEvent event)
    {
        event.registerReloadListener(Loader.INSTANCE);
    }

    @SubscribeEvent
    public static void onRegisterBlockColor(final RegisterColorHandlersEvent.Block event)
    {
        // replace cauldron with plains default color
        event.register((state, level, pos, p_92624_) -> {
            return level != null && pos != null ? BiomeColors.getAverageWaterColor(level, pos) : 4159204;
         }, Blocks.WATER_CAULDRON);
    }
}
