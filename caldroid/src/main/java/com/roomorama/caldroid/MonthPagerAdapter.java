package com.roomorama.caldroid;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import hirondelle.date4j.DateTime;

public class MonthPagerAdapter extends FragmentStateAdapter {
    public MonthPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, @NonNull DateCaldroidFragment _parent) {
        super(fragmentManager, lifecycle);
        parent = _parent;

        DateTime minDateTime = parent.getMinDateTime();
        mMaxDateTime = parent.getMaxDateTime();
        if ((minDateTime != null) && (mMaxDateTime != null)) {
            int minEpochMonth = (minDateTime.getYear() * 12) + (minDateTime.getMonth() - 1);
            int maxEpochMonth = (mMaxDateTime.getYear() * 12 ) + (mMaxDateTime.getMonth() - 1);
            mItemCount = maxEpochMonth - minEpochMonth + 1;
        } else {
            mItemCount = MonthViewPagerHelper.OFFSET + 1;
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return parent.createDateGridFragment(position);
    }

    @Override
    public int getItemCount() {
        return mItemCount;
    }

    public DateTime positionToDateTime(int _position) {
        int monthShift = mItemCount - 1 - _position;
        return mMaxDateTime.minus(0, monthShift, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
    }

    public int dateTimeToPosition(@NonNull DateTime _dateTime) {
        int todayMonthEpoch = mMaxDateTime.getYear() * 12 + (mMaxDateTime.getMonth() - 1);
        int askedMonthEpoch = _dateTime.getYear() * 12 + (_dateTime.getMonth() - 1);

        return mItemCount - (todayMonthEpoch - askedMonthEpoch) - 1;
    }

    private final DateCaldroidFragment parent;
    private final int mItemCount;
    private final DateTime mMaxDateTime;
}
