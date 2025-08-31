package com.ldtteam.blockui.context;

public enum DebugLevel
{
    // ordering implies hiearchy, ie. each level contains levels above itself
    NONE,
    /**
     * Display element borders
     */
    BORDER,
    /**
     * Display element IDs
     */
    ID,
    /**
     * Display shadow children
     */
    SHADOW;

    public boolean isEnabled(final DebugLevel requiredLevel)
    {
        return requiredLevel.ordinal() <= this.ordinal();
    }
}
