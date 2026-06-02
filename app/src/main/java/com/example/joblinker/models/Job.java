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
    private String employmentType; // On-site, Hybrid, Remote (work arrangement)
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

    // المُنشئ الافتراضي (مطلوب لـ Firebase)
    public Job() {
        this.jobActive = true;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.viewCount = 0;
        this.applicantCount = 0;
        this.salaryCurrency = "USD";
    }

    // مُنشئ بالحقول الأساسية
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

    // تُرجع المعرف الفريد للوظيفة
    public String getJobId() {
        return jobId;
    }

    // تُرجع مسمى الوظيفة
    public String getJobTitle() {
        return jobTitle;
    }

    // تُرجع اسم الشركة التي تعرض الوظيفة
    public String getJobCompany() {
        return jobCompany;
    }

    // تُرجع معرف مستخدم صاحب العمل الذي نشر الوظيفة
    public String getJobEmployerId() {
        return jobEmployerId;
    }

    // تُرجع تصنيف الوظيفة (مثلاً: هندسة، تسويق)
    public String getJobCategory() {
        return jobCategory;
    }

    // تُرجع نوع الوظيفة (مثلاً: دوام كامل، دوام جزئي، عن بُعد)
    public String getJobType() {
        return jobType;
    }

    // تُرجع طريقة العمل (مثلاً: في الموقع، هجين، عن بُعد)
    public String getEmploymentType() {
        return employmentType;
    }

    // تُرجع البلد الذي توجد فيه الوظيفة
    public String getJobCountry() {
        return jobCountry;
    }

    // تُرجع المدينة التي توجد فيها الوظيفة
    public String getJobCity() {
        return jobCity;
    }

    // تُرجع الوصف الكامل للوظيفة
    public String getJobDescription() {
        return jobDescription;
    }

    // تُرجع قائمة المهارات المطلوبة للوظيفة
    public List<String> getJobSkills() {
        return jobSkills;
    }

    // تُرجع الحد الأدنى للراتب لهذه الوظيفة
    public double getJobSalaryMin() {
        return jobSalaryMin;
    }

    // تُرجع الحد الأقصى للراتب لهذه الوظيفة
    public double getJobSalaryMax() {
        return jobSalaryMax;
    }

    // تُرجع رمز العملة المستخدم للراتب (مثلاً: USD)
    public String getSalaryCurrency() {
        return salaryCurrency;
    }

    // تُرجع الطابع الزمني لآخر موعد لتقديم الطلبات
    public long getDeadline() {
        return deadline;
    }

    // تُرجع الطابع الزمني لوقت إنشاء الوظيفة
    public long getCreatedAt() {
        return createdAt;
    }

    // تُرجع الطابع الزمني لآخر تحديث للوظيفة
    public long getUpdatedAt() {
        return updatedAt;
    }

    // تُرجع true إذا كان إعلان الوظيفة نشطاً حالياً
    public boolean isJobActive() {
        return jobActive;
    }

    // تُرجع عدد مرات مشاهدة إعلان الوظيفة
    public int getViewCount() {
        return viewCount;
    }

    // تُرجع عدد المتقدمين للوظيفة
    public int getApplicantCount() {
        return applicantCount;
    }

    // تُرجع قائمة معرّفات المستخدمين الذين تقدموا لهذه الوظيفة
    public List<String> getApplicants() {
        return applicants;
    }

    // تُرجع رابط صورة شعار الشركة
    public String getCompanyLogoUrl() {
        return companyLogoUrl;
    }

    // تُرجع رابط موقع الشركة الإلكتروني
    public String getCompanyWebsite() {
        return companyWebsite;
    }

    // تُرجع البريد الإلكتروني للتواصل بشأن الوظيفة
    public String getContactEmail() {
        return contactEmail;
    }

    // تُرجع رقم الهاتف للتواصل بشأن الوظيفة
    public String getContactPhone() {
        return contactPhone;
    }

    // ==================== SETTERS ====================

    // تُعيّن المعرف الفريد للوظيفة
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    // تُعيّن مسمى الوظيفة
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    // تُعيّن اسم الشركة التي تعرض الوظيفة
    public void setJobCompany(String jobCompany) {
        this.jobCompany = jobCompany;
    }

    // تُعيّن معرف مستخدم صاحب العمل الذي نشر الوظيفة
    public void setJobEmployerId(String jobEmployerId) {
        this.jobEmployerId = jobEmployerId;
    }

    // تُعيّن تصنيف الوظيفة
    public void setJobCategory(String jobCategory) {
        this.jobCategory = jobCategory;
    }

    // تُعيّن نوع الوظيفة (مثلاً: دوام كامل، دوام جزئي)
    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    // تُعيّن طريقة العمل (مثلاً: في الموقع، هجين)
    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    // تُعيّن البلد الذي توجد فيه الوظيفة
    public void setJobCountry(String jobCountry) {
        this.jobCountry = jobCountry;
    }

    // تُعيّن المدينة التي توجد فيها الوظيفة
    public void setJobCity(String jobCity) {
        this.jobCity = jobCity;
    }

    // تُعيّن الوصف الكامل للوظيفة
    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    // تُعيّن قائمة المهارات المطلوبة للوظيفة
    public void setJobSkills(List<String> jobSkills) {
        this.jobSkills = jobSkills;
    }

    // تُعيّن الحد الأدنى للراتب لهذه الوظيفة
    public void setJobSalaryMin(double jobSalaryMin) {
        this.jobSalaryMin = jobSalaryMin;
    }

    // تُعيّن الحد الأقصى للراتب لهذه الوظيفة
    public void setJobSalaryMax(double jobSalaryMax) {
        this.jobSalaryMax = jobSalaryMax;
    }

    // تُعيّن رمز العملة للراتب
    public void setSalaryCurrency(String salaryCurrency) {
        this.salaryCurrency = salaryCurrency;
    }

    // تُعيّن الطابع الزمني لآخر موعد لتقديم الطلبات
    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    // تُعيّن الطابع الزمني لوقت إنشاء الوظيفة
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // تُعيّن الطابع الزمني لآخر تحديث للوظيفة
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // تُعيّن ما إذا كان إعلان الوظيفة نشطاً حالياً
    public void setJobActive(boolean jobActive) {
        this.jobActive = jobActive;
    }

    // تُعيّن عداد المشاهدات لهذا الإعلان
    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    // تُعيّن عدد المتقدمين لهذه الوظيفة
    public void setApplicantCount(int applicantCount) {
        this.applicantCount = applicantCount;
    }

    // تُعيّن قائمة معرّفات المستخدمين الذين تقدموا لهذه الوظيفة
    public void setApplicants(List<String> applicants) {
        this.applicants = applicants;
    }

    // تُعيّن رابط صورة شعار الشركة
    public void setCompanyLogoUrl(String companyLogoUrl) {
        this.companyLogoUrl = companyLogoUrl;
    }

    // تُعيّن رابط موقع الشركة الإلكتروني
    public void setCompanyWebsite(String companyWebsite) {
        this.companyWebsite = companyWebsite;
    }

    // تُعيّن البريد الإلكتروني للتواصل بشأن الوظيفة
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    // تُعيّن رقم الهاتف للتواصل بشأن الوظيفة
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

    // ── Urgent flag ───────────────────────────────
    private boolean urgent = false;

    // تُرجع true إذا كانت الوظيفة مُصنَّفة على أنها عاجلة
    @com.google.firebase.firestore.PropertyName("urgent")
    public boolean isUrgent() { return urgent; }

    // تُعيّن ما إذا كانت الوظيفة مُصنَّفة على أنها عاجلة
    @com.google.firebase.firestore.PropertyName("urgent")
    public void setUrgent(boolean urgent) { this.urgent = urgent; }

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