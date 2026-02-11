package com.example.joblinker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public class LocaleHelper {

    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";
    private static final String PREFS_NAME = "JobLinkerPrefs";

    /**
     * Set the app's language
     */
    public static Context setLocale(Context context, String language) {
        persist(context, language);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        }

        return updateResourcesLegacy(context, language);
    }

    /**
     * Get saved language
     */
    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(SELECTED_LANGUAGE, "en"); // Default to English
    }

    /**
     * Save language preference
     */
    private static void persist(Context context, String language) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(SELECTED_LANGUAGE, language).apply();
    }

    /**
     * Update resources for API 24+
     */
    private static Context updateResources(Context context, String language) {
        Locale locale = getLocaleFromLanguage(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        return context.createConfigurationContext(configuration);
    }

    /**
     * Update resources for older APIs
     */
    @SuppressWarnings("deprecation")
    private static Context updateResourcesLegacy(Context context, String language) {
        Locale locale = getLocaleFromLanguage(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale);
        }

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return context;
    }

    /**
     * Convert language name to locale
     */
    private static Locale getLocaleFromLanguage(String language) {
        switch (language.toLowerCase()) {
            case "arabic":
            case "ar":
                return new Locale("ar");
            case "hebrew":
            case "he":
                return new Locale("he");
            case "spanish":
            case "es":
                return new Locale("es");
            case "french":
            case "fr":
                return new Locale("fr");
            case "german":
            case "de":
                return new Locale("de");
            case "italian":
            case "it":
                return new Locale("it");
            case "portuguese":
            case "pt":
                return new Locale("pt");
            case "russian":
            case "ru":
                return new Locale("ru");
            case "chinese":
            case "zh":
                return new Locale("zh");
            case "japanese":
            case "ja":
                return new Locale("ja");
            case "korean":
            case "ko":
                return new Locale("ko");
            case "hindi":
            case "hi":
                return new Locale("hi");
            case "dutch":
            case "nl":
                return new Locale("nl");
            case "swedish":
            case "sv":
                return new Locale("sv");
            case "norwegian":
            case "no":
                return new Locale("no");
            case "danish":
            case "da":
                return new Locale("da");
            case "finnish":
            case "fi":
                return new Locale("fi");
            case "polish":
            case "pl":
                return new Locale("pl");
            case "turkish":
            case "tr":
                return new Locale("tr");
            case "english":
            case "en":
            default:
                return new Locale("en");
        }
    }

    /**
     * Get language code from full name
     */
    public static String getLanguageCode(String language) {
        switch (language.toLowerCase()) {
            case "arabic": return "ar";
            case "hebrew": return "he";
            case "spanish": return "es";
            case "french": return "fr";
            case "german": return "de";
            case "italian": return "it";
            case "portuguese": return "pt";
            case "russian": return "ru";
            case "chinese": return "zh";
            case "japanese": return "ja";
            case "korean": return "ko";
            case "hindi": return "hi";
            case "dutch": return "nl";
            case "swedish": return "sv";
            case "norwegian": return "no";
            case "danish": return "da";
            case "finnish": return "fi";
            case "polish": return "pl";
            case "turkish": return "tr";
            case "english":
            default: return "en";
        }
    }
}