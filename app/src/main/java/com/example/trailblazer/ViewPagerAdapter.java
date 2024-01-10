package com.example.trailblazer;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A FragmentStateAdapter subclass for managing a collection of Fragments and titles for each fragment.
 * This adapter is used with ViewPager2 to provide a tabbed interface where each tab corresponds to a Fragment.
 */
public class ViewPagerAdapter extends FragmentStateAdapter {
    private final List<Fragment> fragmentList = new ArrayList<>();
    private final List<String> fragmentTitles = new ArrayList<>();
    public ViewPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /**
     * Adds a Fragment along with its title to the adapter.
     *
     * @param fragment The Fragment to be added.
     * @param title The title associated with the Fragment.
     */
    public void addFragment(Fragment fragment, String title) {
        fragmentList.add(fragment);
        fragmentTitles.add(title);
    }

    /**
     * Creates the Fragment for the given position.
     *
     * @param position The position of the Fragment in the ViewPager.
     * @return The Fragment associated with the specified position.
     */
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    /**
     * Returns the total number of Fragments in the adapter.
     *
     * @return The total number of Fragments.
     */
    @Override
    public int getItemCount() {
        return fragmentList.size();
    }
}
