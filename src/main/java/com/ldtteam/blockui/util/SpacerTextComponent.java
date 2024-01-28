package com.ldtteam.blockui.util;

import com.ldtteam.blockui.mod.BlockUI;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

/**
 * Allows to add customsized spacer into text. Works ONLY in OUR text elements.
 */
public record SpacerTextComponent(int pixelHeight) implements ComponentContents
{
    private static final MapCodec<SpacerTextComponent> CODEC = RecordCodecBuilder
        .mapCodec(instance -> instance.group(Codec.INT.fieldOf("pixelHeight").forGetter(SpacerTextComponent::pixelHeight))
            .apply(instance, SpacerTextComponent::new));
    public static final ComponentContents.Type<SpacerTextComponent> TYPE = new ComponentContents.Type<>(CODEC, BlockUI.MOD_ID + "_spacer");

    public static MutableComponent of(final int pixelHeight)
    {
        return MutableComponent.create(new SpacerTextComponent(pixelHeight));
    }

    public FormattedCharSequence getVisualOrderText()
    {
        return new FormattedSpacerComponent(pixelHeight);
    }

    @Override
    public Type<?> type()
    {
        return TYPE;
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
