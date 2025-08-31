package com.ldtteam.blockui.context.gui;

import com.ldtteam.blockui.UiWindow;
import net.minecraft.client.gui.screens.Screen;

public class ScreenAdapter extends Screen
{
    public ScreenAdapter(final UiWindow window)
    {
        super(window.getTitle());
    }

    public void closeGui()
    {}

    public double getRenderScale()
    {
        return 0;
    }

    public double getVanillaGuiScale()
    {
        return 0;
    }

    public int getAbsoluteMouseX()
    {
        return 0;
    }

    public int getAbsoluteMouseY()
    {
        return 0;
    }
}
