package com.ldtteam.blockui;

import com.ldtteam.blockui.element.shadow.AbstractDelegatedPane;
import com.ldtteam.blockui.parse.Parser;
import com.ldtteam.blockui.parse.ParsingCodec;
import com.ldtteam.blockui.util.MouseButton;

/**
 * Anything that has any user action is group an group made of shadow/delegated panes
 */
public abstract class AbstractShadowGroupPane extends AbstractPaneGroup
{
    public static final ParsingCodec<?> CODEC = ParsingCodec.<AbstractShadowGroupPane>forAbstract(PaneGroup.CODEC)
        .field("enabled", Parser.BOOL, (p, enabled) -> p.enabled = enabled);

    protected boolean enabled = true;

    protected AbstractShadowGroupPane(final AbstractPaneGroup parent)
    {
        super(parent);
    }

    @Override
    protected boolean processMouseButton(final MouseButton button, final double mx, final double my)
    {
        return true;
    }

    @Override
    protected boolean processMouseDrag(final double mx, final double my, final double deltaX, final double deltaY)
    {
        return true;
    }

    @Override
    protected boolean processMouseScroll(final double wheel, final double mx, final double my)
    {
        return true;
    }

    @Override
    protected void onSizeChange(int oldWidth, int oldHeight)
    {
        super.onSizeChange(oldWidth, oldHeight);

        // resize delegated only
        for (final AbstractPane c : getChildsView())
        {
            if (c instanceof final AbstractDelegatedPane delegated)
            // TODO: bug - everything is absDelegated
            {
                delegated.setSizeInternal(width, height);
            }
        }
    }

    public interface AccessibleEnabled
    {
        default void setEnabled(final boolean enabled)
        {
            ((AbstractShadowGroupPane) this).enabled = enabled;
        }

        default boolean isEnabled()
        {
            return ((AbstractShadowGroupPane) this).enabled;
        }
    }
}
