package com.roomorama.caldroid;

import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.Date;
import java.util.Map;

import androidx.annotation.Nullable;
import hirondelle.date4j.DateTime;

/*
 * DateCaldroidListener inform when user clicks on a valid date (not within disabled
 * dates, and valid between min/max dates)
 * <p/>
 * The method onChangeMonth is optional, user can always override this to listen
 * to month change event
 *
 * @author thomasdao
 */
public class DateCaldroidListener {
    /*
     * Inform client user has clicked on a date
     *
     * @param date
     * @param view
     */
    public void onSelectDate(Date ignoredDate, View ignoredView) {
        // can work over model - do nothing
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
     * Inform client that calendar has changed month
     *
     * @param month
     * @param year
     */
    public void onChangeMonth(int ignoredMonth, int ignoredYear) {
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
    public Map<DateTime, Drawable> getBackgroundDateTimeMap(int month, int year) {
        return null;
    }

    // Called when title was clicked
    public void onTitleClicked(int ignoredMonth, int ignoredYear) {

    }
}
