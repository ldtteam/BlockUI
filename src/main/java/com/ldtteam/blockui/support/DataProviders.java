package com.ldtteam.blockui.support;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.util.records.SizeI;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

public class DataProviders
{
    /**
     * Interface for a data provider that updates pane scrolling list pane info.
     */
    public interface DataProvider
    {
        /**
         * Override this to provide the number of rows.
         *
         * @return number of rows in the list
         */
        int getElementCount();

        /**
         * Override this to pick a custom size for this element. Tuple arguments are width and height, in that order.
         *
         * @param index   the index of the row/list element
         * @param rowPane the parent Pane for the row, containing the elements to update
         * @return a new size for the element, or null to use the template element size.
         */
        default @Nullable SizeI getElementSize(int index, Pane rowPane)
        {
            return null;
        }

        /**
         * Override this to update the Panes for a given row.
         *
         * @param index   the index of the row/list element
         * @param rowPane the parent Pane for the row, containing the elements to update
         */
        void updateElement(int index, Pane rowPane);
    }

    /**
     * Interface for a data provider that also includes checkbox information per row.
     */
    public interface CheckListDataProvider extends DataProvider
    {
        /**
         * Get the ID of the child element which is the checkbox.
         *
         * @return the ID or null in case you want to disable checkbox support.
         */
        String getCheckboxId();

        /**
         * Determine if the item according to the {@link DataProvider} is active or not.
         *
         * @param index the row index.
         * @return true if the button should be in the checked state.
         */
        boolean isChecked(final int index);

        /**
         * Update the state of the check button, the checked parameter is the new checked state.
         *
         * @param index   the row index.
         * @param checked whether the item is checked or not.
         */
        void setChecked(final int index, final boolean checked);
    }

    /**
     * Interface for a data provider that updates pane scrolling list pane info.
     */
    public interface DropdownDataProvider
    {
        int getElementCount();

        @Deprecated
        String getLabel(final int index);

        default MutableComponent getLabelNew(final int index)
        {
            return Component.literal(getLabel(index));
        }
    }
}
