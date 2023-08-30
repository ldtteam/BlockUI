package com.ldtteam.blockui.util.texture;

import com.ldtteam.blockui.util.resloc.OutOfJarResourceLocation;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
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

    public OutOfJarTexture(final OutOfJarResourceLocation resourceLocation)
    {
        this.resourceLocation = resourceLocation;
    }

    @Override
    public void load(final ResourceManager resourceManager) throws IOException
    {
        final Resource resource = OutOfJarResourceLocation.getResourceHandle(resourceLocation, resourceManager);

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
        if (!(textureManager.getTexture(resourceLocation, null) instanceof OutOfJarTexture))
        {
            textureManager.register(resourceLocation, new OutOfJarTexture(resourceLocation));
        }
    }
}
