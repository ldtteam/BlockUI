package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.CheckBox;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.support.DataProviders.CheckListDataProvider;
import com.ldtteam.blockui.views.ScrollingList.DataProvider;
import com.ldtteam.blockui.util.records.SizeI;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.ScrollingList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Sets up gui for dynamic scrolling lists.
 */
public class ScrollingListsGui
{
    public static void setup(final BOWindow window)
    {
        // Case 1: A regular list
        final ScrollingList list1 = window.findPaneOfTypeByID("list1", ScrollingList.class);
        list1.setDataProvider(new DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return 10;
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                rowPane.findPaneByType(Text.class).setText(Component.literal("Hi " + index));
            }
        });

        // Case 2: A list with dynamic height items
        final ScrollingList list2 = window.findPaneOfTypeByID("list2", ScrollingList.class);
        list2.setDataProvider(new DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return 10;
            }

            @Override
            public @Nullable SizeI getElementSize(final int index, final Pane rowPane)
            {
                return index % 2 == 0 ? new SizeI(100, 40) : null;
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                rowPane.findPaneByType(Text.class).setText(Component.literal("Hi " + index));
            }
        });

        // Case 3: A scrolling check list
        final List<Boolean> booleans = new ArrayList<>(List.of(false, false, false, false, false, false, true, false, false, false));
        final ScrollingList list3 = window.findPaneOfTypeByID("list3", ScrollingList.class);
        list3.setDataProvider(new CheckListDataProvider()
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

                // Test for 2 variants of disabled buttons
                if (index == 4 || index == 6)
                {
                    rowPane.findPaneOfTypeByID(getCheckboxId(), CheckBox.class).disable();
                }
            }
        });
    }
}
