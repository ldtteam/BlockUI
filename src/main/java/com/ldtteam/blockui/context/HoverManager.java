package com.ldtteam.blockui.context;

import com.ldtteam.blockui.AbstractPane;
import org.jetbrains.annotations.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Predicate;

public class HoverManager
{
    private final Map<AbstractPane, HoverInfo> registeredHovers = new IdentityHashMap<>();

    public void tick()
    {
        registeredHovers.forEach((source, info) -> {
            // start (not open && cursor)
            if (!info.open && source.wasCursorInPaneLastFrame())
            {
                info.runHoverStartAction();
            }

            // stop (open && cursor)
            if (info.open && !source.wasCursorInPaneLastFrame())
            {
                // ask ticker if want to survive
                if (!info.runHoverTickAction(source))
                {
                    info.runHoverStopAction();
                }
            }
        });
    }

    public void add(final AbstractPane source,
        final Runnable onHoverStartAction,
        final Predicate<AbstractPane> onHoverTickAction,
        final Runnable onHoverStopAction)
    {
        final HoverInfo info = new HoverInfo(onHoverStartAction, onHoverTickAction, onHoverStopAction);
        registeredHovers.put(source, info);
        info.runHoverStopAction(); // move hover target to valid stop state
    }

    public void remove(final AbstractPane source)
    {
        final HoverInfo removed = registeredHovers.remove(source);
        if (removed != null && removed.open)
        {
            removed.runHoverStopAction();
        }
    }

    private static class HoverInfo
    {
        @Nullable
        private final Runnable onHoverStartAction;
        @Nullable
        private final Predicate<AbstractPane> onHoverTickAction;
        @Nullable
        private final Runnable onHoverStopAction;

        private boolean open = false;

        private HoverInfo(final Runnable onHoverStartAction,
            final Predicate<AbstractPane> onHoverTickAction,
            final Runnable onHoverStopAction)
        {
            this.onHoverStartAction = onHoverStartAction;
            this.onHoverTickAction = onHoverTickAction;
            this.onHoverStopAction = onHoverStopAction;
        }

        private void runHoverStartAction()
        {
            open = true;
            if (onHoverStartAction != null)
            {
                onHoverStartAction.run();
            }
        }

        private boolean runHoverTickAction(final AbstractPane source)
        {
            if (onHoverTickAction != null)
            {
                return onHoverTickAction.test(source);
            }
            return false;
        }

        private void runHoverStopAction()
        {
            if (onHoverStopAction != null)
            {
                onHoverStopAction.run();
            }
            open = false;
        }
    }
}
