package com.example.joblinker.utils;

import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateTimeHelper {

    private static final String TAG = "DateTimeHelper";

    // Date Format Patterns
    private static final String PATTERN_TIME_12H = "h:mm a";
    private static final String PATTERN_TIME_24H = "HH:mm";
    private static final String PATTERN_DATE_SHORT = "MMM dd, yyyy";
    private static final String PATTERN_DATE_MEDIUM = "MMMM dd, yyyy";
    private static final String PATTERN_DATE_LONG = "EEEE, MMMM dd, yyyy";
    private static final String PATTERN_DATETIME = "MMM dd, yyyy h:mm a";
    private static final String PATTERN_DATETIME_FULL = "EEEE, MMMM dd, yyyy h:mm a";
    private static final String PATTERN_DAY_NAME = "EEEE";
    private static final String PATTERN_MONTH_YEAR = "MMMM yyyy";
    private static final String PATTERN_ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * Format timestamp to time string (e.g., "2:30 PM")
     */
    public static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_TIME_12H, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Format timestamp to 24-hour time string (e.g., "14:30")
     */
    public static String formatTime24H(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_TIME_24H, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Format timestamp to date string (e.g., "Jan 15, 2025")
     */
    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_DATE_SHORT, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Format timestamp to medium date string (e.g., "January 15, 2025")
     */
    public static String formatDateMedium(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_DATE_MEDIUM, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Format timestamp to long date string (e.g., "Monday, January 15, 2025")
     */
    public static String formatDateLong(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_DATE_LONG, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Format timestamp to datetime string (e.g., "Jan 15, 2025 2:30 PM")
     */
    public static String formatDateTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_DATETIME, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Format timestamp to full datetime string
     */
    public static String formatDateTimeFull(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_DATETIME_FULL, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Get relative time string (e.g., "2 hours ago", "Just now", "Yesterday")
     */
    public static String getRelativeTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        // Just now (less than 1 minute)
        if (diff < TimeUnit.MINUTES.toMillis(1)) {
            return "Just now";
        }
        // Minutes ago
        else if (diff < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        }
        // Hours ago
        else if (diff < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        }
        // Days ago (up to 7 days)
        else if (diff < TimeUnit.DAYS.toMillis(7)) {
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            if (days == 1) {
                return "Yesterday";
            }
            return days + " days ago";
        }
        // Weeks ago (up to 4 weeks)
        else if (diff < TimeUnit.DAYS.toMillis(30)) {
            long weeks = TimeUnit.MILLISECONDS.toDays(diff) / 7;
            return weeks + (weeks == 1 ? " week ago" : " weeks ago");
        }
        // Months ago (up to 12 months)
        else if (diff < TimeUnit.DAYS.toMillis(365)) {
            long months = TimeUnit.MILLISECONDS.toDays(diff) / 30;
            return months + (months == 1 ? " month ago" : " months ago");
        }
        // Years ago
        else {
            long years = TimeUnit.MILLISECONDS.toDays(diff) / 365;
            return years + (years == 1 ? " year ago" : " years ago");
        }
    }

    /**
     * Get relative time for future dates (e.g., "in 2 hours", "in 3 days")
     */
    public static String getRelativeTimeFuture(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = timestamp - now;

        if (diff <= 0) {
            return "Now";
        }

        // Minutes
        if (diff < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            return "in " + minutes + (minutes == 1 ? " minute" : " minutes");
        }
        // Hours
        else if (diff < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            return "in " + hours + (hours == 1 ? " hour" : " hours");
        }
        // Days
        else if (diff < TimeUnit.DAYS.toMillis(7)) {
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            if (days == 1) {
                return "Tomorrow";
            }
            return "in " + days + " days";
        }
        // Weeks
        else if (diff < TimeUnit.DAYS.toMillis(30)) {
            long weeks = TimeUnit.MILLISECONDS.toDays(diff) / 7;
            return "in " + weeks + (weeks == 1 ? " week" : " weeks");
        }
        // Months
        else if (diff < TimeUnit.DAYS.toMillis(365)) {
            long months = TimeUnit.MILLISECONDS.toDays(diff) / 30;
            return "in " + months + (months == 1 ? " month" : " months");
        }
        // Years
        else {
            long years = TimeUnit.MILLISECONDS.toDays(diff) / 365;
            return "in " + years + (years == 1 ? " year" : " years");
        }
    }

    /**
     * Format call duration from seconds (e.g., "02:45" or "01:30:15")
     */
    public static String formatCallDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, secs);
        }
    }

    /**
     * Format duration from milliseconds (e.g., "2h 30m" or "45m 30s")
     */
    public static String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            hours = hours % 24;
            return days + "d " + hours + "h";
        } else if (hours > 0) {
            minutes = minutes % 60;
            return hours + "h " + minutes + "m";
        } else if (minutes > 0) {
            seconds = seconds % 60;
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }

    /**
     * Get message timestamp display format
     * - If today: show time (e.g., "2:30 PM")
     * - If yesterday: show "Yesterday"
     * - If this week: show day name (e.g., "Monday")
     * - Otherwise: show date (e.g., "Jan 15")
     */
    public static String getMessageTimestamp(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        // If today, show time
        if (isToday(timestamp)) {
            return formatTime(timestamp);
        }
        // If yesterday
        else if (isYesterday(timestamp)) {
            return "Yesterday";
        }
        // If within a week, show day name
        else if (diff < TimeUnit.DAYS.toMillis(7)) {
            SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_DAY_NAME, Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
        // Otherwise show date
        else {
            return formatDate(timestamp);
        }
    }

    /**
     * Get conversation timestamp (similar to WhatsApp format)
     */
    public static String getConversationTimestamp(long timestamp) {
        if (isToday(timestamp)) {
            return formatTime(timestamp);
        } else if (isYesterday(timestamp)) {
            return "Yesterday";
        } else if (isThisWeek(timestamp)) {
            SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_DAY_NAME, Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } else if (isThisYear(timestamp)) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }

    /**
     * Check if timestamp is today
     */
    public static boolean isToday(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        Calendar today = Calendar.getInstance();

        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Check if timestamp is yesterday
     */
    public static boolean isYesterday(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        return calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Check if timestamp is in this week
     */
    public static boolean isThisWeek(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        Calendar now = Calendar.getInstance();

        return calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                calendar.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * Check if timestamp is in this year
     */
    public static boolean isThisYear(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        Calendar now = Calendar.getInstance();

        return calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR);
    }

    /**
     * Get day name from timestamp (e.g., "Monday")
     */
    public static String getDayName(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_DAY_NAME, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Get month and year (e.g., "January 2025")
     */
    public static String getMonthYear(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_MONTH_YEAR, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Get time difference in human readable format
     */
    public static String getTimeDifference(long startTime, long endTime) {
        long diff = endTime - startTime;

        if (diff < 0) {
            return "0s";
        }

        return formatDuration(diff);
    }

    /**
     * Parse date string to timestamp
     */
    public static long parseDate(String dateString, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
            Date date = sdf.parse(dateString);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get start of day timestamp
     */
    public static long getStartOfDay(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * Get end of day timestamp
     */
    public static long getEndOfDay(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    /**
     * Get age from date of birth
     */
    public static int getAge(long dateOfBirth) {
        Calendar dob = Calendar.getInstance();
        dob.setTimeInMillis(dateOfBirth);

        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }

    /**
     * Check if date is in the past
     */
    public static boolean isPast(long timestamp) {
        return timestamp < System.currentTimeMillis();
    }

    /**
     * Check if date is in the future
     */
    public static boolean isFuture(long timestamp) {
        return timestamp > System.currentTimeMillis();
    }

    /**
     * Add days to timestamp
     */
    public static long addDays(long timestamp, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTimeInMillis();
    }

    /**
     * Add hours to timestamp
     */
    public static long addHours(long timestamp, int hours) {
        return timestamp + TimeUnit.HOURS.toMillis(hours);
    }

    /**
     * Add minutes to timestamp
     */
    public static long addMinutes(long timestamp, int minutes) {
        return timestamp + TimeUnit.MINUTES.toMillis(minutes);
    }

    /**
     * Get timestamp for specific date
     */
    public static long getTimestamp(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * Get timestamp for specific datetime
     */
    public static long getTimestamp(int year, int month, int day, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, hour, minute, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * Format timestamp to ISO 8601 format
     */
    public static String formatISO8601(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_ISO_8601, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(timestamp));
    }

    /**
     * Parse ISO 8601 date string to timestamp
     */
    public static long parseISO8601(String dateString) {
        return parseDate(dateString, PATTERN_ISO_8601);
    }

    /**
     * Get greeting based on time of day
     */
    public static String getGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return "Good morning";
        } else if (hour >= 12 && hour < 17) {
            return "Good afternoon";
        } else if (hour >= 17 && hour < 21) {
            return "Good evening";
        } else {
            return "Good night";
        }
    }

    /**
     * Check if two timestamps are on the same day
     */
    public static boolean isSameDay(long timestamp1, long timestamp2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(timestamp1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(timestamp2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Get current timestamp
     */
    public static long now() {
        return System.currentTimeMillis();
    }

    /**
     * Get timestamp for tomorrow at midnight
     */
    public static long getTomorrow() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * Get timestamp for yesterday at midnight
     */
    public static long getYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}