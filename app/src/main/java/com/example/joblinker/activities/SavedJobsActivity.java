package com.example.joblinker.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import com.example.joblinker.R;
import com.example.joblinker.adapters.JobAdapter;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.models.Job;

import java.util.ArrayList;
import java.util.List;

public class SavedJobsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private JobLinkerFirebaseManager firebaseManager;
    private JobAdapter jobAdapter;
    private final List<Job> savedJobs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_jobs);

        firebaseManager = JobLinkerFirebaseManager.getInstance();

        toolbar     = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recycler_saved_jobs);
        progressBar  = findViewById(R.id.progress_bar);
        tvEmpty      = findViewById(R.id.tv_empty);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        jobAdapter = new JobAdapter(this, savedJobs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(jobAdapter);

        loadSavedJobs();
    }

    private void loadSavedJobs() {
        String userId = firebaseManager.getCurrentUserId();
        if (userId == null) return;

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        firebaseManager.getSavedJobs(userId,
            new JobLinkerFirebaseManager.ListCallback<String>() {
                @Override
                public void onSuccess(List<String> jobIds) {
                    if (jobIds.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                        return;
                    }
                    // Fetch each job by ID
                    savedJobs.clear();
                    final int[] count = {0};
                    for (String jobId : jobIds) {
                        firebaseManager.getJob(jobId,
                            new JobLinkerFirebaseManager.DataCallback<Job>() {
                                @Override
                                public void onSuccess(Job job) {
                                    savedJobs.add(job);
                                    count[0]++;
                                    if (count[0] == jobIds.size()) {
                                        progressBar.setVisibility(View.GONE);
                                        jobAdapter.notifyDataSetChanged();
                                        if (savedJobs.isEmpty())
                                            tvEmpty.setVisibility(View.VISIBLE);
                                    }
                                }
                                @Override
                                public void onFailure(String error) {
                                    count[0]++;
                                    if (count[0] == jobIds.size()) {
                                        progressBar.setVisibility(View.GONE);
                                        jobAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                    }
                }

                @Override
                public void onFailure(String error) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SavedJobsActivity.this,
                        "Error loading saved jobs: " + error, Toast.LENGTH_SHORT).show();
                }
            });
    }
}
