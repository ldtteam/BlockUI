package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.Parsers;
import com.ldtteam.blockui.mod.Log;
import com.ldtteam.blockui.util.records.SizeI;
import com.ldtteam.blockui.util.resloc.OutOfJarResourceLocation;
import com.ldtteam.blockui.util.texture.OutOfJarTexture;
import com.mojang.blaze3d.platform.NativeImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.neoforged.fml.loading.FMLEnvironment;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.Iterator;
import java.util.Objects;

/**
 * Simple image element.
 */
public class Image extends Pane 
{
    protected Identifier resourceLocation = null;
    protected int        u                = 0;
    protected int v = 0;
    protected int uWidth = 0;
    protected int vHeight = 0;

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

        resourceLocation = params.getResource("source");
    }

    /**
     * Load and image from a {@link Identifier} and return a {@link Tuple} containing its width and height.
     *
     * @param resourceLocation The {@link Identifier} pointing to the image.
     * @return Width and height.
     */
    public static SizeI getImageDimensions(final Identifier resourceLocation)
    {
        // this is called by most of image classes -> parse our textures
        OutOfJarTexture.assertLoadedDefaultManagers(resourceLocation);

        final int pos = resourceLocation.getPath().lastIndexOf(".");

        if (pos == -1)
        {
            try (InputStream is = OutOfJarResourceLocation.openStream(resourceLocation, Minecraft.getInstance().getResourceManager());
                NativeImage nativeImage = NativeImage.read(is))
            {
                return new SizeI(nativeImage.getWidth(), nativeImage.getHeight());
            }
            catch (final Exception e)
            {
                throw new IllegalStateException("No extension for file: " + resourceLocation.toString(), e);
            }
        }

        final String suffix = resourceLocation.getPath().substring(pos + 1);
        final Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix(suffix);

        while (it.hasNext())
        {
            final ImageReader reader = it.next();
            try (InputStream is = OutOfJarResourceLocation.openStream(resourceLocation, Minecraft.getInstance().getResourceManager());
                ImageInputStream stream = ImageIO.createImageInputStream(is))
            {
                reader.setInput(stream);

                return new SizeI(reader.getWidth(reader.getMinIndex()), reader.getHeight(reader.getMinIndex()));
            }
            catch (final NoSuchFileException | FileNotFoundException e)
            {
                // dont log these, texture manager logs it anyway
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

        if (!FMLEnvironment.isProduction())
        {
            throw new RuntimeException("Couldn't resolve size for image: " + resourceLocation);
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
    public void setImage(final Identifier rl, final int u, final int v, final int uWidth, final int vHeight)
    {
        if (Objects.equals(rl, resourceLocation) && this.u == u && this.v == v && this.uWidth == uWidth && this.vHeight == vHeight)
        {
            return;
        }

        this.resourceLocation = rl;
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
    public void setImage(final Identifier rl, final boolean keepUv)
    {
        if (keepUv)
        {
            setImage(rl, u, v, uWidth, vHeight);
        }
        else
        {
            setImage(rl, 0, 0, 0, 0);
        }
    }

    /**
     * Draw this image on the GUI.
     *
     * @param mx Mouse x (relative to parent)
     * @param my Mouse y (relative to parent)
     */
    @Override
    public void drawSelf(final BOGuiGraphics target, final double mx, final double my)
    {
        if (!FMLEnvironment.isProduction())
        {
            Objects.requireNonNull(resourceLocation, () -> "Missing image source: " + id + " | " + window.getXmlResourceLocation());
        }

        target.guiGraphics().blit(resourceLocation, x, y, u, v, width, height, uWidth, vHeight);
    }
}
