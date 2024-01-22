package com.ldtteam.blockui.support;

import com.ldtteam.blockui.views.ScrollingList.DataProvider;

import java.util.List;

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

    @FunctionalInterface
    public interface CheckedGetter
    {
        boolean apply(final int index);
    }

    @FunctionalInterface
    public interface CheckedSetter
    {
        void apply(final int index, final boolean checked);
    }

    /**
     * Interface for a data provider that also includes checkbox information per row.
     */
    public abstract static class ConsumerCheckListDataProvider implements CheckListDataProvider
    {
        private final CheckedGetter checkedGetter;

        private final CheckedSetter checkedSetter;

        protected ConsumerCheckListDataProvider(final List<Boolean> booleans)
        {
            this.checkedGetter = booleans::get;
            this.checkedSetter = booleans::set;
        }

        protected ConsumerCheckListDataProvider(final CheckedGetter checkedGetter, final CheckedSetter checkedSetter)
        {
            this.checkedGetter = checkedGetter;
            this.checkedSetter = checkedSetter;
        }

        /**
         * Determine if the item according to the {@link DataProvider} is active or not.
         *
         * @param index the row index.
         * @return true if the button should be in the checked state.
         */
        @Override
        public final boolean isChecked(final int index)
        {
            return checkedGetter.apply(index);
        }

        @Override
        public final void setChecked(final int index, final boolean checked)
        {
            checkedSetter.apply(index, checked);
        }
    }
}
