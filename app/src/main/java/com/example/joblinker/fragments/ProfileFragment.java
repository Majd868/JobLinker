package com.example.joblinker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import com.example.joblinker.R;
import com.bumptech.glide.Glide;
import com.example.joblinker.activities.EditProfileActivity;
import com.example.joblinker.activities.LoginActivity;
import com.example.joblinker.activities.SavedJobsActivity;
import com.example.joblinker.activities.SettingActivity;
import com.example.joblinker.adapters.JobAdapter;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.models.Job;
import com.example.joblinker.models.User;
import com.example.joblinker.utils.ImageUtils;
import com.example.joblinker.utils.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    // ── Views ──────────────────────────────────────────
    private ImageView ivAvatar;
    private View viewOnline;
    private TextView tvUserName, tvJobsCount, tvRating, tvConnectionsCount;
    private TextView tvLocation, tvEmail, tvPhone, tvBio;
    private Chip chipRole;
    private MaterialButton btnEditProfile, btnSettings, btnLogout;
    private LinearLayout layoutLocation, layoutEmail, layoutPhone, layoutSkills, layoutMyJobs;
    private LinearLayout layoutSavedJobs;           // shown for job seekers
    private TextView tvSavedJobsCount;
    private ChipGroup chipGroupSkills;
    private RecyclerView recyclerMyJobs;

    // ── Data ───────────────────────────────────────────
    private JobLinkerFirebaseManager firebaseManager;
    private SharedPreferencesManager prefsManager;
    private User currentUser;
    private List<Job> myJobs;
    private JobAdapter jobAdapter;

    // ─────────────────────────────────────────────────
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseManager = JobLinkerFirebaseManager.getInstance();
        prefsManager    = SharedPreferencesManager.getInstance(requireContext());
        myJobs          = new ArrayList<>();

        initializeViews(view);
        setupClickListeners();
        setupRecyclerView();
        loadUserProfile();

        return view;
    }

    // ── View binding ──────────────────────────────────
    private void initializeViews(View view) {
        ivAvatar            = view.findViewById(R.id.iv_avatar);
        viewOnline          = view.findViewById(R.id.view_online);
        tvUserName          = view.findViewById(R.id.tv_user_name);
        chipRole            = view.findViewById(R.id.chip_role);
        tvJobsCount         = view.findViewById(R.id.tv_jobs_count);
        tvRating            = view.findViewById(R.id.tv_rating);
        tvConnectionsCount  = view.findViewById(R.id.tv_connections_count);
        layoutLocation      = view.findViewById(R.id.layout_location);
        tvLocation          = view.findViewById(R.id.tv_location);
        layoutEmail         = view.findViewById(R.id.layout_email);
        tvEmail             = view.findViewById(R.id.tv_email);
        layoutPhone         = view.findViewById(R.id.layout_phone);
        tvPhone             = view.findViewById(R.id.tv_phone);
        tvBio               = view.findViewById(R.id.tv_bio);
        layoutSkills        = view.findViewById(R.id.layout_skills);
        chipGroupSkills     = view.findViewById(R.id.chip_group_skills);
        layoutMyJobs        = view.findViewById(R.id.layout_my_jobs);
        recyclerMyJobs      = view.findViewById(R.id.recycler_my_jobs);
        layoutSavedJobs     = view.findViewById(R.id.layout_saved_jobs);   // optional view
        tvSavedJobsCount    = view.findViewById(R.id.tv_saved_jobs_count); // optional view
        btnEditProfile      = view.findViewById(R.id.btn_edit_profile);
        btnSettings         = view.findViewById(R.id.btn_settings);
        btnLogout           = view.findViewById(R.id.btn_logout);
    }

    // ── Click listeners ───────────────────────────────
    private void setupClickListeners() {

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), EditProfileActivity.class)));
        }

        if (btnSettings != null) {
            btnSettings.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SettingActivity.class)));
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showLogoutDialog());
        }

        // Tap on saved-jobs row → open saved jobs screen
        if (layoutSavedJobs != null) {
            layoutSavedJobs.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(requireContext(), SavedJobsActivity.class));
                } catch (Exception e) {
                    Toast.makeText(requireContext(),
                        "Error opening saved jobs", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupRecyclerView() {
        if (recyclerMyJobs == null) return;
        jobAdapter = new JobAdapter(requireContext(), myJobs);
        recyclerMyJobs.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerMyJobs.setAdapter(jobAdapter);
        recyclerMyJobs.setNestedScrollingEnabled(false);
    }

    // ══════════════════════════════════════════════════
    // DATA LOADING
    // ══════════════════════════════════════════════════

    public void loadUserProfile() {
        String userId = firebaseManager.getCurrentUserId();
        if (userId == null) return;

        firebaseManager.getUser(userId, new JobLinkerFirebaseManager.DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (!isAdded()) return;
                currentUser = user;
                displayUserProfile(user);
                loadStats(user);
            }

            @Override
            public void onFailure(String error) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                    "Error loading profile: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Display profile data ──────────────────────────
    private void displayUserProfile(User user) {
        // Avatar
        ImageUtils.loadCircularImage(requireContext(), user.getAvatarUrl(), ivAvatar);

        // Online indicator
        if (viewOnline != null)
            viewOnline.setVisibility(user.isOnline() ? View.VISIBLE : View.GONE);

        // Name & role chip
        if (tvUserName != null)
            tvUserName.setText(user.getUserName() != null ? user.getUserName() : "");
        if (chipRole != null) {
            boolean isEmployer = "employer".equalsIgnoreCase(user.getUserRole());
            chipRole.setText(isEmployer ? "Employer" : "Job Seeker");
        }

        // Location
        String location = buildLocation(user);
        if (layoutLocation != null) {
            if (!location.isEmpty()) {
                if (tvLocation != null) tvLocation.setText(location);
                layoutLocation.setVisibility(View.VISIBLE);
            } else {
                layoutLocation.setVisibility(View.GONE);
            }
        }

        // Email
        if (layoutEmail != null) {
            if (user.getUserEmail() != null && !user.getUserEmail().isEmpty()) {
                if (tvEmail != null) tvEmail.setText(user.getUserEmail());
                layoutEmail.setVisibility(View.VISIBLE);
            } else {
                layoutEmail.setVisibility(View.GONE);
            }
        }

        // Phone
        if (layoutPhone != null) {
            if (user.getUserPhone() != null && !user.getUserPhone().isEmpty()) {
                if (tvPhone != null) tvPhone.setText(user.getUserPhone());
                layoutPhone.setVisibility(View.VISIBLE);
            } else {
                layoutPhone.setVisibility(View.GONE);
            }
        }

        // Bio
        if (tvBio != null) {
            if (user.getUserBio() != null && !user.getUserBio().isEmpty()) {
                tvBio.setText(user.getUserBio());
                tvBio.setVisibility(View.VISIBLE);
            } else {
                tvBio.setVisibility(View.GONE);
            }
        }

        // Skills (job seekers only)
        boolean isJobSeeker = !"employer".equalsIgnoreCase(user.getUserRole());
        if (layoutSkills != null) {
            if (isJobSeeker && user.getUserSkills() != null && !user.getUserSkills().isEmpty()) {
                layoutSkills.setVisibility(View.VISIBLE);
                if (chipGroupSkills != null) {
                    chipGroupSkills.removeAllViews();
                    for (String skill : user.getUserSkills()) {
                        Chip chip = new Chip(requireContext());
                        chip.setText(skill);
                        chip.setClickable(false);
                        chipGroupSkills.addView(chip);
                    }
                }
            } else {
                layoutSkills.setVisibility(View.GONE);
            }
        }

        // My Jobs section (employers only)
        boolean isEmployer = "employer".equalsIgnoreCase(user.getUserRole());
        if (layoutMyJobs != null) {
            if (isEmployer) {
                layoutMyJobs.setVisibility(View.VISIBLE);
                loadMyJobs(user.getUserId());
            } else {
                layoutMyJobs.setVisibility(View.GONE);
            }
        }

        // Saved jobs section (job seekers only)
        if (layoutSavedJobs != null) {
            layoutSavedJobs.setVisibility(isJobSeeker ? View.VISIBLE : View.GONE);
        }
    }

    // ── Load stats (counts) ───────────────────────────
    private void loadStats(User user) {
        String userId = user.getUserId();
        boolean isEmployer = "employer".equalsIgnoreCase(user.getUserRole());

        // Jobs count
        if (isEmployer) {
            firebaseManager.getJobsByEmployer(userId,
                new JobLinkerFirebaseManager.ListCallback<Job>() {
                    @Override public void onSuccess(List<Job> jobs) {
                        if (!isAdded()) return;
                        if (tvJobsCount != null)
                            tvJobsCount.setText(String.valueOf(jobs.size()));
                    }
                    @Override public void onFailure(String error) {
                        if (tvJobsCount != null) tvJobsCount.setText("0");
                    }
                });
        } else {
            // For job seekers show their applications count
            firebaseManager.getApplicationsByJobSeeker(userId,
                new JobLinkerFirebaseManager.ListCallback<com.example.joblinker.models.Application>() {
                    @Override public void onSuccess(List<com.example.joblinker.models.Application> apps) {
                        if (!isAdded()) return;
                        if (tvJobsCount != null)
                            tvJobsCount.setText(String.valueOf(apps.size()));
                    }
                    @Override public void onFailure(String error) {
                        if (tvJobsCount != null) tvJobsCount.setText("0");
                    }
                });
        }

        // Saved jobs count (job seekers)
        if (!isEmployer) {
            firebaseManager.getSavedJobs(userId,
                new JobLinkerFirebaseManager.ListCallback<String>() {
                    @Override public void onSuccess(List<String> savedIds) {
                        if (!isAdded()) return;
                        if (tvSavedJobsCount != null)
                            tvSavedJobsCount.setText(String.valueOf(savedIds.size()));
                    }
                    @Override public void onFailure(String error) {
                        if (tvSavedJobsCount != null) tvSavedJobsCount.setText("0");
                    }
                });
        }

        // Rating — default 5.0 until you build a reviews system
        if (tvRating != null) tvRating.setText("5.0");

        // Connections — count from conversations (unique chat partners)
        firebaseManager.getUserConversations(userId,
            new JobLinkerFirebaseManager.ListCallback<com.example.joblinker.models.Conversation>() {
                @Override public void onSuccess(List<com.example.joblinker.models.Conversation> convs) {
                    if (!isAdded()) return;
                    if (tvConnectionsCount != null)
                        tvConnectionsCount.setText(String.valueOf(convs.size()));
                }
                @Override public void onFailure(String error) {
                    if (tvConnectionsCount != null) tvConnectionsCount.setText("0");
                }
            });
    }

    // ── Load employer's own jobs ───────────────────────
    private void loadMyJobs(String employerId) {
        firebaseManager.getJobsByEmployer(employerId,
            new JobLinkerFirebaseManager.ListCallback<Job>() {
                @Override
                public void onSuccess(List<Job> jobs) {
                    if (!isAdded()) return;
                    myJobs.clear();
                    myJobs.addAll(jobs);
                    if (jobAdapter != null) jobAdapter.notifyDataSetChanged();
                    if (tvJobsCount != null)
                        tvJobsCount.setText(String.valueOf(jobs.size()));
                }

                @Override
                public void onFailure(String error) {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(),
                        "Error loading jobs: " + error, Toast.LENGTH_SHORT).show();
                }
            });
    }

    // ── Helpers ───────────────────────────────────────
    private String buildLocation(User user) {
        String city    = user.getUserCity();
        String country = user.getUserCountry();
        if (city != null && country != null && !city.isEmpty() && !country.isEmpty())
            return city + ", " + country;
        if (city != null && !city.isEmpty()) return city;
        if (country != null && !country.isEmpty()) return country;
        return "";
    }

    // ── Logout dialog ─────────────────────────────────
    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.logout)
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton(R.string.yes, (dialog, which) -> performLogout())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void performLogout() {
        String userId = firebaseManager.getCurrentUserId();
        if (userId != null) {
            firebaseManager.updateUserOnlineStatus(userId, false);
        }
        firebaseManager.logout();
        prefsManager.clearAll();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Clear Glide memory cache so the updated avatar is always re-fetched
        // from Firestore instead of showing the stale cached version
        Glide.get(requireContext()).clearMemory();
        loadUserProfile();
    }
}
