package com.ldtteam.blockui.parse;

public interface PaneBiApplier<T, U>
{
    void accept(T pane, U first, U second);

    public interface PaneApplier<T, U>
    {
        void accept(T pane, U value);
    }

    public interface ShadowPaneAccessor<T, U>
    {
        U accept(T pane);
    }
}
