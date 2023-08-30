package com.ldtteam.blockui.util.texture;

import com.ldtteam.blockui.util.resloc.OutOfJarResourceLocation;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import java.io.IOException;

/**
 * Inspired by {@link SimpleTexture}
 */
public class OutOfJarTexture extends AbstractTexture
{
    protected final OutOfJarResourceLocation resourceLocation;
    private boolean redirectToSprite = false;

    public OutOfJarTexture(final OutOfJarResourceLocation resourceLocation)
    {
        this.resourceLocation = resourceLocation;
    }

    @Override
    public void load(final ResourceManager resourceManager) throws IOException
    {
        final Resource resource = OutOfJarResourceLocation.getResourceHandle(resourceLocation, resourceManager);

        // redirect to sprite
        if (resource.metadata().getSection(AnimationMetadataSection.SERIALIZER).isPresent())
        {
            redirectToSprite = true;
            throw new IOException("Vanilla hack: redirecting loading to sprite texture, do NOT report this exception, it IS intended");
            // ^ throwing anything else but IO crashes client, but we need to take missing texture path (so this object dies properly)
        }

        final TextureMetadataSection textureMeta = resource.metadata().getSection(TextureMetadataSection.SERIALIZER).orElse(null);
        final NativeImage nativeImage;

        try (var is = resource.open())
        {
            nativeImage = NativeImage.read(is);
        }
        TextureUtil.prepareImage(getId(), 0, nativeImage.getWidth(), nativeImage.getHeight());

        if (textureMeta != null)
        {
            nativeImage.upload(0,
                0,
                0,
                0,
                0,
                nativeImage.getWidth(),
                nativeImage.getHeight(),
                textureMeta.isBlur(),
                textureMeta.isClamp(),
                false,
                true);
        }
        else
        {
            nativeImage.upload(0, 0, 0, true);
        }
    }

    public static void assertLoaded(final OutOfJarResourceLocation resourceLocation, final TextureManager textureManager)
    {
        final AbstractTexture current = textureManager.getTexture(resourceLocation, null);
        if (!IsOurTexture.isOur(current))
        {
            final OutOfJarTexture outOfJarTexture = new OutOfJarTexture(resourceLocation);
            textureManager.register(resourceLocation, outOfJarTexture);

            if (outOfJarTexture.redirectToSprite)
            {
                textureManager.register(resourceLocation, new SpriteTexture(resourceLocation));
            }
        }
    }
}
