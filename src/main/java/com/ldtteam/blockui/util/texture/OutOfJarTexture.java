package com.ldtteam.blockui.util.texture;

import com.ldtteam.blockui.mod.BlockUI;
import com.ldtteam.blockui.util.resloc.OutOfJarResourceLocation;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.loading.FMLEnvironment;
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

    public static AbstractTexture assertLoadedDefaultManagers(final ResourceLocation resLoc)
    {
        return assertLoaded(resLoc, Minecraft.getInstance().getTextureManager(), Minecraft.getInstance().getResourceManager());
    }

    /**
     * Checks whether given resLoc should be loaded into given textureManager as outOfJar or sprite texture
     * 
     * @return valid texture instance (including missing texture)
     */
    public static AbstractTexture assertLoaded(final ResourceLocation resLoc, final TextureManager textureManager, final ResourceManager resourceManager)
    {
        final AbstractTexture current = textureManager.getTexture(resLoc);
        if (!(resLoc instanceof final OutOfJarResourceLocation outOfJarResLoc))
        {
            // if not out-of-jar use normal vanilla systems
            return current;
        }

        if (IsOurTexture.isOur(current))
        {
            return current;
        }

        if (current == MissingTextureAtlasSprite.getTexture())
        {
            if (!FMLEnvironment.production && !resLoc.getNamespace().equals(BlockUI.MOD_ID))
            {
                throw new IllegalArgumentException("Missing texture: " + resLoc);
            }

            return current;
        }

        final OutOfJarTexture outOfJarTexture = new OutOfJarTexture(outOfJarResLoc);
        textureManager.register(outOfJarResLoc, outOfJarTexture); // this causes texture to load

        if (!outOfJarTexture.redirectToSprite)
        {
            return outOfJarTexture;
        }
        else
        {
            final SpriteTexture spriteTexture = new SpriteTexture(outOfJarResLoc);
            textureManager.register(outOfJarResLoc, spriteTexture);
            return spriteTexture;
        }
    }
}
