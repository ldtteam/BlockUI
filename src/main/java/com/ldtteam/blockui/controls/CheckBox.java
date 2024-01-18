package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.PaneParams;
import net.minecraft.network.chat.Component;

public class CheckBox extends Button
{
    private boolean checked = false;

    public CheckBox(final PaneParams params)
    {
        super(params);
    }

    public boolean isChecked()
    {
        return checked;
    }

    public void setChecked(final boolean checked)
    {
        this.checked = checked;
        setText(Component.literal(checked ? "Yes" : "No"));
    }
}
