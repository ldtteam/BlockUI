package com.ldtteam.blockui.context;

public enum DelayedLevel
{
    // we are given 10k z levels (using forge gui stack)
    // keep using real z values (although this is used as cost for prior queue)
    /**
     * Just to make enum full, renders immediately
     */
    NONE(0),
    /**
     * For hovering elements, which may contain anything like normal gui
     */
    HOVER(1000),
    /**
     * For tooltip hovers, which may containg anything but tooltip
     */
    TOOLTIP(2000);

    public final int zLevel;

    private DelayedLevel(final int zLevel)
    {
        this.zLevel = zLevel;
    }
}
