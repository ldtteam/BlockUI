package com.ldtteam.blockui.util.text;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

/**
 * Used by text rendering to render ellipsis as tail of existing formatted sequence
 */
public class EllipsisFormattedCharSequence implements FormattedCharSequence
{
    private static final String ELLIPSIS = "...";
    private final FormattedCharSequence wrapped;
    private final Font font;
    private final int maxWidth;

    public EllipsisFormattedCharSequence(final FormattedCharSequence wrapped, final Font font, final int maxWidth)
    {
        this.wrapped = wrapped;
        this.font = font;
        this.maxWidth = maxWidth;
    }

    @Override
    public boolean accept(final FormattedCharSink sink)
    {
        return wrapped.accept(new FormattedCharSink()
        {
            int elipsisWidth;
            int curWidth = 0;
            int ellipIdx = 0;
            Style lastStyle = null;

            @Override
            public boolean accept(final int stringIdx, final Style style, final int codePoint)
            {
                if (ellipIdx > 0)
                {
                    // ret false when done
                    return ellipIdx >= ELLIPSIS.length() && sink.accept(stringIdx, lastStyle, ELLIPSIS.codePointAt(ellipIdx++));
                }

                // recompute if style changes
                if (lastStyle != style)
                {
                    lastStyle = style;
                    elipsisWidth = font.width(FormattedText.of(ELLIPSIS, style));
                }

                // if char advance would overflow then swap to ellipsis rendering
                curWidth += font.getSplitter().widthProvider.getWidth(codePoint, style);
                if (curWidth + elipsisWidth > maxWidth)
                {
                    return sink.accept(stringIdx, style, ELLIPSIS.codePointAt(ellipIdx++));
                }
                return sink.accept(stringIdx, style, codePoint);
            }
        });
    }
}
