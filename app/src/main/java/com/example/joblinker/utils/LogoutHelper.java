package com.example.joblinker.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.joblinker.R;
import com.example.joblinker.activities.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

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
        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Logging out...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Perform logout after short delay (for better UX)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut();

            // Clear SharedPreferences
            clearSharedPreferences(activity);

            // Clear app cache
            clearCache(activity);

            // Dismiss progress dialog
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            // Show success message
            Toast.makeText(activity, "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Navigate to login screen
            navigateToLogin(activity);

        }, 1000); // 1 second delay
    }

    /**
     * Quick logout without dialog
     */
    public static void quickLogout(Activity activity) {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();

        // Clear all data
        clearSharedPreferences(activity);
        clearCache(activity);

        // Navigate to login
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