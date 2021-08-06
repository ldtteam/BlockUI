package com.ldtteam.blockui.hooks;

import com.ldtteam.blockui.Loader;
import com.ldtteam.blockui.views.BOWindow;

/**
 * Window wrapper
 */
public class HookWindow<T> extends BOWindow
{
    protected final HookManager<T, ?, ?>.WindowEntry windowHolder;

    HookWindow(final HookManager<T, ?, ?>.WindowEntry windowHolder)
    {
        super();
        Loader.createFromXMLFile(windowHolder.hook.guiLoc, this);

        this.windowHolder = windowHolder;
        screen = new HookScreen(this);
    }

    @Override
    public void onOpened()
    {
        windowHolder.hook.onOpen.onAction(windowHolder.thing, this, windowHolder.hook.trigger.getType());
    }

    @Override
    public void onClosed()
    {
        windowHolder.hook.onClose.onAction(windowHolder.thing, this, windowHolder.hook.trigger.getType());
    }

    public T getHookThing()
    {
        return windowHolder.thing;
    }

    @Override
    public HookScreen getScreen()
    {
        return (HookScreen) super.getScreen();
    }

    @Override
    public WindowRenderType getRenderType()
    {
        return WindowRenderType.FIXED;
    }

    @Override
    @Deprecated
    public void open()
    {
        // noop
    }

    @Override
    @Deprecated
    public void close()
    {
        // noop
    }

    @Override
    public boolean doesWindowPauseGame()
    {
        return false;
    }
}
