package com.example.joblinker.utils;

import android.text.TextUtils;
import android.util.Patterns;

public class ValidationHelper {

    /**
     * Validate email address
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validate phone number
     */
    public static boolean isValidPhone(String phone) {
        return !TextUtils.isEmpty(phone) && Patterns.PHONE.matcher(phone).matches() && phone.length() >= 10;
    }

    /**
     * Validate password
     */
    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 6;
    }

    /**
     * Validate URL
     */
    public static boolean isValidUrl(String url) {
        return !TextUtils.isEmpty(url) && Patterns.WEB_URL.matcher(url).matches();
    }

    /**
     * Check if string is empty
     */
    public static boolean isEmpty(String text) {
        return TextUtils.isEmpty(text) || text.trim().isEmpty();
    }

    /**
     * Validate passwords match
     */
    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        return !TextUtils.isEmpty(password) && password.equals(confirmPassword);
    }

    /**
     * Validate name
     */
    public static boolean isValidName(String name) {
        return !TextUtils.isEmpty(name) && name.trim().length() >= 2;
    }

    /**
     * Validate number
     */
    public static boolean isValidNumber(String number) {
        if (TextUtils.isEmpty(number)) return false;
        try {
            Double.parseDouble(number);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}