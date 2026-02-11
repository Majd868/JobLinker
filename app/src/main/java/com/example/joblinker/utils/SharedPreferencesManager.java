package com.example.joblinker.utils;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private static final String PREF_NAME = "JobLinkerPrefs";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_ROLE = "userRole";
    private static final String KEY_USER_AVATAR = "userAvatar";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_CURRENCY = "currency";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notificationsEnabled";
    private static final String KEY_ONLINE_STATUS_VISIBLE = "onlineStatusVisible";

    private static SharedPreferencesManager instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized SharedPreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesManager(context.getApplicationContext());
        }
        return instance;
    }

    // User ID
    public void setUserId(String userId) {
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    // User Name
    public void setUserName(String userName) {
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }

    // User Email
    public void setUserEmail(String userEmail) {
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.apply();
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    // User Role
    public void setUserRole(String userRole) {
        editor.putString(KEY_USER_ROLE, userRole);
        editor.apply();
    }

    public String getUserRole() {
        return sharedPreferences.getString(KEY_USER_ROLE, "JobSeeker");
    }

    public boolean isEmployer() {
        return "Employer".equals(getUserRole());
    }

    // User Avatar
    public void setUserAvatar(String avatarUrl) {
        editor.putString(KEY_USER_AVATAR, avatarUrl);
        editor.apply();
    }

    public String getUserAvatar() {
        return sharedPreferences.getString(KEY_USER_AVATAR, "");
    }

    // Language
    public void setLanguage(String language) {
        editor.putString(KEY_LANGUAGE, language);
        editor.apply();
    }

    public String getLanguage() {
        return sharedPreferences.getString(KEY_LANGUAGE, "en");
    }

    // Currency
    public void setCurrency(String currency) {
        editor.putString(KEY_CURRENCY, currency);
        editor.apply();
    }

    public String getCurrency() {
        return sharedPreferences.getString(KEY_CURRENCY, "USD");
    }

    // Login Status
    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Notifications
    public void setNotificationsEnabled(boolean enabled) {
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled);
        editor.apply();
    }

    public boolean areNotificationsEnabled() {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    // Online Status Visibility
    public void setOnlineStatusVisible(boolean visible) {
        editor.putBoolean(KEY_ONLINE_STATUS_VISIBLE, visible);
        editor.apply();
    }

    public boolean isOnlineStatusVisible() {
        return sharedPreferences.getBoolean(KEY_ONLINE_STATUS_VISIBLE, true);
    }

    // Clear all data
    public void clearAll() {
        editor.clear();
        editor.apply();
    }
}