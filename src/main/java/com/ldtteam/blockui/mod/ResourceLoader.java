package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.context.RenderContextType;
import com.ldtteam.blockui.util.OutOfJarResourceLocation;
import com.ldtteam.blockui.util.Vec2i;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class ResourceLoader extends SimplePreparableReloadListener
{
    private static final Logger LOG = BlockUI.getLogger();
    public static final ResourceLoader INSTANCE = new ResourceLoader();

    private final Map<ResourceLocation, Vec2i> imageDimensions = new HashMap<>();

    private ResourceLoader()
    {
        // singleton
    }

    /**
     * @return {@link Vec2i#EMPTY} if cannot determine image size, position vector otherwise
     */
    public Vec2i getImageDimensions(final ResourceLocation resourceLocation)
    {
        return imageDimensions.computeIfAbsent(resourceLocation, this::calculateImageDimensions);
    }

    // TODO: use stb loading?
    private Vec2i calculateImageDimensions(final ResourceLocation resLoc)
    {
        final int pos = resLoc.getPath().lastIndexOf(".");

        if (pos == -1)
        {
            LOG.error("Cannot determine image dimensions because of no extension for file: " + resLoc.toString());
            return Vec2i.EMPTY;
        }

        final String suffix = resLoc.getPath().substring(pos + 1);
        final var it = ImageIO.getImageReadersBySuffix(suffix);
        while (it.hasNext())
        {
            final ImageReader reader = it.next();
            try (ImageInputStream stream = ImageIO
                .createImageInputStream(OutOfJarResourceLocation.openStream(resLoc, Minecraft.getInstance().getResourceManager())))
            {
                reader.setInput(stream);

                return new Vec2i(reader.getWidth(reader.getMinIndex()), reader.getHeight(reader.getMinIndex()));
            }
            catch (final IOException e)
            {
                LOG.warn("Error during reading image dimensions with: " + reader.getClass().getSimpleName(), e);
            }
            finally
            {
                reader.dispose();
            }
        }

        return Vec2i.EMPTY;
    }

    @Override
    protected Object prepare(ResourceManager p_10796_, ProfilerFiller p_10797_)
    {
        // TODO: colors, then guis
        return null;
    }

    @Override
    protected void apply(Object p_10793_, ResourceManager p_10794_, ProfilerFiller p_10795_)
    {
        imageDimensions.clear();
        RenderContextType.applyToAll(ctx -> ctx.resourceReloaded = true);
    }

    /**
     * Make sure that if given resLoc is out-of-jar then it's properly recognized by vanilla texture manager
     */
    public void ensureLoadedOutOfJarResLoc(final ResourceLocation rl)
    {
        final TextureManager tm = Minecraft.getInstance().getTextureManager();
        if (tm.getTexture(rl, null) == null && rl instanceof final OutOfJarResourceLocation nioResLoc)
        {
            final AbstractTexture texture = new AbstractTexture()
            {
                @Override
                public void load(ResourceManager p_117955_) throws IOException
                {}
            };

            try (var is = OutOfJarResourceLocation.openStream(nioResLoc, Minecraft.getInstance().getResourceManager()))
            {
                final NativeImage nativeImage = NativeImage.read(is);
                TextureUtil.prepareImage(texture.getId(), 0, nativeImage.getWidth(), nativeImage.getHeight());
                nativeImage.upload(0, 0, 0, true);

                tm.register(nioResLoc, texture);
            }
            catch (final IOException e)
            {
                LOG.error("Can't load image: " + nioResLoc.toString(), e);

                texture.releaseId();
                tm.register(nioResLoc, MissingTextureAtlasSprite.getTexture());
            }
        }
    }
}
