package com.example.joblinker.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.joblinker.fragments.RegisterStep1Fragment;
import com.example.joblinker.fragments.RegisterStep2Fragment;
import com.example.joblinker.fragments.RegisterStep3Fragment;

/**
 * ViewPager2 adapter for registration steps
 */
public class RegisterPagerAdapter extends FragmentStateAdapter {

    private static final int NUM_STEPS = 3;

    // Step positions
    public static final int STEP_1 = 0;
    public static final int STEP_2 = 1;
    public static final int STEP_3 = 2;

    /**
     * Constructor
     * @param fragmentActivity The parent activity
     */
    public RegisterPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /**
     * Create fragment for each step
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case STEP_1:
                return new RegisterStep1Fragment();
            case STEP_2:
                return new RegisterStep2Fragment();
            case STEP_3:
                return new RegisterStep3Fragment();
            default:
                return new RegisterStep1Fragment();
        }
    }

    /**
     * Get total number of steps
     */
    @Override
    public int getItemCount() {
        return NUM_STEPS;
    }
}