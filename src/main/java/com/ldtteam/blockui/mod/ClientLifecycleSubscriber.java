package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.Loader;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.model.sprite.AtlasManager.AtlasConfig;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterTextureAtlasesEvent;
import net.neoforged.neoforge.event.ModMismatchEvent;

import java.util.Set;

public class ClientLifecycleSubscriber
{
    @SubscribeEvent
    public static void onAddClientReloadListenersEvent(final AddClientReloadListenersEvent event)
    {
        event.addListener(Loader.RELOADABLE_LISTEN_RES_LOC, Loader.INSTANCE);
    }

    @SubscribeEvent
    public static void onRegisterTextureAtlasesEvent(final RegisterTextureAtlasesEvent event)
    {
        // TODO: port 26.1 validate if we get autoloaded in vanilla gui atlas or we need our own
        event.register(new AtlasConfig(Identifier.fromNamespaceAndPath(BlockUI.MOD_ID, "textures/atlas/blockui_gui.png"),
            Identifier.fromNamespaceAndPath(BlockUI.MOD_ID, "blockui_gui"),
            false,
            Set.of(GuiMetadataSection.TYPE)));
    }

    @SubscribeEvent
    public static void onModMismatch(final ModMismatchEvent event)
    {
        // there are no world data and rest is mod compat anyway
        event.getVersionDifference(BlockUI.MOD_ID).ifPresent(id -> event.markResolved(BlockUI.MOD_ID));
    }
}
