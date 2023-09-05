package com.ldtteam.blockui.util.texture;

import com.ldtteam.blockui.util.resloc.OutOfJarResourceLocation;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.SpriteTicker;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.jetbrains.annotations.Nullable;
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

    public SpriteTexture(final ResourceLocation resourceLocation)
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

        sprite = SpriteLoader.loadSprite(resourceLocation, resource);
        ticker = sprite.createTicker();

        TextureUtil.prepareImage(getId(), 0, width(), height());
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

    public int width()
    {
        return sprite.width();
    }

    public int height()
    {
        return sprite.height();
    }

    @Nullable
    public static SpriteTexture checkLoaded(final ResourceLocation resourceLocation,
        final TextureManager textureManager,
        final ResourceManager resourceManager)
    {
        // already loaded
        final AbstractTexture current = textureManager.getTexture(resourceLocation, null);
        if (IsOurTexture.isOur(current))
        {
            return current instanceof final SpriteTexture sprite ? sprite : null;
        }

        // not mcmeta
        if (!OutOfJarResourceLocation.fileExists(resourceLocation.withSuffix(".mcmeta"), resourceManager))
        {
            return null;
        }

        // parse mcmeta (or exit)
        final Resource resource = OutOfJarResourceLocation.getResourceHandle(resourceLocation, resourceManager);
        final ResourceMetadata metadata;
        try
        {
            metadata = resource.metadata();
        }
        catch (final IOException e)
        {
            // want to log this, but this is called from frame rendering => log spam
            return null;
        }

        // mcmeta has animation -> create sprite
        if (metadata.getSection(AnimationMetadataSection.SERIALIZER).isPresent())
        {
            final SpriteTexture sprite = new SpriteTexture(resourceLocation);
            textureManager.register(resourceLocation, sprite);
            return sprite;
        }

        // mcmeta has NOT animation
        return null;
    }
}
