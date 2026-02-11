package com.example.joblinker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.joblinker.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    // Views
    private MaterialToolbar toolbar;
    private FloatingActionButton fabChangePhoto;
    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private TextInputEditText etBio;
    private TextInputEditText etCity;
    private AutoCompleteTextView actvCountry;
    private AutoCompleteTextView actvLanguage;
    private AutoCompleteTextView actvCurrency;
    private RadioGroup rgUserRole;
    private MaterialRadioButton rbJobSeeker;
    private MaterialRadioButton rbEmployer;
    private MaterialCardView cardRoleInfo;
    private TextView tvRoleInfo;
    private MaterialButton btnSaveChanges;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private String userId;
    private String currentRole;

    // Data arrays
    private String[] countries;
    private String[] languages;
    private String[] currencies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        // Initialize data arrays
        initializeDataArrays();

        // Initialize views
        initViews();

        // Setup dropdowns
        setupDropdowns();

        // Load user data
        loadUserData();

        // Setup listeners
        setupListeners();
    }

    private void initializeDataArrays() {
        // Countries list
        countries = new String[]{
                "Select Country",
                "United States",
                "United Kingdom",
                "Canada",
                "Australia",
                "Germany",
                "France",
                "Italy",
                "Spain",
                "Netherlands",
                "Belgium",
                "Switzerland",
                "Austria",
                "Sweden",
                "Norway",
                "Denmark",
                "Finland",
                "Poland",
                "Czech Republic",
                "Portugal",
                "Ireland",
                "Greece",
                "Israel",
                "United Arab Emirates",
                "Saudi Arabia",
                "Qatar",
                "Kuwait",
                "Bahrain",
                "Oman",
                "Jordan",
                "Lebanon",
                "Egypt",
                "Morocco",
                "Tunisia",
                "Algeria",
                "India",
                "Pakistan",
                "Bangladesh",
                "Sri Lanka",
                "Nepal",
                "China",
                "Japan",
                "South Korea",
                "Singapore",
                "Malaysia",
                "Thailand",
                "Vietnam",
                "Philippines",
                "Indonesia",
                "New Zealand",
                "Brazil",
                "Mexico",
                "Argentina",
                "Chile",
                "Colombia",
                "Peru",
                "Venezuela",
                "South Africa",
                "Nigeria",
                "Kenya",
                "Ghana",
                "Ethiopia",
                "Tanzania",
                "Uganda",
                "Rwanda"
        };

        // Languages list
        languages = new String[]{
                "English",
                "Arabic",
                "Hebrew",
                "Spanish",
                "French",
                "German",
                "Italian",
                "Portuguese",
                "Russian",
                "Chinese",
                "Japanese",
                "Korean",
                "Hindi",
                "Urdu",
                "Bengali",
                "Dutch",
                "Swedish",
                "Norwegian",
                "Danish",
                "Finnish",
                "Polish",
                "Turkish",
                "Thai",
                "Vietnamese",
                "Indonesian",
                "Malay"
        };

        // Currencies list
        currencies = new String[]{
                "USD - US Dollar",
                "EUR - Euro",
                "GBP - British Pound",
                "CAD - Canadian Dollar",
                "AUD - Australian Dollar",
                "CHF - Swiss Franc",
                "JPY - Japanese Yen",
                "CNY - Chinese Yuan",
                "INR - Indian Rupee",
                "AED - UAE Dirham",
                "SAR - Saudi Riyal",
                "QAR - Qatari Riyal",
                "KWD - Kuwaiti Dinar",
                "BHD - Bahraini Dinar",
                "OMR - Omani Rial",
                "JOD - Jordanian Dinar",
                "EGP - Egyptian Pound",
                "ILS - Israeli Shekel",
                "ZAR - South African Rand",
                "BRL - Brazilian Real",
                "MXN - Mexican Peso",
                "SGD - Singapore Dollar",
                "HKD - Hong Kong Dollar",
                "NZD - New Zealand Dollar",
                "SEK - Swedish Krona",
                "NOK - Norwegian Krone",
                "DKK - Danish Krone",
                "PLN - Polish Zloty",
                "THB - Thai Baht",
                "MYR - Malaysian Ringgit",
                "IDR - Indonesian Rupiah",
                "PHP - Philippine Peso",
                "VND - Vietnamese Dong",
                "KRW - South Korean Won",
                "TRY - Turkish Lira"
        };
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        fabChangePhoto = findViewById(R.id.fabChangePhoto);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etBio = findViewById(R.id.etBio);
        etCity = findViewById(R.id.etCity);
        actvCountry = findViewById(R.id.actvCountry);
        actvLanguage = findViewById(R.id.actvLanguage);
        actvCurrency = findViewById(R.id.actvCurrency);
        rgUserRole = findViewById(R.id.rgUserRole);
        rbJobSeeker = findViewById(R.id.rbJobSeeker);
        rbEmployer = findViewById(R.id.rbEmployer);
        cardRoleInfo = findViewById(R.id.cardRoleInfo);
        tvRoleInfo = findViewById(R.id.tvRoleInfo);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        progressBar = findViewById(R.id.progressBar);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    private void setupDropdowns() {
        // Country dropdown
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                countries
        );
        actvCountry.setAdapter(countryAdapter);

        // Language dropdown
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                languages
        );
        actvLanguage.setAdapter(languageAdapter);

        // Currency dropdown
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                currencies
        );
        actvCurrency.setAdapter(currencyAdapter);
    }

    private void loadUserData() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // Load email from Firebase Auth
            etEmail.setText(currentUser.getEmail());

            // Load other data from Firestore
            firestore.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("userName");
                            String phone = documentSnapshot.getString("userPhone");
                            String bio = documentSnapshot.getString("userBio");
                            String city = documentSnapshot.getString("userCity");
                            String country = documentSnapshot.getString("userCountry");
                            String language = documentSnapshot.getString("userLanguage");
                            String currency = documentSnapshot.getString("userCurrency");
                            currentRole = documentSnapshot.getString("userRole");

                            // Set data to fields
                            if (name != null) etFullName.setText(name);
                            if (phone != null) etPhone.setText(phone);
                            if (bio != null) etBio.setText(bio);
                            if (city != null) etCity.setText(city);
                            if (country != null) actvCountry.setText(country, false);
                            if (language != null) actvLanguage.setText(language, false);
                            if (currency != null) actvCurrency.setText(currency, false);

                            // Set role
                            if ("employer".equals(currentRole)) {
                                rbEmployer.setChecked(true);
                                updateRoleInfo(false);
                            } else {
                                rbJobSeeker.setChecked(true);
                                updateRoleInfo(true);
                            }
                        } else {
                            // Load from SharedPreferences as fallback
                            loadFromSharedPreferences();
                        }
                        progressBar.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading user data", e);
                        loadFromSharedPreferences();
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
                    });
        } else {
            loadFromSharedPreferences();
            progressBar.setVisibility(View.GONE);
        }
    }

    private void loadFromSharedPreferences() {
        String name = getSharedPreferences("JobLinkerPrefs", MODE_PRIVATE)
                .getString("userName", "");
        String email = getSharedPreferences("JobLinkerPrefs", MODE_PRIVATE)
                .getString("userEmail", "");
        String phone = getSharedPreferences("JobLinkerPrefs", MODE_PRIVATE)
                .getString("userPhone", "");
        String city = getSharedPreferences("JobLinkerPrefs", MODE_PRIVATE)
                .getString("userCity", "");
        String country = getSharedPreferences("JobLinkerPrefs", MODE_PRIVATE)
                .getString("userCountry", "");
        String language = getSharedPreferences("JobLinkerPrefs", MODE_PRIVATE)
                .getString("userLanguage", "English");
        String currency = getSharedPreferences("JobLinkerPrefs", MODE_PRIVATE)
                .getString("userCurrency", "USD - US Dollar");
        String role = getSharedPreferences("JobLinkerPrefs", MODE_PRIVATE)
                .getString("userRole", "job_seeker");

        etFullName.setText(name);
        etEmail.setText(email);
        etPhone.setText(phone);
        etCity.setText(city);
        actvCountry.setText(country, false);
        actvLanguage.setText(language, false);
        actvCurrency.setText(currency, false);
        currentRole = role;

        if ("employer".equals(role)) {
            rbEmployer.setChecked(true);
            updateRoleInfo(false);
        } else {
            rbJobSeeker.setChecked(true);
            updateRoleInfo(true);
        }
    }

    private void setupListeners() {
        // Toolbar back button
        toolbar.setNavigationOnClickListener(v -> finish());

        // Change photo button
        fabChangePhoto.setOnClickListener(v -> showPhotoOptions());

        // Role selection
        rgUserRole.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isJobSeeker = (checkedId == R.id.rbJobSeeker);
            updateRoleInfo(isJobSeeker);
        });

        // Save button
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void updateRoleInfo(boolean isJobSeeker) {
        if (isJobSeeker) {
            tvRoleInfo.setText("As a Job Seeker, you can browse and apply for jobs posted by employers.");
        } else {
            tvRoleInfo.setText("As an Employer, you can post jobs and view applicants for your job postings.");
        }
    }

    private void showPhotoOptions() {
        String[] options = {"Take Photo", "Choose from Gallery", "Remove Photo"};

        new AlertDialog.Builder(this)
                .setTitle("Change Profile Picture")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            Toast.makeText(this, "Take Photo - Coming soon", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            Toast.makeText(this, "Choose from Gallery - Coming soon", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            Toast.makeText(this, "Remove Photo - Coming soon", Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
                .show();
    }

    private void saveChanges() {
        // Get values
        String name = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String country = actvCountry.getText().toString().trim();
        String language = actvLanguage.getText().toString().trim();
        String currency = actvCurrency.getText().toString().trim();
        String newRole = rbJobSeeker.isChecked() ? "job_seeker" : "employer";

        // Validation
        if (name.isEmpty()) {
            etFullName.setError(getString(R.string.error_required_field));
            etFullName.requestFocus();
            return;
        }

        if (country.isEmpty() || country.equals("Select Country")) {
            Toast.makeText(this, "Please select a country", Toast.LENGTH_SHORT).show();
            return;
        }

        if (language.isEmpty()) {
            Toast.makeText(this, "Please select a language", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currency.isEmpty()) {
            Toast.makeText(this, "Please select a currency", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if role changed
        if (!newRole.equals(currentRole)) {
            showRoleChangeConfirmation(name, phone, bio, city, country, language, currency, newRole);
        } else {
            updateProfile(name, phone, bio, city, country, language, currency, newRole);
        }
    }

    private void showRoleChangeConfirmation(String name, String phone, String bio, String city, String country, String language, String currency, String newRole) {
        String roleText = "employer".equals(newRole) ? "Employer" : "Job Seeker";

        new AlertDialog.Builder(this)
                .setTitle("Change User Role?")
                .setMessage("You are changing your role to " + roleText + ". This will affect how you use the app. Continue?")
                .setPositiveButton("Yes, Change", (dialog, which) ->
                        updateProfile(name, phone, bio, city, country, language, currency, newRole))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateProfile(String name, String phone, String bio, String city, String country, String language, String currency, String role) {
        progressBar.setVisibility(View.VISIBLE);
        btnSaveChanges.setEnabled(false);

        // Prepare data
        Map<String, Object> userData = new HashMap<>();
        userData.put("userName", name);
        userData.put("userPhone", phone);
        userData.put("userBio", bio);
        userData.put("userCity", city);
        userData.put("userCountry", country);
        userData.put("userLanguage", language);
        userData.put("userCurrency", currency);
        userData.put("userRole", role);
        userData.put("updatedAt", System.currentTimeMillis());

        // Update Firestore
        if (userId != null) {
            firestore.collection("users")
                    .document(userId)
                    .update(userData)
                    .addOnSuccessListener(aVoid -> {
                        // Update SharedPreferences
                        updateSharedPreferences(name, phone, bio, city, country, language, currency, role);

                        progressBar.setVisibility(View.GONE);
                        btnSaveChanges.setEnabled(true);

                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                        // Return to previous screen
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating profile", e);
                        progressBar.setVisibility(View.GONE);
                        btnSaveChanges.setEnabled(true);
                        Toast.makeText(this, "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Just update SharedPreferences if no user ID
            updateSharedPreferences(name, phone, bio, city, country, language, currency, role);
            progressBar.setVisibility(View.GONE);
            btnSaveChanges.setEnabled(true);
            Toast.makeText(this, "Profile updated locally", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateSharedPreferences(String name, String phone, String bio, String city, String country, String language, String currency, String role) {
        getSharedPreferences("JobLinkerPrefs", MODE_PRIVATE)
                .edit()
                .putString("userName", name)
                .putString("userPhone", phone)
                .putString("userBio", bio)
                .putString("userCity", city)
                .putString("userCountry", country)
                .putString("userLanguage", language)
                .putString("userCurrency", currency)
                .putString("userRole", role)
                .apply();

        currentRole = role;
    }
}