package com.example.joblinker.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.joblinker.fragments.LoginEmailFragment;
import com.example.joblinker.fragments.LoginPhoneFragment;

/**
 * ViewPager2 adapter for login tabs (Email and Phone)
 */
public class LoginPagerAdapter extends FragmentStateAdapter {

    private static final int NUM_PAGES = 2;

    // Tab positions
    public static final int TAB_EMAIL = 0;
    public static final int TAB_PHONE = 1;

    /**
     * Constructor
     * @param fragmentActivity The parent activity
     */
    public LoginPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /**
     * Create fragment for each tab position
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case TAB_EMAIL:
                return new LoginEmailFragment();
            case TAB_PHONE:
                return new LoginPhoneFragment();
            default:
                return new LoginEmailFragment();
        }
    }

    /**
     * Get total number of pages/tabs
     */
    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}