package com.ldtteam.blockui.views;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.views.ScrollingListContainer.RowSizeModifier;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.function.IntSupplier;

import static com.ldtteam.blockui.controls.AbstractTextElement.DEFAULT_TEXT_COLOR;
import static com.ldtteam.blockui.controls.AbstractTextElement.DEFAULT_TEXT_SCALE;

/**
 * A ScrollingList is a View which can contain 0 or more children of a specific Pane or View type
 * and are ordered sequentially.
 * <p>
 * All children are set to a Top version of their alignment, and have their Y coordinates overwritten.
 */
public class ScrollingList extends ScrollingView
{
    protected int childSpacing = 0;

    // Runtime
    protected DataProvider dataProvider;

    private int maxHeight;

    /**
     * Default constructor required by Blockout.
     */
    public ScrollingList()
    {
        super();
    }

    /**
     * Constructs a ScrollingList from PaneParams.
     *
     * @param params Params for the ScrollingList
     */
    public ScrollingList(final PaneParams params)
    {
        super(params);
        childSpacing = params.getInteger("childspacing", childSpacing);
        this.setMaxHeight(height);

        setEmptyTextColor(params.getColor("emptycolor", DEFAULT_TEXT_COLOR));
        setEmptyText(params.getMultilineText("emptytext"));
        setEmptyTextScale(params.getDouble("emptyscale", DEFAULT_TEXT_SCALE));
    }

    /**
     * Set the text shown when there are no items in the data provider.
     *
     * @param emptyText the component.
     */
    public void setEmptyText(final MutableComponent emptyText)
    {
        setEmptyText(List.of(emptyText));
    }

    /**
     * Set the text shown when there are no items in the data provider.
     *
     * @param emptyText the list of components.
     */
    public void setEmptyText(final List<MutableComponent> emptyText)
    {
        ((ScrollingListContainer) container).setEmptyText(emptyText);
    }

    /**
     * Set the text color for the empty text.
     *
     * @param emptyTextColor the color.
     */
    public void setEmptyTextColor(final int emptyTextColor)
    {
        ((ScrollingListContainer) container).setEmptyTextColor(emptyTextColor);
    }

    /**
     * Set the text scale for the empty text.
     *
     * @param emptyTextScale the text scale.
     */
    public void setEmptyTextScale(final double emptyTextScale)
    {
        ((ScrollingListContainer) container).setEmptyTextScale(emptyTextScale);
    }

    /**
     * Max height setter.
     *
     * @param maxHeight the height to set.
     */
    public void setMaxHeight(final int maxHeight)
    {
        this.maxHeight = maxHeight;
    }

    public void setDataProvider(final IntSupplier countSupplier, final IPaneUpdater paneUpdater)
    {
        setDataProvider(new DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return countSupplier.getAsInt();
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                paneUpdater.apply(index, rowPane);
            }
        });
    }

    public void setDataProvider(final DataProvider p)
    {
        dataProvider = p;
        refreshElementPanes();
    }

    /**
     * Use the data provider to update all the element panes.
     */
    public void refreshElementPanes()
    {
        ((ScrollingListContainer) container).refreshElementPanes(dataProvider, maxHeight, childSpacing);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        refreshElementPanes();
    }

    @Override
    protected ScrollingContainer createScrollingContainer()
    {
        return new ScrollingListContainer(this);
    }

    @Override
    public void parseChildren(final PaneParams params)
    {
        final List<PaneParams> childNodes = params.getChildren();
        if (childNodes.isEmpty())
        {
            return;
        }

        // Get the PaneParams for this child, because we'll need it in the future
        // to create more nodes
        if (container instanceof final ScrollingListContainer scrollingListContainer)
        {
            scrollingListContainer.setListNodeParams(childNodes.get(0));
        }
    }

    /**
     * Get the element list index for the provided pane.
     *
     * @param pane the pane to find the index of.
     * @return the index.
     */
    public int getListElementIndexByPane(final Pane pane)
    {
        return ((ScrollingListContainer) container).getListElementIndexByPane(pane);
    }

    /**
     * Interface for a data provider that updates pane scrolling list pane info.
     */
    public interface DataProvider
    {
        /**
         * Override this to provide the number of rows.
         *
         * @return number of rows in the list
         */
        int getElementCount();

        /**
         * Override this to pick a custom size for this element. Event contains the logic to modify the old size.
         *
         * @param index    the index of the row/list element.
         * @param modifier the object used to modify the size.
         */
        default void modifyRowSize(int index, final RowSizeModifier modifier)
        {
            // No implementation by default
        }

        /**
         * Override this to update the Panes for a given row.
         *
         * @param index   the index of the row/list element
         * @param rowPane the parent Pane for the row, containing the elements to update
         */
        void updateElement(int index, Pane rowPane);
    }

    @FunctionalInterface
    public interface IPaneUpdater
    {
        /**
         * Called to update a single pane of the given index with data.
         *
         * @param index   The index to update.
         * @param rowPane The pane to fill.
         */
        void apply(int index, Pane rowPane);
    }
}
