package com.example.joblinker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

    private final Handler handler = new Handler(Looper.getMainLooper());

    // true after the 2-second splash delay elapses
    private boolean splashReady = false;
    // true between onStart() and onStop() — i.e. app is in foreground
    private boolean isVisible   = false;

    private JobLinkerFirebaseManager firebaseManager;
    private SharedPreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        firebaseManager = JobLinkerFirebaseManager.getInstance();
        prefsManager    = SharedPreferencesManager.getInstance(this);

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

        // After 2s mark splash as ready; if the app is still visible, navigate
        // immediately. If the screen was locked in the meantime, navigation is
        // deferred to onStart() — this avoids the BAL (Background Activity Launch)
        // block that Android 10+ enforces on TOP_SLEEPING processes.
        handler.postDelayed(() -> {
            splashReady = true;
            if (isVisible) navigate();
        }, 2000);
    }

    // يضبط حالة الظهور على true وينتقل فوراً إن كان تأخير الـ 2 ثانية قد انتهى مسبقاً
    @Override
    protected void onStart() {
        super.onStart();
        isVisible = true;
        // If the 2-second delay already elapsed while we were in background, navigate now
        if (splashReady) navigate();
    }

    // يضبط حالة الظهور على false عند انتقال النشاط إلى الخلفية
    @Override
    protected void onStop() {
        super.onStop();
        isVisible = false;
    }

    // يلغي جميع callbacks المعلّقة في Handler عند تدمير النشاط
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    // ينتقل إلى MainActivity إذا كان مسجّلاً الدخول، أو إلى LoginActivity بخلاف ذلك، ثم يُنهي شاشة البداية
    private void navigate() {
        if (isFinishing() || isDestroyed()) return;
        Intent intent;
        if (prefsManager.isLoggedIn() && firebaseManager.getCurrentUser() != null) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
