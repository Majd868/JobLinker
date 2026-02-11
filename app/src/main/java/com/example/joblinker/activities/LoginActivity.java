package com.example.joblinker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.joblinker.models.User;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseUser;

import com.example.joblinker.R;
import com.example.joblinker.adapters.LoginPagerAdapter;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.utils.SharedPreferencesManager;

public class LoginActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TextView tvSignUp;

    private LoginPagerAdapter pagerAdapter;
    private JobLinkerFirebaseManager firebaseManager;
    private SharedPreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseManager = JobLinkerFirebaseManager.getInstance();
        prefsManager = SharedPreferencesManager.getInstance(this);

        // Check if already logged in
        if (prefsManager.isLoggedIn() && firebaseManager.getCurrentUser() != null) {
            navigateToMain();
            return;
        }

        initializeViews();
        setupViewPager();
        setupClickListeners();
    }

    private void initializeViews() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        tvSignUp = findViewById(R.id.tv_sign_up);
    }

    private void setupViewPager() {
        pagerAdapter = new LoginPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        if (position == LoginPagerAdapter.TAB_EMAIL) {
                            tab.setText(R.string.email);
                        } else if (position == LoginPagerAdapter.TAB_PHONE) {
                            tab.setText(R.string.phone);
                        }
                    }
                }
        ).attach();

        // Optional: Disable swipe between tabs
        // viewPager.setUserInputEnabled(false);
    }

    private void setupClickListeners() {
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Called by login fragments when login is successful
     */
    public void onLoginSuccess(FirebaseUser user) {
        // Save login state
        prefsManager.setLoggedIn(true);
        prefsManager.setUserId(user.getUid());

        if (user.getEmail() != null) {
            prefsManager.setUserEmail(user.getEmail());
        }

        // Load user data and navigate
        firebaseManager.getUser(user.getUid(),
                new JobLinkerFirebaseManager.DataCallback<User>() {
                    @Override
                    public void onSuccess(com.example.joblinker.models.User userData) {
                        // Save user data to preferences
                        prefsManager.setUserName(userData.getUserName());
                        prefsManager.setUserRole(userData.getUserRole());

                        if (userData.getAvatarUrl() != null) {
                            prefsManager.setUserAvatar(userData.getAvatarUrl());
                        }

                        if (userData.getUserLanguage() != null) {
                            prefsManager.setLanguage(userData.getUserLanguage());
                        }

                        if (userData.getUserCurrency() != null) {
                            prefsManager.setCurrency(userData.getUserCurrency());
                        }

                        // Navigate to main activity
                        navigateToMain();
                    }

                    @Override
                    public void onFailure(String error) {
                        // User data not found, but login successful
                        // This might happen for new users
                        Toast.makeText(LoginActivity.this,
                                "Please complete your profile", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    }
                });
    }

    /**
     * Navigate to MainActivity
     */
    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Get current tab position
     */
    public int getCurrentTab() {
        return viewPager.getCurrentItem();
    }

    /**
     * Switch to specific tab
     */
    public void switchToTab(int position) {
        if (position >= 0 && position < pagerAdapter.getItemCount()) {
            viewPager.setCurrentItem(position, true);
        }
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // If on first tab, allow normal back behavior
            super.onBackPressed();
        } else {
            // Otherwise, go back to previous tab
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }
}