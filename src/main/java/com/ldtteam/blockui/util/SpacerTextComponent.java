package com.ldtteam.blockui.util;

import net.minecraft.network.chat.BaseComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

/**
 * Allows to add customsized spacer into text. Works ONLY in OUR text elements.
 */
public class SpacerTextComponent extends BaseComponent
{
    private final int pixelHeight;

    public SpacerTextComponent(final int pixelHeight)
    {
        this.pixelHeight = pixelHeight;
    }

    public int getPixelHeight()
    {
        return pixelHeight;
    }

    @Override
    public BaseComponent plainCopy()
    {
        return new SpacerTextComponent(pixelHeight);
    }

    @Override
    public FormattedCharSequence getVisualOrderText()
    {
        return new FormattedSpacerComponent(pixelHeight);
    }

    public class FormattedSpacerComponent implements FormattedCharSequence
    {
        private final int pixelHeight;

        private FormattedSpacerComponent(final int pixelHeight)
        {
            this.pixelHeight = pixelHeight;
        }

        public int getPixelHeight()
        {
            return pixelHeight;
        }

        @Override
        public boolean accept(final FormattedCharSink p_13732_)
        {
            return true;
        }
    }
}
