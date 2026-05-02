package com.ldtteam.blockui.views;

import com.ldtteam.blockui.PaneParams;
import net.minecraft.client.input.KeyEvent;

/**
 * An OverlayView is a full screen View which is displayed on top of the window.
 */
public class OverlayView extends View
{
    /**
     * Constructs a barebones View.
     */
    public OverlayView()
    {
        super();
    }

    /**
     * Constructs a OverlayView from PaneParams.
     *
     * @param params Params for the View.
     */
    public OverlayView(final PaneParams params)
    {
        super(params);
    }

    /**
     * hide the view when click on.
     */
    @Override
    public boolean click(final double mx, final double my)
    {
        if (super.click(mx, my))
        {
            return true;
        }

        hide();
        return false;
    }

    /**
     * hide the view when click on.
     */
    @Override
    public boolean rightClick(final double mx, final double my)
    {
        if (super.rightClick(mx, my))
        {
            return true;
        }

        hide();
        return false;
    }

    /**
     * Called when a key is pressed.
     * hide the view when ESC is pressed.
     *
     * @return false at all times - do nothing.
     */
    @Override
    public boolean onKeyEvent(final KeyEvent event)
    {
        if (isVisible() && event.isEscape())
        {
            setVisible(false);
            return true;
        }

        return super.onKeyEvent(event);
    }
}
