package com.example.joblinker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joblinker.R;
import com.example.joblinker.activities.FilterActivity;
import com.example.joblinker.activities.JobDetailsActivity;
import com.example.joblinker.activities.PostJobActivity;
import com.example.joblinker.adapters.JobAdapter;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.models.Job;
import com.example.joblinker.utils.SharedPreferencesManager;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
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
    private final List<Job> allJobs = new ArrayList<>();
    private final List<Job> filteredJobs = new ArrayList<>();
    private JobLinkerFirebaseManager firebaseManager;
    private SharedPreferencesManager prefsManager;
    private ListenerRegistration jobsListener;

    // Chip filter (quick filter)
    private String currentFilter = "All";

    // Advanced filter page state
    private String filterJobType = "All";
    private String filterCategory = "All";
    private String filterLocation = "";
    private long filterSalaryMin = -1;
    private long filterSalaryMax = -1;
    private String filterSort = FilterActivity.SORT_NEWEST;

    private ActivityResultLauncher<Intent> filterLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jobs, container, false);

        firebaseManager = JobLinkerFirebaseManager.getInstance();
        prefsManager = SharedPreferencesManager.getInstance(requireContext());

        setupFilterLauncher();
        initializeViews(view);
        setupRecyclerView();
        setupSearchListener();
        setupChipListener();
        setupClickListeners();
        loadJobs();

        return view;
    }

    private void setupFilterLauncher() {
        filterLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != android.app.Activity.RESULT_OK || result.getData() == null) {
                        return;
                    }

                    Intent data = result.getData();

                    filterJobType = data.getStringExtra(FilterActivity.EXTRA_FILTER_JOB_TYPE);
                    if (filterJobType == null) filterJobType = "All";

                    filterCategory = data.getStringExtra(FilterActivity.EXTRA_FILTER_CATEGORY);
                    if (filterCategory == null) filterCategory = "All";

                    filterLocation = data.getStringExtra(FilterActivity.EXTRA_FILTER_LOCATION);
                    if (filterLocation == null) filterLocation = "";

                    filterSalaryMin = data.getLongExtra(FilterActivity.EXTRA_FILTER_SALARY_MIN, -1);
                    filterSalaryMax = data.getLongExtra(FilterActivity.EXTRA_FILTER_SALARY_MAX, -1);

                    filterSort = data.getStringExtra(FilterActivity.EXTRA_FILTER_SORT);
                    if (filterSort == null) filterSort = FilterActivity.SORT_NEWEST;

                    applyAllFilters();
                }
        );
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
        fabPostJob.setVisibility(prefsManager.isEmployer() ? View.VISIBLE : View.GONE);
    }

    private void setupRecyclerView() {
        jobAdapter = new JobAdapter(requireContext(), filteredJobs);
        recyclerJobs.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerJobs.setAdapter(jobAdapter);

        // If your adapter doesn't have this listener, remove this block.
        jobAdapter.setOnJobClickListener(new JobAdapter.OnJobClickListener() {
            @Override
            public void onJobClick(Job job) {
                Intent intent = new Intent(requireContext(), JobDetailsActivity.class);
                intent.putExtra(JobDetailsActivity.EXTRA_JOB_ID, job.getJobId());
                startActivity(intent);
            }

            @Override
            public void onSaveClick(Job job) {
                Toast.makeText(requireContext(),
                        "Saved: " + job.getJobTitle(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyAllFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupChipListener() {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds == null || checkedIds.isEmpty()) return;

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
            } else {
                currentFilter = "All";
            }

            applyAllFilters();
        });
    }

    private void setupClickListeners() {
        btnFilter.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), FilterActivity.class);

            // Send current filter state to filter page
            intent.putExtra(FilterActivity.EXTRA_FILTER_JOB_TYPE, filterJobType);
            intent.putExtra(FilterActivity.EXTRA_FILTER_CATEGORY, filterCategory);
            intent.putExtra(FilterActivity.EXTRA_FILTER_LOCATION, filterLocation);
            intent.putExtra(FilterActivity.EXTRA_FILTER_SALARY_MIN, filterSalaryMin);
            intent.putExtra(FilterActivity.EXTRA_FILTER_SALARY_MAX, filterSalaryMax);
            intent.putExtra(FilterActivity.EXTRA_FILTER_SORT, filterSort);

            filterLauncher.launch(intent);
        });

        fabPostJob.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PostJobActivity.class);
            startActivity(intent);
        });
    }

    private void loadJobs() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        recyclerJobs.setVisibility(View.GONE);

        jobsListener = firebaseManager.listenToActiveJobs(
                new JobLinkerFirebaseManager.ListCallback<Job>() {
                    @Override
                    public void onSuccess(List<Job> jobs) {
                        progressBar.setVisibility(View.GONE);

                        allJobs.clear();
                        if (jobs != null) allJobs.addAll(jobs);

                        applyAllFilters();
                    }

                    @Override
                    public void onFailure(String error) {
                        progressBar.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                        recyclerJobs.setVisibility(View.GONE);

                        Toast.makeText(requireContext(),
                                "Error loading jobs: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void applyAllFilters() {
        String searchQuery = etSearch.getText() == null ? "" : etSearch.getText().toString().trim();

        filteredJobs.clear();

        for (Job job : allJobs) {
            if (job == null) continue;

            // 1) Search
            boolean matchesSearch = TextUtils.isEmpty(searchQuery)
                    || containsIgnoreCase(job.getJobTitle(), searchQuery)
                    || containsIgnoreCase(job.getJobCompany(), searchQuery)
                    || containsIgnoreCase(job.getJobDescription(), searchQuery);

            if (!matchesSearch) continue;

            // 2) Quick chip filter (job type)
            boolean matchesChip = "All".equals(currentFilter)
                    || TextUtils.equals(job.getJobType(), currentFilter);
            if (!matchesChip) continue;

            // 3) Advanced: Job type
            boolean matchesJobType = "All".equals(filterJobType)
                    || TextUtils.equals(job.getJobType(), filterJobType);
            if (!matchesJobType) continue;

            // 4) Advanced: Category
            boolean matchesCategory = "All".equals(filterCategory)
                    || TextUtils.equals(job.getJobCategory(), filterCategory);
            if (!matchesCategory) continue;

            // 5) Advanced: Location contains
            boolean matchesLocation = TextUtils.isEmpty(filterLocation)
                    || containsIgnoreCase(job.getLocation(), filterLocation);
            if (!matchesLocation) continue;

            // 6) Advanced: Salary min/max
            // NOTE: Your job salary seems to be a String range (salaryRange).
            // If you want real salary filtering, you need numeric salary fields in Job OR parse the string.
            // (left as TODO)

            filteredJobs.add(job);
        }

        // 7) Sort by createdAt
        if (FilterActivity.SORT_OLDEST.equals(filterSort)) {
            Collections.sort(filteredJobs, (a, b) -> Long.compare(safeCreatedAt(a), safeCreatedAt(b)));
        } else {
            Collections.sort(filteredJobs, (a, b) -> Long.compare(safeCreatedAt(b), safeCreatedAt(a)));
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

    private boolean containsIgnoreCase(String source, String q) {
        if (source == null || q == null) return false;
        return source.toLowerCase().contains(q.toLowerCase());
    }

    private long safeCreatedAt(Job job) {
        try {
            return job.getCreatedAt();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (jobsListener != null) {
            jobsListener.remove();
            jobsListener = null;
        }
    }
}