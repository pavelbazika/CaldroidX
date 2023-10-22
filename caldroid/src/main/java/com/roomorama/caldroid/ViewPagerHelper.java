package com.roomorama.caldroid;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;
import hirondelle.date4j.DateTime;

public class ViewPagerHelper {
    public static final int OFFSET = 1000;

    public ViewPagerHelper() {}

    public void setSixWeeksInCalendar(boolean sixWeeksInCalendar) {
        this.sixWeeksInCalendar = sixWeeksInCalendar;
        rowHeight = 0;
    }

    public void resizeCalendarViewPager(@NonNull ViewPager2 _pager, @NonNull View _childView, @NonNull ArrayList<DateTime> _datesInMonth) {
        // Calculate row height
        int rows = _datesInMonth.size() / 7;

        if (rowHeight == 0) {
            int width = _pager.getMeasuredWidth();

            // Use the previously measured width but simplify the calculations
            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width,
                    View.MeasureSpec.EXACTLY);


            _childView.measure(widthMeasureSpec, View.MeasureSpec
                    .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            rowHeight = _childView.getMeasuredHeight();
        }

        // Calculate height of the calendar
        int calHeight;

        // If fit 6 weeks, we need 6 rows
        if (sixWeeksInCalendar) {
            calHeight = rowHeight * 6;
        } else { // Otherwise we return correct number of rows
            calHeight = rowHeight * rows;
        }

        // Prevent small vertical scroll
        calHeight -= 12;
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(calHeight,
                View.MeasureSpec.EXACTLY);

        ViewGroup.LayoutParams layoutParams = _pager.getLayoutParams();
        layoutParams.height = heightMeasureSpec;
        _pager.setLayoutParams(layoutParams);
    }

    private boolean sixWeeksInCalendar = false;

    /**
     * Use internally to decide height of the calendar
     */
    private int rowHeight = 0;

}
