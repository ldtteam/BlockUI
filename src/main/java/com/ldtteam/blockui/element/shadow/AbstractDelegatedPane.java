package com.ldtteam.blockui.element.shadow;

import com.ldtteam.blockui.AbstractPane;
import com.ldtteam.blockui.AbstractPaneGroup;
import com.ldtteam.blockui.context.RenderContext;

/**
 * Exposes pane fields to package so parents of delegated can access them
 */
public abstract class AbstractDelegatedPane extends AbstractPane
{
    protected AbstractDelegatedPane(final AbstractPaneGroup parent)
    {
        super(parent);
    }

    protected final void visible(final boolean vis)
    {
        visible = vis;
    }

    protected final void sized(final int width, final int height)
    {
        super.setSizeInternal(width, height);
    }

    @Override
    protected abstract void drawSelf(final RenderContext ctx, final double mx, final double my);
}
