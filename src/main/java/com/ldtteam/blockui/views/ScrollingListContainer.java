package com.ldtteam.blockui.views;

import com.ldtteam.blockui.Loader;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.mod.Log;

/**
 * A Blockout pane that contains a scrolling line of other panes.
 */
public class ScrollingListContainer extends ScrollingContainer
{
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
        int currentYpos = 0;

        final Pane template = Loader.createFromPaneParams(listNodeParams, null);
        if (template == null)
        {
            Log.getLogger().error("Scrolling list template could not be loaded. Is there a reference to another layout in the list children?");
            return;
        }

        final EventMutableSizeI event = new EventMutableSizeI();

        final int numElements = (dataProvider != null) ? dataProvider.getElementCount() : 0;
        if (dataProvider != null)
        {
            for (int i = 0; i < numElements; ++i)
            {
                event.reset(template.getWidth(), template.getHeight());
                dataProvider.getElementSize(i, event);

                final int elementHeight = event.height;
                if (currentYpos + elementHeight >= scrollY && currentYpos <= scrollY + height)
                {
                    final Pane child;
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
                    }

                    child.setPosition(0, currentYpos);
                    if (event.modified)
                    {
                        child.setSize(event.width, event.height);
                    }

                    dataProvider.updateElement(i, child);
                }

                currentYpos += elementHeight + childSpacing;
            }
        }

        while (children.size() > numElements)
        {
            removeChild(children.get(numElements));
        }

        setContentHeight(currentYpos - childSpacing);
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
     * Event class for modifying row item size.
     */
    public static class EventMutableSizeI
    {
        /**
         * The width for the row item.
         */
        private int width;

        /**
         * The height for the row item.
         */
        private int height;

        /**
         * Whether the width/height was modified.
         */
        private boolean modified;

        /**
         * Get the current width of the row item.
         *
         * @return the width.
         */
        public int getWidth()
        {
            return width;
        }

        /**
         * Set a new width for this row item.
         *
         * @param width the new width.
         */
        public void setWidth(int width)
        {
            setSize(width, this.height);
        }

        /**
         * Get the current height of the row item.
         *
         * @return the height.
         */
        public int getHeight()
        {
            return height;
        }

        /**
         * Set a new height for this row item.
         *
         * @param height the new height.
         */
        public void setHeight(int height)
        {
            setSize(this.width, height);
        }

        /**
         * Adjust the existing size by a given offset.
         *
         * @param diffW the difference in width.
         * @param diffH the difference in height.
         */
        public void adjustSize(int diffW, int diffH)
        {
            setSize(this.width + diffW, this.height + diffH);
        }

        /**
         * Set a new width and height for this row item.
         *
         * @param height the new height.
         */
        public void setSize(int width, int height)
        {
            this.width = width;
            this.height = height;
            this.modified = true;
        }

        /**
         * Resets the event back to initial state.
         *
         * @param width  the initial width.
         * @param height the initial height.
         */
        void reset(final int width, final int height)
        {
            this.width = width;
            this.height = height;
            this.modified = false;
        }
    }
}
