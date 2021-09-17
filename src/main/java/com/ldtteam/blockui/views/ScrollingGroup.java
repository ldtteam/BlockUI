package com.ldtteam.blockui.views;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;

/**
 * A Group is a View which enforces the position of children to be
 * a Y-sorted list in the order they are added.
 * <p>
 * All children are set to a Top version of their alignment, and have their Y coordinates overwritten
 */
public class ScrollingGroup extends ScrollingView
{
    protected int childSpacing = 0;

    /**
     * Required default constructor.
     */
    public ScrollingGroup()
    {
        super();
    }

    /**
     * Load from xml.
     *
     * @param params xml parameters.
     */
    public ScrollingGroup(final PaneParams params)
    {
        super(params);
        childSpacing = params.getInteger("childspacing", childSpacing);
    }

    /**
     * Redirect all predefined children into our container.
     *
     * @param params the xml parameters.
     */
    @Override
    public void parseChildren(final PaneParams params)
    {
        container.parseChildren(params);

        for (int i = 1; i < container.children.size(); i++)
        {
            final Pane child = container.children.get(i);
            final Pane lastChild = container.children.get(i-1);
            final int childY = lastChild.getY() + lastChild.getHeight() + childSpacing;

            child.setPosition(0, childY);
        }

        container.computeContentHeight();
    }

    @Override
    public void adjustChild(final Pane child)
    {
        int childY = 0;
        if (children.size() >= 2)
        {
            final Pane lastChild = children.get(children.size() - 2);
            childY = lastChild.getY() + lastChild.getHeight() + childSpacing;
        }

        child.setPosition(0, childY);
    }

    @Override
    public void removeChild(final Pane child)
    {
        super.removeChild(child);

        final int formerChildY = child.getY();
        final int formerChildHeight = child.getHeight() + childSpacing;

        for (final Pane c : children)
        {
            if (c.getY() > formerChildY)
            {
                c.moveBy(0, -formerChildHeight);
            }
        }
    }
}
