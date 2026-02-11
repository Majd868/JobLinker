package com.example.joblinker.activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import com.example.joblinker.R;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.models.Job;
import com.example.joblinker.models.User;
import com.example.joblinker.utils.DateTimeHelper;
import com.example.joblinker.utils.ImageUtils;
import com.example.joblinker.utils.SharedPreferencesManager;

public class JobDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_JOB_ID = "job_id";
    public static final String EXTRA_JOB = "job";

    private MaterialToolbar toolbar;
    private NestedScrollView scrollView;
    private ProgressBar progressBar;

    // Header Views
    private ImageView ivCompanyLogo;
    private TextView tvJobTitle;
    private TextView tvCompanyName;
    private TextView tvLocation;
    private TextView tvSalary;
    private TextView tvPostedTime;
    private Chip chipCategory;
    private Chip chipJobType;
    private ImageButton btnSave;

    // Details Views
    private TextView tvDescription;
    private ChipGroup chipGroupSkills;
    private TextView tvDeadline;
    private TextView tvApplications;
    private TextView tvViews;

    // Employer Info
    private LinearLayout layoutEmployerInfo;
    private ImageView ivEmployerAvatar;
    private TextView tvEmployerName;
    private TextView tvEmployerEmail;
    private MaterialButton btnContactEmployer;

    // Action Buttons
    private MaterialButton btnApply;
    private MaterialButton btnShare;

    private JobLinkerFirebaseManager firebaseManager;
    private SharedPreferencesManager prefsManager;
    private Job currentJob;
    private String jobId;
    private boolean isSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_details);

        firebaseManager = JobLinkerFirebaseManager.getInstance();
        prefsManager = SharedPreferencesManager.getInstance(this);

        initializeViews();
        setupToolbar();
        getIntentData();
        setupClickListeners();
        loadJobDetails();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        scrollView = findViewById(R.id.scroll_view);
        progressBar = findViewById(R.id.progress_bar);

        // Header
        ivCompanyLogo = findViewById(R.id.iv_company_logo);
        tvJobTitle = findViewById(R.id.tv_job_title);
        tvCompanyName = findViewById(R.id.tv_company_name);
        tvLocation = findViewById(R.id.tv_location);
        tvSalary = findViewById(R.id.tv_salary);
        tvPostedTime = findViewById(R.id.tv_posted_time);
        chipCategory = findViewById(R.id.chip_category);
        chipJobType = findViewById(R.id.chip_job_type);
        btnSave = findViewById(R.id.btn_save);

        // Details
        tvDescription = findViewById(R.id.tv_description);
        chipGroupSkills = findViewById(R.id.chip_group_skills);
        tvDeadline = findViewById(R.id.tv_deadline);
        tvApplications = findViewById(R.id.tv_applications);
        tvViews = findViewById(R.id.tv_views);

        // Employer Info
        layoutEmployerInfo = findViewById(R.id.layout_employer_info);
        ivEmployerAvatar = findViewById(R.id.iv_employer_avatar);
        tvEmployerName = findViewById(R.id.tv_employer_name);
        tvEmployerEmail = findViewById(R.id.tv_employer_email);
        btnContactEmployer = findViewById(R.id.btn_contact_employer);

        // Action Buttons
        btnApply = findViewById(R.id.btn_apply);
        btnShare = findViewById(R.id.btn_share);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void getIntentData() {
        Intent intent = getIntent();

        // Try to get job object first
        if (intent.hasExtra(EXTRA_JOB)) {
            currentJob = (Job) intent.getSerializableExtra(EXTRA_JOB);
            if (currentJob != null) {
                jobId = currentJob.getJobId();
            }
        }

        // Fall back to job ID
        if (jobId == null && intent.hasExtra(EXTRA_JOB_ID)) {
            jobId = intent.getStringExtra(EXTRA_JOB_ID);
        }
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> toggleSaveJob());
        btnApply.setOnClickListener(v -> applyForJob());
        btnShare.setOnClickListener(v -> shareJob());
        btnContactEmployer.setOnClickListener(v -> contactEmployer());
    }

    private void loadJobDetails() {
        if (currentJob != null) {
            displayJobDetails(currentJob);
            incrementViewCount();
            checkIfSaved();
        } else if (jobId != null) {
            showLoading(true);
            firebaseManager.getJob(jobId, new JobLinkerFirebaseManager.DataCallback<Job>() {
                @Override
                public void onSuccess(Job job) {
                    showLoading(false);
                    currentJob = job;
                    displayJobDetails(job);
                    incrementViewCount();
                    checkIfSaved();
                }

                @Override
                public void onFailure(String error) {
                    showLoading(false);
                    Toast.makeText(JobDetailsActivity.this,
                            "Error loading job: " + error, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            Toast.makeText(this, "Invalid job", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayJobDetails(Job job) {
        // Header
        ImageUtils.loadCompanyLogo(this, job.getCompanyLogoUrl(), ivCompanyLogo);
        tvJobTitle.setText(job.getJobTitle());
        tvCompanyName.setText(job.getJobCompany());
        tvLocation.setText(job.getLocation());
        tvSalary.setText(job.getSalaryRange());
        tvPostedTime.setText(DateTimeHelper.getRelativeTime(job.getCreatedAt()));
        chipCategory.setText(job.getJobCategory());
        chipJobType.setText(job.getJobType());

        // Description
        tvDescription.setText(job.getJobDescription());

        // Skills
        if (job.getJobSkills() != null && !job.getJobSkills().isEmpty()) {
            chipGroupSkills.setVisibility(View.VISIBLE);
            chipGroupSkills.removeAllViews();

            for (String skill : job.getJobSkills()) {
                Chip chip = new Chip(this);
                chip.setText(skill);
                chip.setClickable(false);
                chip.setChipBackgroundColorResource(R.color.surface_dark);
                chipGroupSkills.addView(chip);
            }
        } else {
            chipGroupSkills.setVisibility(View.GONE);
        }

        // Deadline
        if (job.getDeadline() > 0) {
            tvDeadline.setText(DateTimeHelper.formatDate(job.getDeadline()));
            tvDeadline.setVisibility(View.VISIBLE);
        } else {
            tvDeadline.setVisibility(View.GONE);
        }

        // Statistics
        tvApplications.setText(String.valueOf(job.getApplicantCount()));
        tvViews.setText(String.valueOf(job.getViewCount()));

        // Load employer info
        loadEmployerInfo(job.getJobEmployerId());

        // Hide apply button if user is employer
        if (prefsManager.isEmployer()) {
            btnApply.setVisibility(View.GONE);
        }
    }

    private void loadEmployerInfo(String employerId) {
        firebaseManager.getUser(employerId, new JobLinkerFirebaseManager.DataCallback<User>() {
            @Override
            public void onSuccess(User employer) {
                layoutEmployerInfo.setVisibility(View.VISIBLE);
                ImageUtils.loadCircularImage(JobDetailsActivity.this,
                        employer.getAvatarUrl(), ivEmployerAvatar);
                tvEmployerName.setText(employer.getUserName());
                tvEmployerEmail.setText(employer.getUserEmail());
            }

            @Override
            public void onFailure(String error) {
                layoutEmployerInfo.setVisibility(View.GONE);
            }
        });
    }

    private void incrementViewCount() {
        if (jobId != null) {
            firebaseManager.incrementJobViewCount(jobId);
        }
    }

    private void checkIfSaved() {
        String userId = firebaseManager.getCurrentUserId();
        if (userId != null && jobId != null) {
            firebaseManager.hasUserSavedJob(userId, jobId,
                    new JobLinkerFirebaseManager.DataCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean saved) {
                            isSaved = saved;
                            updateSaveButton();
                        }

                        @Override
                        public void onFailure(String error) {
                            // Ignore error
                        }
                    });
        }
    }

    private void toggleSaveJob() {
        String userId = firebaseManager.getCurrentUserId();
        if (userId == null || jobId == null) {
            Toast.makeText(this, "Please login to save jobs", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);

        if (isSaved) {
            // Unsave
            firebaseManager.unsaveJob(userId, jobId, new JobLinkerFirebaseManager.VoidCallback() {
                @Override
                public void onSuccess() {
                    isSaved = false;
                    updateSaveButton();
                    btnSave.setEnabled(true);
                    Toast.makeText(JobDetailsActivity.this,
                            "Job removed from saved", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String error) {
                    btnSave.setEnabled(true);
                    Toast.makeText(JobDetailsActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Save
            firebaseManager.saveJob(userId, jobId, new JobLinkerFirebaseManager.VoidCallback() {
                @Override
                public void onSuccess() {
                    isSaved = true;
                    updateSaveButton();
                    btnSave.setEnabled(true);
                    Toast.makeText(JobDetailsActivity.this,
                            "Job saved successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String error) {
                    btnSave.setEnabled(true);
                    Toast.makeText(JobDetailsActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateSaveButton() {
        if (isSaved) {
            btnSave.setImageResource(R.drawable.ic_bookmark);
        } else {
            btnSave.setImageResource(R.drawable.ic_bookmark_border);
        }
    }

    private void applyForJob() {
        if (currentJob == null) return;

        String userId = firebaseManager.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(this, "Please login to apply", Toast.LENGTH_SHORT).show();
            return;
        }

        btnApply.setEnabled(false);
        btnApply.setText("Applying...");

        firebaseManager.addJobApplicant(jobId, userId, new JobLinkerFirebaseManager.VoidCallback() {
            @Override
            public void onSuccess() {
                btnApply.setText("Applied");
                Toast.makeText(JobDetailsActivity.this,
                        "Application submitted successfully!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                btnApply.setEnabled(true);
                btnApply.setText(R.string.apply_now);
                Toast.makeText(JobDetailsActivity.this,
                        "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareJob() {
        if (currentJob == null) return;

        String shareText = String.format(
                "Check out this job opportunity!\n\n" +
                        "%s at %s\n\n" +
                        "Location: %s\n" +
                        "Salary: %s\n\n" +
                        "Shared via JobLinker",
                currentJob.getJobTitle(),
                currentJob.getJobCompany(),
                currentJob.getLocation(),
                currentJob.getSalaryRange()
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, currentJob.getJobTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share job via"));
    }

    private void contactEmployer() {
        if (currentJob == null) return;

        // Navigate to chat with employer
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_USER_ID, currentJob.getJobEmployerId());
        intent.putExtra(ChatActivity.EXTRA_USER_NAME, tvEmployerName.getText().toString());
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        scrollView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_job_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_share) {
            shareJob();
            return true;
        } else if (id == R.id.action_report) {
            Toast.makeText(this, "Report job feature coming soon", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}