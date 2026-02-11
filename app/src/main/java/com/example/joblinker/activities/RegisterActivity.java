package com.example.joblinker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;

import com.example.joblinker.R;
import com.example.joblinker.adapters.RegisterPagerAdapter;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.joblinker.fragments.RegisterStep1Fragment;
import com.example.joblinker.fragments.RegisterStep2Fragment;
import com.example.joblinker.fragments.RegisterStep3Fragment;
import com.example.joblinker.models.User;
import com.example.joblinker.utils.SharedPreferencesManager;

public class RegisterActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ViewPager2 viewPager;
    private MaterialButton btnBack, btnNext;
    private View indicatorStep1, indicatorStep2, indicatorStep3;

    private RegisterPagerAdapter pagerAdapter;
    private JobLinkerFirebaseManager firebaseManager;
    private SharedPreferencesManager prefsManager;

    private int currentStep = 0;
    private User registrationUser;
    private String registrationPassword; // Store temporarily for registration

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseManager = JobLinkerFirebaseManager.getInstance();
        prefsManager = SharedPreferencesManager.getInstance(this);
        registrationUser = new User();

        initializeViews();
        setupToolbar();
        setupViewPager();
        setupClickListeners();
        updateStepIndicators();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.view_pager);
        btnBack = findViewById(R.id.btn_back);
        btnNext = findViewById(R.id.btn_next);
        indicatorStep1 = findViewById(R.id.indicator_step1);
        indicatorStep2 = findViewById(R.id.indicator_step2);
        indicatorStep3 = findViewById(R.id.indicator_step3);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViewPager() {
        pagerAdapter = new RegisterPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setUserInputEnabled(false); // Disable swipe

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentStep = position;
                updateStepIndicators();
                updateButtons();
            }
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            if (currentStep > 0) {
                viewPager.setCurrentItem(currentStep - 1, true);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (validateCurrentStep()) {
                if (currentStep < 2) {
                    viewPager.setCurrentItem(currentStep + 1, true);
                } else {
                    completeRegistration();
                }
            }
        });
    }

    private void updateStepIndicators() {
        int primaryColor = getResources().getColor(R.color.primary, null);
        int dividerColor = getResources().getColor(R.color.divider, null);

        indicatorStep1.setBackgroundColor(currentStep >= 0 ? primaryColor : dividerColor);
        indicatorStep2.setBackgroundColor(currentStep >= 1 ? primaryColor : dividerColor);
        indicatorStep3.setBackgroundColor(currentStep >= 2 ? primaryColor : dividerColor);
    }

    private void updateButtons() {
        btnBack.setVisibility(currentStep > 0 ? View.VISIBLE : View.GONE);

        if (currentStep == 2) {
            btnNext.setText(R.string.complete_registration);
        } else {
            btnNext.setText(R.string.next);
        }
    }

    private boolean validateCurrentStep() {
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentByTag("f" + viewPager.getCurrentItem());

        if (currentFragment instanceof RegisterStep1Fragment) {
            return ((RegisterStep1Fragment) currentFragment).validateAndSaveData();
        } else if (currentFragment instanceof RegisterStep2Fragment) {
            return ((RegisterStep2Fragment) currentFragment).validateAndSaveData();
        } else if (currentFragment instanceof RegisterStep3Fragment) {
            // Validation and registration handled in fragment
            return true;
        }

        return true;
    }

    private void completeRegistration() {
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentByTag("f" + viewPager.getCurrentItem());

        if (currentFragment instanceof RegisterStep3Fragment) {
            ((RegisterStep3Fragment) currentFragment).validateAndCompleteRegistration();
        }
    }

    /**
     * Called by RegisterStep3Fragment when registration is complete
     */
    public void onRegistrationComplete() {
        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
        navigateToMain();
    }

    private void navigateToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Getters and Setters for registration data
    public User getRegistrationUser() {
        return registrationUser;
    }

    public void setRegistrationUser(User user) {
        this.registrationUser = user;
    }

    public String getRegistrationPassword() {
        return registrationPassword;
    }

    public void setRegistrationPassword(String password) {
        this.registrationPassword = password;
    }

    public void moveToNextStep() {
        if (currentStep < 2) {
            viewPager.setCurrentItem(currentStep + 1, true);
        }
    }

    public void moveToPreviousStep() {
        if (currentStep > 0) {
            viewPager.setCurrentItem(currentStep - 1, true);
        }
    }

    public int getCurrentStep() {
        return currentStep;
    }

    @Override
    public void onBackPressed() {
        if (currentStep > 0) {
            viewPager.setCurrentItem(currentStep - 1, true);
        } else {
            super.onBackPressed();
        }
    }
}