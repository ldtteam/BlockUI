package com.ldtteam.blockui.hooks;

import com.ldtteam.blockui.Loader;
import com.ldtteam.blockui.views.BOWindow;

import net.minecraft.resources.ResourceLocation;

import com.ldtteam.blockui.views.BOWindow.WindowRenderType;

/**
 * Window wrapper
 */
public class HookWindow<T, U> extends BOWindow
{
    protected final HookManager<T, U, ?>.WindowEntry windowHolder;

    HookWindow(final HookManager<T, U, ?>.WindowEntry windowHolder)
    {
        super();
        Loader.createFromXMLFile(windowHolder.hook.guiLoc, this);

        this.windowHolder = windowHolder;
        screen = new HookScreen(this);
    }

    @Override
    public void onOpened()
    {
        windowHolder.hook.onOpen.onAction(windowHolder.thing, this, windowHolder.hook.trigger);
    }

    @Override
    public void onClosed()
    {
        windowHolder.hook.onClose.onAction(windowHolder.thing, this, windowHolder.hook.trigger);
    }

    public T getHookThing()
    {
        return windowHolder.thing;
    }

    public ResourceLocation getHookThingRegistryKey()
    {
        return windowHolder.hook.getTargetThingRegistryKey();
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
