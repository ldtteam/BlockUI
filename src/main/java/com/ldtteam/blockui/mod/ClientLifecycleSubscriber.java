package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.AtlasManager;
import com.ldtteam.blockui.Loader;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.event.ModMismatchEvent;

public class ClientLifecycleSubscriber
{
    @SubscribeEvent
    public static void onRegisterReloadListeners(final RegisterClientReloadListenersEvent event)
    {
        event.registerReloadListener(Loader.INSTANCE);
        AtlasManager.INSTANCE.addAtlas(event::registerReloadListener, BlockUI.MOD_ID);
    }

    @SubscribeEvent
    public static void onRegisterBlockColor(final RegisterColorHandlersEvent.Block event)
    {
        // replace cauldron with plains default color (4159204, with slighty more light in HSL += 8%)
        event.register(
            (state, level, pos, tintIndex) -> level != null && pos != null ? BiomeColors.getAverageWaterColor(level, pos) : 0x638fe9,
            Blocks.WATER_CAULDRON);
    }

    @SubscribeEvent
    public static void onModMismatch(final ModMismatchEvent event)
    {
        // there are no world data and rest is mod compat anyway
        event.markResolved(BlockUI.MOD_ID);
    }
}
