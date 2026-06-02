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

    // مُنشئ خاص يُهيئ كائن التفضيلات المشتركة والمحرر
    private SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // تُرجع النسخة الوحيدة من الفئة، وتُنشئها عند الحاجة
    public static synchronized SharedPreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesManager(context.getApplicationContext());
        }
        return instance;
    }

    // معرف المستخدم
    // تحفظ معرف المستخدم المسجّل دخوله في التفضيلات المشتركة
    public void setUserId(String userId) {
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    // تُرجع معرف المستخدم المخزَّن، أو null إذا لم يُعيَّن
    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    // اسم المستخدم
    // تحفظ الاسم المعروض للمستخدم في التفضيلات المشتركة
    public void setUserName(String userName) {
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    // تُرجع الاسم المعروض للمستخدم المخزَّن
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }

    // البريد الإلكتروني للمستخدم
    // تحفظ عنوان البريد الإلكتروني للمستخدم في التفضيلات المشتركة
    public void setUserEmail(String userEmail) {
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.apply();
    }

    // تُرجع عنوان البريد الإلكتروني للمستخدم المخزَّن
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    // دور المستخدم
    // تحفظ دور المستخدم (باحث عن عمل أو صاحب عمل) في التفضيلات المشتركة
    public void setUserRole(String userRole) {
        editor.putString(KEY_USER_ROLE, userRole);
        editor.apply();
    }

    // تُرجع دور المستخدم المخزَّن، والافتراضي هو JobSeeker
    public String getUserRole() {
        return sharedPreferences.getString(KEY_USER_ROLE, "JobSeeker");
    }

    // تُرجع true إذا كان دور المستخدم صاحب عمل (غير حساس لحالة الأحرف)
    public boolean isEmployer() {
        // Case-insensitive — EditProfileActivity saves "employer", RegisterStep1 saves "Employer"
        return "employer".equalsIgnoreCase(getUserRole());
    }

    // صورة المستخدم
    // تحفظ رابط صورة الملف الشخصي للمستخدم في التفضيلات المشتركة
    public void setUserAvatar(String avatarUrl) {
        editor.putString(KEY_USER_AVATAR, avatarUrl);
        editor.apply();
    }

    // تُرجع رابط صورة الملف الشخصي المخزَّن
    public String getUserAvatar() {
        return sharedPreferences.getString(KEY_USER_AVATAR, "");
    }

    // اللغة
    // تحفظ رمز اللغة المفضلة للمستخدم في التفضيلات المشتركة
    public void setLanguage(String language) {
        editor.putString(KEY_LANGUAGE, language);
        editor.apply();
    }

    // تُرجع رمز اللغة المخزَّن، والافتراضي هو الإنجليزية
    public String getLanguage() {
        return sharedPreferences.getString(KEY_LANGUAGE, "en");
    }

    // العملة
    // تحفظ رمز العملة المفضلة للمستخدم في التفضيلات المشتركة
    public void setCurrency(String currency) {
        editor.putString(KEY_CURRENCY, currency);
        editor.apply();
    }

    // تُرجع رمز العملة المخزَّن، والافتراضي هو USD
    public String getCurrency() {
        return sharedPreferences.getString(KEY_CURRENCY, "USD");
    }

    // حالة تسجيل الدخول
    // تحفظ حالة تسجيل دخول المستخدم في التفضيلات المشتركة
    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    // تُرجع true إذا كان هناك مستخدم مسجّل دخوله حالياً
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // الإشعارات
    // تحفظ ما إذا كانت الإشعارات الفورية مفعّلة
    public void setNotificationsEnabled(boolean enabled) {
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled);
        editor.apply();
    }

    // تُرجع true إذا كانت الإشعارات الفورية مفعّلة
    public boolean areNotificationsEnabled() {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    // ظهور حالة الاتصال
    // تحفظ ما إذا كانت حالة اتصال المستخدم مرئية للآخرين
    public void setOnlineStatusVisible(boolean visible) {
        editor.putBoolean(KEY_ONLINE_STATUS_VISIBLE, visible);
        editor.apply();
    }

    // تُرجع true إذا كانت حالة اتصال المستخدم مرئية للآخرين
    public boolean isOnlineStatusVisible() {
        return sharedPreferences.getBoolean(KEY_ONLINE_STATUS_VISIBLE, true);
    }

    // ── Generic accessors (used by SettingActivity) ──────────────────

    // تحفظ قيمة منطقية تحت المفتاح المحدد
    public void saveBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    // تُرجع قيمة منطقية للمفتاح المحدد، أو الافتراضية إذا لم توجد
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    // تحفظ قيمة صحيحة تحت المفتاح المحدد
    public void saveInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    // تُرجع قيمة صحيحة للمفتاح المحدد، أو الافتراضية إذا لم توجد
    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    // تحفظ قيمة نصية تحت المفتاح المحدد
    public void saveString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    // تُرجع قيمة نصية للمفتاح المحدد، أو الافتراضية إذا لم توجد
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    // مسح جميع البيانات
    // تمسح جميع التفضيلات المخزَّنة وتعيد تهيئة حالة التطبيق
    public void clearAll() {
        editor.clear();
        editor.apply();
    }
}