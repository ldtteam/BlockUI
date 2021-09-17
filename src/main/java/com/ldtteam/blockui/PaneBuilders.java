package com.ldtteam.blockui;

import com.ldtteam.blockui.controls.AbstractTextBuilder.TextBuilder;
import com.ldtteam.blockui.controls.AbstractTextBuilder.TooltipBuilder;
import com.ldtteam.blockui.controls.Tooltip;

import net.minecraft.network.chat.MutableComponent;

public final class PaneBuilders
{
    private PaneBuilders()
    {
        // utility class
    }

    /**
     * Tooltip element builder.
     * Don't forget to set hoverPane.
     *
     * @see TooltipBuilder#hoverPane(Pane)
     * @see #singleLineTooltip(MutableComponent, Pane)
     */
    public static TooltipBuilder tooltipBuilder()
    {
        return new TooltipBuilder();
    }

    /**
     * Text element builder.
     */
    public static TextBuilder textBuilder()
    {
        return new TextBuilder();
    }

    /**
     * Macro build method for single-line tooltips.
     *
     * @param text tooltip text
     * @param hoverPane tooltip parent pane
     * @return tooltip element
     */
    public static Tooltip singleLineTooltip(final MutableComponent text, final Pane hoverPane)
    {
        return tooltipBuilder().append(text).hoverPane(hoverPane).build();
    }
}
