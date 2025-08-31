package com.ldtteam.blockui.util;

import com.ldtteam.blockui.AbstractPaneGroup;

/**
 * Generic mouse event callback interface
 */
@FunctionalInterface
public interface MouseEventCallback
{
    /**
     * @param  pane event acceptor
     * @param  mx   mouse x relative to parent top-left corner
     * @param  my   mouse y relative to parent top-left corner
     * @return      true if event was used or propagation needs to be stopped
     */
    boolean accept(AbstractPaneGroup pane, double mx, double my);
}
