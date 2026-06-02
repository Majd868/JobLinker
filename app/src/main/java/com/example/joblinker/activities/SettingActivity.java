package com.example.joblinker.activities;

import com.example.joblinker.utils.ProgressDialogHelper;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.example.joblinker.R;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.models.User;
import com.example.joblinker.utils.LogoutHelper;
import com.example.joblinker.utils.SharedPreferencesManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingActivity extends BaseActivity {

    private static final String TAG = "SettingActivity";

    // ── Views ──────────────────────────────────────────
    private MaterialToolbar toolbar;
    private ImageView ivProfilePicture;
    private TextView tvUserName, tvUserEmail, tvVerificationStatus;

    // Account section
    private LinearLayout btnEditProfile;
    private LinearLayout btnChangePassword;
    private LinearLayout btnVerifyEmail;
    private LinearLayout btnSavedJobs;
    private LinearLayout btnMyApplications;

    // Preferences
    private SwitchMaterial switchNotifications;
    private SwitchMaterial switchJobAlerts;
    private SwitchMaterial switchMessageNotifications;
    private SwitchMaterial switchDarkMode;
    private SwitchMaterial switchOnlineStatus;

    // Privacy
    private LinearLayout btnBlockedUsers;
    private LinearLayout btnPrivacyProfile;

    // Support
    private LinearLayout btnHelpSupport;
    private LinearLayout btnTerms;
    private LinearLayout btnPrivacyPolicy;
    private LinearLayout btnAbout;

    // Danger zone
    private LinearLayout btnDeleteAccount;
    private MaterialButton btnLogout;

    // ── Firebase & utils ──────────────────────────────
    private FirebaseAuth firebaseAuth;
    private JobLinkerFirebaseManager firebaseManager;
    private SharedPreferencesManager prefsManager;
    private User currentUser;

    // ─────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        firebaseAuth    = FirebaseAuth.getInstance();
        firebaseManager = JobLinkerFirebaseManager.getInstance();
        prefsManager    = SharedPreferencesManager.getInstance(this);

        initViews();
        loadPreferences();
        loadUserData();
        setupListeners();
    }

    // يربط جميع عناصر شاشة الإعدادات ويضبط شريط الأدوات بزر الرجوع
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ivProfilePicture       = findViewById(R.id.ivProfilePicture);
        tvUserName             = findViewById(R.id.tvUserName);
        tvUserEmail            = findViewById(R.id.tvUserEmail);
        tvVerificationStatus   = findViewById(R.id.tvVerificationStatus); // optional, may not exist in older XML

        btnEditProfile         = findViewById(R.id.btnEditProfile);
        btnChangePassword      = findViewById(R.id.btnChangePassword);
        btnVerifyEmail         = findViewById(R.id.btnVerifyEmail);
        btnSavedJobs           = findViewById(R.id.btnSavedJobs);
        btnMyApplications      = findViewById(R.id.btnMyApplications);

        switchNotifications        = findViewById(R.id.switchNotifications);
        switchJobAlerts            = findViewById(R.id.switchJobAlerts);
        switchMessageNotifications = findViewById(R.id.switchMessageNotifications);
        switchDarkMode             = findViewById(R.id.switchDarkMode);
        switchOnlineStatus         = findViewById(R.id.switchOnlineStatus);

        btnBlockedUsers   = findViewById(R.id.btnBlockedUsers);
        btnPrivacyProfile = findViewById(R.id.btnPrivacyProfile);

        btnHelpSupport   = findViewById(R.id.btnHelpSupport);
        btnTerms         = findViewById(R.id.btnTerms);
        btnPrivacyPolicy = findViewById(R.id.btnPrivacyPolicy);
        btnAbout         = findViewById(R.id.btnAbout);

        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnLogout        = findViewById(R.id.btnLogout);
    }

    // يقرأ علامات التفضيل المحفوظة ويضبط الحالة المحددة الأولية لجميع مفاتيح التبديل
    private void loadPreferences() {
        boolean notif   = prefsManager.getBoolean("notificationsEnabled", true);
        boolean jobs    = prefsManager.getBoolean("jobAlertsEnabled", true);
        boolean msgs    = prefsManager.getBoolean("messageNotificationsEnabled", true);
        boolean dark    = prefsManager.getBoolean("darkModeEnabled", false);
        boolean online  = prefsManager.getBoolean("onlineStatusEnabled", true);

        if (switchNotifications        != null) switchNotifications.setChecked(notif);
        if (switchJobAlerts            != null) switchJobAlerts.setChecked(jobs);
        if (switchMessageNotifications != null) switchMessageNotifications.setChecked(msgs);
        if (switchDarkMode             != null) switchDarkMode.setChecked(dark);
        if (switchOnlineStatus         != null) switchOnlineStatus.setChecked(online);
    }

    // يحمّل اسم المستخدم الحالي وبريده الإلكتروني وحالة التحقق وصورته من Firebase
    private void loadUserData() {
        FirebaseUser fUser = firebaseAuth.getCurrentUser();
        if (fUser == null) return;

        // Show email immediately from Firebase Auth
        if (tvUserEmail != null) tvUserEmail.setText(fUser.getEmail());

        // Verification badge
        if (tvVerificationStatus != null) {
            tvVerificationStatus.setText(fUser.isEmailVerified() ? "✓ Verified" : "Not verified");
        }

        // Full profile from Firestore
        firebaseManager.getUser(fUser.getUid(), new JobLinkerFirebaseManager.DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                if (tvUserName != null) tvUserName.setText(user.getUserName());
                if (ivProfilePicture != null && user.getAvatarUrl() != null) {
                    Glide.with(SettingActivity.this)
                         .load(user.getAvatarUrl())
                         .circleCrop()
                         .skipMemoryCache(true)
                         .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                         .placeholder(R.drawable.ic_user_placeholder)
                         .into(ivProfilePicture);
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to load user: " + error);
            }
        });
    }

    // يسجّل مستمعي النقر والتبديل لجميع عناصر الإعدادات والمفاتيح وأزرار الإجراءات
    private void setupListeners() {

        // ── ACCOUNT ──────────────────────────────────

        // Edit profile → open EditProfileActivity
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));
        }

        // Change password → inline dialog
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }

        // Verify / re-send verification email
        if (btnVerifyEmail != null) {
            btnVerifyEmail.setOnClickListener(v -> sendVerificationEmail());
        }

        // Saved jobs → open SavedJobsActivity
        if (btnSavedJobs != null) {
            btnSavedJobs.setOnClickListener(v ->
                startActivity(new Intent(this, com.example.joblinker.activities.SavedJobsActivity.class)));
        }

        // My Applications → placeholder (activity not yet implemented)
        if (btnMyApplications != null) {
            btnMyApplications.setOnClickListener(v ->
                showSimpleInfo("My Applications",
                    "All your job applications will appear here with their current status."));
        }

        // ── PREFERENCES / SWITCHES ────────────────────

        if (switchNotifications != null) {
            switchNotifications.setOnCheckedChangeListener((btn, checked) -> {
                prefsManager.saveBoolean("notificationsEnabled", checked);
                // If turning off, also silence sub-switches
                if (!checked) {
                    if (switchJobAlerts != null)            { switchJobAlerts.setChecked(false); prefsManager.saveBoolean("jobAlertsEnabled", false); }
                    if (switchMessageNotifications != null) { switchMessageNotifications.setChecked(false); prefsManager.saveBoolean("messageNotificationsEnabled", false); }
                }
                Toast.makeText(this,
                    "Notifications " + (checked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
            });
        }

        if (switchJobAlerts != null) {
            switchJobAlerts.setOnCheckedChangeListener((btn, checked) -> {
                prefsManager.saveBoolean("jobAlertsEnabled", checked);
                Toast.makeText(this,
                    "Job alerts " + (checked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
            });
        }

        if (switchMessageNotifications != null) {
            switchMessageNotifications.setOnCheckedChangeListener((btn, checked) -> {
                prefsManager.saveBoolean("messageNotificationsEnabled", checked);
                Toast.makeText(this,
                    "Message notifications " + (checked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
            });
        }

        if (switchDarkMode != null) {
            switchDarkMode.setOnCheckedChangeListener((btn, checked) -> {
                prefsManager.saveBoolean("darkModeEnabled", checked);
                AppCompatDelegate.setDefaultNightMode(
                    checked ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO);
            });
        }

        if (switchOnlineStatus != null) {
            switchOnlineStatus.setOnCheckedChangeListener((btn, checked) -> {
                prefsManager.saveBoolean("onlineStatusEnabled", checked);
                String uid = prefsManager.getUserId();
                if (uid != null) {
                    firebaseManager.updateUserOnlineStatus(uid, checked);
                }
                Toast.makeText(this,
                    "Online status " + (checked ? "visible" : "hidden"),
                    Toast.LENGTH_SHORT).show();
            });
        }

        // ── PRIVACY ───────────────────────────────────

        if (btnBlockedUsers != null) {
            btnBlockedUsers.setOnClickListener(v -> showBlockedUsersDialog());
        }

        if (btnPrivacyProfile != null) {
            btnPrivacyProfile.setOnClickListener(v -> showProfileVisibilityDialog());
        }

        // ── SUPPORT ───────────────────────────────────

        if (btnHelpSupport != null) {
            btnHelpSupport.setOnClickListener(v -> showHelpDialog());
        }

        if (btnTerms != null) {
            btnTerms.setOnClickListener(v -> showTextDialog("Terms & Conditions",
                "By using JobLinker you agree to our terms of service.\n\n" +
                "• You must be 18 or older to use this app.\n" +
                "• You are responsible for the accuracy of your profile.\n" +
                "• Employers must post genuine job listings only.\n" +
                "• Spam, abuse, or fraudulent activity will result in account termination.\n" +
                "• JobLinker reserves the right to remove any content that violates these terms.\n\n" +
                "For the full terms visit our website."));
        }

        if (btnPrivacyPolicy != null) {
            btnPrivacyPolicy.setOnClickListener(v -> showTextDialog("Privacy Policy",
                "Your privacy matters to us.\n\n" +
                "• We collect only the information needed to operate the app.\n" +
                "• Your data is stored securely in Google Firebase.\n" +
                "• We do not sell your personal information to third parties.\n" +
                "• You can delete your account and all data at any time.\n" +
                "• We use anonymous analytics to improve the app experience.\n\n" +
                "For the full privacy policy visit our website."));
        }

        if (btnAbout != null) {
            btnAbout.setOnClickListener(v -> showTextDialog("About JobLinker",
                "JobLinker v1.0\n\n" +
                "JobLinker connects job seekers with employers through a modern, " +
                "easy-to-use mobile platform.\n\n" +
                "Features:\n" +
                "• Post and browse job listings\n" +
                "• Real-time chat between seekers and employers\n" +
                "• Voice & video calls powered by Agora\n" +
                "• Smart job matching and filters\n" +
                "• Push notifications for new jobs and messages\n\n" +
                "Built with ❤️ using Firebase & Android."));
        }

        // ── DANGER ZONE ───────────────────────────────

        if (btnDeleteAccount != null) {
            btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> LogoutHelper.showLogoutDialog(this));
        }
    }

    // ══════════════════════════════════════════════════
    // DIALOG IMPLEMENTATIONS
    // ══════════════════════════════════════════════════

    // يُضخّم تخطيط مربع حوار تغيير كلمة المرور، مع الرجوع إلى مربع حوار بسيط إذا كان التخطيط مفقوداً
    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_change_password, null);

        EditText etCurrentPassword = dialogView.findViewById(R.id.et_current_password);
        EditText etNewPassword     = dialogView.findViewById(R.id.et_new_password);
        EditText etConfirmPassword = dialogView.findViewById(R.id.et_confirm_password);

        // Fallback: if layout doesn't exist yet, use a simple 3-field approach
        if (etCurrentPassword == null) {
            // Build a simple dialog manually
            showSimpleChangePasswordDialog();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Change", (dialog, which) -> {
                    String current = etCurrentPassword.getText().toString().trim();
                    String newPass  = etNewPassword.getText().toString().trim();
                    String confirm  = etConfirmPassword.getText().toString().trim();
                    performChangePassword(current, newPass, confirm);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ينشئ مربع حوار برمجياً بثلاثة حقول لتغيير كلمة المرور عند غياب ملف تخطيط XML
    private void showSimpleChangePasswordDialog() {
        // Current password field
        final EditText etCurrent = new EditText(this);
        etCurrent.setHint("Current password");
        etCurrent.setInputType(
            android.text.InputType.TYPE_CLASS_TEXT |
            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        final EditText etNew = new EditText(this);
        etNew.setHint("New password (min 6 chars)");
        etNew.setInputType(
            android.text.InputType.TYPE_CLASS_TEXT |
            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        final EditText etConfirm = new EditText(this);
        etConfirm.setHint("Confirm new password");
        etConfirm.setInputType(
            android.text.InputType.TYPE_CLASS_TEXT |
            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int)(16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, 0);
        layout.addView(etCurrent);
        layout.addView(etNew);
        layout.addView(etConfirm);

        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(layout)
                .setPositiveButton("Change", (dialog, which) ->
                    performChangePassword(
                        etCurrent.getText().toString().trim(),
                        etNew.getText().toString().trim(),
                        etConfirm.getText().toString().trim()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // يُعيد المصادقة بكلمة المرور الحالية ثم يحدّث كلمة مرور Firebase إذا كانت صالحة
    private void performChangePassword(String current, String newPass, String confirm) {
        if (TextUtils.isEmpty(current)) {
            Toast.makeText(this, "Enter your current password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPass.length() < 6) {
            Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPass.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) return;

        ProgressDialogHelper pd = new ProgressDialogHelper(this, "Changing password…");
        pd.show();

        // Re-authenticate first
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), current);
        user.reauthenticate(credential).addOnCompleteListener(reAuthTask -> {
            if (!reAuthTask.isSuccessful()) {
                pd.dismiss();
                Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                return;
            }

            // Now change the password
            firebaseManager.changePassword(newPass, new JobLinkerFirebaseManager.VoidCallback() {
                @Override
                public void onSuccess() {
                    pd.dismiss();
                    Toast.makeText(SettingActivity.this,
                        "Password changed successfully!", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(String error) {
                    pd.dismiss();
                    Toast.makeText(SettingActivity.this,
                        "Failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    // يرسل رابط التحقق من البريد الإلكتروني عبر Firebase إلى المستخدم الحالي إذا لم يكن محققاً بعد
    private void sendVerificationEmail() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) return;

        if (user.isEmailVerified()) {
            Toast.makeText(this, "Your email is already verified ✓", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialogHelper pd = new ProgressDialogHelper(this, "Sending verification email…");
        pd.show();

        user.sendEmailVerification().addOnCompleteListener(task -> {
            pd.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(this,
                    "Verification email sent to " + user.getEmail(),
                    Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this,
                    "Failed to send email. Try again later.",
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    // يعرض مربع حوار يسرد المستخدمين المحجوبين (فارغ حالياً؛ مكان مؤقت لميزة مستقبلية)
    private void showBlockedUsersDialog() {
        // Load blocked users from Firestore user document
        if (currentUser == null) {
            Toast.makeText(this, "Loading user data…", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user has a blocked list field; show list or empty message
        new AlertDialog.Builder(this)
                .setTitle("Blocked Users")
                .setMessage("You have not blocked anyone.\n\nTo block a user, open their profile and tap 'Block User'.")
                .setPositiveButton("OK", null)
                .show();
        // TODO: when you add a blockedUsers list field to the User model,
        //  populate a real ListView/RecyclerView here.
    }

    // يعرض مربع حوار لاختيار من يمكنه رؤية الملف الشخصي ويحفظ الاختيار
    private void showProfileVisibilityDialog() {
        String[] options = {"Everyone", "Registered users only", "Nobody"};
        int saved = prefsManager.getInt("profileVisibility", 0);

        new AlertDialog.Builder(this)
                .setTitle("Who can see my profile")
                .setSingleChoiceItems(options, saved, null)
                .setPositiveButton("Save", (dialog, which) -> {
                    int selected = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    prefsManager.saveInt("profileVisibility", selected);
                    Toast.makeText(this,
                        "Profile visibility set to: " + options[selected],
                        Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // يعرض مربع حوار يحتوي على الأسئلة الشائعة ومعلومات التواصل مع الدعم
    private void showHelpDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Help & Support")
                .setMessage(
                    "Frequently Asked Questions\n\n" +
                    "Q: How do I apply for a job?\n" +
                    "A: Browse jobs, open a job listing, and tap 'Apply Now'.\n\n" +
                    "Q: How do I post a job?\n" +
                    "A: Switch your account role to Employer in Edit Profile, then tap '+' on the Jobs tab.\n\n" +
                    "Q: How do I chat with an employer?\n" +
                    "A: Open a job listing and tap 'Contact Employer', or go to the Chats tab.\n\n" +
                    "Q: How do I delete my account?\n" +
                    "A: Go to Settings → Delete Account.\n\n" +
                    "For further help, contact: support@joblinker.app")
                .setPositiveButton("OK", null)
                .show();
    }

    // يعرض مربع تنبيه بسيط قابل للإغلاق بالعنوان والمحتوى النصي المعطيين
    private void showTextDialog(String title, String content) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton("Close", null)
                .show();
    }

    // يعرض مربع حوار إعلامي كمكان مؤقت للميزات غير المنفّذة بعد
    private void showSimpleInfo(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    // يعرض مربع حوار تحذيري يصف ما سيُحذف قبل الانتقال إلى تأكيد كلمة المرور
    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Delete Account")
                .setMessage(
                    "This will permanently delete:\n\n" +
                    "• Your profile and all personal data\n" +
                    "• All your job posts (if employer)\n" +
                    "• All your applications\n" +
                    "• All your messages and chat history\n\n" +
                    "This action CANNOT be undone. Are you sure?")
                .setPositiveButton("Delete", (dialog, which) -> confirmDeleteWithPassword())
                .setNegativeButton("Cancel", null)
                .show();
    }

    // يطلب من المستخدم إدخال كلمة المرور لتأكيد الحذف الدائم للحساب
    private void confirmDeleteWithPassword() {
        final EditText etPassword = new EditText(this);
        etPassword.setHint("Enter your password to confirm");
        etPassword.setInputType(
            android.text.InputType.TYPE_CLASS_TEXT |
            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        int pad = (int)(16 * getResources().getDisplayMetrics().density);
        etPassword.setPadding(pad, pad, pad, 0);

        new AlertDialog.Builder(this)
                .setTitle("Confirm Password")
                .setMessage("Enter your password to permanently delete your account.")
                .setView(etPassword)
                .setPositiveButton("Delete Forever", (dialog, which) -> {
                    String password = etPassword.getText().toString().trim();
                    if (TextUtils.isEmpty(password)) {
                        Toast.makeText(this, "Password required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    performDeleteAccount(password);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // يُعيد المصادقة بكلمة المرور المقدَّمة ويحذف حساب Firebase والبيانات المحلية نهائياً
    private void performDeleteAccount(String password) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) return;

        ProgressDialogHelper pd = new ProgressDialogHelper(this, "Deleting account…");
        pd.show();

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        user.reauthenticate(credential).addOnCompleteListener(reAuthTask -> {
            if (!reAuthTask.isSuccessful()) {
                pd.dismiss();
                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseManager.deleteAccount(new JobLinkerFirebaseManager.VoidCallback() {
                @Override
                public void onSuccess() {
                    pd.dismiss();
                    prefsManager.clearAll();
                    Toast.makeText(SettingActivity.this,
                        "Account deleted.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

                @Override
                public void onFailure(String error) {
                    pd.dismiss();
                    Toast.makeText(SettingActivity.this,
                        "Failed to delete account: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
