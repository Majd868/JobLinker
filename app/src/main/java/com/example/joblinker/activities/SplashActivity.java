package com.example.joblinker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.joblinker.R;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.utils.SharedPreferencesManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView ivLogo   = findViewById(R.id.iv_logo);
        TextView tvAppName = findViewById(R.id.tv_app_name);
        TextView tvTagline = findViewById(R.id.tv_tagline);

        // Logo: scale up + fade in
        AnimationSet logoAnim = new AnimationSet(true);
        ScaleAnimation scale = new ScaleAnimation(0.4f, 1f, 0.4f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(600);
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(600);
        logoAnim.addAnimation(scale);
        logoAnim.addAnimation(fadeIn);
        logoAnim.setFillAfter(true);
        ivLogo.startAnimation(logoAnim);

        // App name: fade in with delay
        AlphaAnimation nameAnim = new AlphaAnimation(0f, 1f);
        nameAnim.setDuration(500);
        nameAnim.setStartOffset(500);
        nameAnim.setFillAfter(true);
        if (tvAppName != null) tvAppName.startAnimation(nameAnim);

        // Tagline: fade in later
        if (tvTagline != null) {
            AlphaAnimation tagAnim = new AlphaAnimation(0f, 1f);
            tagAnim.setDuration(500);
            tagAnim.setStartOffset(800);
            tagAnim.setFillAfter(true);
            tvTagline.startAnimation(tagAnim);
        }

        // Navigate after 2s
        JobLinkerFirebaseManager firebaseManager = JobLinkerFirebaseManager.getInstance();
        SharedPreferencesManager prefsManager    = SharedPreferencesManager.getInstance(this);

        ivLogo.postDelayed(() -> {
            Intent intent;
            if (prefsManager.isLoggedIn() && firebaseManager.getCurrentUser() != null) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2000);
    }
}
