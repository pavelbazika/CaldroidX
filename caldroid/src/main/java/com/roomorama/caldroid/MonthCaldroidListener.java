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

public abstract class MonthCaldroidListener {
    /*
     * Inform client user has clicked on a date
     *
     * @param date
     * @param view
     */
    public abstract void onSelectDate(Date date, View view);


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
     * Inform client that calendar has changed year
     *
     * @param month
     * @param year
     */
    public void onChangeYear(int year) {
        // Do nothing
    }


    /*
     * Inform client that DateCaldroidFragment view has been created and views are
     * no longer null. Useful for customization of button and text views
     */
    public void onCaldroidViewCreated() {
        // Do nothing
    }

    @Nullable
    public Map<DateTime, Drawable> getBackgroundMonthMap(int year) {
        return null;
    }

    @NonNull
    // month[1-12] -> text
    public Map<Integer, String> getCellTexts(int _year) {
        HashMap<Integer, String> result = new HashMap<>();

        Calendar cal = Calendar.getInstance();
        for (int month = 0; month < 12; month++) {
            cal.set(_year, month, 1);
            result.put(month + 1, cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()));
        }

        return result;
    }

    // Called when title was clicked
    public void onTitleClicked(int _year) {

    }
}
