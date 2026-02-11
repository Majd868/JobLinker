package com.example.joblinker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.joblinker.R;
import com.example.joblinker.fragments.ChatsFragment;
import com.example.joblinker.fragments.JobsFragment;
import com.example.joblinker.fragments.ProfileFragment;
import com.example.joblinker.utils.LogoutHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends BaseActivity  {

    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize bottom navigation
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new JobsFragment());
        }

        // Setup bottom navigation listener
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_jobs) {
                selectedFragment = new JobsFragment();
            } else if (itemId == R.id.nav_chats) {
                selectedFragment = new ChatsFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
}