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

    public DatabaseManager() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    // ===== USER OPERATIONS =====

    // Create/Update user
    public Task<Void> saveUser(User user) {
        return firestore.collection(USERS_COLLECTION)
                .document(user.getUserId())
                .set(user);
    }

    // Get user by ID
    public Task<com.google.firebase.firestore.DocumentSnapshot> getUser(String userId) {
        return firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get();
    }

    // ===== JOB OPERATIONS =====

    // Create/Update job
    public Task<Void> saveJob(Job job) {
        return firestore.collection(JOBS_COLLECTION)
                .document(job.getJobId())
                .set(job);
    }

    // Get job by ID
    public Task<com.google.firebase.firestore.DocumentSnapshot> getJob(String jobId) {
        return firestore.collection(JOBS_COLLECTION)
                .document(jobId)
                .get();
    }

    // Get all jobs (with pagination)
    public Query getAllJobs() {
        return firestore.collection(JOBS_COLLECTION)
                .whereEqualTo("status", "Open")
                .orderBy("createdAt", Query.Direction.DESCENDING);
    }

    // Get jobs by employer
    public Query getJobsByEmployer(String employerId) {
        return firestore.collection(JOBS_COLLECTION)
                .whereEqualTo("employerId", employerId)
                .orderBy("createdAt", Query.Direction.DESCENDING);
    }

    // Delete job
    public Task<Void> deleteJob(String jobId) {
        return firestore.collection(JOBS_COLLECTION)
                .document(jobId)
                .delete();
    }

    // ===== APPLICATION OPERATIONS =====

    // Create/Update application
    public Task<Void> saveApplication(Application application) {
        return firestore.collection(APPLICATIONS_COLLECTION)
                .document(application.getApplicationId())
                .set(application);
    }

    // Get application by ID
    public Task<com.google.firebase.firestore.DocumentSnapshot> getApplication(String applicationId) {
        return firestore.collection(APPLICATIONS_COLLECTION)
                .document(applicationId)
                .get();
    }

    // Get applications for a job seeker
    public Query getApplicationsByJobSeeker(String jobSeekerUserId) {
        return firestore.collection(APPLICATIONS_COLLECTION)
                .whereEqualTo("jobSeekerUserId", jobSeekerUserId)
                .orderBy("appliedAt", Query.Direction.DESCENDING);
    }

    // Get applications for a job
    public Query getApplicationsByJob(String jobId) {
        return firestore.collection(APPLICATIONS_COLLECTION)
                .whereEqualTo("jobId", jobId);
    }

    // Update application status
    public Task<Void> updateApplicationStatus(String applicationId, String status) {
        return firestore.collection(APPLICATIONS_COLLECTION)
                .document(applicationId)
                .update("status", status);
    }

    // Delete application
    public Task<Void> deleteApplication(String applicationId) {
        return firestore.collection(APPLICATIONS_COLLECTION)
                .document(applicationId)
                .delete();
    }
}