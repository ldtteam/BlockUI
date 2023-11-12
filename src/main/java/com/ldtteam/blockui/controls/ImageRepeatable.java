package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.Parsers;
import com.ldtteam.blockui.util.records.SizeI;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;

/**
 * Image element with repeatable middle part.
 * 
 * @deprecated use normal {@link Image} and move texture into atlas
 */
@Deprecated(forRemoval = true, since = "1.20.2")
public class ImageRepeatable extends Pane
{
    public static final int MINECRAFT_DEFAULT_TEXTURE_IMAGE_SIZE = 256;

    protected ResourceLocation resourceLocation;
    protected int u = 0;
    protected int v = 0;
    protected int uWidth = 0;
    protected int vHeight = 0;
    protected int uRepeat = 0;
    protected int vRepeat = 0;
    protected int repeatWidth = 0;
    protected int repeatHeight = 0;
    protected int fileWidth = MINECRAFT_DEFAULT_TEXTURE_IMAGE_SIZE;
    protected int fileHeight = MINECRAFT_DEFAULT_TEXTURE_IMAGE_SIZE;

    /**
     * Default Constructor.
     */
    public ImageRepeatable()
    {
        super();
    }

    /**
     * Constructor used by the xml loader.
     *
     * @param params PaneParams loaded from the xml.
     */
    public ImageRepeatable(final PaneParams params)
    {
        super(params);
        resourceLocation = params.getResource("source", this::loadMapDimensions);

        params.applyShorthand("textureoffset", Parsers.INT, 2, a -> {
            u = a.get(0);
            v = a.get(1);
        });

        params.applyShorthand("texturesize", Parsers.INT, 2, a -> {
            uWidth = a.get(0);
            vHeight = a.get(1);
        });

        params.applyShorthand("repeatoffset", Parsers.INT, 2, a -> {
            uRepeat = a.get(0);
            vRepeat = a.get(1);
        });

        params.applyShorthand("repeatsize", Parsers.INT, 2, a -> {
            repeatWidth = a.get(0);
            repeatHeight = a.get(1);
        });
    }

    private void loadMapDimensions(final ResourceLocation rl)
    {
        final SizeI dimensions = Image.getImageDimensions(rl);
        fileWidth = dimensions.width();
        fileHeight = dimensions.height();
    }

    /**
     * Set the image.
     *
     * @param source String path.
     */
    public void setImageLoc(final String source)
    {
        setImageLoc(source != null ? new ResourceLocation(source) : null);
    }

    /**
     * Set the image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImageLoc(final ResourceLocation loc)
    {
        resourceLocation = loc;
        loadMapDimensions(loc);
    }

    /**
     * Set the texture box sizes.
     *
     * @param u            texture start offset [texels]
     * @param v            texture start offset [texels]
     * @param uWidth       texture rendering box [texels]
     * @param vHeight      texture rendering box [texels]
     * @param uRepeat      offset relative to u, v [texels], smaller than uWidth
     * @param vRepeat      offset relative to u, v [texels], smaller than vHeight
     * @param repeatWidth  size of repeatable box in texture [texels], smaller than or equal uWidth - uRepeat
     * @param repeatHeight size of repeatable box in texture [texels], smaller than or equal vHeight - vRepeat
     */
    public void setImageSize(final int u, final int v,
        final int uWidth, final int vHeight,
        final int uRepeat, final int vRepeat,
        final int repeatWidth, final int repeatHeight)
    {
        this.u = u;
        this.v = v;
        this.uWidth = uWidth;
        this.vHeight = vHeight;
        this.uRepeat = uRepeat;
        this.vRepeat = vRepeat;
        this.repeatWidth = repeatWidth;
        this.repeatHeight = repeatHeight;
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
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        blitRepeatable(target.pose(),
            resourceLocation,
            x,
            y,
            width,
            height,
            u,
            v,
            uWidth == 0 ? fileWidth : uWidth,
            vHeight == 0 ? fileHeight : vHeight,
            fileWidth,
            fileHeight,
            uRepeat,
            vRepeat,
            repeatWidth,
            repeatHeight);

        RenderSystem.disableBlend();
    }
}
