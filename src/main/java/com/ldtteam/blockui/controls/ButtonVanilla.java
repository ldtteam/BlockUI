package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.PaneParams;

/**
 * BlockOut implementation of a Vanilla Button.
 * 
 * @deprecated use pure {@link ButtonImage}, it's defaulted to vanilla
 */
@Deprecated(forRemoval = true, since = "1.20.2")
public class ButtonVanilla extends ButtonImage
{
    /**
     * Default constructor.
     */
    public ButtonVanilla()
    {
        super();
    }

    /**
     * Constructor called when loaded from xml.
     *
     * @param params PaneParams from xml file.
     */
    public ButtonVanilla(final PaneParams params)
    {
        super(params);
    }
}
