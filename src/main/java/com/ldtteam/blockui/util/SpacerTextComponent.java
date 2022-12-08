package com.ldtteam.blockui.util;

import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

/**
 * Allows to add customsized spacer into text. Works ONLY in OUR text elements.
 */
public record SpacerTextComponent(int pixelHeight) implements ComponentContents
{
    public static MutableComponent of(final int pixelHeight)
    {
        return MutableComponent.create(new SpacerTextComponent(pixelHeight));
    }

    public FormattedCharSequence getVisualOrderText()
    {
        return new FormattedSpacerComponent(pixelHeight);
    }

    public record FormattedSpacerComponent(int pixelHeight) implements FormattedCharSequence
    {
        @Override
        public boolean accept(final FormattedCharSink p_13732_)
        {
            return true;
        }
    }
}
