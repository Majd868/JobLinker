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

import com.example.joblinker.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import com.example.joblinker.R;
import com.example.joblinker.activities.EditProfileActivity;
import com.example.joblinker.activities.LoginActivity;
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

    private ImageView ivAvatar;
    private View viewOnline;
    private TextView tvUserName, tvJobsCount, tvRating, tvConnectionsCount;
    private TextView tvLocation, tvEmail, tvPhone, tvBio;
    private Chip chipRole;
    private MaterialButton btnEditProfile, btnSettings, btnLogout;
    private LinearLayout layoutLocation, layoutEmail, layoutPhone, layoutSkills, layoutMyJobs;
    private ChipGroup chipGroupSkills;
    private RecyclerView recyclerMyJobs;

    private JobLinkerFirebaseManager firebaseManager;
    private SharedPreferencesManager prefsManager;
    private User currentUser;
    private List<Job> myJobs;
    private JobAdapter jobAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseManager = JobLinkerFirebaseManager.getInstance();
        prefsManager = SharedPreferencesManager.getInstance(requireContext());
        myJobs = new ArrayList<>();

        initializeViews(view);
        setupClickListeners();
        setupRecyclerView();
        loadUserProfile();

        return view;
    }

    private void initializeViews(View view) {
        ivAvatar = view.findViewById(R.id.iv_avatar);
        viewOnline = view.findViewById(R.id.view_online);
        tvUserName = view.findViewById(R.id.tv_user_name);
        chipRole = view.findViewById(R.id.chip_role);
        tvJobsCount = view.findViewById(R.id.tv_jobs_count);
        tvRating = view.findViewById(R.id.tv_rating);
        tvConnectionsCount = view.findViewById(R.id.tv_connections_count);
        layoutLocation = view.findViewById(R.id.layout_location);
        tvLocation = view.findViewById(R.id.tv_location);
        layoutEmail = view.findViewById(R.id.layout_email);
        tvEmail = view.findViewById(R.id.tv_email);
        layoutPhone = view.findViewById(R.id.layout_phone);
        tvPhone = view.findViewById(R.id.tv_phone);
        tvBio = view.findViewById(R.id.tv_bio);
        layoutSkills = view.findViewById(R.id.layout_skills);
        chipGroupSkills = view.findViewById(R.id.chip_group_skills);
        layoutMyJobs = view.findViewById(R.id.layout_my_jobs);
        recyclerMyJobs = view.findViewById(R.id.recycler_my_jobs);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnSettings = view.findViewById(R.id.btn_settings);
        btnLogout = view.findViewById(R.id.btn_logout);
    }

    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditProfileActivity.class);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SettingActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void setupRecyclerView() {
        jobAdapter = new JobAdapter(requireContext(), myJobs);
        recyclerMyJobs.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerMyJobs.setAdapter(jobAdapter);
    }

    private void loadUserProfile() {
        String userId = firebaseManager.getCurrentUserId();

        firebaseManager.getUser(userId, new JobLinkerFirebaseManager.DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                displayUserProfile(user);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(requireContext(),
                        "Error loading profile: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserProfile(User user) {
        // Avatar
        ImageUtils.loadCircularImage(requireContext(), user.getAvatarUrl(), ivAvatar);

        // Online status
        viewOnline.setVisibility(user.isOnline() ? View.VISIBLE : View.GONE);

        // Basic info
        tvUserName.setText(user.getUserName());
        chipRole.setText(user.getUserRole());

        // Stats (hardcoded for now - implement actual logic)
        tvJobsCount.setText("0");
        tvRating.setText("5.0");
        tvConnectionsCount.setText("0");

        // Location
        if (user.getLocation() != null && !user.getLocation().isEmpty()) {
            tvLocation.setText(user.getLocation());
            layoutLocation.setVisibility(View.VISIBLE);
        } else {
            layoutLocation.setVisibility(View.GONE);
        }

        // Email
        if (user.getUserEmail() != null && !user.getUserEmail().isEmpty()) {
            tvEmail.setText(user.getUserEmail());
            layoutEmail.setVisibility(View.VISIBLE);
        } else {
            layoutEmail.setVisibility(View.GONE);
        }

        // Phone
        if (user.getUserPhone() != null && !user.getUserPhone().isEmpty()) {
            tvPhone.setText(user.getUserPhone());
            layoutPhone.setVisibility(View.VISIBLE);
        } else {
            layoutPhone.setVisibility(View.GONE);
        }

        // Bio
        if (user.getUserBio() != null && !user.getUserBio().isEmpty()) {
            tvBio.setText(user.getUserBio());
            tvBio.setVisibility(View.VISIBLE);
        } else {
            tvBio.setVisibility(View.GONE);
        }

        // Skills (for job seekers)
        if (user.isJobSeeker() && user.getUserSkills() != null && !user.getUserSkills().isEmpty()) {
            layoutSkills.setVisibility(View.VISIBLE);
            chipGroupSkills.removeAllViews();

            for (String skill : user.getUserSkills()) {
                Chip chip = new Chip(requireContext());
                chip.setText(skill);
                chip.setClickable(false);
                chipGroupSkills.addView(chip);
            }
        } else {
            layoutSkills.setVisibility(View.GONE);
        }

        // My Jobs (for employers)
        if (user.isEmployer()) {
            layoutMyJobs.setVisibility(View.VISIBLE);
            loadMyJobs(user.getUserId());
        } else {
            layoutMyJobs.setVisibility(View.GONE);
        }
    }

    private void loadMyJobs(String employerId) {
        firebaseManager.getJobsByEmployer(employerId,
                new JobLinkerFirebaseManager.ListCallback<Job>() {
                    @Override
                    public void onSuccess(List<Job> jobs) {
                        myJobs.clear();
                        myJobs.addAll(jobs);
                        jobAdapter.notifyDataSetChanged();

                        tvJobsCount.setText(String.valueOf(jobs.size()));
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(requireContext(),
                                "Error loading jobs: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.logout)
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton(R.string.yes, (dialog, which) -> performLogout())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void performLogout() {
        // Update user status to offline
        String userId = firebaseManager.getCurrentUserId();
        if (userId != null) {
            firebaseManager.updateUserOnlineStatus(userId, false);
        }

        // Logout from Firebase
        firebaseManager.logout();

        // Clear shared preferences
        prefsManager.clearAll();

        // Navigate to login
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload profile when returning to fragment
        loadUserProfile();
    }
}