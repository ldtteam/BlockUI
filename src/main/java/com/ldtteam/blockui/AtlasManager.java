package com.ldtteam.blockui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSpriteManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Splits global vanilla gui atlas on per mod-id basis. Requires atlas definition in atlases/<mod_id>_gui.json. "directory" sources in
 * the atlas definition must have unique path (ideally contain mod_id) or they join across all mod ids.
 */
public class AtlasManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AtlasManager.class);
    public static final AtlasManager INSTANCE = new AtlasManager();

    private final Map<String, CustomGuiSpriteManager> modAtlases = new HashMap<>();

    private AtlasManager()
    {}

    public void addAtlas(final Consumer<PreparableReloadListener> resourceRegistry, final String modId)
    {
        modAtlases.computeIfAbsent(modId, id -> {
            final CustomGuiSpriteManager spriteManager = new CustomGuiSpriteManager(Minecraft.getInstance().getTextureManager(), id);
            resourceRegistry.accept(spriteManager);
            return spriteManager;
        });
    }

    public TextureAtlasSprite getSprite(final ResourceLocation resLoc)
    {
        final CustomGuiSpriteManager spriteManager = modAtlases.get(resLoc.getNamespace());
        return spriteManager == null ? Minecraft.getInstance().getGuiSprites().getSprite(resLoc) : spriteManager.getSprite(resLoc);
    }

    public void dumpAtlases(final Path dumpingFolder)
    {
        modAtlases.forEach((modId, spriteManager) -> {
            try
            {
                spriteManager.textureAtlas.dumpContents(spriteManager.textureAtlas.location(),
                    Files.createDirectories(dumpingFolder.resolve(modId)));
            }
            catch (final IOException e)
            {
                LOGGER.warn("Failed to dump atlas for mod id: " + modId, e);
            }
        });
    }

    public static GuiSpriteScaling getSpriteScaling(final TextureAtlasSprite textureAtlasSprite)
    {
        return textureAtlasSprite.contents()
            .metadata()
            .getSection(GuiMetadataSection.TYPE)
            .orElse(GuiMetadataSection.DEFAULT)
            .scaling();
    }

    /**
     * Based on {@link GuiSpriteManager}
     */
    private class CustomGuiSpriteManager extends TextureAtlasHolder
    {
        private CustomGuiSpriteManager(final TextureManager textureManager, final String modId)
        {
            super(textureManager,
                new ResourceLocation(modId, "textures/atlas/" + modId + "_gui.png"),
                new ResourceLocation(modId, modId + "_gui"),
                GuiSpriteManager.METADATA_SECTIONS);
        }

        @Override
        public TextureAtlasSprite getSprite(final ResourceLocation resLoc)
        {
            return super.getSprite(resLoc);
        }
    }
}
