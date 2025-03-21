package com.example.project.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.project.ui_all.StatisticsActivity;
import com.example.project.ui_all.statistics.*;

public class StatisticsPagerAdapter extends FragmentStateAdapter {

    private static final int NUM_TABS = 4;

    public StatisticsPagerAdapter(@NonNull StatisticsActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new StatisticsDayFragment();
            case 1: return new StatisticsWeekFragment();
            case 2: return new StatisticsMonthFragment();
            case 3: return new StatisticsYearFragment();
            default: return new StatisticsWeekFragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}
