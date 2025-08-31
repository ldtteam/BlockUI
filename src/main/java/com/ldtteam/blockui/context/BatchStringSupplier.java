package com.ldtteam.blockui.context;

import net.minecraft.util.FormattedCharSequence;

@FunctionalInterface
public interface BatchStringSupplier
{
    void accept(RenderSink sink);

    @FunctionalInterface
    public interface RenderSink
    {
        void render(FormattedCharSequence text, int x, int y);
    }
}
