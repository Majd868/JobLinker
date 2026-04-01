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
    private final Set<String> savedJobIds = new HashSet<>();
    private OnJobClickListener listener;

    public interface OnJobClickListener {
        void onJobClick(Job job);
        void onSaveClick(Job job, boolean isSaved);
    }

    public JobAdapter(Context context, List<Job> jobs) {
        this.context = context;
        this.jobs    = jobs;
    }

    public void setOnJobClickListener(OnJobClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Job> newJobs) {
        final List<Job> old = new ArrayList<>(jobs);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return old.size(); }
            @Override public int getNewListSize() { return newJobs.size(); }
            @Override public boolean areItemsTheSame(int o, int n) {
                String oid = old.get(o).getJobId(), nid = newJobs.get(n).getJobId();
                return oid != null && oid.equals(nid);
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                Job a = old.get(o), b = newJobs.get(n);
                return safeEq(a.getJobTitle(), b.getJobTitle())
                    && safeEq(a.getJobCompany(), b.getJobCompany())
                    && a.getJobSalaryMin() == b.getJobSalaryMin();
            }
        });
        jobs.clear();
        jobs.addAll(newJobs);
        diff.dispatchUpdatesTo(this);
    }

    public void setSavedJobIds(Set<String> ids) {
        savedJobIds.clear();
        if (ids != null) savedJobIds.addAll(ids);
        notifyDataSetChanged();
    }

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

    @NonNull @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new JobViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.item_job, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder h, int pos) {
        bind(h, jobs.get(pos));
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder h, int pos, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && "save_changed".equals(payloads.get(0))) {
            updateSaveIcon(h, jobs.get(pos)); return;
        }
        super.onBindViewHolder(h, pos, payloads);
    }

    private void bind(@NonNull JobViewHolder h, Job job) {
        ImageUtils.loadCompanyLogo(context, job.getCompanyLogoUrl(), h.ivCompanyLogo);

        h.tvJobTitle.setText(safe(job.getJobTitle()));
        h.tvCompanyName.setText(safe(job.getJobCompany()));
        h.tvLocation.setText(safe(job.getLocation()));
        h.tvSalary.setText(safe(job.getSalaryRange()));
        h.chipCategory.setText(safe(job.getJobCategory()));
        h.chipJobType.setText(safe(job.getJobType()));

        // Posted time + applicant count
        String time = DateTimeHelper.getRelativeTime(job.getCreatedAt());
        int apps = job.getApplicantCount();
        h.tvPostedTime.setText(apps > 0 ? time + " · " + apps + " applied" : time);

        // Remote chip
        if (h.chipRemote != null) {
            boolean remote = "Remote".equalsIgnoreCase(job.getJobType())
                || (job.getLocation() != null && job.getLocation().toLowerCase().contains("remote"));
            h.chipRemote.setVisibility(remote ? View.VISIBLE : View.GONE);
        }

        // Urgent badge
        if (h.tvUrgent != null) {
            h.tvUrgent.setVisibility(job.isUrgent() ? View.VISIBLE : View.GONE);
        }

        updateSaveIcon(h, job);

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onJobClick(job); });
        h.btnSave.setOnClickListener(v -> {
            if (listener == null || job.getJobId() == null) return;
            boolean cur = savedJobIds.contains(job.getJobId());
            markJobSaved(job.getJobId(), !cur);
            listener.onSaveClick(job, !cur);
        });
    }

    private void updateSaveIcon(@NonNull JobViewHolder h, Job job) {
        boolean saved = job.getJobId() != null && savedJobIds.contains(job.getJobId());
        h.btnSave.setImageResource(saved ? R.drawable.ic_bookmark : R.drawable.ic_bookmark_border);
    }

    private static String safe(String s) { return s != null ? s : ""; }
    private static boolean safeEq(String a, String b) { return a == null ? b == null : a.equals(b); }

    @Override public int getItemCount() { return jobs.size(); }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCompanyLogo;
        TextView tvJobTitle, tvCompanyName, tvLocation, tvSalary, tvPostedTime, tvUrgent;
        Chip chipCategory, chipJobType, chipRemote;
        ImageButton btnSave;

        JobViewHolder(@NonNull View v) {
            super(v);
            ivCompanyLogo = v.findViewById(R.id.iv_company_logo);
            tvJobTitle    = v.findViewById(R.id.tv_job_title);
            tvCompanyName = v.findViewById(R.id.tv_company_name);
            tvLocation    = v.findViewById(R.id.tv_location);
            tvSalary      = v.findViewById(R.id.tv_salary);
            chipCategory  = v.findViewById(R.id.chip_category);
            chipJobType   = v.findViewById(R.id.chip_job_type);
            chipRemote    = v.findViewById(R.id.chip_remote);
            tvPostedTime  = v.findViewById(R.id.tv_posted_time);
            tvUrgent      = v.findViewById(R.id.tv_urgent);
            btnSave       = v.findViewById(R.id.btn_save);
        }
    }
}
