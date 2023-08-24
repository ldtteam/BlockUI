package com.ldtteam.blockui.util;

import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import java.util.function.BooleanSupplier;

/**
 * Allows to add toggleable component into text. Works ONLY in OUR text elements.
 */
public record ToggleableTextComponent(BooleanSupplier condition, MutableComponent data) implements ComponentContents
{
    /**
     * @param condition if contidition returns true then data will get rendered
     * @param data what to render when condition returns true
     */
    public static MutableComponent of(final BooleanSupplier condition, final MutableComponent data)
    {
        return MutableComponent.create(new ToggleableTextComponent(condition, data));
    }

    public FormattedCharSequence getVisualOrderText()
    {
        return new FormattedToggleableCharSequence(condition, data.getVisualOrderText());
    }

    public record FormattedToggleableCharSequence(BooleanSupplier condition, FormattedCharSequence data) implements FormattedCharSequence
    {
        @Override
        public boolean accept(final FormattedCharSink p_13732_)
        {
            return true;
        }
    }
}