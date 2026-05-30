package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.Parsers;
import com.ldtteam.blockui.mod.BlockUI;
import com.ldtteam.blockui.util.records.SizeI;
import com.ldtteam.blockui.util.texture.OutOfJarTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.resources.Identifier;
import java.util.Objects;

/**
 * Simple image element.
 */
public class Image extends Pane
{
    protected Identifier resourceLocation = null;
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
        requireNonNull(resourceLocation, "Missing image texture (if dynamic in code use: minecraft:missingno)");
    }

    /**
     * Load and image from a {@link Identifier} and return a {@link SizeI} containing its width and height.
     *
     * @param resourceLocation The {@link Identifier} pointing to the image.
     * @return Width and height.
     */
    public static SizeI getImageDimensions(final Identifier resourceLocation)
    {
        final var texture = Minecraft.getInstance().getTextureManager().getTexture(resourceLocation).getTexture();
        return new SizeI(texture.getWidth(0), texture.getHeight(0));
    }

    /**
     * Set the image.
     *
     * @param rl      Identifier for the image.
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
        requireNonNull(resourceLocation, "Missing image texture");

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
     * @param rl     Identifier for the image.
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
        requireNonNull(resourceLocation, "Missing image texture");

        if (resolvedBlit == null)
        {
            resolvedBlit = resolveBlit(resourceLocation, u, v, uWidth, vHeight);
        }

        resolvedBlit.blit(target, x, y, width, height);
    }

    /**
     * @param resLoc texture resource location
     * @return resolved blit - with precomputed values and detached from all possible instances
     */
    public static ResolvedBlit resolveBlit(final Identifier resLoc)
    {
        return resolveBlit(resLoc, 0, 0, 0, 0);
    }

    /**
     * @param resLoc texture resource location
     * @param u in texels
     * @param v in texels
     * @param uWidth in texels, zero = max
     * @param vHeight in texels, zero = max
     * @return resolved blit - with precomputed values and detached from all possible instances
     */
    public static ResolvedBlit resolveBlit(final Identifier resLoc, final int u, final int v, final int uWidth, final int vHeight)
    {
        // if bad input skip resolving
        if (resLoc == null || resLoc == MissingTextureAtlasSprite.getLocation())
        {
            return (ps, x, y, w, h, c) -> blit(ps, MissingTextureAtlasSprite.getLocation(), x, y, w, h, c);
        }

        // this is called by most of image classes -> parse our textures
        OutOfJarTexture.assertLoadedDefaultManagers(resLoc);

        final TextureAtlas guiAtlas =
            Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(BlockUI.NAMESPACE_TO_ATLAS_MAP.get(resLoc.getNamespace()));
        final TextureAtlasSprite atlasSprite = guiAtlas.getSprite(resLoc);

        // unless we sprited missing texture pass to sprite blit (intentional object equality)
        if (atlasSprite != guiAtlas.missingSprite())
        {
            return resolveSprite(atlasSprite, atlasSprite.contents().getAdditionalMetadata(GuiMetadataSection.TYPE).orElse(GuiMetadataSection.DEFAULT).scaling());
        }

        // if full blit do normal blit
        if (u == 0 && v == 0 && uWidth == 0 && vHeight == 0)
        {
            return (ps, x, y, w, h, c) -> blit(ps, resLoc, x, y, w, h, c);
        }

        // else map u,v to float
        final SizeI mapSize = getImageDimensions(resLoc);
        final float uMin = u / (float) mapSize.width();
        final float uMax = uWidth == 0 ? 1.0f : uMin + uWidth / (float) mapSize.width();
        final float vMin = v / (float) mapSize.height();
        final float vMax = vHeight == 0 ? 1.0f : vMin + vHeight / (float) mapSize.height();

        return (ps, x, y, w, h, c) -> blit(ps, resLoc, x, y, w, h, uMin, vMin, uMax, vMax, c);
    }
}
