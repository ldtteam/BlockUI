package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.ScrollingList.CheckListDataProvider;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sets up gui for check lists.
 */
public class CheckListGui
{
    public static void setup(final BOWindow window)
    {
        final List<Boolean> booleans = new ArrayList<>(List.of(false, false, false, false, false, false, false, false, false, false));

        final ScrollingList list1 = window.findPaneOfTypeByID("list1", ScrollingList.class);
        list1.setDataProvider(new CheckListDataProvider()
        {
            @Override
            public String getCheckboxId()
            {
                return "checkbox";
            }

            @Override
            public boolean isChecked(final int index)
            {
                return booleans.get(index);
            }

            @Override
            public void setChecked(final int index, final boolean checked)
            {
                booleans.set(index, checked);
            }

            @Override
            public int getElementCount()
            {
                return booleans.size();
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                rowPane.findPaneByType(Text.class).setText(Component.literal("Hi " + index));
            }
        });
    }
}
