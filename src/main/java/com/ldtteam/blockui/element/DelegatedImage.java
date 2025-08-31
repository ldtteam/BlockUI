package com.ldtteam.blockui.element;

import com.ldtteam.blockui.AbstractPane;
import com.ldtteam.blockui.AbstractPaneGroup;
import com.ldtteam.blockui.context.RenderContext;
import com.ldtteam.blockui.context.RenderContext.RepeatableExtension;
import com.ldtteam.blockui.element.shadow.AbstractDelegatedPane;
import com.ldtteam.blockui.mod.ResourceLoader;
import com.ldtteam.blockui.parse.Parser;
import com.ldtteam.blockui.parse.ParsingCodec;
import com.ldtteam.blockui.util.Crash;
import com.ldtteam.blockui.util.Vec2i;
import com.ldtteam.blockui.util.Crash.CheckArgument;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;

public class DelegatedImage extends AbstractDelegatedPane
{
    public static final String ID = "image";
    public static final ParsingCodec<DelegatedImage> CODEC = ParsingCodec.forDelegated(ID, DelegatedImage::new)
        .field("path", Parser.RES_LOC, DelegatedImage::setTextureLoc)
        .bifield("u", "v", "uvPos", Parser.INT, 0, 0, (p, u, v) -> p.setTextureBox(u, v, -1, -1))
        .bifield("uWidth", "vHeight", "uvSize", Parser.INT, -2, -2, (p, uW, vH) -> p.setTextureBox(-1, -1, uW, vH))
        .bifield("repeatU",
            "repeatV",
            "repeatPos",
            Parser.INT,
            0,
            0,
            (p, u, v) -> p.setRepeatableTextureBox(-1, -1, -1, -1, u, v, -1, -1))
        .bifield("repeatWidth",
            "repeatHeight",
            "repeatSize",
            Parser.INT,
            -2,
            -2,
            (p, uW, vH) -> p.setRepeatableTextureBox(-1, -1, -1, -1, -1, -1, uW, vH));

    protected float uMin = 0;
    protected float vMin = 0;
    protected float uMax = 1;
    protected float vMax = 1;

    protected ResourceLocation textureLoc = MissingTextureAtlasSprite.getLocation();

    @Nullable
    protected RepeatableExtension repeatExt = null;
    protected int repeatPaddingX = 0;
    protected int repeatPaddingY = 0;

    public DelegatedImage(final AbstractPaneGroup parent)
    {
        super(parent);
    }

    /**
     * @param u       in texels, -1 to keep
     * @param v       in texels, -1 to keep
     * @param uWidth  in texels, -1 to keep, -2 for full size
     * @param vHeight in texels, -1 to keep, -2 for full size
     */
    public DelegatedImage setTextureBox(final int u, final int v, final int uWidth, final int vHeight)
    {
        final Vec2i imageDims = ResourceLoader.INSTANCE.getImageDimensions(textureLoc);

        if (imageDims == Vec2i.EMPTY)
        {
            textureLoc = MissingTextureAtlasSprite.getLocation();
            repeatExt = null;
            return this;
        }

        final float textureWidth = imageDims.x(), textureHeight = imageDims.y();

        this.uMin = u == -1 ? uMin : u / textureWidth;
        this.vMin = v == -1 ? vMin : v / textureHeight;
        this.uMax = uWidth == -1 ? uMax :
            (uWidth == -2 ? 1 : uMin + CheckArgument.inRange(uWidth, 1, Integer.MAX_VALUE, "U width") / textureWidth);
        this.vMax = vHeight == -1 ? vMax :
            (vHeight == -2 ? 1 : vMin + CheckArgument.inRange(vHeight, 1, Integer.MAX_VALUE, "V height") / textureHeight);

        this.repeatExt = null;

        return this;
    }

    /**
     * Repeat box is relative to u/v pointing to entire (sub)texture, -1 to keep values
     * 
     * @param u             in texels
     * @param v             in texels
     * @param uWidth        in texels, -2 for full size
     * @param vHeight       in texels, -2 for full size
     * @param uRepeat       in texels, repeat box with anchor at u/v (ie. middle box of 3x3 sliced image)
     * @param vRepeat       in texels, repeat box with anchor at u/v (ie. middle box of 3x3 sliced image)
     * @param uRepeatWidth  in texels, repeat box with anchor at u/v (ie. middle box of 3x3 sliced image), -2 for full size
     * @param vRepeatHeight in texels, repeat box with anchor at u/v (ie. middle box of 3x3 sliced image), -2 for full size
     */
    public DelegatedImage setRepeatableTextureBox(final int u,
        final int v,
        int uWidth,
        int vHeight,
        int uRepeat,
        int vRepeat,
        int uRepeatWidth,
        int vRepeatHeight)
    {
        final Vec2i imageDims = ResourceLoader.INSTANCE.getImageDimensions(textureLoc);

        if (imageDims == Vec2i.EMPTY)
        {
            textureLoc = MissingTextureAtlasSprite.getLocation();
            repeatExt = null;
            return this;
        }

        final float textureWidth = imageDims.x(), textureHeight = imageDims.y();

        // resolve -1
        if (uWidth == -1) uWidth = (int) ((uMax - uMin) * textureWidth);
        if (vHeight == -1) vHeight = (int) ((vMax - vMin) * textureHeight);
        if (uRepeat == -1) uRepeat = repeatExt == null ? 0 : repeatExt.xAdjust;
        if (vRepeat == -1) vRepeat = repeatExt == null ? 0 : repeatExt.yAdjust;
        if (uRepeatWidth == -1) uRepeatWidth = repeatExt == null ? -2 : repeatExt.width; // fallback to full size
        if (vRepeatHeight == -1) vRepeatHeight = repeatExt == null ? -2 : repeatExt.height; // fallback to full size

        // resolve -2
        if (uWidth == -2) uWidth = imageDims.x();
        if (vHeight == -2) vHeight = imageDims.y();
        if (uRepeatWidth == -2) uRepeatWidth = uWidth - (uRepeat == -1 ? repeatExt.xAdjust : uRepeat);
        if (vRepeatHeight == -2) vRepeatHeight = vHeight - (vRepeat == -1 ? repeatExt.yAdjust : vRepeat);

        setTextureBox(u, v, uWidth, vHeight);

        final float repeatExtuMin = uMin + uRepeat / textureWidth;
        final float repeatExtvMin = vMin + vRepeat / textureHeight;

        repeatExt = new RepeatableExtension(uRepeat,
            vRepeat,
            CheckArgument.inRange(uRepeatWidth, 1, Integer.MAX_VALUE, "U repeat width"),
            CheckArgument.inRange(vRepeatHeight, 1, Integer.MAX_VALUE, "V repeat height"),
            repeatExtuMin,
            repeatExtuMin + uRepeatWidth / textureWidth,
            repeatExtvMin,
            repeatExtvMin + vRepeatHeight / textureHeight);

        repeatPaddingX = uWidth - uRepeatWidth;
        repeatPaddingY = vHeight - vRepeatHeight;

        recalculateCountsUsingPaneSize(width, height);

        return this;
    }

    public DelegatedImage setTextureLoc(final ResourceLocation textureLoc)
    {
        this.textureLoc = Objects.requireNonNull(textureLoc);

        if (ResourceLoader.INSTANCE.getImageDimensions(textureLoc) == Vec2i.EMPTY)
        {
            Crash.argumentIfDev("Texture not found?! " + textureLoc);
        }

        return this;
    }

    public ResourceLocation getTextureLoc()
    {
        return textureLoc;
    }

    @Override
    protected void onAABBchange(final AABBchangeReason reason)
    {
        super.onAABBchange(reason);
        if (reason == AABBchangeReason.SIZE && repeatExt != null)
        {
            recalculateCountsUsingPaneSize(width, height);
        }
    }

    protected void recalculateCountsUsingPaneSize(final int widthPx, final int heightPx)
    {
        repeatExt.countX = (widthPx - repeatPaddingX) / repeatExt.width;
        repeatExt.countY = (heightPx - repeatPaddingY) / repeatExt.height;
    }

    @Override
    protected void drawSelf(final RenderContext ctx, final double mx, final double my)
    {
        ctx.blitRepeatable(textureLoc, alignedX, alignedY, width, height, uMin, vMin, uMax, vMax, repeatExt);
    }

    public static class Image extends DelegatedImage implements RenderSetters, AccessibleId
    {
        public static final ParsingCodec<Image> CODEC =
            ParsingCodec.of(ID, Image::new, AbstractPane.CODEC).copyFieldsFrom(DelegatedImage.CODEC);

        public Image(final AbstractPaneGroup parent)
        {
            super(parent);
        }
    }
}
