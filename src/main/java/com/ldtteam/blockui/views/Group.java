package com.ldtteam.blockui.views;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;

/**
 * A Group is a View which enforces the position of children to be
 * a Y-sorted list in the order they are added.
 * <p>
 * All children are set to a Top version of their alignment, and have their Y coordinates overwritten.
 */
public class Group extends View
{
    private int spacing = 0;

    /**
     * Required default constructor.
     */
    public Group()
    {
        super();
    }

    /**
     * Constructs a View from PaneParams.
     *
     * @param params Params for the Pane.
     */
    public Group(final PaneParams params)
    {
        super(params);
        spacing = params.getInteger("spacing", spacing);
    }

    @Override
    public void adjustChild(final Pane child)
    {
        int childX = child.getX();
        int childY = spacing;
        int childWidth = child.getWidth();
        final int childHeight = child.getHeight();

        // Adjust for horizontal size and alignment
        if (childWidth < 0)
        {
            childWidth = getInteriorWidth();
        }
        else if (child.getAlignment().isRightAligned())
        {
            childX = (getInteriorWidth() - childWidth) - childX;
        }
        else if (child.getAlignment().isHorizontalCentered())
        {
            childX = ((getInteriorWidth() - childWidth) / 2) + childX;
        }

        for (        final Pane c : children)
        {
            if (c == child)
            {
                break;
            }
            childY = c.getY() + c.getHeight() + spacing;
        }

        child.setSize(childWidth, childHeight);
        child.setPosition(childX, childY);
    }

    @Override
    public void removeChild(final Pane child)
    {
        super.removeChild(child);

        final int formerChildY = child.getY();
        final int formerChildHeight = child.getHeight();

        for (        final Pane c : children)
        {
            if (c.getY() > formerChildY)
            {
                c.moveBy(0, -formerChildHeight);
            }
        }
    }
}
