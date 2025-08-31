package com.ldtteam.blockui.element.shadow;

import com.ldtteam.blockui.AbstractPane;
import com.ldtteam.blockui.AbstractPaneGroup;
import com.ldtteam.blockui.AbstractShadowGroupPane;
import com.ldtteam.blockui.context.DelayedLevel;
import com.ldtteam.blockui.context.RenderContext;
import com.ldtteam.blockui.element.DelegatedShape;
import com.ldtteam.blockui.element.DelegatedText;
import com.ldtteam.blockui.util.Crash;
import com.ldtteam.blockui.element.DelegatedShape.VanillaTooltip;
import java.util.Objects;
import java.util.function.Function;

public class Tooltip<B extends AbstractDelegatedPane, T extends AbstractPane> extends AbstractShadowGroupPane
{
    protected B background;
    protected T contentPane;

    protected boolean autoWidth = true;
    protected boolean autoHeight = true;
    protected int maxAutoWidth = 200 + 2 * 4; // vanilla
    protected int maxAutoHeight;

    public Tooltip(final AbstractPaneGroup parent,
        final Function<AbstractPaneGroup, B> backgroudFactory,
        final Function<AbstractPaneGroup, T> contentPaneFactory)
    {
        super(parent);
        this.background = backgroudFactory.apply(this);
        this.contentPane = contentPaneFactory.apply(this);
        this.renderLevel = DelayedLevel.TOOLTIP;
    }

    public B getBackground()
    {
        return background;
    }

    public T getContentPane()
    {
        return contentPane;
    }

    /**
     * @param contentPane if parent of contentPane is not this button then it gets changed, old content pane gets unlinked
     */
    public void setContentPane(final T contentPane)
    {
        this.contentPane.unlinkParentAndRemoveHover();
        this.contentPane = Objects.requireNonNull(contentPane);
        if (contentPane.getParent() != this)
        {
            contentPane.setParent(this);
        }
    }

    /**
     * {@inheritDoc} Does not call super because most of checks are invalid, this element effectively has no position
     */
    @Override
    protected void onAABBchange(final AABBchangeReason reason)
    {
        if (width <= 0 || height <= 0)
        {
            throw Crash.argument("Size <= 0, w=%d, h=%d".formatted(width, height));
        }
    }

    @Override
    protected void drawSelf(final RenderContext ctx, final double mx, final double my)
    {
        // TODO: setsize
        ctx.pushTooltipTranslate(window.getRenderingAdapter(), width, height);
        super.drawSelf(ctx, mx, my);
        ctx.pop();
    }

    public static Tooltip<DelegatedShape<VanillaTooltip>, DelegatedText> vanillaTextTooltip(final AbstractPaneGroup parent)
    {
        return new Tooltip<>(parent, DelegatedShape::new, DelegatedText::new);
    }
}
