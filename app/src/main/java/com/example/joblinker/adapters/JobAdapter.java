package com.example.joblinker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.example.joblinker.R;
import com.example.joblinker.models.Job;
import com.example.joblinker.utils.DateTimeHelper;
import com.example.joblinker.utils.ImageUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private final Context context;
    private final List<Job> jobs;

    // Track saved job IDs locally for instant UI feedback
    private final Set<String> savedJobIds = new HashSet<>();

    private OnJobClickListener listener;

    public interface OnJobClickListener {
        void onJobClick(Job job);
        void onSaveClick(Job job, boolean isSaved);
    }

    public JobAdapter(Context context, List<Job> jobs) {
        this.context = context;
        this.jobs = jobs;
    }

    public void setOnJobClickListener(OnJobClickListener listener) {
        this.listener = listener;
    }

    /**
     * Update the adapter list using DiffUtil for efficient, animated updates.
     * Replaces notifyDataSetChanged() — no more full-list flicker on every search keystroke.
     */
    public void submitList(List<Job> newJobs) {
        final List<Job> oldJobs = new ArrayList<>(jobs);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return oldJobs.size(); }
            @Override public int getNewListSize() { return newJobs.size(); }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                Job o = oldJobs.get(oldPos);
                Job n = newJobs.get(newPos);
                if (o.getJobId() == null || n.getJobId() == null) return false;
                return o.getJobId().equals(n.getJobId());
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                Job o = oldJobs.get(oldPos);
                Job n = newJobs.get(newPos);
                boolean titleSame = safeEquals(o.getJobTitle(), n.getJobTitle());
                boolean companySame = safeEquals(o.getJobCompany(), n.getJobCompany());
                boolean salarySame = o.getJobSalaryMin() == n.getJobSalaryMin()
                        && o.getJobSalaryMax() == n.getJobSalaryMax();
                return titleSame && companySame && salarySame;
            }
        });

        jobs.clear();
        jobs.addAll(newJobs);
        diffResult.dispatchUpdatesTo(this);
    }

    /** Bulk-set which jobs are saved (called once on fragment start). */
    public void setSavedJobIds(Set<String> ids) {
        savedJobIds.clear();
        if (ids != null) savedJobIds.addAll(ids);
        notifyDataSetChanged();
    }

    /** Flip one job's saved state and refresh only that row — no full redraw. */
    public void markJobSaved(String jobId, boolean saved) {
        if (jobId == null) return;
        if (saved) savedJobIds.add(jobId); else savedJobIds.remove(jobId);
        for (int i = 0; i < jobs.size(); i++) {
            if (jobId.equals(jobs.get(i).getJobId())) {
                notifyItemChanged(i, "save_changed");
                break;
            }
        }
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        bindJob(holder, jobs.get(position));
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && "save_changed".equals(payloads.get(0))) {
            // Partial bind: only refresh the bookmark icon — avoids image flicker
            updateSaveIcon(holder, jobs.get(position));
            return;
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    private void bindJob(@NonNull JobViewHolder holder, Job job) {
        // Load real company logo URL (was always null before this fix)
        ImageUtils.loadCompanyLogo(context, job.getCompanyLogoUrl(), holder.ivCompanyLogo);

        holder.tvJobTitle.setText(safe(job.getJobTitle()));
        holder.tvCompanyName.setText(safe(job.getJobCompany()));
        holder.tvLocation.setText(job.getLocation());
        holder.tvSalary.setText(job.getSalaryRange());
        holder.chipCategory.setText(safe(job.getJobCategory()));
        holder.chipJobType.setText(safe(job.getJobType()));
        holder.tvPostedTime.setText(DateTimeHelper.getRelativeTime(job.getCreatedAt()));

        updateSaveIcon(holder, job);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onJobClick(job);
        });

        holder.btnSave.setOnClickListener(v -> {
            if (listener == null || job.getJobId() == null) return;
            boolean currentlySaved = savedJobIds.contains(job.getJobId());
            markJobSaved(job.getJobId(), !currentlySaved);      // optimistic update
            listener.onSaveClick(job, !currentlySaved);
        });
    }

    private void updateSaveIcon(@NonNull JobViewHolder holder, Job job) {
        boolean saved = job.getJobId() != null && savedJobIds.contains(job.getJobId());
        holder.btnSave.setImageResource(
                saved ? R.drawable.ic_bookmark : R.drawable.ic_bookmark_border);
    }

    private static String safe(String s) { return s != null ? s : ""; }
    private static boolean safeEquals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    @Override
    public int getItemCount() { return jobs.size(); }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCompanyLogo;
        TextView tvJobTitle, tvCompanyName, tvLocation, tvSalary, tvPostedTime;
        Chip chipCategory, chipJobType;
        ImageButton btnSave;

        JobViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCompanyLogo = itemView.findViewById(R.id.iv_company_logo);
            tvJobTitle    = itemView.findViewById(R.id.tv_job_title);
            tvCompanyName = itemView.findViewById(R.id.tv_company_name);
            tvLocation    = itemView.findViewById(R.id.tv_location);
            tvSalary      = itemView.findViewById(R.id.tv_salary);
            chipCategory  = itemView.findViewById(R.id.chip_category);
            chipJobType   = itemView.findViewById(R.id.chip_job_type);
            tvPostedTime  = itemView.findViewById(R.id.tv_posted_time);
            btnSave       = itemView.findViewById(R.id.btn_save);
        }
    }
}
