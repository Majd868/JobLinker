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

    // يعالج بث BOOT_COMPLETED ويُطلق تهيئة التطبيق بعد إعادة التشغيل
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

    // يُنفّذ أي تهيئة مطلوبة بعد إقلاع الجهاز دون تحديث حالة الاتصال لأن التطبيق غير مفتوح
    private void initializeApp(Context context) {
        // Note: do NOT set the user online here — the app is not open after a reboot,
        // only the OS fired a broadcast. Online status is set in MainActivity/onResume.
        Log.d(TAG, "Boot completed — skipping online-status update (app not yet open)");
    }
}