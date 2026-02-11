package com.example.joblinker.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.joblinker.R;
import com.example.joblinker.activities.CallActivity;

/**
 * Foreground service for managing voice/video calls
 */
public class CallService extends Service {

    private static final String TAG = "CallService";
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "call_service_channel";

    public static final String ACTION_START_CALL = "com.joblinker.START_CALL";
    public static final String ACTION_END_CALL = "com.joblinker.END_CALL";
    public static final String ACTION_ANSWER_CALL = "com.joblinker.ANSWER_CALL";
    public static final String ACTION_DECLINE_CALL = "com.joblinker.DECLINE_CALL";

    public static final String EXTRA_CALL_ID = "call_id";
    public static final String EXTRA_CALLER_ID = "caller_id";
    public static final String EXTRA_CALLER_NAME = "caller_name";
    public static final String EXTRA_CALL_TYPE = "call_type";

    private String currentCallId;
    private String callType;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        String action = intent.getAction();

        if (ACTION_START_CALL.equals(action)) {
            handleStartCall(intent);
        } else if (ACTION_END_CALL.equals(action)) {
            handleEndCall();
        } else if (ACTION_ANSWER_CALL.equals(action)) {
            handleAnswerCall(intent);
        } else if (ACTION_DECLINE_CALL.equals(action)) {
            handleDeclineCall(intent);
        }

        return START_NOT_STICKY;
    }

    private void handleStartCall(Intent intent) {
        currentCallId = intent.getStringExtra(EXTRA_CALL_ID);
        String callerName = intent.getStringExtra(EXTRA_CALLER_NAME);
        callType = intent.getStringExtra(EXTRA_CALL_TYPE);

        // Start foreground service with notification
        Notification notification = createCallNotification(callerName, "Ongoing call...");
        startForeground(NOTIFICATION_ID, notification);
    }

    private void handleEndCall() {
        // Clean up and stop service
        stopForeground(true);
        stopSelf();
    }

    private void handleAnswerCall(Intent intent) {
        String callId = intent.getStringExtra(EXTRA_CALL_ID);
        String callerName = intent.getStringExtra(EXTRA_CALLER_NAME);

        // Launch CallActivity
        Intent callIntent = new Intent(this, CallActivity.class);
        callIntent.putExtra(CallActivity.EXTRA_CALL_TYPE, callType);
        callIntent.putExtra(CallActivity.EXTRA_RECEIVER_NAME, callerName);
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(callIntent);

        // Update notification
        Notification notification = createCallNotification(callerName, "Call in progress...");
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    private void handleDeclineCall(Intent intent) {
        // Decline the call and stop service
        stopForeground(true);
        stopSelf();
    }

    private Notification createCallNotification(String callerName, String contentText) {
        Intent notificationIntent = new Intent(this, CallActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // End Call Action
        Intent endCallIntent = new Intent(this, CallService.class);
        endCallIntent.setAction(ACTION_END_CALL);
        PendingIntent endCallPendingIntent = PendingIntent.getService(
                this,
                1,
                endCallIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_phone)
                .setContentTitle(callerName != null ? callerName : "JobLinker Call")
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_call_end, "End Call", endCallPendingIntent);

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Call Service",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Ongoing voice and video calls");
            channel.enableVibration(true);
            channel.setShowBadge(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up resources
    }
}