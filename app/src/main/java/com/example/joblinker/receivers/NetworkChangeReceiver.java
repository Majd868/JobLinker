package com.example.joblinker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.utils.SharedPreferencesManager;

/**
 * Receiver for network connectivity changes
 * Updates user online status based on network availability
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            boolean isConnected = isNetworkAvailable(context);

            Log.d(TAG, "Network connectivity changed. Connected: " + isConnected);

            handleNetworkChange(context, isConnected);
        }
    }

    /**
     * Check if network is available
     */
    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return false;
            }

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            );
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }

    /**
     * Handle network connectivity changes
     */
    private void handleNetworkChange(Context context, boolean isConnected) {
        SharedPreferencesManager prefsManager = SharedPreferencesManager.getInstance(context);

        // Only update status if user is logged in
        if (!prefsManager.isLoggedIn()) {
            return;
        }

        String userId = prefsManager.getUserId();
        if (userId == null) {
            return;
        }

        JobLinkerFirebaseManager firebaseManager = JobLinkerFirebaseManager.getInstance();

        if (isConnected) {
            // Network is available - set user online
            firebaseManager.updateUserOnlineStatus(userId, true);
            Log.d(TAG, "User set to online: " + userId);

            // Resume any pending operations
            // For example: sync messages, upload pending data, etc.

        } else {
            // Network is unavailable - set user offline
            firebaseManager.updateUserOnlineStatus(userId, false);
            Log.d(TAG, "User set to offline: " + userId);

            // Handle offline mode
            // For example: queue pending operations, show offline indicator, etc.
        }
    }
}