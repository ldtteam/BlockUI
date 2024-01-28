package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.AtlasManager;
import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.Parsers;
import com.ldtteam.blockui.mod.Log;
import com.ldtteam.blockui.util.records.SizeI;
import com.ldtteam.blockui.util.resloc.OutOfJarResourceLocation;
import com.ldtteam.blockui.util.texture.OutOfJarTexture;
import com.ldtteam.blockui.util.texture.SpriteTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.neoforged.fml.loading.FMLEnvironment;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Iterator;
import java.util.Objects;

/**
 * Simple image element.
 */
public class Image extends Pane 
{
    protected ResourceLocation resourceLocation = null;
    protected int u = 0;
    protected int v = 0;
    protected int uWidth = 0;
    protected int vHeight = 0;
    protected ResolvedBlit resolvedBlit = null;

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
     * Set the map dimensions of the image to render.
     * @param height the map height.
     * @param width the map width.
     * 
     * @deprecated functionality removed, either display full texture and use u,v coords setter or use atlas
     */
    @Deprecated(forRemoval = true, since = "1.20.2")
    public void setMapDimensions(final int height, final int width)
    {
    }

    /**
     * Load and image from a {@link ResourceLocation} and return a {@link Tuple} containing its width and height.
     *
     * @param resourceLocation The {@link ResourceLocation} pointing to the image.
     * @return Width and height.
     */
    public static SizeI getImageDimensions(final ResourceLocation resourceLocation)
    {
        // this is called by most of image classes -> parse our textures
        OutOfJarTexture.assertLoadedDefaultManagers(resourceLocation);

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

        if (!FMLEnvironment.production)
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
    public void setImage(final ResourceLocation rl, final int u, final int v, final int uWidth, final int vHeight)
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
        this.resolvedBlit = null;
    }

    /**
     * Set the image.
     *
     * @param rl     ResourceLocation for the image.
     * @param keepUv whether to keep previous u and v values or use full size
     */
    public void setImage(final ResourceLocation rl, final boolean keepUv)
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
        if (resolvedBlit == null)
        {
            resolveBlit();
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        resolvedBlit.blit(target.pose(), x, y, width, height);
        RenderSystem.disableBlend();
    }

    /**
     * Resolves current image settings
     */
    protected void resolveBlit()
    {
        if (!FMLEnvironment.production)
        {
            Objects.requireNonNull(resourceLocation, () -> id + " | " + window.getXmlResourceLocation());
        }
        else if (resourceLocation == null)
        {
            resourceLocation = MissingTextureAtlasSprite.getLocation();
        }
        resolvedBlit = resolveBlit(resourceLocation, u, v, uWidth, vHeight);
    }
    
    /**
     * @param resLoc texture resource location
     * @return resolved blit - with precomputed values and detached from all possible instances
     */
    public static ResolvedBlit resolveBlit(final ResourceLocation resLoc)
    {
        return resolveBlit(resLoc, 0, 0, 0, 0);
    }

    /**
     * @param resLoc texture resource location
     * @param u in texels
     * @param v in texels
     * @param uWidth in texels
     * @param vHeight in texels
     * @return resolved blit - with precomputed values and detached from all possible instances
     */
    public static ResolvedBlit resolveBlit(final ResourceLocation resLoc, final int u, final int v, final int uWidth, final int vHeight)
    {
        // if bad input skip resolving
        if (resLoc == null || resLoc == MissingTextureAtlasSprite.getLocation())
        {
            return (ps, x, y, w, h) -> blit(ps, MissingTextureAtlasSprite.getLocation(), x, y, w, h);
        }

        final TextureAtlasSprite atlasSprite = AtlasManager.INSTANCE.getSprite(resLoc);

        // unless we sprited missing texture pass to sprite blit
        if (atlasSprite.contents().name() != MissingTextureAtlasSprite.getLocation())
        {
            return resolveSprite(atlasSprite, AtlasManager.getSpriteScaling(atlasSprite));
        }
        
        // if our sprite or full blit do normal blit
        final AbstractTexture texture = OutOfJarTexture.assertLoadedDefaultManagers(resLoc);
        if (texture instanceof SpriteTexture || (u == 0 && v == 0 && uWidth == 0 && vHeight == 0))
        {
            // Mojang bug: if texture is null = nothing is registered to resLoc now
            // then blit will leak one opengl texture id every time this is null
            // so if null use missingTexture instead
            final ResourceLocation notBugged = texture == null ? MissingTextureAtlasSprite.getLocation() : resLoc;
            return (ps, x, y, w, h) -> blit(ps, notBugged, x, y, w, h);
        }

        // else map u,v to float
        final SizeI mapSize = getImageDimensions(resLoc);
        final float uMin = u / mapSize.width();
        final float uMax = uWidth == 0 ? 1 : uMin + uWidth / mapSize.width();
        final float vMin = v / mapSize.height();
        final float vMax = vHeight == 0 ? 1 : vMin + vHeight / mapSize.height();

        return (ps, x, y, w, h) -> blit(ps, resLoc, x, y, uWidth, vHeight, uMin, vMin, uMax, vMax);
    }
}
