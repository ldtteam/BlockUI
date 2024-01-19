package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.Alignment;
import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.Parsers;
import com.ldtteam.blockui.support.ImageData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Clickable image.
 */
public class ButtonImage extends Button
{
    /**
     * Default size is a small square button.
     */
    private static final int DEFAULT_BUTTON_SIZE = 20;

    /**
     * The image for the regular state.
     */
    @NotNull
    protected ImageData imageRegular = ImageData.MISSING;

    /**
     * The image for the hover state.
     */
    @NotNull
    protected ImageData imageHover = ImageData.MISSING;

    /**
     * The image for the disabled state.
     */
    @NotNull
    protected ImageData imageDisabled = ImageData.MISSING;

    /**
     * Default constructor. Makes a small square button.
     */
    public ButtonImage()
    {
        super(Alignment.MIDDLE, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_SHADOW, DEFAULT_TEXT_WRAP);

        width = DEFAULT_BUTTON_SIZE;
        height = DEFAULT_BUTTON_SIZE;
        recalcTextRendering();
    }

    /**
     * Constructor called by the xml loader.
     *
     * @param params PaneParams provided in the xml.
     */
    public ButtonImage(final PaneParams params)
    {
        super(params, Alignment.MIDDLE, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_SHADOW, DEFAULT_TEXT_WRAP);

        imageRegular = loadImageInfo(params, "source", "image");
        imageHover = loadImageInfo(params, "highlight", "highlight");
        imageDisabled = loadImageInfo(params, "disabled", "disabled");

        loadTextInfo(params);
    }

    /**
     * Loads the parameters for any of the images.
     *
     * @param params     PaneParams provided in the xml.
     * @param source     the name of the source attribute
     * @param propPrefix the prefix for other properties like size and offset.
     */
    protected ImageData loadImageInfo(final PaneParams params, final String source, final String propPrefix)
    {
        final ResourceLocation imageSource = params.getResource(source, MissingTextureAtlasSprite.getLocation().getPath());
        if (imageSource.equals(MissingTextureAtlasSprite.getLocation()))
        {
            return ImageData.MISSING;
        }

        final AtomicInteger imageOffsetX = new AtomicInteger();
        final AtomicInteger imageOffsetY = new AtomicInteger();
        final AtomicInteger imageWidth = new AtomicInteger();
        final AtomicInteger imageHeight = new AtomicInteger();

        params.applyShorthand(propPrefix + "offset", Parsers.INT, 2, a -> {
            imageOffsetX.set(a.get(0));
            imageOffsetY.set(a.get(1));
        });

        params.applyShorthand(propPrefix + "size", Parsers.INT, 2, a -> {
            imageWidth.set(a.get(0));
            imageHeight.set(a.get(1));
        });

        return new ImageData(imageSource, imageOffsetX.get(), imageOffsetY.get(), imageWidth.get(), imageHeight.get());
    }

    /**
     * Loads the parameters for the button textContent.
     *
     * @param params PaneParams provided in the xml.
     */
    private void loadTextInfo(final PaneParams params)
    {
        textColor = params.getColor("textcolor", textColor);
        // match textColor by default
        textHoverColor = params.getColor("texthovercolor", textColor);
        // match textColor by default
        textDisabledColor = params.getColor("textdisabledcolor", textColor);

        params.applyShorthand("textoffset", Parsers.INT, 2, a -> {
            textOffsetX = a.get(0);
            textOffsetY = a.get(1);
        });

        params.applyShorthand("textbox", Parsers.INT, 2, a -> {
            textWidth = a.get(0);
            textHeight = a.get(1);
        });

        recalcTextRendering();
    }

    /**
     * Set the default image.
     *
     * @param loc     ResourceLocation for the image.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param w       image width.
     * @param h       image height.
     */
    public void setImage(final ResourceLocation loc, final int offsetX, final int offsetY, final int w, final int h)
    {
        imageRegular = new ImageData(loc, offsetX, offsetY, w, h);
    }

    /**
     * Set the default image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImage(final ResourceLocation loc, final boolean keepUv)
    {
        if (keepUv && !imageRegular.equals(ImageData.MISSING))
        {
            imageRegular = new ImageData(loc, imageRegular.offsetX(), imageRegular.offsetY(), imageRegular.width(), imageRegular.height());
        }
        else
        {
            imageRegular = new ImageData(loc, 0, 0, 0, 0);
        }
    }

    /**
     * Set the hover image.
     *
     * @param loc     ResourceLocation for the image.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param w       image width.
     * @param h       image height.
     */
    public void setImageHighlight(final ResourceLocation loc, final int offsetX, final int offsetY, final int w, final int h)
    {
        imageHover = new ImageData(loc, offsetX, offsetY, w, h);
    }

    /**
     * Set the hover image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImageHighlight(final ResourceLocation loc, final boolean keepUv)
    {
        if (keepUv && !imageHover.equals(ImageData.MISSING))
        {
            imageHover = new ImageData(loc, imageHover.offsetX(), imageHover.offsetY(), imageHover.width(), imageHover.height());
        }
        else
        {
            imageHover = new ImageData(loc, 0, 0, 0, 0);
        }
    }

    /**
     * Set the disabled image.
     *
     * @param loc     ResourceLocation for the image.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param w       image width.
     * @param h       image height.
     */
    public void setImageDisabled(final ResourceLocation loc, final int offsetX, final int offsetY, final int w, final int h)
    {
        imageDisabled = new ImageData(loc, offsetX, offsetY, w, h);
    }

    /**
     * Set the disabled image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImageDisabled(final ResourceLocation loc, final boolean keepUv)
    {
        if (keepUv && !imageDisabled.equals(ImageData.MISSING))
        {
            imageDisabled = new ImageData(loc, imageDisabled.offsetX(), imageDisabled.offsetY(), imageDisabled.width(), imageDisabled.height());
        }
        else
        {
            imageDisabled = new ImageData(loc, 0, 0, 0, 0);
        }
    }

    /**
     * Override to apply some special formatting to the image before drawing.
     */
    public void beforeDrawImage()
    {
        if (!enabled)
        {
            RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
        }
        else if (wasCursorInPane)
        {
            RenderSystem.setShaderColor(1.1F, 1.1F, 1.1F, 1.0F);
        }
    }

    /**
     * Determine what image to render.
     *
     * @return the image data instance.
     */
    public ImageData getImageToDraw()
    {
        if (!enabled && !imageDisabled.equals(ImageData.MISSING))
        {
            return imageDisabled;
        }
        else if (wasCursorInPane && !imageHover.equals(ImageData.MISSING))
        {
            return imageHover;
        }
        return imageRegular;
    }

    /**
     * Draw the button.
     * Decide what image to use, and possibly draw textContent.
     *
     * @param mx Mouse x (relative to parent)
     * @param my Mouse y (relative to parent)
     */
    @Override
    public void drawSelf(final BOGuiGraphics target, final double mx, final double my)
    {
        if (!FMLEnvironment.production)
        {
            Objects.requireNonNull(imageRegular, () -> id + " | " + window.getXmlResourceLocation());
        }

        final PoseStack ms = target.pose();

        ImageData imageData = getImageToDraw();

        beforeDrawImage();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        blit(ms,
          imageData.image(),
          x,
          y,
          width,
          height,
          imageData.offsetX(),
          imageData.offsetY(),
          imageData.getWidth(),
          imageData.getHeight(),
          imageData.mapWidth(),
          imageData.mapHeight());

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        super.drawSelf(target, mx, my);
    }

    @Override
    public void setSize(final int w, final int h)
    {
        final int newTextWidth = (int) ((double) (textWidth * w) / width);
        final int newTextHeight = (int) ((double) (textHeight * h) / height);

        super.setSize(w, h);

        textWidth = newTextWidth;
        textHeight = newTextHeight;
        recalcTextRendering();
    }

    /**
     * Sets text offset for rendering, relative to element start.
     * Is automatically shrinked to element width and height.
     *
     * @param textOffsetX left offset
     * @param textOffsetY top offset
     */
    public void setTextOffset(final int textOffsetX, final int textOffsetY)
    {
        this.textOffsetX = Mth.clamp(textOffsetX, 0, width);
        this.textOffsetY = Mth.clamp(textOffsetY, 0, height);
    }

    /**
     * Sets text rendering box.
     * Is automatically shrinked to element width and height minus text offsets.
     *
     * @param textWidth  horizontal size
     * @param textHeight vertical size
     */
    public void setTextRenderBox(final int textWidth, final int textHeight)
    {
        this.textWidth = Mth.clamp(textWidth, 0, width - textOffsetX);
        this.textHeight = Mth.clamp(textHeight, 0, height - textOffsetY);
        recalcTextRendering();
    }
}
