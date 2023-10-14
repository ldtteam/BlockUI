package com.ldtteam.blockui.views;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;

/**
 * A Group is a View which enforces the position of children to be
 * a Y-sorted list in the order they are added.
 * <p>
 * All children are set to a Top version of their alignment, and have their Y coordinates overwritten.
 * <p>
 * If horizontal="true" then it changes to an X-sorted list aligned to Left.
 */
public class Group extends View
{
    private int spacing = 0;
    private boolean horizontal = false;

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
        horizontal = params.getBoolean("horizontal", horizontal);
    }

    @Override
    public void adjustChild(final Pane child)
    {
        if (horizontal)
        {
            adjustChildHorizontal(child);
        } else
        {
            adjustChildVertical(child);
        }
    }

    private void adjustChildHorizontal(final Pane child)
    {
        int childX = spacing;
        int childY = child.getY();
        final int childWidth = child.getWidth();
        int childHeight = child.getHeight();

        // Adjust for vertical size and alignment
        if (childHeight < 0)
        {
            childHeight = getInteriorHeight();
        }
        else if (child.getAlignment().isBottomAligned())
        {
            childY = (getInteriorHeight() - childHeight) - childY;
        }
        else if (child.getAlignment().isVerticalCentered())
        {
            childY = ((getInteriorHeight() - childHeight) / 2) + childY;
        }

        for (final Pane c : children)
        {
            if (c == child)
            {
                break;
            }
            childX = c.getX() + c.getWidth() + spacing;
        }

        child.setSize(childWidth, childHeight);
        child.setPosition(childX, childY);
    }

    private void adjustChildVertical(final Pane child)
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

        for (final Pane c : children)
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

        if (horizontal)
        {
            final int formerChildX = child.getX();
            final int formerChildWidth = child.getWidth();

            for (final Pane c : children)
            {
                if (c.getX() > formerChildX)
                {
                    c.moveBy(-formerChildWidth, 0);
                }
            }
        }
        else
        {
            final int formerChildY = child.getY();
            final int formerChildHeight = child.getHeight();

            for (final Pane c : children)
            {
                if (c.getY() > formerChildY)
                {
                    c.moveBy(0, -formerChildHeight);
                }
            }
        }
    }
}
