package com.ldtteam.blockui.context;

import com.ldtteam.blockui.context.gui.GuiGraphicsRenderContext;
import java.util.function.Consumer;

public final class RenderContextType
{
    public static final GuiGraphicsRenderContext GUI = new GuiGraphicsRenderContext();
    public static final GuiGraphicsRenderContext OVERLAY = new GuiGraphicsRenderContext();
    public static final RenderContext WORLD_HOOK = new GuiGraphicsRenderContext();

    private static final RenderContext[] ALL = {GUI, OVERLAY, WORLD_HOOK};

    public static final void applyToAll(final Consumer<RenderContext> fnc)
    {
        for (final RenderContext ctx : ALL)
        {
            fnc.accept(ctx);
        }
    }
}
