package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.Parsers;
import com.ldtteam.blockui.mod.Log;
import com.ldtteam.blockui.util.records.SizeI;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

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
        resourceLocation = params.getResource("source", this::loadMapDimensions);
        Objects.requireNonNull(resourceLocation);

        params.applyShorthand("imageoffset", Parsers.INT, 2, a -> {
            u = a.get(0);
            v = a.get(1);
        });

        params.applyShorthand("imagesize", Parsers.INT, 2, a -> {
            uWidth = a.get(0);
            vHeight = a.get(1);
        });
    }

    private void loadMapDimensions(final ResourceLocation rl)
    {
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
            throw new RuntimeException("Invalid blit width for image: id - " + id + " , window - " + xmlLoc);
        }
        else if (v + (vHeight == 0 ? mapHeight : vHeight) > mapHeight)
        {
            throw new RuntimeException("Invalid blit height for image: id - " + id + " , window - " + xmlLoc);
        }
    }

    @Override
    public void setSize(final int w, final int h)
    {
        super.setSize(w, h);
        checkBlitSize();
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
            try (ImageInputStream stream = ImageIO
                .createImageInputStream(Minecraft.getInstance().getResourceManager().getResource(resourceLocation).getInputStream()))
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
        resourceLocation = rl;
        this.u = u;
        this.v = v;
        this.uWidth = uWidth;
        this.vHeight = vHeight;

        loadMapDimensions(rl);
    }

    /**
     * Set the image.
     *
     * @param rl     ResourceLocation for the image.
     * @param keepUv whether to keep previous u and v values or use full size
     */
    public void setImage(final ResourceLocation rl, final boolean keepUv)
    {
        resourceLocation = rl;

        if (!keepUv)
        {
            u = 0;
            v = 0;
            uWidth = 0;
            vHeight = 0;
        }

        loadMapDimensions(rl);
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
}
