package com.roomorama.caldroid;

import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
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
    public void onLongClickDate(Date date, View view) {
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
    public void onTitleClicked(int _year) {

    }

    @Nullable
    public Map<DateTime, Drawable> getBackgroundYearMap() {
        return null;
    }

    @Nullable
    public Map<Integer, String> getCellTexts() {
        return null;
    }
}
