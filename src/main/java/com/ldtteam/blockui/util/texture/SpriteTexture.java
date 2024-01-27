package com.ldtteam.blockui.util.texture;

import com.ldtteam.blockui.util.resloc.OutOfJarResourceLocation;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.SpriteTicker;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Dynamic texture based on given sprite info
 */
public class SpriteTexture extends AbstractTexture implements Tickable
{
    private final ResourceLocation resourceLocation;

    private SpriteContents sprite;
    private SpriteTicker ticker;

    /**
     * intentionally out-of-jar ctor, for normal locations use vanilla atlases
     */
    public SpriteTexture(final OutOfJarResourceLocation resourceLocation)
    {
        this.resourceLocation = resourceLocation;
    }

    @Override
    public void load(final ResourceManager resourceManager) throws IOException
    {
        // cleanup old data
        close();
        
        if (!OutOfJarResourceLocation.fileExists(resourceLocation, resourceManager))
        {
            throw new FileNotFoundException(resourceLocation.toString());
        }

        final Resource resource = OutOfJarResourceLocation.getResourceHandle(resourceLocation, resourceManager);

        sprite = SpriteResourceLoader.create(SpriteLoader.DEFAULT_METADATA_SECTIONS).loadSprite(resourceLocation, resource);
        ticker = sprite.createTicker();

        TextureUtil.prepareImage(getId(), 0, sprite.width(), sprite.height());
        sprite.uploadFirstFrame(0, 0);
    }

    @Override
    public void tick()
    {
        if (ticker != null)
        {
            bind();
            ticker.tickAndUpload(0, 0);
        }
    }

    @Override
    public void close()
    {
        if (sprite != null)
        {
            sprite.close();
        }
        if (ticker != null)
        {
            ticker.close();
        }
    }
}
