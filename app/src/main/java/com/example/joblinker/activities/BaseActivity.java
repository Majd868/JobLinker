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

    // يطبّق اللغة المحفوظة قبل ربط سياق النشاط
    @Override
    protected void attachBaseContext(Context newBase) {
        String language = LocaleHelper.getLanguage(newBase);
        super.attachBaseContext(LocaleHelper.setLocale(newBase, language));
    }

    // يقرأ اللغة المحفوظة حالياً ويطبّقها على هذا النشاط
    protected void updateLanguage() {
        String language = LocaleHelper.getLanguage(this);
        LocaleHelper.setLocale(this, language);
    }

    // يغيّر لغة التطبيق ويعيد إنشاء النشاط لتطبيق الإعدادات المحلية الجديدة
    protected void changeLanguage(String language) {
        String languageCode = LocaleHelper.getLanguageCode(language);
        LocaleHelper.setLocale(this, languageCode);
        recreate(); // Restart activity to apply changes
    }
}