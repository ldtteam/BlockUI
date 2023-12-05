package com.ldtteam.blockui.util;

import com.ldtteam.blockui.mod.BlockUI;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import java.util.function.BooleanSupplier;

/**
 * Allows to add toggleable component into text. Works ONLY in OUR text elements.
 */
public record ToggleableTextComponent(BooleanSupplier condition, MutableComponent data) implements ComponentContents
{
    private static final MapCodec<ToggleableTextComponent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
        .group(ComponentSerialization.CODEC.fieldOf("data").forGetter(ToggleableTextComponent::data),
            Codec.BOOL.fieldOf("condition").forGetter(comp -> comp.condition().getAsBoolean()))
        .apply(instance, (data, conditionValue) -> new ToggleableTextComponent(() -> conditionValue, (MutableComponent) data)));
    public static final ComponentContents.Type<ToggleableTextComponent> TYPE = new ComponentContents.Type<>(CODEC, BlockUI.MOD_ID + "_toggle");

    /**
     * @param condition if contidition returns true then data will get rendered
     * @param data what to render when condition returns true
     */
    public static MutableComponent of(final BooleanSupplier condition, final MutableComponent data)
    {
        return MutableComponent.create(new ToggleableTextComponent(condition, data));
    }

    /**
     * @param negatedCondition if contidition returns false then data will get rendered
     * @param data what to render when condition returns false
     */
    public static MutableComponent ofNegated(final BooleanSupplier negatedCondition, final MutableComponent data)
    {
        return MutableComponent.create(new ToggleableTextComponent(() -> !negatedCondition.getAsBoolean(), data));
    }

    public FormattedCharSequence getVisualOrderText()
    {
        return new FormattedToggleableCharSequence(condition, data.getVisualOrderText());
    }

    @Override
    public Type<?> type()
    {
        return TYPE;
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
