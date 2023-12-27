package com.roomorama.caldroid;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import hirondelle.date4j.DateTime;

public class YearPagerAdapter extends FragmentStateAdapter {
    public YearPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, @NonNull MonthCaldroidFragment _parent) {
        super(fragmentManager, lifecycle);
        parent = _parent;

        DateTime minDateTime = parent.getMinDateTime();
        mMaxDateTime = parent.getMaxDateTime();
        if ((minDateTime != null) && (mMaxDateTime != null)) {
            mItemCount = mMaxDateTime.getYear() - minDateTime.getYear() + 1;
        } else {
            mItemCount = MonthViewPagerHelper.OFFSET + 1;
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return parent.createMonthGridFragment(position);
    }

    @Override
    public int getItemCount() {
        return mItemCount;
    }

    public DateTime positionToDateTime(int _position) {
        int yearShift = mItemCount - 1 - _position;
        return mMaxDateTime.minus(yearShift, 0, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
    }

    public int dateTimeToPosition(@NonNull DateTime _dateTime) {
        return mItemCount - (mMaxDateTime.getYear() - _dateTime.getYear()) - 1;
    }

    private final MonthCaldroidFragment parent;
    private final int mItemCount;
    private final DateTime mMaxDateTime;
}
