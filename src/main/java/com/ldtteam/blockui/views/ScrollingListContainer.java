package com.ldtteam.blockui.views;

import com.ldtteam.blockui.Loader;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.util.SafeError;
import com.ldtteam.blockui.util.records.SizeI;
import com.ldtteam.blockui.views.ScrollingList.DataProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A Blockout pane that contains a scrolling line of other panes.
 */
public class ScrollingListContainer extends ScrollingContainer
{
    /**
     * The xml parameters for the row panes.
     */
    @Nullable
    private PaneParams listNodeParams;

    /**
     * The template size for the rows.
     */
    private SizeI templateSize = new SizeI(0, 0);

    ScrollingListContainer(final ScrollingList owner)
    {
        super(owner);
    }

    /**
     * Set the new xml parameters for the row panes.
     *
     * @param listNodeParams the new xml parameters.
     */
    public void setListNodeParams(final @NotNull PaneParams listNodeParams)
    {
        this.listNodeParams = listNodeParams;

        final Pane template = Loader.createFromPaneParams(listNodeParams, null);
        if (template == null)
        {
            SafeError.throwInDev(new IllegalStateException("Scrolling list template could not be loaded. Is there a reference to another layout in the list children?"));
            return;
        }

        this.templateSize = new SizeI(template.getWidth(), template.getHeight());
    }

    /**
     * Creates, deletes, and updates existing Panes for elements in the list based on the DataProvider.
     *
     * @param dataProvider data provider object, shouldn't be null.
     * @param height       the maximum height of the parent.
     * @param childSpacing the spacing between each row.
     */
    public void refreshElementPanes(final DataProvider dataProvider, final int height, final int childSpacing)
    {
        int currentYpos = 0;

        if (listNodeParams == null)
        {
            SafeError.throwInDev(new IllegalStateException("Template size is not defined. Does the scrolling list have a child?"));
            return;
        }

        final int numElements = (dataProvider != null) ? dataProvider.getElementCount() : 0;
        if (numElements > 0)
        {
            final RowSizeModifier modifier = new RowSizeModifier();

            for (int i = 0; i < numElements; ++i)
            {
                modifier.reset(templateSize.width(), templateSize.height());
                dataProvider.modifyRowSize(i, modifier);

                final int elementHeight = modifier.height;
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
                    if (modifier.modified)
                    {
                        child.setSize(modifier.width, modifier.height);
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
     * Class for modifying row item size.
     */
    public static class RowSizeModifier
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
         * Resets the modifier back to initial state.
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
