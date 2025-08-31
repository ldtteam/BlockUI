package com.ldtteam.blockui.element;

import com.ldtteam.blockui.AbstractPaneGroup;
import com.ldtteam.blockui.context.RenderContext;
import com.ldtteam.blockui.element.shadow.AbstractDelegatedPane;

public class DelegatedEntityIcon extends AbstractDelegatedPane
{
    public DelegatedEntityIcon(final AbstractPaneGroup parent)
    {
        super(parent);
    }

    @Override
    protected void drawSelf(final RenderContext ctx, final double mx, final double my)
    {
        throw new UnsupportedOperationException("Unimplemented method 'drawSelf'");
    }

    public static class EntityIcon extends DelegatedEntityIcon implements RenderSetters, AccessibleId
    {
        public EntityIcon(final AbstractPaneGroup parent)
        {
            super(parent);
        }
    }
}
