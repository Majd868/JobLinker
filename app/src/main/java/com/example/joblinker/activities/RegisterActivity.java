package com.example.joblinker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import com.example.joblinker.R;
import com.example.joblinker.adapters.RegisterPagerAdapter;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.fragments.RegisterStep1Fragment;
import com.example.joblinker.fragments.RegisterStep2Fragment;
import com.example.joblinker.fragments.RegisterStep3Fragment;
import com.example.joblinker.models.User;
import com.example.joblinker.utils.SharedPreferencesManager;

public class RegisterActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ViewPager2 viewPager;
    private MaterialButton btnBack, btnNext;
    private View indicatorStep1, indicatorStep2, indicatorStep3;

    private RegisterPagerAdapter pagerAdapter;
    private JobLinkerFirebaseManager firebaseManager;
    private SharedPreferencesManager prefsManager;

    private int currentStep = 0;

    // THESE HOLD YOUR DATA ACROSS ALL FRAGMENTS
    private User registrationUser;
    private String registrationPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseManager = JobLinkerFirebaseManager.getInstance();
        prefsManager = SharedPreferencesManager.getInstance(this);

        // Initialize the User object so it's never null
        registrationUser = new User();

        initializeViews();
        setupToolbar();
        setupViewPager();
        setupClickListeners();
        updateStepIndicators();
    }

    // يربط شريط الأدوات وعارض الصفحات وأزرار التنقل وعناصر مؤشر الخطوات
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.view_pager);
        btnBack = findViewById(R.id.btn_back);
        btnNext = findViewById(R.id.btn_next);
        indicatorStep1 = findViewById(R.id.indicator_step1);
        indicatorStep2 = findViewById(R.id.indicator_step2);
        indicatorStep3 = findViewById(R.id.indicator_step3);
    }

    // يضبط شريط الأدوات بمستمع للرجوع للخلف
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    // يضبط ViewPager2 مع تعطيل السحب وإضافة callback لتتبع الخطوة الحالية
    private void setupViewPager() {
        pagerAdapter = new RegisterPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setUserInputEnabled(false); // Disable swipe

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentStep = position;
                updateStepIndicators();
                updateButtons();
            }
        });
    }

    // يضبط مستمعي زرّي الرجوع والتالي للتنقل بين الخطوات وتشغيل التسجيل في الخطوة الأخيرة
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            if (currentStep > 0) {
                viewPager.setCurrentItem(currentStep - 1, true);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (validateCurrentStep()) {
                if (currentStep < 2) {
                    viewPager.setCurrentItem(currentStep + 1, true);
                } else {
                    completeRegistration();
                }
            }
        });
    }

    // يُضيء أشرطة مؤشر الخطوات حتى الخطوة الحالية بما فيها
    private void updateStepIndicators() {
        int primaryColor = getResources().getColor(R.color.primary, null);
        int dividerColor = getResources().getColor(R.color.divider, null);

        indicatorStep1.setBackgroundColor(currentStep >= 0 ? primaryColor : dividerColor);
        indicatorStep2.setBackgroundColor(currentStep >= 1 ? primaryColor : dividerColor);
        indicatorStep3.setBackgroundColor(currentStep >= 2 ? primaryColor : dividerColor);
    }

    // يبدّل ظهور زر الرجوع ويغيّر تسمية زر التالي إلى "اكتمال" في الخطوة الأخيرة
    private void updateButtons() {
        btnBack.setVisibility(currentStep > 0 ? View.VISIBLE : View.GONE);

        if (currentStep == 2) {
            btnNext.setText(R.string.complete_registration);
        } else {
            btnNext.setText(R.string.next);
        }
    }

    // يُعيد الجزء المرئي حالياً من ViewPager2 باستخدام وسم معرّفه الثابت
    private Fragment getCurrentFragment() {
        long itemId = pagerAdapter.getItemId(viewPager.getCurrentItem());
        return getSupportFragmentManager().findFragmentByTag("f" + itemId);
    }

    // يُفوّض التحقق من المدخلات إلى جزء الخطوة المرئي حالياً ويُعيد نتيجته
    private boolean validateCurrentStep() {
        Fragment currentFragment = getCurrentFragment();

        if (currentFragment instanceof RegisterStep1Fragment) {
            return ((RegisterStep1Fragment) currentFragment).validateAndSaveData();
        } else if (currentFragment instanceof RegisterStep2Fragment) {
            return ((RegisterStep2Fragment) currentFragment).validateAndSaveData();
        } else if (currentFragment instanceof RegisterStep3Fragment) {
            return true;
        }

        // If fragment is null, return FALSE so they can't skip validation
        return false;
    }

    // يُشغّل منطق إتمام التسجيل في جزء الخطوة الثالثة
    private void completeRegistration() {
        Fragment currentFragment = getCurrentFragment();

        if (currentFragment instanceof RegisterStep3Fragment) {
            ((RegisterStep3Fragment) currentFragment).validateAndCompleteRegistration();
        }
    }

    // يُستدعى من جزء الخطوة الثالثة عند اكتمال التسجيل بنجاح؛ يعرض رسالة ويتنقل إلى الشاشة الرئيسية
    public void onRegistrationComplete() {
        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
        navigateToMain();
    }

    // يشغّل MainActivity ويمسح مكدس التنقل بعد التسجيل الناجح
    private void navigateToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // يُعيد كائن المستخدم المشترك الذي يتراكم فيه البيانات عبر خطوات التسجيل
    public User getRegistrationUser() {
        return registrationUser;
    }

    // يحدّث كائن المستخدم المشترك بالبيانات المجمّعة في جزء خطوة التسجيل
    public void setRegistrationUser(User user) {
        this.registrationUser = user;
    }

    // يُعيد كلمة المرور المُدخلة أثناء التسجيل لاستخدامها عند إنشاء حساب Firebase
    public String getRegistrationPassword() {
        return registrationPassword;
    }

    // يخزّن كلمة المرور المقدَّمة في خطوة تسجيل لإنشاء الحساب لاحقاً
    public void setRegistrationPassword(String password) {
        this.registrationPassword = password;
    }

    // يُقدّم ViewPager2 إلى خطوة التسجيل التالية إذا لم تكن على الخطوة الأخيرة بعد
    public void moveToNextStep() {
        if (currentStep < 2) {
            viewPager.setCurrentItem(currentStep + 1, true);
        }
    }

    // ينتقل إلى خطوة التسجيل السابقة عند الضغط على زر الرجوع بدلاً من إغلاق النشاط
    @Override
    public void onBackPressed() {
        if (currentStep > 0) {
            viewPager.setCurrentItem(currentStep - 1, true);
        } else {
            super.onBackPressed();
        }
    }
}