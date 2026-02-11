package com.example.joblinker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.joblinker.R;
import com.example.joblinker.utils.LogoutHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingActivity extends BaseActivity  {

    private static final String TAG = "SettingActivity";

    // Views
    private MaterialToolbar toolbar;
    private ImageView ivProfilePicture;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private SwitchMaterial switchNotifications;
    private SwitchMaterial switchDarkMode;
    private SwitchMaterial switchOnlineStatus;
    private LinearLayout btnEditProfile;
    private LinearLayout btnChangePassword;
    private MaterialButton btnLogout;

    // Firebase
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");

        try {
            Log.d(TAG, "Setting content view");
            setContentView(R.layout.activity_settings);
            Log.d(TAG, "Content view set successfully");

            // Initialize Firebase
            Log.d(TAG, "Initializing Firebase");
            firebaseAuth = FirebaseAuth.getInstance();
            Log.d(TAG, "Firebase initialized");

            // Initialize views
            Log.d(TAG, "Initializing views");
            initViews();
            Log.d(TAG, "Views initialized");

            // Load user data
            Log.d(TAG, "Loading user data");
            loadUserData();
            Log.d(TAG, "User data loaded");

            // Setup listeners
            Log.d(TAG, "Setting up listeners");
            setupListeners();
            Log.d(TAG, "Listeners set up");

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        try {
            Log.d(TAG, "Finding toolbar");
            toolbar = findViewById(R.id.toolbar);
            Log.d(TAG, "Toolbar found: " + (toolbar != null));

            Log.d(TAG, "Finding ivProfilePicture");
            ivProfilePicture = findViewById(R.id.ivProfilePicture);
            Log.d(TAG, "ivProfilePicture found: " + (ivProfilePicture != null));

            Log.d(TAG, "Finding tvUserName");
            tvUserName = findViewById(R.id.tvUserName);
            Log.d(TAG, "tvUserName found: " + (tvUserName != null));

            Log.d(TAG, "Finding tvUserEmail");
            tvUserEmail = findViewById(R.id.tvUserEmail);
            Log.d(TAG, "tvUserEmail found: " + (tvUserEmail != null));

            Log.d(TAG, "Finding switchNotifications");
            switchNotifications = findViewById(R.id.switchNotifications);
            Log.d(TAG, "switchNotifications found: " + (switchNotifications != null));

            Log.d(TAG, "Finding switchDarkMode");
            switchDarkMode = findViewById(R.id.switchDarkMode);
            Log.d(TAG, "switchDarkMode found: " + (switchDarkMode != null));

            Log.d(TAG, "Finding switchOnlineStatus");
            switchOnlineStatus = findViewById(R.id.switchOnlineStatus);
            Log.d(TAG, "switchOnlineStatus found: " + (switchOnlineStatus != null));

            Log.d(TAG, "Finding btnEditProfile");
            btnEditProfile = findViewById(R.id.btnEditProfile);
            Log.d(TAG, "btnEditProfile found: " + (btnEditProfile != null));

            Log.d(TAG, "Finding btnChangePassword");
            btnChangePassword = findViewById(R.id.btnChangePassword);
            Log.d(TAG, "btnChangePassword found: " + (btnChangePassword != null));

            Log.d(TAG, "Finding btnLogout");
            btnLogout = findViewById(R.id.btnLogout);
            Log.d(TAG, "btnLogout found: " + (btnLogout != null));

            // Setup toolbar
            if (toolbar != null) {
                Log.d(TAG, "Setting up toolbar");
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayShowTitleEnabled(true);
                    getSupportActionBar().setTitle("Settings");
                }
                Log.d(TAG, "Toolbar set up successfully");
            } else {
                Log.w(TAG, "Toolbar is null!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in initViews", e);
            e.printStackTrace();
            Toast.makeText(this, "Error initializing views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData() {
        try {
            Log.d(TAG, "Getting current user");
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            Log.d(TAG, "Current user: " + (currentUser != null ? currentUser.getEmail() : "null"));

            if (currentUser != null) {
                // Load from Firebase
                String displayName = currentUser.getDisplayName();
                String email = currentUser.getEmail();

                if (tvUserName != null) {
                    tvUserName.setText(displayName != null ? displayName : "User");
                }
                if (tvUserEmail != null) {
                    tvUserEmail.setText(email != null ? email : "");
                }
            } else {
                // Load from SharedPreferences
                String userName = getSharedPreferences("JobLinkerPrefs", MODE_PRIVATE)
                        .getString("userName", "User");
                String userEmail = getSharedPreferences("JobLinkerPrefs", MODE_PRIVATE)
                        .getString("userEmail", "");

                Log.d(TAG, "Loading from SharedPreferences: " + userName);

                if (tvUserName != null) {
                    tvUserName.setText(userName);
                }
                if (tvUserEmail != null) {
                    tvUserEmail.setText(userEmail);
                }
            }

            // Load preferences
            loadPreferences();
        } catch (Exception e) {
            Log.e(TAG, "Error in loadUserData", e);
            e.printStackTrace();
        }
    }

    private void loadPreferences() {
        try {
            boolean notificationsEnabled = getSharedPreferences("JobLinkerPrefs", MODE_PRIVATE)
                    .getBoolean("notificationsEnabled", true);
            boolean darkModeEnabled = getSharedPreferences("JobLinkerPrefs", MODE_PRIVATE)
                    .getBoolean("darkModeEnabled", false);
            boolean onlineStatusEnabled = getSharedPreferences("JobLinkerPrefs", MODE_PRIVATE)
                    .getBoolean("onlineStatusEnabled", true);

            if (switchNotifications != null) {
                switchNotifications.setChecked(notificationsEnabled);
            }
            if (switchDarkMode != null) {
                switchDarkMode.setChecked(darkModeEnabled);
            }
            if (switchOnlineStatus != null) {
                switchOnlineStatus.setChecked(onlineStatusEnabled);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in loadPreferences", e);
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        try {
            // Toolbar back button
            if (toolbar != null) {
                toolbar.setNavigationOnClickListener(v -> finish());
            }

            // Logout button
            if (btnLogout != null) {
                btnLogout.setOnClickListener(v -> {
                    try {
                        LogoutHelper.showLogoutDialog(this);
                    } catch (Exception e) {
                        Log.e(TAG, "Error in logout", e);
                        e.printStackTrace();
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.w(TAG, "btnLogout is null!");
            }

            // Edit profile
            if (btnEditProfile != null) {
                btnEditProfile.setOnClickListener(v -> {
                    Toast.makeText(this, "Edit Profile - Coming soon", Toast.LENGTH_SHORT).show();
                });
            }

            // Change password
            if (btnChangePassword != null) {
                btnChangePassword.setOnClickListener(v -> {
                    Toast.makeText(this, "Change Password - Coming soon", Toast.LENGTH_SHORT).show();
                });
            }

            // Notification switch
            if (switchNotifications != null) {
                switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    getSharedPreferences("JobLinkerPrefs", MODE_PRIVATE)
                            .edit()
                            .putBoolean("notificationsEnabled", isChecked)
                            .apply();
                    Toast.makeText(this, "Notifications " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
                });
            }

            // Dark mode switch
            if (switchDarkMode != null) {
                switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    getSharedPreferences("JobLinkerPrefs", MODE_PRIVATE)
                            .edit()
                            .putBoolean("darkModeEnabled", isChecked)
                            .apply();
                    Toast.makeText(this, "Dark mode " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
                });
            }

            // Online status switch
            if (switchOnlineStatus != null) {
                switchOnlineStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    getSharedPreferences("JobLinkerPrefs", MODE_PRIVATE)
                            .edit()
                            .putBoolean("onlineStatusEnabled", isChecked)
                            .apply();
                    Toast.makeText(this, "Online status " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in setupListeners", e);
            e.printStackTrace();
            Toast.makeText(this, "Error setting up listeners: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}