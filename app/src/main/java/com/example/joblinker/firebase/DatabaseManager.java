package com.example.joblinker.firebase;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.android.gms.tasks.Task;
import com.example.joblinker.models.User;
import com.example.joblinker.models.Job;
import com.example.joblinker.models.Application;

public class DatabaseManager {
    private FirebaseFirestore firestore;
    private static final String USERS_COLLECTION = "users";
    private static final String JOBS_COLLECTION = "jobs";
    private static final String APPLICATIONS_COLLECTION = "applications";

    // يُنشئ كائن DatabaseManager ويُهيئ مثيل FirebaseFirestore
    public DatabaseManager() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    // ===== USER OPERATIONS =====

    // إنشاء مستخدم أو تحديث بياناته
    public Task<Void> saveUser(User user) {
        return firestore.collection(USERS_COLLECTION)
                .document(user.getUserId())
                .set(user);
    }

    // الحصول على المستخدم بواسطة المعرّف
    public Task<com.google.firebase.firestore.DocumentSnapshot> getUser(String userId) {
        return firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get();
    }

    // ===== JOB OPERATIONS =====

    // إنشاء وظيفة أو تحديث بياناتها
    public Task<Void> saveJob(Job job) {
        return firestore.collection(JOBS_COLLECTION)
                .document(job.getJobId())
                .set(job);
    }

    // الحصول على الوظيفة بواسطة المعرّف
    public Task<com.google.firebase.firestore.DocumentSnapshot> getJob(String jobId) {
        return firestore.collection(JOBS_COLLECTION)
                .document(jobId)
                .get();
    }

    // الحصول على جميع الوظائف مع دعم التصفح الصفحي
    public Query getAllJobs() {
        return firestore.collection(JOBS_COLLECTION)
                .whereEqualTo("status", "Open")
                .orderBy("createdAt", Query.Direction.DESCENDING);
    }

    // الحصول على الوظائف الخاصة بصاحب العمل
    public Query getJobsByEmployer(String employerId) {
        return firestore.collection(JOBS_COLLECTION)
                .whereEqualTo("employerId", employerId)
                .orderBy("createdAt", Query.Direction.DESCENDING);
    }

    // حذف وظيفة
    public Task<Void> deleteJob(String jobId) {
        return firestore.collection(JOBS_COLLECTION)
                .document(jobId)
                .delete();
    }

    // ===== APPLICATION OPERATIONS =====

    // إنشاء طلب توظيف أو تحديثه
    public Task<Void> saveApplication(Application application) {
        return firestore.collection(APPLICATIONS_COLLECTION)
                .document(application.getApplicationId())
                .set(application);
    }

    // الحصول على طلب التوظيف بواسطة المعرّف
    public Task<com.google.firebase.firestore.DocumentSnapshot> getApplication(String applicationId) {
        return firestore.collection(APPLICATIONS_COLLECTION)
                .document(applicationId)
                .get();
    }

    // الحصول على الطلبات المقدَّمة من قبل باحث عن عمل
    public Query getApplicationsByJobSeeker(String jobSeekerUserId) {
        return firestore.collection(APPLICATIONS_COLLECTION)
                .whereEqualTo("jobSeekerUserId", jobSeekerUserId)
                .orderBy("appliedAt", Query.Direction.DESCENDING);
    }

    // الحصول على الطلبات المقدَّمة لوظيفة معيّنة
    public Query getApplicationsByJob(String jobId) {
        return firestore.collection(APPLICATIONS_COLLECTION)
                .whereEqualTo("jobId", jobId);
    }

    // تحديث حالة طلب التوظيف
    public Task<Void> updateApplicationStatus(String applicationId, String status) {
        return firestore.collection(APPLICATIONS_COLLECTION)
                .document(applicationId)
                .update("status", status);
    }

    // حذف طلب التوظيف
    public Task<Void> deleteApplication(String applicationId) {
        return firestore.collection(APPLICATIONS_COLLECTION)
                .document(applicationId)
                .delete();
    }
}