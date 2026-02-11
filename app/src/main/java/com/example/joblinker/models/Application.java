package com.example.joblinker.models;

public class Application {
    private String applicationId;
    private String jobId;
    private String jobSeekerUserId;
    private String employerUserId;
    private String status; // "Applied", "Shortlisted", "Rejected", "Accepted"
    private String coverLetter;
    private long appliedAt;
    private long updatedAt;
    private String jobTitle;
    private String jobSeekerName;

    // Empty constructor (required for Firebase)
    public Application() {}

    // Constructor with main fields
    public Application(String applicationId, String jobId, String jobSeekerUserId,
                       String employerUserId, String coverLetter) {
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.jobSeekerUserId = jobSeekerUserId;
        this.employerUserId = employerUserId;
        this.status = "Applied";
        this.coverLetter = coverLetter;
        this.appliedAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getJobSeekerUserId() { return jobSeekerUserId; }
    public void setJobSeekerUserId(String jobSeekerUserId) { this.jobSeekerUserId = jobSeekerUserId; }

    public String getEmployerUserId() { return employerUserId; }
    public void setEmployerUserId(String employerUserId) { this.employerUserId = employerUserId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }

    public long getAppliedAt() { return appliedAt; }
    public void setAppliedAt(long appliedAt) { this.appliedAt = appliedAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getJobSeekerName() { return jobSeekerName; }
    public void setJobSeekerName(String jobSeekerName) { this.jobSeekerName = jobSeekerName; }
}