package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.Parsers;
import com.ldtteam.blockui.mod.Log;
import com.ldtteam.blockui.util.records.SizeI;
import com.ldtteam.blockui.util.resloc.OutOfJarResourceLocation;
import com.ldtteam.blockui.util.sprite.Sprite;
import com.ldtteam.blockui.util.sprite.Sprite.SpriteTicker;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Tuple;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * Simple image element.
 */
public class Image extends Pane 
{
    protected ResourceLocation resourceLocation;
    protected int u = 0;
    protected int v = 0;
    protected int uWidth = 0;
    protected int vHeight = 0;
    protected int mapWidth = 0;
    protected int mapHeight = 0;

    /**
     * Default Constructor.
     */
    public Image()
    {
        super();
    }

    /**
     * Constructor used by the xml loader.
     *
     * @param params PaneParams loaded from the xml.
     */
    public Image(final PaneParams params)
    {
        super(params);

        params.applyShorthand("imageoffset", Parsers.INT, 2, a -> {
            u = a.get(0);
            v = a.get(1);
        });

        params.applyShorthand("imagesize", Parsers.INT, 2, a -> {
            uWidth = a.get(0);
            vHeight = a.get(1);
        });

        resourceLocation = params.getResource("source", this::loadMapDimensions);
    }

    private void loadMapDimensions(final ResourceLocation rl)
    {
        if (OutOfJarResourceLocation.fileExists(OutOfJarResourceLocation.withSuffix(rl, ".mcmeta"), Minecraft.getInstance().getResourceManager()))
        {
            final Sprite sprite = Sprite.loadSprite(rl,
                OutOfJarResourceLocation.getResourceHandle(rl, Minecraft.getInstance().getResourceManager()));
            if (sprite != null)
            {
                mapWidth = sprite.width();
                mapHeight = sprite.height();
                checkBlitSize();

                // hack texture manager, this automatically closes old texture if present
                final var tm = Minecraft.getInstance().getTextureManager();
                if (!(tm.getTexture(rl) instanceof final SpriteTexture dynTex) || dynTex.getPixels() == null ||
                    dynTex.getPixels().getWidth() != mapWidth ||
                    dynTex.getPixels().getHeight() != mapHeight)
                {
                    tm.register(rl, new SpriteTexture(mapWidth, mapHeight, sprite));
                }

                return;
            }
        }

        final SizeI dimensions = getImageDimensions(rl);
        mapWidth = dimensions.width();
        mapHeight = dimensions.height();
        checkBlitSize();
    }

    private void checkBlitSize()
    {
        final String xmlLoc = window == null ? "unknown" : window.getXmlResourceLocation().toString();
        if (u + (uWidth == 0 ? mapWidth : uWidth) > mapWidth)
        {
            throw new RuntimeException("Invalid blit width for image: id - " + id + ", window - " + xmlLoc);
        }
        else if (v + (vHeight == 0 ? mapHeight : vHeight) > mapHeight)
        {
            throw new RuntimeException("Invalid blit height for image: id - " + id + ", window - " + xmlLoc);
        }
    }

    @Override
    public void setSize(final int w, final int h)
    {
        super.setSize(w, h);
        checkBlitSize();
    }

    /**
     * Set the map dimensions of the image to render.
     * @param height the map height.
     * @param width the map width.
     */
    public void setMapDimensions(final int height, final int width)
    {
        this.mapHeight = height;
        this.mapWidth = width;
    }

    /**
     * Load and image from a {@link ResourceLocation} and return a {@link Tuple} containing its width and height.
     *
     * @param resourceLocation The {@link ResourceLocation} pointing to the image.
     * @return Width and height.
     */
    public static SizeI getImageDimensions(final ResourceLocation resourceLocation)
    {
        final int pos = resourceLocation.getPath().lastIndexOf(".");

        if (pos == -1)
        {
            throw new IllegalStateException("No extension for file: " + resourceLocation.toString());
        }

        final String suffix = resourceLocation.getPath().substring(pos + 1);
        final Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix(suffix);

        while (it.hasNext())
        {
            final ImageReader reader = it.next();
            try (ImageInputStream stream =
                ImageIO.createImageInputStream(OutOfJarResourceLocation.openStream(resourceLocation, Minecraft.getInstance().getResourceManager())))
            {
                reader.setInput(stream);

                return new SizeI(reader.getWidth(reader.getMinIndex()), reader.getHeight(reader.getMinIndex()));
            }
            catch (final IOException e)
            {
                Log.getLogger().warn(e);
            }
            finally
            {
                reader.dispose();
            }
        }

        return new SizeI(0, 0);
    }

    /**
     * Set the image.
     *
     * @param rl      ResourceLocation for the image.
     * @param u       image x offset.
     * @param v       image y offset.
     * @param uWidth  image width.
     * @param vHeight image height.
     */
    public void setImage(final ResourceLocation rl, final int u, final int v, final int uWidth, final int vHeight)
    {
        if (!Objects.equals(rl, resourceLocation))
        {
            loadMapDimensions(rl);
        }

        resourceLocation = rl;
        this.u = u;
        this.v = v;
        this.uWidth = uWidth;
        this.vHeight = vHeight;
    }

    /**
     * Set the image.
     *
     * @param rl     ResourceLocation for the image.
     * @param keepUv whether to keep previous u and v values or use full size
     */
    public void setImage(final ResourceLocation rl, final boolean keepUv)
    {
        if (!Objects.equals(rl, resourceLocation))
        {
            loadMapDimensions(rl);
        }

        resourceLocation = rl;

        if (!keepUv)
        {
            u = 0;
            v = 0;
            uWidth = 0;
            vHeight = 0;
        }
    }

    /**
     * Draw this image on the GUI.
     *
     * @param mx Mouse x (relative to parent)
     * @param my Mouse y (relative to parent)
     */
    @Override
    public void drawSelf(final PoseStack ms, final double mx, final double my)
    {
        Objects.requireNonNull(resourceLocation, () -> id + " | " + window.getXmlResourceLocation());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        if (u != 0 || v != 0 || uWidth != 0 || vHeight != 0)
        {
            blit(ms,
                resourceLocation,
                x,
                y,
                width,
                height,
                u,
                v,
                uWidth == 0 ? mapWidth : uWidth,
                vHeight == 0 ? mapHeight : vHeight,
                mapWidth,
                mapHeight);
        }
        else
        {
            blit(ms, resourceLocation, x, y, width, height);
        }

        RenderSystem.disableBlend();
    }

    /**
     * Dynamic texture based on given sprite info
     */
    public class SpriteTexture extends DynamicTexture implements Tickable
    {
        private final Sprite sprite;
        private final SpriteTicker ticker;

        public SpriteTexture(final int width, final int height, final Sprite sprite)
        {
            super(width, height, false);
            this.sprite = sprite;
            this.ticker = sprite.createTicker();

            // we need the native image to be there, but it can be dealocated (sprite contents has its own native image)
            getPixels().close();
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
            sprite.close();
            if (ticker != null)
            {
                ticker.close();
            }
        }

        @Override
        public void reset(final TextureManager texManager, final ResourceManager resManager, final ResourceLocation resLoc, final Executor executor)
        {
            // we will populate it later (once needed)
            // don't release directly -> CME!
            Minecraft.getInstance().tell(() -> texManager.release(resLoc));
        }
    }
}
