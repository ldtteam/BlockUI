package com.ldtteam.blockui.hooks;

import com.ldtteam.blockui.BOScreen;
import com.ldtteam.blockui.hooks.TriggerMechanism.Type;
import com.ldtteam.blockui.views.ScrollingList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Screen wrapper.
 */
public class HookScreen extends BOScreen
{
    private final boolean captureScroll;
    private ScrollingList scrollListener = null;

    HookScreen(final HookWindow<?> window)
    {
        super(window);
        captureScroll = window.windowHolder.hook.trigger.getType() == Type.RAY_TRACE;
    }

    @Override
    @Deprecated
    public void render(final PoseStack ms, final int mx, final int my, final float f)
    {
        render(ms);
    }

    public void render(final PoseStack ms)
    {
        if (minecraft == null || !isOpen) // should never happen though
        {
            return;
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        ms.translate(-width / 2, -height, 0.0d);
        window.draw(ms, -1, -1);
    }

    @Override
    @Deprecated
    public boolean mouseScrolled(final double mx, final double my, final double scrollDiff)
    {
        return mouseScrolled(scrollDiff);
    }

    public boolean mouseScrolled(final double scrollDiff)
    {
        if (captureScroll && scrollListener != null && scrollDiff != 0)
        {
            scrollListener.scrollInput(scrollDiff * 10, scrollListener.getX() + 1, scrollListener.getY() + 1);
            return true; // TODO: would be nice to not stop event propagation when scrolling list is not scrollable
        }
        return false;
    }

    @Override
    public void init()
    {
        // noop
    }

    @Override
    public void tick()
    {
        if (minecraft != null)
        {
            if (!isOpen)
            {
                if (captureScroll)
                {
                    scrollListener = window.findFirstPaneByType(ScrollingList.class);
                    if (scrollListener != null)
                    {
                        HookManager.setScrollListener(this);
                    }
                }
                window.onOpened();
                isOpen = true;
            }
            else
            {
                window.onUpdate();
            }
        }
    }

    @Override
    public void removed()
    {
        window.onClosed();
        if (HookManager.getScrollListener() == this)
        {
            HookManager.setScrollListener(null);
        }
    }

    public HookWindow<?> getWindow()
    {
        return (HookWindow<?>) window;
    }
}
