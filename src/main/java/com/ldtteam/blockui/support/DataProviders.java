package com.ldtteam.blockui.support;

import com.ldtteam.blockui.views.ScrollingList.DataProvider;

/**
 * More specific data provider instances.
 */
public class DataProviders
{
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
}
