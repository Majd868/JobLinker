package com.example.joblinker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.example.joblinker.R;
import com.example.joblinker.models.Job;
import com.example.joblinker.utils.DateTimeHelper;
import com.example.joblinker.utils.ImageUtils;

import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private Context context;
    private List<Job> jobs;
    private OnJobClickListener listener;

    public interface OnJobClickListener {
        void onJobClick(Job job);
        void onSaveClick(Job job);
    }

    public JobAdapter(Context context, List<Job> jobs) {
        this.context = context;
        this.jobs = jobs;
    }

    public void setOnJobClickListener(OnJobClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobs.get(position);

        // Company logo (placeholder for now)
        ImageUtils.loadCompanyLogo(context, null, holder.ivCompanyLogo);

        // Job details
        holder.tvJobTitle.setText(job.getJobTitle());
        holder.tvCompanyName.setText(job.getJobCompany());
        holder.tvLocation.setText(job.getLocation());
        holder.tvSalary.setText(job.getSalaryRange());
        holder.chipCategory.setText(job.getJobCategory());
        holder.chipJobType.setText(job.getJobType());
        holder.tvPostedTime.setText(DateTimeHelper.getRelativeTime(job.getCreatedAt()));

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onJobClick(job);
            }
        });

        holder.btnSave.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSaveClick(job);
            }
        });
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCompanyLogo;
        TextView tvJobTitle, tvCompanyName, tvLocation, tvSalary, tvPostedTime;
        Chip chipCategory, chipJobType;
        ImageButton btnSave;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCompanyLogo = itemView.findViewById(R.id.iv_company_logo);
            tvJobTitle = itemView.findViewById(R.id.tv_job_title);
            tvCompanyName = itemView.findViewById(R.id.tv_company_name);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvSalary = itemView.findViewById(R.id.tv_salary);
            chipCategory = itemView.findViewById(R.id.chip_category);
            chipJobType = itemView.findViewById(R.id.chip_job_type);
            tvPostedTime = itemView.findViewById(R.id.tv_posted_time);
            btnSave = itemView.findViewById(R.id.btn_save);
        }
    }
}