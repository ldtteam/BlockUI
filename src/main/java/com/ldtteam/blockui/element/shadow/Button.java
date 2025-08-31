package com.ldtteam.blockui.element.shadow;

import com.ldtteam.blockui.AbstractPane;
import com.ldtteam.blockui.AbstractPane.AccessibleId;
import com.ldtteam.blockui.AbstractPane.RenderSetters;
import com.ldtteam.blockui.AbstractPaneGroup;
import com.ldtteam.blockui.AbstractShadowGroupPane;
import com.ldtteam.blockui.AbstractShadowGroupPane.AccessibleEnabled;
import com.ldtteam.blockui.attribute.RelativeAlignment;
import com.ldtteam.blockui.context.RenderContext;
import com.ldtteam.blockui.element.DelegatedImage;
import com.ldtteam.blockui.element.DelegatedImage.Image;
import com.ldtteam.blockui.element.DelegatedItemIcon.ItemIcon;
import com.ldtteam.blockui.element.DelegatedText.Text;
import com.ldtteam.blockui.parse.Parser;
import com.ldtteam.blockui.parse.ParsingCodec;
import com.ldtteam.blockui.util.MouseButton;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class Button<T extends AbstractPane> extends AbstractShadowGroupPane implements RenderSetters, AccessibleId, AccessibleEnabled
{
    public static final ParsingCodec<Button<AbstractPane>> CODEC =
        ParsingCodec.of("button", Button::new, AbstractShadowGroupPane.CODEC)
            .field("autoresizeContentPane", Parser.BOOL, Button::setShouldContentPaneFollowButtonResize)
            .requireNext()
            .shadowElement("texture", DelegatedImage.CODEC, Button::getTexture)
            .shadowElement("hoverTexture", DelegatedImage.CODEC, Button::getHoverTexture)
            .shadowElement("disabledTexture", DelegatedImage.CODEC, Button::getDisabledTexture)
            .requireNext()
            .shadowSingleChild("content", Button::setContentPane);

    public static final Runnable NO_ACTION = () -> {};

    protected static final float HOVER_COLOR_MODIFIER = 1.1f;
    protected static final float DISABLED_COLOR_MODIFIER = 0.5f;

    protected T contentPane;
    protected boolean shouldContentPaneFollowButtonResize = true;
    protected final DelegatedImage texture;
    protected DelegatedImage hoverTexture;
    protected DelegatedImage disabledTexture;
    protected Runnable clickAction = NO_ACTION;

    private Button(final AbstractPaneGroup parent)
    {
        super(parent);
        this.texture = new DelegatedImage(this);
        this.hoverTexture = new WrappedDelegatedImage(this, texture, HOVER_COLOR_MODIFIER);
        this.disabledTexture = new WrappedDelegatedImage(this, texture, DISABLED_COLOR_MODIFIER);
    }

    public Button(final AbstractPaneGroup parent, final Function<AbstractPaneGroup, T> contentPaneFactory)
    {
        this(parent);
        this.contentPane = contentPaneFactory.apply(this);
    }

    @Override
    protected void drawSelf(final RenderContext ctx, final double mx, final double my)
    {
        texture.visible(enabled && !wasCursorInPane);
        hoverTexture.visible(enabled && wasCursorInPane);
        disabledTexture.visible(!enabled);
        super.drawSelf(ctx, mx, my);
    }

    @Override
    protected void onSizeChange(final int oldWidth, final int oldHeight)
    {
        super.onSizeChange(oldWidth, oldHeight);

        if (shouldContentPaneFollowButtonResize && contentPane instanceof final RenderSetters pane)
        {
            pane.adjustSize(width - oldWidth, height - oldHeight);
        }
    }

    @Override
    protected boolean processMouseButton(final MouseButton button, final double mx, final double my)
    {
        if (button == MouseButton.LEFT)
        {
            clickAction.run();
        }
        return true;
    }

    @Override
    protected AbstractPane traverseHierarchy(final Predicate<AbstractPane> terminalCondition)
    {
        AbstractPane result = super.traverseHierarchy(terminalCondition);

        // replace wrapped hack with normal
        if (result == hoverTexture && hoverTexture instanceof WrappedDelegatedImage)
        {
            result = getHoverTexture();
        }
        else if (result == disabledTexture && disabledTexture instanceof WrappedDelegatedImage)
        {
            result = getHoverTexture();
        }

        return result;
    }

    public void setClickAction(final Runnable clickAction)
    {
        this.clickAction = clickAction;
    }

    /**
     * @param contentPane if parent of contentPane is not this button then it gets changed, old content pane gets unlinked
     */
    public void setContentPane(final T contentPane)
    {
        if (this.contentPane != null) // null check cuz of parsing codec
        {
            this.contentPane.unlinkParentAndRemoveHover();
        }

        this.contentPane = Objects.requireNonNull(contentPane);
        if (contentPane.getParent() != this)
        {
            contentPane.setParent(this);
        }
    }

    public T getContentPane()
    {
        return contentPane;
    }

    public void setShouldContentPaneFollowButtonResize(final boolean followResize)
    {
        this.shouldContentPaneFollowButtonResize = followResize;
    }

    public boolean shouldContentPaneFollowButtonResize()
    {
        return shouldContentPaneFollowButtonResize;
    }

    public DelegatedImage getTexture()
    {
        return texture;
    }

    private DelegatedImage createDelegatedImage()
    {
        final DelegatedImage child = new DelegatedImage(this);
        moveChildBeforeAnother(child, contentPane);
        child.sized(width, height);
        return child;
    }

    public DelegatedImage getHoverTexture()
    {
        if (hoverTexture instanceof WrappedDelegatedImage)
        {
            hoverTexture = createDelegatedImage();
        }
        return hoverTexture;
    }

    public DelegatedImage getDisabledTexture()
    {
        if (disabledTexture instanceof WrappedDelegatedImage)
        {
            disabledTexture = createDelegatedImage();
        }
        return disabledTexture;
    }

    /**
     * A bit hacky way to supply hover/disabled, assumes delegate is at same pos/size as wrapping pane
     */
    protected static class WrappedDelegatedImage extends DelegatedImage
    {
        private final AbstractDelegatedPane delegate;
        private final float colorModifier;

        public WrappedDelegatedImage(final AbstractPaneGroup parent, final DelegatedImage delegate, final float colorModifier)
        {
            super(parent);
            this.delegate = delegate;
            this.colorModifier = colorModifier;
        }

        @Override
        protected void drawSelf(final RenderContext ctx, final double mx, final double my)
        {
            ctx.setColorModifier(colorModifier);
            delegate.drawSelf(ctx, mx, my);
            ctx.restoreOneColorModifier();
        }
    }

    public static Button<Text> buttonWithText(final AbstractPaneGroup parent)
    {
        final Button<Text> button = new Button<>(parent, Text::new);
        button.contentPane.setTextAlignment(RelativeAlignment.MIDDLE_MIDDLE);
        return button;
    }

    public static Button<ItemIcon> buttonWithItemIcon(final AbstractPaneGroup parent, final boolean withVanillaTooltip)
    {
        final Button<ItemIcon> button = new Button<>(parent, ItemIcon::new);
        button.contentPane.setSizeToScaleOfVanilla(1);
        button.contentPane.setAlignment(RelativeAlignment.MIDDLE_MIDDLE);
        button.shouldContentPaneFollowButtonResize = false;
        if (withVanillaTooltip)
        {
            button.contentPane.replaceHoverWithVanillaTooltip();
        }
        return button;
    }

    public static Button<Image> buttonWithImage(final AbstractPaneGroup parent)
    {
        final Button<Image> button = new Button<>(parent, Image::new);
        button.contentPane.setAlignment(RelativeAlignment.MIDDLE_MIDDLE);
        return button;
    }
}
