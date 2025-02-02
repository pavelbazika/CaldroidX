package com.roomorama.caldroid;

import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.Date;
import java.util.Map;

import androidx.annotation.Nullable;
import hirondelle.date4j.DateTime;

public abstract class YearCaldroidListener {
    /*
     * Inform client user has clicked on a date
     *
     * @param date
     * @param view
     */
    public void onSelectDate(Date date, View view) {

    }


    /*
     * Inform client user has long clicked on a date
     *
     * @param date
     * @param view
     */
    public void onLongClickDate(Date ignoredDate, View ignoredView) {
        // Do nothing
    }


    /*
     * Inform client that DateCaldroidFragment view has been created and views are
     * no longer null. Useful for customization of button and text views
     */
    public void onCaldroidViewCreated() {
        // Do nothing
    }


    // Called when title was clicked
    public void onTitleClicked(int ignoredYear) {

    }

    @Nullable
    public Map<DateTime, Drawable> getBackgroundYearMap(int _maxYear) {
        return null;
    }

    @Nullable
    public Map<Integer, String> getCellTexts(int _maxYear) {
        return null;
    }
}
