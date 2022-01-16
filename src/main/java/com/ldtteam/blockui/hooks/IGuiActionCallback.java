package com.ldtteam.blockui.hooks;

import com.ldtteam.blockui.views.BOWindow;

/**
 * Callback for gui open/close action.
 */
@FunctionalInterface
public interface IGuiActionCallback<T>
{
    /**
     * Default impl with no action.
     */
    public static IGuiActionCallback<?> NO_ACTION = (t, w, tt) -> {};

    /**
     * @param thing       instance of Forge-registered type
     * @param window      window attached to instance above
     * @param triggerType trigger condition type
     */
    void onAction(final T thing, final BOWindow window, final TriggerMechanism triggerType);

    /**
     * @return default impl with no action
     */
    @SuppressWarnings("unchecked")
    public static <T> IGuiActionCallback<T> noAction()
    {
        return (IGuiActionCallback<T>) NO_ACTION;
    }
}
