package com.example.joblinker.fragments;



import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.ListenerRegistration;

import com.example.joblinker.R;
import com.example.joblinker.activities.PostJobActivity;
import com.example.joblinker.adapters.JobAdapter;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.models.Job;
import com.example.joblinker.utils.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.List;

public class JobsFragment extends Fragment {

    private EditText etSearch;
    private ImageButton btnFilter;
    private ChipGroup chipGroup;
    private RecyclerView recyclerJobs;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private FloatingActionButton fabPostJob;

    private JobAdapter jobAdapter;
    private List<Job> allJobs;
    private List<Job> filteredJobs;
    private JobLinkerFirebaseManager firebaseManager;
    private SharedPreferencesManager prefsManager;
    private ListenerRegistration jobsListener;

    private String currentFilter = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jobs, container, false);

        firebaseManager = JobLinkerFirebaseManager.getInstance();
        prefsManager = SharedPreferencesManager.getInstance(requireContext());
        allJobs = new ArrayList<>();
        filteredJobs = new ArrayList<>();

        initializeViews(view);
        setupRecyclerView();
        setupSearchListener();
        setupChipListener();
        setupClickListeners();
        loadJobs();

        return view;
    }

    private void initializeViews(View view) {
        etSearch = view.findViewById(R.id.et_search);
        btnFilter = view.findViewById(R.id.btn_filter);
        chipGroup = view.findViewById(R.id.chip_group);
        recyclerJobs = view.findViewById(R.id.recycler_jobs);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        progressBar = view.findViewById(R.id.progress_bar);
        fabPostJob = view.findViewById(R.id.fab_post_job);

        // Show FAB only for employers
        if (prefsManager.isEmployer()) {
            fabPostJob.setVisibility(View.VISIBLE);
        } else {
            fabPostJob.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        jobAdapter = new JobAdapter(requireContext(), filteredJobs);
        recyclerJobs.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerJobs.setAdapter(jobAdapter);
    }

    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterJobs(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupChipListener() {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);

            if (checkedId == R.id.chip_all) {
                currentFilter = "All";
            } else if (checkedId == R.id.chip_full_time) {
                currentFilter = "Full-time";
            } else if (checkedId == R.id.chip_part_time) {
                currentFilter = "Part-time";
            } else if (checkedId == R.id.chip_remote) {
                currentFilter = "Remote";
            } else if (checkedId == R.id.chip_contract) {
                currentFilter = "Contract";
            } else if (checkedId == R.id.chip_internship) {
                currentFilter = "Internship";
            }

            filterJobs(etSearch.getText().toString());
        });
    }

    private void setupClickListeners() {
        btnFilter.setOnClickListener(v -> {
            // TODO: Show advanced filter dialog
            Toast.makeText(requireContext(), "Advanced filters coming soon", Toast.LENGTH_SHORT).show();
        });

        fabPostJob.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PostJobActivity.class);
            startActivity(intent);
        });
    }

    private void loadJobs() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        jobsListener = firebaseManager.listenToActiveJobs(
                new JobLinkerFirebaseManager.ListCallback<Job>() {
                    @Override
                    public void onSuccess(List<Job> jobs) {
                        progressBar.setVisibility(View.GONE);
                        allJobs.clear();
                        allJobs.addAll(jobs);
                        filterJobs(etSearch.getText().toString());
                    }

                    @Override
                    public void onFailure(String error) {
                        progressBar.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                        Toast.makeText(requireContext(),
                                "Error loading jobs: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterJobs(String searchQuery) {
        filteredJobs.clear();

        for (Job job : allJobs) {
            boolean matchesSearch = searchQuery.isEmpty() ||
                    job.getJobTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    job.getJobCompany().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    job.getJobDescription().toLowerCase().contains(searchQuery.toLowerCase());

            boolean matchesFilter = currentFilter.equals("All") ||
                    job.getJobType().equals(currentFilter);

            if (matchesSearch && matchesFilter) {
                filteredJobs.add(job);
            }
        }

        jobAdapter.notifyDataSetChanged();

        if (filteredJobs.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerJobs.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerJobs.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (jobsListener != null) {
            jobsListener.remove();
        }
    }
}