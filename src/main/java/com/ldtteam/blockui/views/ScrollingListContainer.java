package com.ldtteam.blockui.views;

import com.ldtteam.blockui.Loader;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.controls.CheckBox;
import com.ldtteam.blockui.support.DataProviders.DataProvider;
import com.ldtteam.blockui.util.records.SizeI;
import com.ldtteam.blockui.support.DataProviders.CheckListDataProvider;
import org.jetbrains.annotations.Nullable;

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
    public void refreshElementPanes(final DataProvider dataProvider, final PaneParams listNodeParams, final int height, final int childSpacing)
    {
        int currentYpos = 0;

        final int numElements = (dataProvider != null) ? dataProvider.getElementCount() : 0;
        if (dataProvider != null)
        {
            for (int i = 0; i < numElements; ++i)
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

                final @Nullable SizeI customElementSize = dataProvider.getElementSize(i, child);
                if (customElementSize != null)
                {
                    child.setSize(customElementSize.width(), customElementSize.height());
                }

                if (currentYpos + child.getHeight() >= scrollY && currentYpos <= scrollY + height)
                {
                    dataProvider.updateElement(i, child);
                }

                if (dataProvider instanceof CheckListDataProvider checkListDataProvider)
                {
                    final CheckBox checkbox = child.findPaneOfTypeByID(checkListDataProvider.getCheckboxId(), CheckBox.class);
                    checkbox.setChecked(checkListDataProvider.isChecked(i));

                    final int index = i;
                    checkbox.setHandler(button -> checkListDataProvider.setChecked(index, !checkListDataProvider.isChecked(index)));
                }

                currentYpos += child.getHeight() + childSpacing;
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
}
