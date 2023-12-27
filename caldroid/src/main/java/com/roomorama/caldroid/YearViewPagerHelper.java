package com.roomorama.caldroid;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;
import hirondelle.date4j.DateTime;

public class YearViewPagerHelper {

    public YearViewPagerHelper() {}

    public void resizeCalendarViewPager(@NonNull View _parent, @NonNull View _childView) {
        // Calculate row height
        int rows = 12 / 4;
        resizeCalendarViewPager(_parent, _childView, rows);
    }

    public void resizeCalendarViewPager(@NonNull View _parent, @NonNull View _childView, int _rows) {
        if (rowHeight == 0) {
            int width = _parent.getMeasuredWidth();

            // Use the previously measured width but simplify the calculations
            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width,
                    View.MeasureSpec.EXACTLY);


            _childView.measure(widthMeasureSpec, View.MeasureSpec
                    .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            rowHeight = _childView.getMeasuredHeight();
        }

        // Calculate height of the calendar
        int calHeight = rowHeight * _rows;

        // Prevent small vertical scroll
        calHeight -= 12;
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(calHeight,
                View.MeasureSpec.EXACTLY);

        ViewGroup.LayoutParams layoutParams = _parent.getLayoutParams();
        layoutParams.height = heightMeasureSpec;
        _parent.setLayoutParams(layoutParams);
    }

    /**
     * Use internally to decide height of the calendar
     */
    private int rowHeight = 0;
}
