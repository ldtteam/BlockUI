package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.Alignment;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.Parsers;
import com.ldtteam.blockui.util.records.SizeI;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

import net.minecraft.util.Mth;

/**
 * Clickable image.
 */
public class ButtonImage extends Button
{
    /**
     * Default size is a small square button.
     */
    private static final int DEFAULT_BUTTON_SIZE = 20;
    protected ResourceLocation image;
    protected ResourceLocation imageHighlight;
    protected ResourceLocation imageDisabled;
    protected int imageOffsetX = 0;
    protected int imageOffsetY = 0;
    protected int imageWidth = 0;
    protected int imageHeight = 0;
    protected int imageMapWidth = 0;
    protected int imageMapHeight = 0;
    protected int highlightOffsetX = 0;
    protected int highlightOffsetY = 0;
    protected int highlightWidth = 0;
    protected int highlightHeight = 0;
    protected int highlightMapWidth = 0;
    protected int highlightMapHeight = 0;
    protected int disabledOffsetX = 0;
    protected int disabledOffsetY = 0;
    protected int disabledWidth = 0;
    protected int disabledHeight = 0;
    protected int disabledMapWidth = 0;
    protected int disabledMapHeight = 0;

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

        loadImageInfo(params);
        loadHighlightInfo(params);
        loadDisabledInfo(params);

        loadTextInfo(params);
    }

    /**
     * Loads the parameters for the normal image.
     *
     * @param params PaneParams provided in the xml.
     */
    private void loadImageInfo(final PaneParams params)
    {
        image = params.getResource("source", this::loadImageDimensions);

        params.applyShorthand("imageoffset", Parsers.INT, 2, a -> {
            imageOffsetX = a.get(0);
            imageOffsetY = a.get(1);
        });

        params.applyShorthand("imagesize", Parsers.INT, 2, a -> {
            imageWidth = a.get(0);
            imageHeight = a.get(1);
        });
    }

    /**
     * Loads the parameters for the hover image.
     *
     * @param params PaneParams provided in the xml.
     */
    private void loadHighlightInfo(final PaneParams params)
    {
        imageHighlight = params.getResource("highlight", this::loadImageHighlightDimensions);

        params.applyShorthand("highlightoffset", Parsers.INT, 2, a -> {
            highlightOffsetX = a.get(0);
            highlightOffsetY = a.get(1);
        });

        params.applyShorthand("highlightsize", Parsers.INT, 2, a -> {
            highlightWidth = a.get(0);
            highlightHeight = a.get(1);
        });
    }

    /**
     * Loads the parameters for the disabled image.
     *
     * @param params PaneParams provided in the xml.
     */
    private void loadDisabledInfo(final PaneParams params)
    {
        imageDisabled = params.getResource("disabled", this::loadImageDisabledDimensions);

        params.applyShorthand("disabledoffset", Parsers.INT, 2, a -> {
            disabledOffsetX = a.get(0);
            disabledOffsetY = a.get(1);
        });

        params.applyShorthand("disabledsize", Parsers.INT, 2, a -> {
            disabledWidth = a.get(0);
            disabledHeight = a.get(1);
        });
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
     * Uses {@link Image#getImageDimensions(ResourceLocation)} to determine the dimensions of image texture.
     */
    private void loadImageDimensions(final ResourceLocation rl)
    {
        final SizeI dimensions = Image.getImageDimensions(rl);
        imageMapWidth = dimensions.width();
        imageMapHeight = dimensions.height();
    }

    /**
     * Uses {@link Image#getImageDimensions(ResourceLocation)} to determine the dimensions of hover image texture.
     */
    private void loadImageHighlightDimensions(final ResourceLocation rl)
    {
        final SizeI dimensions = Image.getImageDimensions(rl);
        highlightMapWidth = dimensions.width();
        highlightMapHeight = dimensions.height();
    }

    /**
     * Uses {@link Image#getImageDimensions(ResourceLocation)} to determine the dimensions of disabled image texture.
     */
    private void loadImageDisabledDimensions(final ResourceLocation rl)
    {
        final SizeI dimensions = Image.getImageDimensions(rl);
        disabledMapWidth = dimensions.width();
        disabledMapHeight = dimensions.height();
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
        if (!Objects.equals(loc, image))
        {
            loadImageDimensions(loc);
        }

        image = loc;
        imageOffsetX = offsetX;
        imageOffsetY = offsetY;
        imageHeight = w;
        imageWidth = h;
    }

    /**
     * Set the default image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImage(final ResourceLocation loc, final boolean keepUv)
    {
        if (!Objects.equals(loc, image))
        {
            loadImageDimensions(loc);
        }

        image = loc;

        if (!keepUv)
        {
            imageOffsetX = 0;
            imageOffsetY = 0;
            imageHeight = 0;
            imageWidth = 0;
        }

        loadImageDimensions(loc);
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
        imageHighlight = loc;
        highlightOffsetX = offsetX;
        highlightOffsetY = offsetY;
        highlightHeight = w;
        highlightWidth = h;

        loadImageHighlightDimensions(loc);
    }

    /**
     * Set the hover image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImageHighlight(final ResourceLocation loc, final boolean keepUv)
    {
        imageHighlight = loc;

        if (!keepUv)
        {
            highlightOffsetX = 0;
            highlightOffsetY = 0;
            highlightHeight = 0;
            highlightWidth = 0;
        }

        loadImageHighlightDimensions(loc);
    }

    /**
     * Set the disabled image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImageDisabled(final ResourceLocation loc, final boolean keepUv)
    {
        imageDisabled = loc;

        if (!keepUv)
        {
            disabledOffsetX = 0;
            disabledOffsetY = 0;
            disabledHeight = 0;
            disabledWidth = 0;
        }

        loadImageDisabledDimensions(loc);
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
        imageDisabled = loc;
        disabledOffsetX = offsetX;
        disabledOffsetY = offsetY;
        disabledHeight = w;
        disabledWidth = h;

        loadImageDisabledDimensions(loc);
    }

    /**
     * Draw the button.
     * Decide what image to use, and possibly draw textContent.
     *
     * @param mx Mouse x (relative to parent)
     * @param my Mouse y (relative to parent)
     */
    @Override
    public void drawSelf(final PoseStack ms, final double mx, final double my)
    {
        Objects.requireNonNull(image, () -> id + " | " + window.getXmlResourceLocation());
        ResourceLocation bind = image;
        int u = imageOffsetX;
        int v = imageOffsetY;
        int w = imageWidth == 0 ? imageMapWidth : imageWidth;
        int h = imageHeight == 0 ? imageMapHeight : imageHeight;
        int mapWidth = imageMapWidth;
        int mapHeight = imageMapHeight;

        if (!enabled)
        {
            if (imageDisabled != null)
            {
                bind = imageDisabled;
                u = disabledOffsetX;
                v = disabledOffsetY;
                w = disabledWidth == 0 ? disabledMapWidth : disabledWidth;
                h = disabledHeight == 0 ? disabledMapHeight : disabledHeight;
                mapWidth = disabledMapWidth;
                mapHeight = disabledMapHeight;
            }
            RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
        }
        else if (wasCursorInPane)
        {
            if (imageHighlight != null)
            {
                bind = imageHighlight;
                u = highlightOffsetX;
                v = highlightOffsetY;
                w = highlightWidth == 0 ? highlightMapWidth : highlightWidth;
                h = highlightHeight == 0 ? highlightMapHeight : highlightHeight;
                mapWidth = highlightMapWidth;
                mapHeight = highlightMapHeight;
            }
            RenderSystem.setShaderColor(1.1F, 1.1F, 1.1F, 1.0F);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        blit(ms, bind, x, y, width, height, u, v, w, h, mapWidth, mapHeight);

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        super.drawSelf(ms, mx, my);
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
