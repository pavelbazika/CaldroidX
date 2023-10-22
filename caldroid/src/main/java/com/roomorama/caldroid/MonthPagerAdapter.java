package com.roomorama.caldroid;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * MonthPagerAdapter holds 4 fragments, which provides fragment for current
 * month, previous month and next month. The extra fragment helps for recycle
 * fragments.
 *
 * @author thomasdao
 */
public class MonthPagerAdapter extends FragmentStateAdapter {
    public MonthPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, @NonNull CaldroidFragment _parent) {
        super(fragmentManager, lifecycle);
        parent = _parent;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return parent.createDataGridFragment(position);
    }

    @Override
    public int getItemCount() {
        // We need 4 gridviews for previous month, current month and next month,
        // and 1 extra fragment for fragment recycle
        return ViewPagerHelper.OFFSET + 1;
    }

    private final CaldroidFragment parent;
}
