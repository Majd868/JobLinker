package com.example.joblinker.models;

import java.io.Serializable;
import java.util.List;

public class Job implements Serializable {

    private String jobId;
    private String jobTitle;
    private String jobCompany;
    private String jobEmployerId;
    private String jobCategory;
    private String jobType; // Full-time, Part-time, Remote, Contract, Internship
    private String jobCountry;
    private String jobCity;
    private String jobDescription;
    private List<String> jobSkills;
    private double jobSalaryMin;
    private double jobSalaryMax;
    private String salaryCurrency;
    private long deadline; // Application deadline timestamp
    private long createdAt;
    private long updatedAt;
    private boolean jobActive;
    private int viewCount;
    private int applicantCount;
    private List<String> applicants; // List of user IDs who applied
    private String companyLogoUrl; // Company logo image URL
    private String companyWebsite;
    private String contactEmail;
    private String contactPhone;

    // Default Constructor (Required for Firebase)
    public Job() {
        this.jobActive = true;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.viewCount = 0;
        this.applicantCount = 0;
        this.salaryCurrency = "USD";
    }

    // Constructor with essential fields
    public Job(String jobTitle, String jobCompany, String jobEmployerId,
               String jobCategory, String jobType) {
        this();
        this.jobTitle = jobTitle;
        this.jobCompany = jobCompany;
        this.jobEmployerId = jobEmployerId;
        this.jobCategory = jobCategory;
        this.jobType = jobType;
    }

    // ==================== GETTERS ====================

    public String getJobId() {
        return jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getJobCompany() {
        return jobCompany;
    }

    public String getJobEmployerId() {
        return jobEmployerId;
    }

    public String getJobCategory() {
        return jobCategory;
    }

    public String getJobType() {
        return jobType;
    }

    public String getJobCountry() {
        return jobCountry;
    }

    public String getJobCity() {
        return jobCity;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public List<String> getJobSkills() {
        return jobSkills;
    }

    public double getJobSalaryMin() {
        return jobSalaryMin;
    }

    public double getJobSalaryMax() {
        return jobSalaryMax;
    }

    public String getSalaryCurrency() {
        return salaryCurrency;
    }

    public long getDeadline() {
        return deadline;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public boolean isJobActive() {
        return jobActive;
    }

    public int getViewCount() {
        return viewCount;
    }

    public int getApplicantCount() {
        return applicantCount;
    }

    public List<String> getApplicants() {
        return applicants;
    }

    public String getCompanyLogoUrl() {
        return companyLogoUrl;
    }

    public String getCompanyWebsite() {
        return companyWebsite;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    // ==================== SETTERS ====================

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public void setJobCompany(String jobCompany) {
        this.jobCompany = jobCompany;
    }

    public void setJobEmployerId(String jobEmployerId) {
        this.jobEmployerId = jobEmployerId;
    }

    public void setJobCategory(String jobCategory) {
        this.jobCategory = jobCategory;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public void setJobCountry(String jobCountry) {
        this.jobCountry = jobCountry;
    }

    public void setJobCity(String jobCity) {
        this.jobCity = jobCity;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public void setJobSkills(List<String> jobSkills) {
        this.jobSkills = jobSkills;
    }

    public void setJobSalaryMin(double jobSalaryMin) {
        this.jobSalaryMin = jobSalaryMin;
    }

    public void setJobSalaryMax(double jobSalaryMax) {
        this.jobSalaryMax = jobSalaryMax;
    }

    public void setSalaryCurrency(String salaryCurrency) {
        this.salaryCurrency = salaryCurrency;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setJobActive(boolean jobActive) {
        this.jobActive = jobActive;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public void setApplicantCount(int applicantCount) {
        this.applicantCount = applicantCount;
    }

    public void setApplicants(List<String> applicants) {
        this.applicants = applicants;
    }

    public void setCompanyLogoUrl(String companyLogoUrl) {
        this.companyLogoUrl = companyLogoUrl;
    }

    public void setCompanyWebsite(String companyWebsite) {
        this.companyWebsite = companyWebsite;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get formatted location string (City, Country)
     */
    public String getLocation() {
        if (jobCity != null && !jobCity.isEmpty() &&
                jobCountry != null && !jobCountry.isEmpty()) {
            return jobCity + ", " + jobCountry;
        } else if (jobCity != null && !jobCity.isEmpty()) {
            return jobCity;
        } else if (jobCountry != null && !jobCountry.isEmpty()) {
            return jobCountry;
        }
        return "Location not specified";
    }

    /**
     * Get formatted salary range string
     */
    public String getSalaryRange() {
        if (jobSalaryMin > 0 && jobSalaryMax > 0) {
            return formatCurrency(jobSalaryMin) + " - " + formatCurrency(jobSalaryMax);
        } else if (jobSalaryMin > 0) {
            return "From " + formatCurrency(jobSalaryMin);
        } else if (jobSalaryMax > 0) {
            return "Up to " + formatCurrency(jobSalaryMax);
        }
        return "Salary not specified";
    }

    /**
     * Format currency with symbol
     */
    private String formatCurrency(double amount) {
        String symbol = getCurrencySymbol();

        // Format large numbers (e.g., 120000 -> 120K)
        if (amount >= 1000) {
            return symbol + String.format("%.0fK", amount / 1000);
        }

        return symbol + String.format("%.0f", amount);
    }

    /**
     * Get currency symbol
     */
    private String getCurrencySymbol() {
        if (salaryCurrency == null) {
            return "$";
        }

        switch (salaryCurrency) {
            case "USD":
                return "$";
            case "EUR":
                return "€";
            case "GBP":
                return "£";
            case "JPY":
                return "¥";
            case "INR":
                return "₹";
            case "AUD":
                return "A$";
            case "CAD":
                return "C$";
            case "CHF":
                return "CHF";
            case "CNY":
                return "¥";
            case "ILS":
                return "₪";
            default:
                return salaryCurrency + " ";
        }
    }

    /**
     * Check if application deadline has passed
     */
    public boolean isDeadlinePassed() {
        if (deadline <= 0) {
            return false;
        }
        return System.currentTimeMillis() > deadline;
    }

    /**
     * Check if user has applied
     */
    public boolean hasUserApplied(String userId) {
        return applicants != null && applicants.contains(userId);
    }

    /**
     * Get days remaining until deadline
     */
    public int getDaysUntilDeadline() {
        if (deadline <= 0) {
            return -1;
        }

        long diff = deadline - System.currentTimeMillis();
        return (int) (diff / (1000 * 60 * 60 * 24));
    }

    /**
     * Increment view count
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * Increment applicant count
     */
    public void incrementApplicantCount() {
        this.applicantCount++;
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validate if job has minimum required fields
     */
    public boolean isValid() {
        return jobTitle != null && !jobTitle.isEmpty() &&
                jobCompany != null && !jobCompany.isEmpty() &&
                jobEmployerId != null && !jobEmployerId.isEmpty() &&
                jobCategory != null && !jobCategory.isEmpty() &&
                jobType != null && !jobType.isEmpty();
    }

    @Override
    public String toString() {
        return "Job{" +
                "jobId='" + jobId + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", jobCompany='" + jobCompany + '\'' +
                ", jobType='" + jobType + '\'' +
                ", location='" + getLocation() + '\'' +
                ", salary='" + getSalaryRange() + '\'' +
                '}';
    }
}