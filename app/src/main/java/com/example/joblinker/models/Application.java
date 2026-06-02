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

    // مُنشئ فارغ (مطلوب لـ Firebase)
    public Application() {}

    // مُنشئ بالحقول الأساسية
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

    // الدوال الجالبة والمحددة
    // تُرجع المعرف الفريد للطلب
    public String getApplicationId() { return applicationId; }
    // تُعيّن المعرف الفريد للطلب
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    // تُرجع معرف الوظيفة التي يخص هذا الطلب
    public String getJobId() { return jobId; }
    // تُعيّن معرف الوظيفة التي يخص هذا الطلب
    public void setJobId(String jobId) { this.jobId = jobId; }

    // تُرجع معرف المستخدم الباحث عن عمل الذي قدّم الطلب
    public String getJobSeekerUserId() { return jobSeekerUserId; }
    // تُعيّن معرف المستخدم الباحث عن عمل الذي قدّم الطلب
    public void setJobSeekerUserId(String jobSeekerUserId) { this.jobSeekerUserId = jobSeekerUserId; }

    // تُرجع معرف مستخدم صاحب العمل الذي نشر الوظيفة
    public String getEmployerUserId() { return employerUserId; }
    // تُعيّن معرف مستخدم صاحب العمل الذي نشر الوظيفة
    public void setEmployerUserId(String employerUserId) { this.employerUserId = employerUserId; }

    // تُرجع الحالة الحالية للطلب (مثلاً: مقدَّم، مختصَر)
    public String getStatus() { return status; }
    // تُعيّن الحالة الحالية للطلب
    public void setStatus(String status) { this.status = status; }

    // تُرجع نص خطاب التغطية المرفق مع الطلب
    public String getCoverLetter() { return coverLetter; }
    // تُعيّن نص خطاب التغطية للطلب
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }

    // تُرجع الطابع الزمني لوقت تقديم الطلب
    public long getAppliedAt() { return appliedAt; }
    // تُعيّن الطابع الزمني لوقت تقديم الطلب
    public void setAppliedAt(long appliedAt) { this.appliedAt = appliedAt; }

    // تُرجع الطابع الزمني لآخر تحديث للحالة
    public long getUpdatedAt() { return updatedAt; }
    // تُعيّن الطابع الزمني لآخر تحديث للحالة
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    // تُرجع مسمى الوظيفة التي يتقدم إليها
    public String getJobTitle() { return jobTitle; }
    // تُعيّن مسمى الوظيفة التي يتقدم إليها
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    // تُرجع الاسم المعروض للباحث عن عمل
    public String getJobSeekerName() { return jobSeekerName; }
    // تُعيّن الاسم المعروض للباحث عن عمل
    public void setJobSeekerName(String jobSeekerName) { this.jobSeekerName = jobSeekerName; }
}