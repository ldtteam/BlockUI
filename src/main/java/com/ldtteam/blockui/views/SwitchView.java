package com.ldtteam.blockui.views;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Mth;

/**
 * Tabbed view.
 */
public class SwitchView extends View
{
    @Nullable
    private Pane currentView;
    protected boolean endlessScrolling = false;

    /**
     * Required default constructor.
     */
    public SwitchView()
    {
        super();
    }

    /**
     * Constructs a View from PaneParams.
     *
     * @param params Params for the Pane.
     */
    public SwitchView(final PaneParams params)
    {
        super(params);
    }

    @Override
    public void parseChildren(final PaneParams params)
    {
        super.parseChildren(params);

        final String defaultView = params.getString("default");
        if (defaultView != null)
        {
            setView(defaultView);
        }
        this.endlessScrolling = params.getBoolean("endless", endlessScrolling);
    }

    /**
     * Switch current view to view with id given as param.
     *
     * @param name id of view
     * @return true if view of given name was found, else false
     */
    public boolean setView(final String name)
    {
        // Immediate children only
        for (final Pane child : children)
        {
            if (child.getID().equals(name))
            {
                setCurrentView(child);
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public Pane findPaneForClick(final double mx, final double my)
    {
        if (currentView != null && currentView.canHandleClick(mx, my))
        {
            return currentView;
        }

        return null;
    }

    @Override
    protected boolean childIsVisible(final Pane child)
    {
        return child == currentView && super.childIsVisible(child);
    }

    @Override
    public void addChild(final Pane child)
    {
        super.addChild(child);
        if (children.size() == 1)
        {
            currentView = child;
            child.setVisible(true);
        }
        else
        {
            child.setVisible(false);
        }
    }

    @Override
    public void adjustChild(final Pane child)
    {
        if (child.getWidth() == 0 || child.getHeight() == 0)
        {
            child.setSize(width - child.getX(), height - child.getY());
        }

        super.adjustChild(child);
    }

    @Override
    public void removeChild(final Pane child)
    {
        super.removeChild(child);
        if (child == currentView)
        {
            if (children.isEmpty())
            {
                currentView = null;
            }
            else
            {
                currentView = children.get(0);
                currentView.setVisible(true);
            }
        }
    }

    @Nullable
    public Pane getCurrentView()
    {
        return currentView;
    }

    private void setCurrentView(final Pane pane)
    {
        if (currentView != null)
        {
            currentView.setVisible(false);
        }
        currentView = pane;
        currentView.setVisible(true);
    }

    /**
     * Get the next tab/view.
     */
    public void nextView()
    {
        setView(true, 1);
    }

    /**
     * Get the last tab/view.
     */
    public void previousView()
    {
        setView(true, -1);
    }

    /**
     * Generic shift method, abstract version of {@link #previousView()} and {@link #nextView()}.
     * New index is clamped between zero and <code>children.size()</code>
     *
     * @param relative whether page param is relative or absolute
     * @param shift    if relative go x views forward/backward, if absolute go to x-th view
     * @return new page index, -1 if empty
     */
    public int setView(final boolean relative, final int shift)
    {
        if (children.isEmpty())
        {
            return -1;
        }

        int newIndex = relative ? children.indexOf(currentView) + shift : shift;
        newIndex = endlessScrolling ? Math.floorMod(newIndex, getChildrenSize()) : newIndex;
        newIndex = Mth.clamp(newIndex, 0, getChildrenSize() - 1);

        setCurrentView(children.get(newIndex));

        return newIndex;
    }

    /**
     * Get amount of views.
     */
    public int getChildrenSize()
    {
        return children.size();
    }

    /**
     * @return true if {@link #setView(boolean, int)} connects first and last view
     */
    public boolean isEndlessScrollingEnabled()
    {
        return endlessScrolling;
    }

    /**
     * @param endlessScrolling true if {@link #setView(boolean, int)} should connect first and last view
     */
    public void setEndlessScrolling(final boolean endlessScrolling)
    {
        this.endlessScrolling = endlessScrolling;
    }
}
