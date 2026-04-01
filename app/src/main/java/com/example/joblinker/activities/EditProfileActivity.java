package com.example.joblinker.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.joblinker.R;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    // Views
    private MaterialToolbar toolbar;
    private FloatingActionButton fabChangePhoto;
    private ImageView ivAvatar;
    private TextInputEditText etFullName, etEmail, etPhone, etBio, etCity;
    private AutoCompleteTextView actvCountry, actvLanguage, actvCurrency;
    private RadioGroup rgUserRole;
    private MaterialRadioButton rbJobSeeker, rbEmployer;
    private MaterialCardView cardRoleInfo;
    private TextView tvRoleInfo;
    private MaterialButton btnSaveChanges;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private JobLinkerFirebaseManager firebaseManager;
    private String userId;
    private String currentRole;

    // Photo state
    private Uri cameraImageUri;
    private String currentAvatarUrl;
    private boolean isUploadingPhoto = false; // guard: block Save while uploading

    // Data
    private String[] countries, languages, currencies;

    // ── Activity result launchers ─────────────────────
    private final ActivityResultLauncher<Intent> cameraLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && cameraImageUri != null) {
                // Grant read permission so ContentResolver can open the URI during upload
                try {
                    getContentResolver().takePersistableUriPermission(
                        cameraImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignored) {
                    // Not all URIs support persistable permissions — that's OK
                }
                showAvatarPreview(cameraImageUri);
                uploadAvatar(cameraImageUri);
            }
        });

    private final ActivityResultLauncher<Intent> galleryLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                if (imageUri != null) {
                    showAvatarPreview(imageUri);
                    uploadAvatar(imageUri);
                }
            }
        });

    // ─────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        firebaseAuth    = FirebaseAuth.getInstance();
        firestore       = FirebaseFirestore.getInstance();
        firebaseManager = JobLinkerFirebaseManager.getInstance();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) userId = currentUser.getUid();

        initializeDataArrays();
        initViews();
        setupDropdowns();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        toolbar        = findViewById(R.id.toolbar);
        fabChangePhoto = findViewById(R.id.fabChangePhoto);
        ivAvatar       = findViewById(R.id.ivProfilePicture);
        etFullName     = findViewById(R.id.etFullName);
        etEmail        = findViewById(R.id.etEmail);
        etPhone        = findViewById(R.id.etPhone);
        etBio          = findViewById(R.id.etBio);
        etCity         = findViewById(R.id.etCity);
        actvCountry    = findViewById(R.id.actvCountry);
        actvLanguage   = findViewById(R.id.actvLanguage);
        actvCurrency   = findViewById(R.id.actvCurrency);
        rgUserRole     = findViewById(R.id.rgUserRole);
        rbJobSeeker    = findViewById(R.id.rbJobSeeker);
        rbEmployer     = findViewById(R.id.rbEmployer);
        cardRoleInfo   = findViewById(R.id.cardRoleInfo);
        tvRoleInfo     = findViewById(R.id.tvRoleInfo);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        progressBar    = findViewById(R.id.progressBar);

        setSupportActionBar(toolbar);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());
        fabChangePhoto.setOnClickListener(v -> showPhotoOptions());
        rgUserRole.setOnCheckedChangeListener((group, checkedId) ->
            updateRoleInfo(checkedId == R.id.rbJobSeeker));
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    // ══════════════════════════════════════════════════
    // PHOTO HANDLING
    // ══════════════════════════════════════════════════

    private void showPhotoOptions() {
        String[] options = {"Take Photo", "Choose from Gallery", "Remove Photo"};
        new AlertDialog.Builder(this)
                .setTitle("Change Profile Picture")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: checkCameraPermissionAndOpen(); break;
                        case 1: openGallery();                  break;
                        case 2: removePhoto();                  break;
                    }
                })
                .show();
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            cameraImageUri = FileProvider.getUriForFile(this,
                getPackageName() + ".fileprovider", photoFile);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            cameraLauncher.launch(cameraIntent);
        } catch (IOException e) {
            Log.e(TAG, "Error creating image file", e);
            Toast.makeText(this, "Could not open camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent galleryIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            galleryIntent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        } else {
            galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        galleryLauncher.launch(galleryIntent);
    }

    private void removePhoto() {
        currentAvatarUrl = null;
        if (ivAvatar != null) {
            ivAvatar.setImageResource(R.drawable.ic_person_placeholder);
        }
        // Clear avatar in Firestore
        if (userId != null) {
            firestore.collection("users").document(userId)
                .update("avatarUrl", null)
                .addOnSuccessListener(v ->
                    Toast.makeText(this, "Photo removed", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to remove photo", Toast.LENGTH_SHORT).show());
        }
    }

    private void showAvatarPreview(Uri uri) {
        if (ivAvatar != null) {
            Glide.with(this).load(uri).circleCrop().into(ivAvatar);
        }
    }

    private void uploadAvatar(Uri imageUri) {
        if (userId == null) return;
        isUploadingPhoto = true;
        progressBar.setVisibility(View.VISIBLE);
        btnSaveChanges.setEnabled(false);
        btnSaveChanges.setText("Uploading photo…");

        // Use a unique path per upload so Firebase Storage doesn't cache old URL
        String path = "avatars/" + userId + "_" + System.currentTimeMillis() + ".jpg";

        firebaseManager.uploadImage(imageUri, path,
            new JobLinkerFirebaseManager.UploadCallback() {
                @Override public void onSuccess(String downloadUrl) {
                    currentAvatarUrl = downloadUrl;
                    isUploadingPhoto = false;
                    progressBar.setVisibility(View.GONE);
                    btnSaveChanges.setEnabled(true);
                    btnSaveChanges.setText("Save Changes");

                    // Preview already shown — also persist URL immediately so
                    // even if user closes without saving it's not lost
                    if (userId != null) {
                        firestore.collection("users").document(userId)
                            .update("avatarUrl", downloadUrl);
                    }
                    Toast.makeText(EditProfileActivity.this,
                        "Photo ready — tap Save to confirm all changes",
                        Toast.LENGTH_SHORT).show();
                }
                @Override public void onProgress(int progress) {
                    btnSaveChanges.setText("Uploading " + progress + "%…");
                }
                @Override public void onFailure(String error) {
                    isUploadingPhoto = false;
                    progressBar.setVisibility(View.GONE);
                    btnSaveChanges.setEnabled(true);
                    btnSaveChanges.setText("Save Changes");
                    Toast.makeText(EditProfileActivity.this,
                        "Photo upload failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    // ══════════════════════════════════════════════════
    // LOAD / SAVE PROFILE
    // ══════════════════════════════════════════════════

    private void loadUserData() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) { loadFromSharedPreferences(); return; }

        etEmail.setText(currentUser.getEmail());

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    setText(etFullName, doc.getString("userName"));
                    setText(etPhone,    doc.getString("userPhone"));
                    setText(etBio,      doc.getString("userBio"));
                    setText(etCity,     doc.getString("userCity"));
                    setDropdown(actvCountry,  doc.getString("userCountry"));
                    setDropdown(actvLanguage, doc.getString("userLanguage"));
                    setDropdown(actvCurrency, doc.getString("userCurrency"));
                    currentRole = doc.getString("userRole");
                    currentAvatarUrl = doc.getString("avatarUrl");

                    if (currentAvatarUrl != null && ivAvatar != null) {
                        Glide.with(this)
                            .load(currentAvatarUrl)
                            .circleCrop()
                            .skipMemoryCache(true)
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                            .placeholder(R.drawable.ic_person_placeholder)
                            .into(ivAvatar);
                    }

                    boolean isEmployer = "employer".equalsIgnoreCase(currentRole);
                    if (isEmployer) rbEmployer.setChecked(true);
                    else            rbJobSeeker.setChecked(true);
                    updateRoleInfo(!isEmployer);
                } else {
                    loadFromSharedPreferences();
                }
                progressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                loadFromSharedPreferences();
                progressBar.setVisibility(View.GONE);
            });
    }

    private void saveChanges() {
        // Block save if photo is still uploading
        if (isUploadingPhoto) {
            Toast.makeText(this, "Please wait — photo is still uploading…",
                Toast.LENGTH_SHORT).show();
            return;
        }

        String name     = etFullName.getText().toString().trim();
        String phone    = etPhone.getText().toString().trim();
        String bio      = etBio.getText().toString().trim();
        String city     = etCity.getText().toString().trim();
        String country  = actvCountry.getText().toString().trim();
        String language = actvLanguage.getText().toString().trim();
        String currency = actvCurrency.getText().toString().trim();
        String newRole  = rbJobSeeker.isChecked() ? "job_seeker" : "employer";

        if (name.isEmpty()) {
            etFullName.setError(getString(R.string.error_required_field));
            etFullName.requestFocus(); return;
        }
        if (country.isEmpty() || country.equals("Select Country")) {
            Toast.makeText(this, "Please select a country", Toast.LENGTH_SHORT).show(); return;
        }
        if (language.isEmpty()) {
            Toast.makeText(this, "Please select a language", Toast.LENGTH_SHORT).show(); return;
        }
        if (currency.isEmpty()) {
            Toast.makeText(this, "Please select a currency", Toast.LENGTH_SHORT).show(); return;
        }

        if (!newRole.equals(currentRole)) {
            new AlertDialog.Builder(this)
                .setTitle("Change User Role?")
                .setMessage("You are changing your role to " +
                    ("employer".equals(newRole) ? "Employer" : "Job Seeker") +
                    ". This will affect how you use the app. Continue?")
                .setPositiveButton("Yes, Change", (d, w) ->
                    updateProfile(name, phone, bio, city, country, language, currency, newRole))
                .setNegativeButton("Cancel", null)
                .show();
        } else {
            updateProfile(name, phone, bio, city, country, language, currency, newRole);
        }
    }

    private void updateProfile(String name, String phone, String bio,
                               String city, String country, String language,
                               String currency, String role) {
        progressBar.setVisibility(View.VISIBLE);
        btnSaveChanges.setEnabled(false);

        Map<String, Object> data = new HashMap<>();
        data.put("userName",    name);
        data.put("userPhone",   phone);
        data.put("userBio",     bio);
        data.put("userCity",    city);
        data.put("userCountry", country);
        data.put("userLanguage", language);
        data.put("userCurrency", currency);
        data.put("userRole",    role);
        data.put("updatedAt",   System.currentTimeMillis());
        // Always write avatarUrl — null clears it (remove photo), non-null updates it
        data.put("avatarUrl", currentAvatarUrl);

        if (userId != null) {
            firestore.collection("users").document(userId).update(data)
                .addOnSuccessListener(v -> {
                    updateSharedPreferences(name, phone, bio, city, country, language, currency, role);
                    progressBar.setVisibility(View.GONE);
                    btnSaveChanges.setEnabled(true);
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSaveChanges.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        }
    }

    // ── Helpers ───────────────────────────────────────
    private void setText(TextInputEditText et, String value) {
        if (et != null && value != null) et.setText(value);
    }
    private void setDropdown(AutoCompleteTextView tv, String value) {
        if (tv != null && value != null) tv.setText(value, false);
    }

    private void updateRoleInfo(boolean isJobSeeker) {
        if (tvRoleInfo == null) return;
        tvRoleInfo.setText(isJobSeeker
            ? "As a Job Seeker, you can browse and apply for jobs posted by employers."
            : "As an Employer, you can post jobs and view applicants for your job postings.");
    }

    private void loadFromSharedPreferences() {
        android.content.SharedPreferences prefs =
            getSharedPreferences("JobLinkerPrefs", MODE_PRIVATE);
        setText(etFullName, prefs.getString("userName", ""));
        setText(etPhone,    prefs.getString("userPhone", ""));
        setText(etBio,      prefs.getString("userBio", ""));
        setText(etCity,     prefs.getString("userCity", ""));
        setDropdown(actvCountry,  prefs.getString("userCountry", ""));
        setDropdown(actvLanguage, prefs.getString("userLanguage", "English"));
        setDropdown(actvCurrency, prefs.getString("userCurrency", "USD - US Dollar"));
        currentRole = prefs.getString("userRole", "job_seeker");
        boolean isEmployer = "employer".equalsIgnoreCase(currentRole);
        if (isEmployer) rbEmployer.setChecked(true);
        else            rbJobSeeker.setChecked(true);
        updateRoleInfo(!isEmployer);
        progressBar.setVisibility(View.GONE);
    }

    private void updateSharedPreferences(String name, String phone, String bio,
                                         String city, String country, String language,
                                         String currency, String role) {
        getSharedPreferences("JobLinkerPrefs", MODE_PRIVATE).edit()
            .putString("userName",    name)
            .putString("userPhone",   phone)
            .putString("userBio",     bio)
            .putString("userCity",    city)
            .putString("userCountry", country)
            .putString("userLanguage", language)
            .putString("userCurrency", currency)
            .putString("userRole",    role)
            .apply();
        currentRole = role;
    }

    // ── Data arrays ───────────────────────────────────
    private void initializeDataArrays() {
        countries = new String[]{"Select Country","United States","United Kingdom","Canada",
            "Australia","Germany","France","Italy","Spain","Netherlands","Belgium","Switzerland",
            "Austria","Sweden","Norway","Denmark","Finland","Poland","Czech Republic","Portugal",
            "Ireland","Greece","Israel","United Arab Emirates","Saudi Arabia","Qatar","Kuwait",
            "Bahrain","Oman","Jordan","Lebanon","Egypt","Morocco","Tunisia","Algeria","India",
            "Pakistan","Bangladesh","Sri Lanka","Nepal","China","Japan","South Korea","Singapore",
            "Malaysia","Thailand","Vietnam","Philippines","Indonesia","New Zealand","Brazil",
            "Mexico","Argentina","Chile","Colombia","Peru","Venezuela","South Africa","Nigeria",
            "Kenya","Ghana","Ethiopia","Tanzania","Uganda","Rwanda"};

        languages = new String[]{"English","Arabic","Hebrew","Spanish","French","German",
            "Italian","Portuguese","Russian","Chinese","Japanese","Korean","Hindi","Urdu",
            "Bengali","Dutch","Swedish","Norwegian","Danish","Finnish","Polish","Turkish",
            "Thai","Vietnamese","Indonesian","Malay"};

        currencies = new String[]{"USD - US Dollar","EUR - Euro","GBP - British Pound",
            "CAD - Canadian Dollar","AUD - Australian Dollar","CHF - Swiss Franc",
            "JPY - Japanese Yen","CNY - Chinese Yuan","INR - Indian Rupee","AED - UAE Dirham",
            "SAR - Saudi Riyal","QAR - Qatari Riyal","KWD - Kuwaiti Dinar","ILS - Israeli Shekel",
            "EGP - Egyptian Pound","ZAR - South African Rand","BRL - Brazilian Real",
            "MXN - Mexican Peso","SGD - Singapore Dollar","NZD - New Zealand Dollar"};
    }

    private void setupDropdowns() {
        actvCountry.setAdapter(new ArrayAdapter<>(this,
            android.R.layout.simple_dropdown_item_1line, countries));
        actvLanguage.setAdapter(new ArrayAdapter<>(this,
            android.R.layout.simple_dropdown_item_1line, languages));
        actvCurrency.setAdapter(new ArrayAdapter<>(this,
            android.R.layout.simple_dropdown_item_1line, currencies));
    }
}
