package com.example.joblinker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.utils.SharedPreferencesManager;

/**
 * Receiver for device boot events
 * Used to initialize app services after device restart
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Device boot completed");

            // Initialize app after boot
            initializeApp(context);
        }
    }

    private void initializeApp(Context context) {
        SharedPreferencesManager prefsManager = SharedPreferencesManager.getInstance(context);

        // Check if user is logged in
        if (prefsManager.isLoggedIn()) {
            String userId = prefsManager.getUserId();

            if (userId != null) {
                // Update user online status
                JobLinkerFirebaseManager firebaseManager = JobLinkerFirebaseManager.getInstance();
                firebaseManager.updateUserOnlineStatus(userId, true);

                Log.d(TAG, "User status updated after boot: " + userId);
            }
        }

        // Initialize other services as needed
        // For example: start background sync, schedule notifications, etc.
    }
}