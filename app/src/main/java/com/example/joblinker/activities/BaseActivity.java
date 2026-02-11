package com.example.joblinker.activities;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.joblinker.utils.LocaleHelper;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Apply saved language
        updateLanguage();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String language = LocaleHelper.getLanguage(newBase);
        super.attachBaseContext(LocaleHelper.setLocale(newBase, language));
    }

    protected void updateLanguage() {
        String language = LocaleHelper.getLanguage(this);
        LocaleHelper.setLocale(this, language);
    }

    protected void changeLanguage(String language) {
        String languageCode = LocaleHelper.getLanguageCode(language);
        LocaleHelper.setLocale(this, languageCode);
        recreate(); // Restart activity to apply changes
    }
}