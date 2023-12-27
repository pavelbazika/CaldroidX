package com.roomorama.caldroid;

// Grid of 12 month cells
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;

import com.caldroid.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MonthGridFragment extends Fragment {
    private GridView gridView;
    private MonthGridAdapter gridAdapter;
    private AdapterView.OnItemClickListener onItemClickListener;
    private AdapterView.OnItemLongClickListener onItemLongClickListener;
    private int gridViewRes = 0;
    private int themeResource = 0;

    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public AdapterView.OnItemLongClickListener getOnItemLongClickListener() {
        return onItemLongClickListener;
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public MonthGridAdapter getGridAdapter() {
        return gridAdapter;
    }

    public void setGridAdapter(MonthGridAdapter gridAdapter) {
        this.gridAdapter = gridAdapter;
    }

    public GridView getGridView() {
        return gridView;
    }

    public void setGridViewRes(int gridViewRes) {
        this.gridViewRes = gridViewRes;
    }

    private void setupGridView() {
        // Client normally needs to provide the adapter and onItemClickListener
        // before the fragment is attached to avoid complex crash due to
        // fragment life cycles
        if (gridAdapter != null) {
            gridView.setAdapter(gridAdapter);
        }

        if (onItemClickListener != null) {
            gridView.setOnItemClickListener(onItemClickListener);
        }
        if (onItemLongClickListener != null) {
            gridView.setOnItemLongClickListener(onItemLongClickListener);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // If gridViewRes is not valid, use default fragment layout
        if (gridViewRes == 0) {
            gridViewRes = R.layout.month_grid_fragment;
        }

        if (themeResource == 0) {
            if (gridAdapter != null) {
                themeResource = gridAdapter.getThemeResource();
            }
        }

        if (gridView == null) {
            LayoutInflater localInflater = MonthCaldroidFragment.getThemeInflater(getActivity(),
                    inflater, themeResource);
            gridView = (GridView) localInflater.inflate(gridViewRes, container, false);
            setupGridView();
        } else {
            ViewGroup parent = (ViewGroup) gridView.getParent();
            if (parent != null) {
                parent.removeView(gridView);
            }
        }

        return gridView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ViewTreeObserver vto = view.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(() -> {
            Fragment parentFragment = getParentFragment();
            if (parentFragment instanceof MonthCaldroidFragment) {
                MonthCaldroidFragment monthCaldroidFragment = (MonthCaldroidFragment)parentFragment;
                monthCaldroidFragment.resizeViewPager(view);
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }
}
