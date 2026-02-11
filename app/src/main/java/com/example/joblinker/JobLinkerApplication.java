package com.example.joblinker;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import com.google.firebase.FirebaseApp;

public class JobLinkerApplication extends Application {

    private static final String TAG = "JobLinkerApp";

    // Notification Channels
    public static final String CHANNEL_MESSAGES = "messages_channel";
    public static final String CHANNEL_JOBS = "jobs_channel";
    public static final String CHANNEL_CALLS = "calls_channel";
    public static final String CHANNEL_GENERAL = "general_channel";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "JobLinker Application started");

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Create notification channels
        createNotificationChannels();

        // Initialize Glide (optional configuration)
        // GlideApp is auto-generated
    }

    /**
     * Create notification channels for Android O and above
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);

            if (manager != null) {
                // Messages Channel
                NotificationChannel messagesChannel = new NotificationChannel(
                        CHANNEL_MESSAGES,
                        "Messages",
                        NotificationManager.IMPORTANCE_HIGH
                );
                messagesChannel.setDescription("New message notifications");
                messagesChannel.enableVibration(true);
                messagesChannel.setShowBadge(true);
                manager.createNotificationChannel(messagesChannel);

                // Jobs Channel
                NotificationChannel jobsChannel = new NotificationChannel(
                        CHANNEL_JOBS,
                        "Job Alerts",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                jobsChannel.setDescription("New job posting notifications");
                jobsChannel.enableVibration(true);
                jobsChannel.setShowBadge(true);
                manager.createNotificationChannel(jobsChannel);

                // Calls Channel
                NotificationChannel callsChannel = new NotificationChannel(
                        CHANNEL_CALLS,
                        "Calls",
                        NotificationManager.IMPORTANCE_HIGH
                );
                callsChannel.setDescription("Incoming call notifications");
                callsChannel.enableVibration(true);
                callsChannel.setShowBadge(true);
                manager.createNotificationChannel(callsChannel);

                // General Channel
                NotificationChannel generalChannel = new NotificationChannel(
                        CHANNEL_GENERAL,
                        "General",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                generalChannel.setDescription("General app notifications");
                manager.createNotificationChannel(generalChannel);

                Log.d(TAG, "Notification channels created");
            }
        }
    }
}