package com.example.joblinker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.joblinker.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class FilterActivity extends AppCompatActivity {

    public static final String EXTRA_FILTER_JOB_TYPE = "filter_job_type";
    public static final String EXTRA_FILTER_CATEGORY = "filter_category";
    public static final String EXTRA_FILTER_LOCATION = "filter_location";
    public static final String EXTRA_FILTER_SALARY_MIN = "filter_salary_min";
    public static final String EXTRA_FILTER_SALARY_MAX = "filter_salary_max";
    public static final String EXTRA_FILTER_SORT = "filter_sort";

    // sort values
    public static final String SORT_NEWEST = "newest";
    public static final String SORT_OLDEST = "oldest";

    private MaterialToolbar toolbar;

    private AutoCompleteTextView dropdownJobType;
    private AutoCompleteTextView dropdownCategory;
    private TextInputEditText etLocation;
    private TextInputEditText etSalaryMin;
    private TextInputEditText etSalaryMax;
    private AutoCompleteTextView dropdownSort;

    private MaterialButton btnApply;
    private MaterialButton btnClear;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        initializeViews();
        setupToolbar();
        setupDropdowns();
        fillFromIntent();
        setupButtons();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);

        dropdownJobType = findViewById(R.id.dropdown_job_type);
        dropdownCategory = findViewById(R.id.dropdown_category);
        etLocation = findViewById(R.id.et_location);
        etSalaryMin = findViewById(R.id.et_salary_min);
        etSalaryMax = findViewById(R.id.et_salary_max);
        dropdownSort = findViewById(R.id.dropdown_sort);

        btnApply = findViewById(R.id.btn_apply_filters);
        btnClear = findViewById(R.id.btn_clear_filters);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupDropdowns() {
        // You can replace these with values from resources if you want
        String[] jobTypes = new String[]{"All", "Full-time", "Part-time", "Remote", "Contract", "Internship"};
        String[] categories = new String[]{"All", "Software", "Design", "Marketing", "Sales", "Finance", "Other"};
        String[] sorts = new String[]{"Newest", "Oldest"};

        dropdownJobType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, jobTypes));
        dropdownCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories));
        dropdownSort.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sorts));

        // defaults
        if (TextUtils.isEmpty(dropdownJobType.getText())) dropdownJobType.setText("All", false);
        if (TextUtils.isEmpty(dropdownCategory.getText())) dropdownCategory.setText("All", false);
        if (TextUtils.isEmpty(dropdownSort.getText())) dropdownSort.setText("Newest", false);
    }

    private void fillFromIntent() {
        Intent intent = getIntent();
        if (intent == null) return;

        setIfPresent(dropdownJobType, intent.getStringExtra(EXTRA_FILTER_JOB_TYPE), "All");
        setIfPresent(dropdownCategory, intent.getStringExtra(EXTRA_FILTER_CATEGORY), "All");

        String location = intent.getStringExtra(EXTRA_FILTER_LOCATION);
        if (location != null) etLocation.setText(location);

        long min = intent.getLongExtra(EXTRA_FILTER_SALARY_MIN, -1);
        long max = intent.getLongExtra(EXTRA_FILTER_SALARY_MAX, -1);
        if (min >= 0) etSalaryMin.setText(String.valueOf(min));
        if (max >= 0) etSalaryMax.setText(String.valueOf(max));

        String sort = intent.getStringExtra(EXTRA_FILTER_SORT);
        if (SORT_OLDEST.equals(sort)) dropdownSort.setText("Oldest", false);
        else dropdownSort.setText("Newest", false);
    }

    private void setIfPresent(AutoCompleteTextView view, String value, String fallback) {
        if (!TextUtils.isEmpty(value)) view.setText(value, false);
        else view.setText(fallback, false);
    }

    private void setupButtons() {
        btnApply.setOnClickListener(v -> {
            Intent data = new Intent();

            data.putExtra(EXTRA_FILTER_JOB_TYPE, dropdownJobType.getText().toString());
            data.putExtra(EXTRA_FILTER_CATEGORY, dropdownCategory.getText().toString());
            data.putExtra(EXTRA_FILTER_LOCATION, safeText(etLocation));

            data.putExtra(EXTRA_FILTER_SALARY_MIN, parseLongOrMinusOne(safeText(etSalaryMin)));
            data.putExtra(EXTRA_FILTER_SALARY_MAX, parseLongOrMinusOne(safeText(etSalaryMax)));

            String sortUi = dropdownSort.getText().toString();
            data.putExtra(EXTRA_FILTER_SORT, "Oldest".equalsIgnoreCase(sortUi) ? SORT_OLDEST : SORT_NEWEST);

            setResult(RESULT_OK, data);
            finish();
        });

        btnClear.setOnClickListener(v -> {
            Intent data = new Intent();
            data.putExtra(EXTRA_FILTER_JOB_TYPE, "All");
            data.putExtra(EXTRA_FILTER_CATEGORY, "All");
            data.putExtra(EXTRA_FILTER_LOCATION, "");
            data.putExtra(EXTRA_FILTER_SALARY_MIN, -1L);
            data.putExtra(EXTRA_FILTER_SALARY_MAX, -1L);
            data.putExtra(EXTRA_FILTER_SORT, SORT_NEWEST);

            setResult(RESULT_OK, data);
            finish();
        });
    }

    private String safeText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private long parseLongOrMinusOne(String s) {
        if (TextUtils.isEmpty(s)) return -1;
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            return -1;
        }
    }
}