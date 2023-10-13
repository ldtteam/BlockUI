package com.ldtteam.blockui.views;

import com.ldtteam.blockui.Loader;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;

/**
 * A Blockout pane that contains a scrolling line of other panes.
 */
public class ScrollingListContainer extends ScrollingContainer
{
    private int listElementHeight = 0;

    ScrollingListContainer(final ScrollingList owner)
    {
        super(owner);
    }

    /**
     * Creates, deletes, and updates existing Panes for elements in the list based on the DataProvider.
     *
     * @param dataProvider   data provider object, shouldn't be null.
     * @param listNodeParams the xml parameters for this pane.
     */
    public void refreshElementPanes(final ScrollingList.DataProvider dataProvider, final PaneParams listNodeParams, final int height, final int childSpacing)
    {
        final int numElements = (dataProvider != null) ? dataProvider.getElementCount() : 0;
        if (dataProvider != null)
        {
            for (int i = 0; i < numElements; ++i)
            {
                final Pane child;
                final int childYpos = listElementHeight * i;
                if (childYpos + listElementHeight >= scrollY && childYpos <= scrollY + height)
                {
                    if (i < children.size())
                    {
                        child = children.get(i);
                    }
                    else
                    {
                        child = Loader.createFromPaneParams(listNodeParams, this);
                        if (child == null)
                        {
                            continue;
                        }

                        if (i == 0)
                        {
                            listElementHeight = child.getHeight() + childSpacing;
                        }
                    }
                    child.setPosition(0, childYpos);

                    dataProvider.updateElement(i, child);
                }
            }
        }

        while (children.size() > numElements)
        {
            removeChild(children.get(numElements));
        }

        setContentHeight(numElements * listElementHeight - childSpacing);
    }

    /**
     * Returns the element list index for the given pane.
     *
     * @param pane the pane to find the index of.
     * @return the index.
     */
    public int getListElementIndexByPane(final Pane pane)
    {
        Pane parentPane = pane;
        while (parentPane != null && parentPane.getParent() != this)
        {
            parentPane = parentPane.getParent();
        }

        if (parentPane == null)
        {
            return -1;
        }

        return getChildren().indexOf(parentPane);
    }

    /**
     * This is an optimized version that relies on the fixed size and order of children to quickly determine.
     *
     * @param mx Mouse X, relative to the top-left of this Pane.
     * @param my Mouse Y, relative to the top-left of this Pane.
     * @return a Pane that will handle a click action.
     */
    @Override
    public Pane findPaneForClick(final double mx, final double my)
    {
        if (children.isEmpty() || listElementHeight == 0)
        {
            return null;
        }

        final int listElement = (int) my / listElementHeight;
        if (listElement < children.size())
        {
            final Pane child = children.get(listElement);
            if (child.canHandleClick(mx, my))
            {
                return child;
            }
        }

        return null;
    }
}
