package com.example.joblinker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.TextView;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JobsFragment extends Fragment {

    // Search debounce: wait 300ms after last keystroke before filtering
    private static final long SEARCH_DEBOUNCE_MS = 300;

    private EditText etSearch;
    private ImageButton btnFilter;
    private ChipGroup chipGroup;
    private RecyclerView recyclerJobs;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private FloatingActionButton fabPostJob;
    private TextView tvResultCount;          // NEW: shows "X jobs found"

    private JobAdapter jobAdapter;
    private final List<Job> allJobs = new ArrayList<>();
    private final List<Job> filteredJobs = new ArrayList<>();
    private JobLinkerFirebaseManager firebaseManager;
    private SharedPreferencesManager prefsManager;
    private ListenerRegistration jobsListener;

    // Debounce handler
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Quick chip filter
    private String currentFilter = "All";

    // Advanced filter state
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

    // يُسجّل ActivityResultLauncher الذي يستقبل تحديدات الفلتر من FilterActivity
    private void setupFilterLauncher() {
        filterLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != android.app.Activity.RESULT_OK
                            || result.getData() == null) return;

                    Intent data = result.getData();
                    filterJobType = nvl(data.getStringExtra(FilterActivity.EXTRA_FILTER_JOB_TYPE), "All");
                    filterCategory = nvl(data.getStringExtra(FilterActivity.EXTRA_FILTER_CATEGORY), "All");
                    filterLocation = nvl(data.getStringExtra(FilterActivity.EXTRA_FILTER_LOCATION), "");
                    filterSalaryMin = data.getLongExtra(FilterActivity.EXTRA_FILTER_SALARY_MIN, -1);
                    filterSalaryMax = data.getLongExtra(FilterActivity.EXTRA_FILTER_SALARY_MAX, -1);
                    filterSort = nvl(data.getStringExtra(FilterActivity.EXTRA_FILTER_SORT),
                            FilterActivity.SORT_NEWEST);

                    applyAllFilters();
                });
    }

    // يبحث عن جميع المكوّنات ويخزّنها، ويُظهر/يُخفي زر نشر الوظيفة بناءً على دور المستخدم
    private void initializeViews(View view) {
        etSearch      = view.findViewById(R.id.et_search);
        btnFilter     = view.findViewById(R.id.btn_filter);
        chipGroup     = view.findViewById(R.id.chip_group);
        recyclerJobs  = view.findViewById(R.id.recycler_jobs);
        layoutEmpty   = view.findViewById(R.id.layout_empty);
        progressBar   = view.findViewById(R.id.progress_bar);
        fabPostJob    = view.findViewById(R.id.fab_post_job);
        tvResultCount = view.findViewById(R.id.tv_result_count);

        fabPostJob.setVisibility(prefsManager.isEmployer() ? View.VISIBLE : View.GONE);
    }

    // يُهيّئ الـ RecyclerView مع JobAdapter ويربط استدعاءات النقر على الوظيفة وزر الحفظ
    private void setupRecyclerView() {
        jobAdapter = new JobAdapter(requireContext(), filteredJobs);
        recyclerJobs.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerJobs.setAdapter(jobAdapter);

        jobAdapter.setOnJobClickListener(new JobAdapter.OnJobClickListener() {
            @Override
            public void onJobClick(Job job) {
                Intent intent = new Intent(requireContext(), JobDetailsActivity.class);
                intent.putExtra(JobDetailsActivity.EXTRA_JOB_ID, job.getJobId());
                startActivity(intent);
            }

            @Override
            public void onSaveClick(Job job, boolean isSaved) {
                // Persist save/unsave to Firebase
                String userId = firebaseManager.getCurrentUserId();
                if (userId == null) {
                    Toast.makeText(requireContext(),
                            "Please login to save jobs", Toast.LENGTH_SHORT).show();
                    jobAdapter.markJobSaved(job.getJobId(), !isSaved); // revert optimistic
                    return;
                }

                JobLinkerFirebaseManager.VoidCallback cb = new JobLinkerFirebaseManager.VoidCallback() {
                    @Override public void onSuccess() { /* UI already updated optimistically */ }
                    @Override public void onFailure(String error) {
                        // Revert optimistic update on failure
                        jobAdapter.markJobSaved(job.getJobId(), !isSaved);
                        Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                };

                if (isSaved) {
                    firebaseManager.saveJob(userId, job.getJobId(), cb);
                } else {
                    firebaseManager.unsaveJob(userId, job.getJobId(), cb);
                }
            }
        });

        // Load saved job IDs once so bookmark icons show correctly from the start
        loadSavedJobIds();
    }

    // يجلب معرّفات الوظائف المحفوظة للمستخدم الحالي من Firebase ويُحدّث أيقونات الإشارة المرجعية
    private void loadSavedJobIds() {
        String userId = firebaseManager.getCurrentUserId();
        if (userId == null) return;

        firebaseManager.getSavedJobIds(userId, new JobLinkerFirebaseManager.ListCallback<String>() {
            @Override
            public void onSuccess(List<String> ids) {
                if (ids == null || !isAdded()) return;
                Set<String> idSet = new HashSet<>(ids);
                jobAdapter.setSavedJobIds(idSet);
            }

            @Override
            public void onFailure(String error) { /* non-critical, ignore */ }
        });
    }

    /**
     * Debounced search: waits 300 ms after the last keystroke before filtering.
     * This avoids running the filter loop on every character typed.
     */
    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> applyAllFilters();
                searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
            }
        });
    }

    // يضبط مجموعة الرقائق للفلترة السريعة لتحديث currentFilter وإعادة تشغيل الفلاتر عند الاختيار
    private void setupChipListener() {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds == null || checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);

            if      (id == R.id.chip_all)        currentFilter = "All";
            else if (id == R.id.chip_full_time)  currentFilter = "Full-time";
            else if (id == R.id.chip_part_time)  currentFilter = "Part-time";
            else if (id == R.id.chip_remote)     currentFilter = "Remote";
            else if (id == R.id.chip_contract)   currentFilter = "Contract";
            else if (id == R.id.chip_internship) currentFilter = "Internship";
            else                                 currentFilter = "All";

            applyAllFilters();
        });
    }

    // يربط زر الفلتر لفتح FilterActivity وزر الإجراء العائم لفتح PostJobActivity
    private void setupClickListeners() {
        btnFilter.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), FilterActivity.class);
            intent.putExtra(FilterActivity.EXTRA_FILTER_JOB_TYPE, filterJobType);
            intent.putExtra(FilterActivity.EXTRA_FILTER_CATEGORY, filterCategory);
            intent.putExtra(FilterActivity.EXTRA_FILTER_LOCATION, filterLocation);
            intent.putExtra(FilterActivity.EXTRA_FILTER_SALARY_MIN, filterSalaryMin);
            intent.putExtra(FilterActivity.EXTRA_FILTER_SALARY_MAX, filterSalaryMax);
            intent.putExtra(FilterActivity.EXTRA_FILTER_SORT, filterSort);
            filterLauncher.launch(intent);
        });

        fabPostJob.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), PostJobActivity.class)));
    }

    // يبدأ مستمع Firestore في الوقت الفعلي للوظائف النشطة ويُشغّل الفلترة عند كل تحديث
    private void loadJobs() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        recyclerJobs.setVisibility(View.GONE);
        updateResultCount(0, true);

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
                        updateResultCount(0, false);
                        Toast.makeText(requireContext(),
                                "Error loading jobs: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // يُطبّق البحث النصي وفلتر الرقائق وجميع الفلاتر المتقدمة على allJobs ثم يُرتّب النتيجة ويعرضها
    private void applyAllFilters() {
        String query = etSearch.getText() == null ? "" : etSearch.getText().toString().trim();

        List<Job> result = new ArrayList<>();

        for (Job job : allJobs) {
            if (job == null) continue;

            // 1) Text search (title, company, description)
            if (!TextUtils.isEmpty(query)) {
                boolean matchesSearch = containsCI(job.getJobTitle(), query)
                        || containsCI(job.getJobCompany(), query)
                        || containsCI(job.getJobDescription(), query);
                if (!matchesSearch) continue;
            }

            // 2) Quick chip (job type)
            if (!"All".equals(currentFilter)
                    && !currentFilter.equals(job.getJobType())) continue;

            // 3) Advanced: job type
            if (!"All".equals(filterJobType)
                    && !filterJobType.equals(job.getJobType())) continue;

            // 4) Advanced: category
            if (!"All".equals(filterCategory)
                    && !filterCategory.equals(job.getJobCategory())) continue;

            // 5) Advanced: location substring
            if (!TextUtils.isEmpty(filterLocation)
                    && !containsCI(job.getLocation(), filterLocation)) continue;

            // 6) Advanced: salary range (numeric fields are available on Job model)
            if (filterSalaryMin > 0 && job.getJobSalaryMax() > 0
                    && job.getJobSalaryMax() < filterSalaryMin) continue;
            if (filterSalaryMax > 0 && job.getJobSalaryMin() > filterSalaryMax) continue;

            result.add(job);
        }

        // 7) Sort
        if (FilterActivity.SORT_OLDEST.equals(filterSort)) {
            Collections.sort(result, (a, b) -> Long.compare(safeCreatedAt(a), safeCreatedAt(b)));
        } else {
            Collections.sort(result, (a, b) -> Long.compare(safeCreatedAt(b), safeCreatedAt(a)));
        }

        // Use DiffUtil-based submitList instead of notifyDataSetChanged
        jobAdapter.submitList(result);

        filteredJobs.clear();
        filteredJobs.addAll(result);

        updateResultCount(result.size(), false);

        if (result.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerJobs.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerJobs.setVisibility(View.VISIBLE);
        }
    }

    /** Show or hide the result counter label */
    private void updateResultCount(int count, boolean loading) {
        if (tvResultCount == null) return;
        if (loading) {
            tvResultCount.setVisibility(View.GONE);
            return;
        }
        tvResultCount.setVisibility(View.VISIBLE);
        if (count == 0) {
            tvResultCount.setText("");
            tvResultCount.setVisibility(View.GONE);
        } else {
            tvResultCount.setText(count + (count == 1 ? " job found" : " jobs found"));
        }
    }

    // يُعيد true إذا كانت src تحتوي على q كسلسلة فرعية بغض النظر عن حالة الأحرف
    private boolean containsCI(String src, String q) {
        if (src == null || q == null) return false;
        return src.toLowerCase().contains(q.toLowerCase());
    }

    // يُعيد بأمان طابع الوقت الزمني لإنشاء الوظيفة، أو 0 في حالة حدوث استثناء
    private long safeCreatedAt(Job job) {
        try { return job.getCreatedAt(); } catch (Exception e) { return 0; }
    }

    private static String nvl(String s, String def) { return s != null ? s : def; }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cancel any pending debounce callback
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        if (jobsListener != null) { jobsListener.remove(); jobsListener = null; }
    }
}
