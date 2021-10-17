package com.ldtteam.blockui.views;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.controls.Scrollbar;
import com.ldtteam.blockui.util.records.Pos2i.MutablePos2i;

/**
 * Basic scrolling view.
 */
public class ScrollingView extends View
{
    private static final int DEFAULT_SCROLLBAR_WIDTH = 8;
    // Runtime
    protected ScrollingContainer container;
    protected Scrollbar scrollbar;
    // Params
    private final int scrollbarWidth = DEFAULT_SCROLLBAR_WIDTH;

    /**
     * Required default constructor.
     */
    public ScrollingView()
    {
        super();
        setup(null);
    }

    /**
     * Load from xml.
     *
     * @param params xml parameters.
     */
    public ScrollingView(final PaneParams params)
    {
        super(params);
        setup(params);
    }

    private void setup(final PaneParams params)
    {
        container = createScrollingContainer();
        container.setPosition(0, 0);
        container.setSize(getInteriorWidth() - scrollbarWidth, getInteriorHeight());
        container.putInside(this);

        if (params != null)
        {
            scrollbar = new Scrollbar(container, params);
        }
        else
        {
            scrollbar = new Scrollbar(container);
        }
        scrollbar.setPosition(getInteriorWidth() - scrollbarWidth, 0);
        scrollbar.setSize(scrollbarWidth, getInteriorHeight());
        scrollbar.putInside(this);
    }

        protected ScrollingContainer createScrollingContainer()
    {
        return new ScrollingContainer(this);
    }

    @Override
    public void setSize(final int w, final int h)
    {
        super.setSize(w, h);
        container.setSize(
            getInteriorWidth() - scrollbarWidth + (scrollbar.getScrollOffsetX() > scrollbarWidth ? scrollbarWidth : scrollbar.getScrollOffsetX()),
            getInteriorHeight());
        scrollbar.setPosition(getInteriorWidth() - scrollbarWidth, 0);
        scrollbar.setSize(scrollbarWidth, getInteriorHeight());
    }

    @Override
    public boolean scrollInput(final double wheel, final double mx, final double my)
    {
        return setScrollY(getScrollY() - (int) wheel);
    }

    public ScrollingContainer getContainer()
    {
        return container;
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
    }

    /**
     * Optimized version of childIsVisible, because we only have two immediate children, which are guaranteed
     * to be visible: the ScrollingContainer and the Scrollbar.
     */
    @Override
    protected boolean childIsVisible(final Pane child)
    {
        return true;
    }

    public double getScrollY()
    {
        return container.getScrollY();
    }

    /**
     * @param scroll new scroll value [pixels]
     * @return true if scroll offset changed
     */
    public boolean setScrollY(final double scroll)
    {
        return container.setScrollY(scroll);
    }

    /**
     * get the height of the content.
     *
     * @return the height of the content.
     */
    public int getContentHeight()
    {
        return container.getContentHeight();
    }
}
