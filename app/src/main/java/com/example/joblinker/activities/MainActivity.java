package com.example.joblinker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.joblinker.R;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.fragments.ChatsFragment;
import com.example.joblinker.fragments.JobsFragment;
import com.example.joblinker.fragments.ProfileFragment;
import com.example.joblinker.utils.LogoutHelper;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class MainActivity extends BaseActivity {

    private BottomNavigationView bottomNav;

    // Cached fragment instances — never recreated on tab switch
    private JobsFragment    jobsFragment;
    private ChatsFragment   chatsFragment;
    private ProfileFragment profileFragment;
    private Fragment        activeFragment;

    // Unread message listener
    private ListenerRegistration unreadListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNavigationView);

        setupFragments();
        setupBottomNav();
        listenForUnreadMessages();
    }

    // ── Create all fragments once, hide/show instead of replace ──
    private void setupFragments() {
        jobsFragment    = new JobsFragment();
        chatsFragment   = new ChatsFragment();
        profileFragment = new ProfileFragment();

        getSupportFragmentManager().beginTransaction()
            .add(R.id.fragmentContainer, profileFragment, "profile").hide(profileFragment)
            .add(R.id.fragmentContainer, chatsFragment,  "chats").hide(chatsFragment)
            .add(R.id.fragmentContainer, jobsFragment,   "jobs")
            .commit();

        activeFragment = jobsFragment;
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_jobs)    switchTo(jobsFragment);
            else if (id == R.id.nav_chats)   switchTo(chatsFragment);
            else if (id == R.id.nav_profile) switchTo(profileFragment);
            return true;
        });
    }

    private void switchTo(Fragment target) {
        if (target == activeFragment) return;
        getSupportFragmentManager().beginTransaction()
            .hide(activeFragment)
            .show(target)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit();
        activeFragment = target;
    }

    // ── Unread messages badge on Chats tab ────────
    private void listenForUnreadMessages() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        unreadListener = FirebaseFirestore.getInstance()
            .collection("messages")
            .whereEqualTo("messageReceiverId", uid)
            .whereEqualTo("messageRead", false)
            .addSnapshotListener((snap, e) -> {
                if (snap == null) return;
                int count = snap.size();
                runOnUiThread(() -> {
                    BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_chats);
                    if (count > 0) {
                        badge.setVisible(true);
                        badge.setNumber(count);
                    } else {
                        badge.setVisible(false);
                        badge.clearNumber();
                    }
                });
            });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            LogoutHelper.showLogoutDialog(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unreadListener != null) unreadListener.remove();
    }
}
