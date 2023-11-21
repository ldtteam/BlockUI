package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.ScrollingListContainer.RowSizeModifier;
import net.minecraft.network.chat.Component;

/**
 * Sets up gui for dynamic scrolling lists.
 */
public class DynamicScrollingListGui
{
    public static void setup(final BOWindow window)
    {
        final ScrollingList list1 = window.findPaneOfTypeByID("list1", ScrollingList.class);
        list1.setDataProvider(new ScrollingList.DataProvider()
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

        final ScrollingList list2 = window.findPaneOfTypeByID("list2", ScrollingList.class);
        list2.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return 20;
            }

            @Override
            public void modifyRowSize(final int index, final RowSizeModifier modifier)
            {
                if (index % 2 == 0)
                {
                    modifier.setSize(100, 40);
                }
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                rowPane.findPaneByType(Text.class).setText(Component.literal("Hi " + index));
            }
        });
    }
}
