package com.example.joblinker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.joblinker.R;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.utils.SharedPreferencesManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        JobLinkerFirebaseManager firebaseManager = JobLinkerFirebaseManager.getInstance();
        SharedPreferencesManager prefsManager = SharedPreferencesManager.getInstance(this);

        new Handler().postDelayed(() -> {
            Intent intent;

            // Check if user is logged in
            if (prefsManager.isLoggedIn() && firebaseManager.getCurrentUser() != null) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }
}