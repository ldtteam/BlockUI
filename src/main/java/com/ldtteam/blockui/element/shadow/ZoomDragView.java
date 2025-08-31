package com.ldtteam.blockui.element.shadow;

import com.ldtteam.blockui.AbstractPane.AccessibleId;
import com.ldtteam.blockui.AbstractPane.RenderSetters;
import com.ldtteam.blockui.AbstractPaneGroup;
import com.ldtteam.blockui.AbstractShadowGroupPane;
import com.ldtteam.blockui.AbstractShadowGroupPane.AccessibleEnabled;

public class ZoomDragView extends AbstractShadowGroupPane implements RenderSetters, AccessibleId, AccessibleEnabled
{
    public ZoomDragView(final AbstractPaneGroup parent)
    {
        super(parent);
    }
}
