package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.Alignment;
import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.Parsers;
import com.ldtteam.blockui.util.texture.ResolvedWidgetSprites;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

import net.minecraft.util.Mth;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * Clickable image.
 */
public class ButtonImage extends Button
{
    public static final WidgetSprites VANILLA_BUTTON = AbstractButton.SPRITES;

    /**
     * Default size is a vanilla button.
     */
    public static final int DEFAULT_BUTTON_WIDTH = 200;
    public static final int DEFAULT_BUTTON_HEIGHT = 20;
    public static final int DEFAULT_ENABLED_COLOR = 0xFFFFFF;
    public static final int DEFAULT_HOVER_COLOR = 0xFFFFA0;
    public static final int DEFAULT_DISABLED_COLOR = 0xA0A0A0;

    protected WidgetSprites textures = VANILLA_BUTTON;
    protected ResolvedWidgetSprites resolvedTextures = null;

    /**
     * Default constructor. Makes a small square button.
     */
    public ButtonImage()
    {
        super(Alignment.MIDDLE, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_COLOR, DEFAULT_TEXT_SHADOW, DEFAULT_TEXT_WRAP);

        setVanillaButton();
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

        if (params.hasAttribute("source"))
        {
            final ResourceLocation enabled = params.getResource("source", MissingTextureAtlasSprite.getLocation());
            final ResourceLocation focused = params.getResource("highlight", enabled);
            final ResourceLocation disabled = params.getResource("disabled", enabled);
            setTextures(new WidgetSprites(enabled, disabled, focused, disabled));
        }
        else
        {
            setVanillaButton();
        }

        loadTextInfo(params);
    }

    public void setVanillaButton()
    {
        if (width == 0)
        {
            width = DEFAULT_BUTTON_WIDTH;
        }
        if (height == 0)
        {
            height = DEFAULT_BUTTON_HEIGHT;
        }
        textures = VANILLA_BUTTON;
        textColor = DEFAULT_ENABLED_COLOR;
        textHoverColor = DEFAULT_HOVER_COLOR;
        textDisabledColor = DEFAULT_DISABLED_COLOR;
        textOffsetX = 3;
        textOffsetY = 3;
        textWidth = width - 2 * textOffsetX;
        textHeight = height - 2 * textOffsetY;
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
        textHoverColor = params.getColor("texthovercolor", textHoverColor);
        // match textColor by default
        textDisabledColor = params.getColor("textdisabledcolor", textDisabledColor);

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
     * @param buttonTextures group of all possible textures for any combination of enables/hovered
     */
    public void setTextures(final WidgetSprites buttonTextures)
    {
        this.textures = buttonTextures;
        this.resolvedTextures = null;
    }

    /**
     * @return group of all possible textures for any combination of enables/hovered
     */
    public WidgetSprites getTextures()
    {
        return textures;
    }

    /**
     * Set the default image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImage(final ResourceLocation loc)
    {
        if (!Objects.equals(loc, textures.enabled()))
        {
            if (Objects.equals(textures.enabled(), textures.enabledFocused()))
            {
                setImageHighlight(loc);
            }

            setTextures(new WidgetSprites(loc, textures.disabled(), textures.enabledFocused(), textures.disabledFocused()));
        }
    }

    /**
     * Set the default image.
     *
     * @param loc     ResourceLocation for the image.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param w       image width.
     * @param h       image height.
     * 
     * @deprecated use same method name only with resLoc
     */
    @Deprecated(forRemoval = true, since = "1.20.2")
    public void setImage(final ResourceLocation loc, final int offsetX, final int offsetY, final int w, final int h)
    {
        setImage(loc);
    }

    /**
     * Set the default image.
     *
     * @param loc ResourceLocation for the image.
     * 
     * @deprecated use same method name only with resLoc
     */
    @Deprecated(forRemoval = true, since = "1.20.2")
    public void setImage(final ResourceLocation loc, final boolean keepUv)
    {
        setImage(loc);
    }

    /**
     * Set the hover image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImageHighlight(final ResourceLocation loc)
    {
        if (!Objects.equals(loc, textures.enabledFocused()))
        {
            setTextures(new WidgetSprites(textures.enabled(), textures.disabled(), loc, textures.disabledFocused()));
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
     * 
     * @deprecated use same method name only with resLoc
     */
    @Deprecated(forRemoval = true, since = "1.20.2")
    public void setImageHighlight(final ResourceLocation loc, final int offsetX, final int offsetY, final int w, final int h)
    {
        setImageHighlight(loc);
    }

    /**
     * Set the hover image.
     *
     * @param loc ResourceLocation for the image.
     * 
     * @deprecated use same method name only with resLoc
     */
    @Deprecated(forRemoval = true, since = "1.20.2")
    public void setImageHighlight(final ResourceLocation loc, final boolean keepUv)
    {
        setImageHighlight(loc);
    }

    /**
     * Set the disabled image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImageDisabled(final ResourceLocation loc)
    {
        if (!Objects.equals(loc, textures.disabled()))
        {
            if (Objects.equals(textures.disabled(), textures.disabledFocused()))
            {
                setImageHighlightDisabled(loc);
            }

            setTextures(new WidgetSprites(textures.enabled(), loc, textures.enabledFocused(), textures.disabledFocused()));
        }
    }

    /**
     * Set the disabled image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImageHighlightDisabled(final ResourceLocation loc)
    {
        if (!Objects.equals(loc, textures.disabledFocused()))
        {
            setTextures(new WidgetSprites(textures.enabled(), textures.disabled(), textures.enabledFocused(), loc));
        }
    }

    /**
     * Set the disabled image.
     *
     * @param loc ResourceLocation for the image.
     * 
     * @deprecated use same method name only with resLoc
     */
    @Deprecated(forRemoval = true, since = "1.20.2")
    public void setImageDisabled(final ResourceLocation loc, final boolean keepUv)
    {
        setImageDisabled(loc);
    }

    /**
     * Set the disabled image.
     *
     * @param loc     ResourceLocation for the image.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param w       image width.
     * @param h       image height.
     * 
     * @deprecated use same method name only with resLoc
     */
    @Deprecated(forRemoval = true, since = "1.20.2")
    public void setImageDisabled(final ResourceLocation loc, final int offsetX, final int offsetY, final int w, final int h)
    {
        setImageDisabled(loc);
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
            Objects.requireNonNull(textures.enabled(), () -> id + " | " + window.getXmlResourceLocation());
        }

        if (resolvedTextures == null)
        {
            resolvedTextures = ResolvedWidgetSprites.fromUnresolved(textures, Image::resolveBlit);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        resolvedTextures.getAndPrepare(isEnabled(), wasCursorInPane).blit(target.pose(), x, y, width, height);

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
