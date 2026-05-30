package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.Loader;
import com.ldtteam.blockui.UiRenderMacros;
import com.ldtteam.blockui.mod.item.BlockStatePipRenderer;
import com.ldtteam.blockui.mod.item.BlockStatePipRenderer.BlockStateRenderState;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.model.sprite.AtlasManager.AtlasConfig;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RegisterTextureAtlasesEvent;
import net.neoforged.neoforge.event.ModMismatchEvent;

import java.util.Set;

public class ClientLifecycleSubscriber
{
    @SubscribeEvent
    public static void onAddClientReloadListeners(final AddClientReloadListenersEvent event)
    {
        event.addListener(Loader.RELOADABLE_LISTEN_RES_LOC, Loader.INSTANCE);
    }

    @SubscribeEvent
    public static void onRegisterTextureAtlases(final RegisterTextureAtlasesEvent event)
    {
        // register vanilla
        BlockUI.NAMESPACE_TO_ATLAS_MAP.put(Identifier.DEFAULT_NAMESPACE, AtlasIds.GUI);

        // register us
        final Identifier atlasKey = BlockUI.resLoc("blockui_gui");
        BlockUI.NAMESPACE_TO_ATLAS_MAP.put(BlockUI.MOD_ID, atlasKey);
        event.register(new AtlasConfig(BlockUI.resLoc("textures/atlas/blockui_gui.png"), atlasKey, false, Set.of(GuiMetadataSection.TYPE)));
    }

    @SubscribeEvent
    public static void onModMismatch(final ModMismatchEvent event)
    {
        // there are no world data and rest is mod compat anyway
        event.getVersionDifference(BlockUI.MOD_ID).ifPresent(id -> event.markResolved(BlockUI.MOD_ID));
    }

    @SubscribeEvent
    public static void onRegisterRenderPipelines(final RegisterRenderPipelinesEvent event)
    {
        event.registerPipeline(UiRenderMacros.GUI_POS_COLOR_LINES);
        event.registerPipeline(UiRenderMacros.GUI_POS_COLOR_TRIANGLES);
        event.registerPipeline(UiRenderMacros.GUI_POS_TEX_COLOR_TRIANGLES);
        event.registerPipeline(UiRenderMacros.GUI_POS_TEX_TRIANGLES);
    }

    @SubscribeEvent
    public static void onRegisterPictureInPictureRenderers(final RegisterPictureInPictureRenderersEvent event)
    {
        event.register(BlockStateRenderState.class, BlockStatePipRenderer::new);
    }
}
