package com.example.joblinker.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.joblinker.R;
import com.example.joblinker.activities.LoginActivity;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;

import java.io.File;

public class LogoutHelper {

    /**
     * Show logout confirmation dialog
     */
    public static void showLogoutDialog(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.logout))
                .setMessage(activity.getString(R.string.logout_message))
                .setPositiveButton(activity.getString(R.string.yes), (dialog, which) ->
                        performLogout(activity))
                .setNegativeButton(activity.getString(R.string.cancel), null)
                .setCancelable(true)
                .show();
    }

    /**
     * Perform logout with progress dialog
     */
    public static void performLogout(Activity activity) {
        ProgressDialogHelper progressDialog = new ProgressDialogHelper(activity, "Logging out...");
        progressDialog.show();

        JobLinkerFirebaseManager firebaseManager = JobLinkerFirebaseManager.getInstance();
        // Read userId BEFORE clearing prefs
        String userId = SharedPreferencesManager.getInstance(activity).getUserId();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Mark user offline before signing out
            if (userId != null) {
                firebaseManager.updateUserOnlineStatus(userId, false);
            }
            // Full logout — removes all Firestore listeners and signs out of Firebase Auth
            firebaseManager.logout();

            clearSharedPreferences(activity);
            clearCache(activity);

            progressDialog.dismiss();
            Toast.makeText(activity, "Logged out successfully", Toast.LENGTH_SHORT).show();
            navigateToLogin(activity);
        }, 1000);
    }

    /**
     * Quick logout without dialog
     */
    public static void quickLogout(Activity activity) {
        JobLinkerFirebaseManager firebaseManager = JobLinkerFirebaseManager.getInstance();
        String userId = SharedPreferencesManager.getInstance(activity).getUserId();
        if (userId != null) firebaseManager.updateUserOnlineStatus(userId, false);
        firebaseManager.logout();
        clearSharedPreferences(activity);
        clearCache(activity);
        Toast.makeText(activity, "Logged out", Toast.LENGTH_SHORT).show();
        navigateToLogin(activity);
    }

    /**
     * Clear SharedPreferences
     */
    private static void clearSharedPreferences(Context context) {
        context.getSharedPreferences("JobLinkerPrefs", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    /**
     * Clear app cache
     */
    private static void clearCache(Context context) {
        try {
            File cache = context.getCacheDir();
            deleteDir(cache);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Recursively delete directory
     */
    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return dir != null && dir.delete();
    }

    /**
     * Navigate to login and clear activity stack
     */
    private static void navigateToLogin(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}