package com.example.joblinker.activities;



import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import com.example.joblinker.R;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.models.Job;
import com.example.joblinker.utils.ValidationHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PostJobActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText etJobTitle, etCompanyName, etCity, etSalaryMin, etSalaryMax;
    private TextInputEditText etDescription, etDeadline, etSkillInput;
    private AutoCompleteTextView etCategory, etEmploymentType, etCountry;
    private MaterialButton btnAddSkill, btnCancel, btnPost;
    private ChipGroup chipGroupSkills;
    private View progressOverlay;

    private JobLinkerFirebaseManager firebaseManager;
    private List<String> skills;
    private long deadlineTimestamp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_job);

        firebaseManager = JobLinkerFirebaseManager.getInstance();
        skills = new ArrayList<>();

        initializeViews();
        setupToolbar();
        setupDropdowns();
        setupClickListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        etJobTitle = findViewById(R.id.et_job_title);
        etCompanyName = findViewById(R.id.et_company_name);
        etCategory = findViewById(R.id.et_category);
        etEmploymentType = findViewById(R.id.et_employment_type);
        etCountry = findViewById(R.id.et_country);
        etCity = findViewById(R.id.et_city);
        etSalaryMin = findViewById(R.id.et_salary_min);
        etSalaryMax = findViewById(R.id.et_salary_max);
        etSkillInput = findViewById(R.id.et_skill_input);
        btnAddSkill = findViewById(R.id.btn_add_skill);
        chipGroupSkills = findViewById(R.id.chip_group_skills);
        etDescription = findViewById(R.id.et_description);
        etDeadline = findViewById(R.id.et_deadline);
        btnCancel = findViewById(R.id.btn_cancel);
        btnPost = findViewById(R.id.btn_post);
        progressOverlay = findViewById(R.id.progress_overlay);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupDropdowns() {
        // Categories
        String[] categories = getResources().getStringArray(R.array.job_categories);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        etCategory.setAdapter(categoryAdapter);

        // Employment Types
        String[] employmentTypes = getResources().getStringArray(R.array.employment_types);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, employmentTypes);
        etEmploymentType.setAdapter(typeAdapter);

        // Countries
        String[] countries = getResources().getStringArray(R.array.countries);
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, countries);
        etCountry.setAdapter(countryAdapter);
    }

    private void setupClickListeners() {
        btnAddSkill.setOnClickListener(v -> addSkill());

        etDeadline.setOnClickListener(v -> showDatePicker());

        btnCancel.setOnClickListener(v -> finish());

        btnPost.setOnClickListener(v -> postJob());
    }

    private void addSkill() {
        String skill = etSkillInput.getText().toString().trim();

        if (ValidationHelper.isEmpty(skill)) {
            etSkillInput.setError("Enter a skill");
            return;
        }

        if (skills.contains(skill)) {
            Toast.makeText(this, "Skill already added", Toast.LENGTH_SHORT).show();
            return;
        }

        skills.add(skill);

        // Add chip
        Chip chip = new Chip(this);
        chip.setText(skill);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            chipGroupSkills.removeView(chip);
            skills.remove(skill);
        });
        chipGroupSkills.addView(chip);

        etSkillInput.setText("");
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    deadlineTimestamp = calendar.getTimeInMillis();
                    etDeadline.setText(String.format("%02d/%02d/%d", month + 1, dayOfMonth, year));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void postJob() {
        // Validate inputs
        String jobTitle = etJobTitle.getText().toString().trim();
        String companyName = etCompanyName.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String employmentType = etEmploymentType.getText().toString().trim();
        String country = etCountry.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String salaryMinStr = etSalaryMin.getText().toString().trim();
        String salaryMaxStr = etSalaryMax.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Validation
        if (ValidationHelper.isEmpty(jobTitle)) {
            etJobTitle.setError("Job title is required");
            etJobTitle.requestFocus();
            return;
        }

        if (ValidationHelper.isEmpty(companyName)) {
            etCompanyName.setError("Company name is required");
            etCompanyName.requestFocus();
            return;
        }

        if (ValidationHelper.isEmpty(category)) {
            etCategory.setError("Category is required");
            etCategory.requestFocus();
            return;
        }

        if (ValidationHelper.isEmpty(employmentType)) {
            etEmploymentType.setError("Employment type is required");
            etEmploymentType.requestFocus();
            return;
        }

        if (ValidationHelper.isEmpty(country)) {
            etCountry.setError("Country is required");
            etCountry.requestFocus();
            return;
        }

        if (ValidationHelper.isEmpty(description)) {
            etDescription.setError("Job description is required");
            etDescription.requestFocus();
            return;
        }

        // Create Job object
        Job job = new Job();
        job.setJobTitle(jobTitle);
        job.setJobCompany(companyName);
        job.setJobEmployerId(firebaseManager.getCurrentUserId());
        job.setJobCategory(category);
        job.setJobType(employmentType);
        job.setJobCountry(country);
        job.setJobCity(city);
        job.setJobDescription(description);
        job.setJobSkills(skills);
        job.setDeadline(deadlineTimestamp);

        // Parse salary
        if (!ValidationHelper.isEmpty(salaryMinStr)) {
            job.setJobSalaryMin(Double.parseDouble(salaryMinStr));
        }
        if (!ValidationHelper.isEmpty(salaryMaxStr)) {
            job.setJobSalaryMax(Double.parseDouble(salaryMaxStr));
        }

        // Show progress
        progressOverlay.setVisibility(View.VISIBLE);
        btnPost.setEnabled(false);

        // Post job to Firebase
        firebaseManager.createJob( job, new JobLinkerFirebaseManager.DataCallback<String>() {
            @Override
            public void onSuccess(String jobId) {
                progressOverlay.setVisibility(View.GONE);
                Toast.makeText(PostJobActivity.this,
                        R.string.job_posted_successfully, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                progressOverlay.setVisibility(View.GONE);
                btnPost.setEnabled(true);
                Toast.makeText(PostJobActivity.this,
                        "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}