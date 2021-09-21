package com.ldtteam.blockui.hooks;

import com.ldtteam.blockui.BOScreen;
import com.ldtteam.blockui.hooks.TriggerMechanism.Type;
import com.ldtteam.blockui.views.ScrollingList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;

/**
 * Screen wrapper.
 */
public class HookScreen extends BOScreen
{
    private final boolean captureScroll;
    private final HookWindow<?, ?> windowTyped;
    private ScrollingList scrollListener = null;

    HookScreen(final HookWindow<?, ?> window)
    {
        super(window);
        this.windowTyped = window;
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
        try
        {
            window.draw(ms, -1, -1);
            window.drawLast(ms, -1, -1);
        }
        catch (final Exception e)
        {
            final CrashReport crashReport = CrashReport.forThrowable(e, "Rendering Hook BO screen");
            final CrashReportCategory category = crashReport.addCategory("Hook BO screen rendering details");
            category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
            category.setDetail("Hook thing type", () -> windowTyped.getHookThingType().getRegistryName().toString());
            category.setDetail("Hook thing", () -> windowTyped.getHookThing().toString());
            throw new ReportedException(crashReport);
        }
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
            try
            {
                scrollListener.scrollInput(scrollDiff * 10, scrollListener.getX() + 1, scrollListener.getY() + 1);
            }
            catch (final Exception e)
            {
                final CrashReport crashReport = CrashReport.forThrowable(e, "MouseScroll event for Hook BO screen");
                final CrashReportCategory category = crashReport.addCategory("Hook BO screen scroll event details");
                category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
                category.setDetail("Scroll value", () -> Double.toString(scrollDiff));
                category.setDetail("Hook thing type", () -> windowTyped.getHookThingType().getRegistryName().toString());
                category.setDetail("Hook thing", () -> windowTyped.getHookThing().toString());
                throw new ReportedException(crashReport);
            }
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
        try
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
        catch (final Exception e)
        {
            final CrashReport crashReport = CrashReport.forThrowable(e, "Ticking/Updating Hook BO screen");
            final CrashReportCategory category = crashReport.addCategory("Hook BO screen update details");
            category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
            category.setDetail("Is opened", () -> Boolean.toString(isOpen));
            category.setDetail("Hook thing type", () -> windowTyped.getHookThingType().getRegistryName().toString());
            category.setDetail("Hook thing", () -> windowTyped.getHookThing().toString());
            throw new ReportedException(crashReport);
        }
    }

    @Override
    public void removed()
    {
        try
        {
            window.onClosed();
        }
        catch (final Exception e)
        {
            final CrashReport crashReport = CrashReport.forThrowable(e, "Closing Hook BO screen");
            final CrashReportCategory category = crashReport.addCategory("Hook BO screen closing details");
            category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
            category.setDetail("Is opened", () -> Boolean.toString(isOpen));
            throw new ReportedException(crashReport);
        }
        if (HookManager.getScrollListener() == this)
        {
            HookManager.setScrollListener(null);
        }
    }

    public HookWindow<?, ?> getWindow()
    {
        return windowTyped;
    }
}
