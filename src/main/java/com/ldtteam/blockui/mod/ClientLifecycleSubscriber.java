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
        // replace cauldron with plains default color (4159204, with slighty more light in HSL += 8%)
        event.register(
            (state, level, pos, tintIndex) -> level != null && pos != null ? BiomeColors.getAverageWaterColor(level, pos) : 0x638fe9,
            Blocks.WATER_CAULDRON);
    }
}
