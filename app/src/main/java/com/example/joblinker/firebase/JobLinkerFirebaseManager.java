package com.example.joblinker.firebase;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.joblinker.models.Call;
import com.joblinker.models.Conversation;
import com.example.joblinker.models.Job;
import com.joblinker.models.Message;
import com.example.joblinker.models.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JobLinkerFirebaseManager {

    private static final String TAG = "FirebaseManager";

    // Firebase instances
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;

    // Collection names
    private static final String USERS_COLLECTION = "users";
    private static final String JOBS_COLLECTION = "jobs";
    private static final String MESSAGES_COLLECTION = "messages";
    private static final String CALLS_COLLECTION = "calls";
    private static final String CONVERSATIONS_COLLECTION = "conversations";
    private static final String APPLICATIONS_COLLECTION = "applications";
    private static final String SAVED_JOBS_COLLECTION = "savedJobs";

    // Storage paths
    private static final String AVATARS_PATH = "avatars";
    private static final String DOCUMENTS_PATH = "documents";
    private static final String CHAT_IMAGES_PATH = "chat_images";

    // Singleton instance
    private static JobLinkerFirebaseManager instance;

    // Listener registrations for cleanup
    private final List<ListenerRegistration> listenerRegistrations;

    // ==================== CALLBACK INTERFACES ====================

    /**
     * Authentication callback
     */
    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String error);
    }

    /**
     * Data callback for single object
     */
    public interface DataCallback<T> {
        void onSuccess(T data);
        void onFailure(String error);
    }

    /**
     * List callback for multiple objects
     */
    public interface ListCallback<T> {
        void onSuccess(List<T> dataList);
        void onFailure(String error);
    }

    /**
     * Upload callback with progress
     */
    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onProgress(int progress);
        void onFailure(String error);
    }

    /**
     * Void callback for operations with no return value
     */
    public interface VoidCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // ==================== CONSTRUCTOR & INITIALIZATION ====================

    /**
     * Private constructor for singleton pattern
     */
    private JobLinkerFirebaseManager() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        listenerRegistrations = new ArrayList<>();
    }

    /**
     * Get singleton instance
     */
    public static synchronized JobLinkerFirebaseManager getInstance() {
        if (instance == null) {
            instance = new JobLinkerFirebaseManager();
        }
        return instance;
    }

    // ==================== AUTHENTICATION METHODS ====================

    /**
     * Register user with email and password
     */
    public void registerWithEmail(String email, String password, final AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Send email verification
                            sendEmailVerification(new VoidCallback() {
                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "Email verification sent");
                                    callback.onSuccess(user);
                                }

                                @Override
                                public void onFailure(String error) {
                                    // Still proceed even if verification email fails
                                    Log.e(TAG, "Failed to send verification email: " + error);
                                    callback.onSuccess(user);
                                }
                            });
                        }
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Registration failed";
                        callback.onFailure(error);
                        Log.e(TAG, "registerWithEmail:failure", task.getException());
                    }
                });
    }

    /**
     * Login with email and password
     */
    public void loginWithEmail(String email, String password, final AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Update online status
                            updateUserOnlineStatus(user.getUid(), true);
                        }
                        callback.onSuccess(user);
                        Log.d(TAG, "loginWithEmail:success");
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Login failed";
                        callback.onFailure(error);
                        Log.e(TAG, "loginWithEmail:failure", task.getException());
                    }
                });
    }

    /**
     * Sign in with phone credential
     */
    public void signInWithPhoneCredential(PhoneAuthCredential credential, final AuthCallback callback) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            updateUserOnlineStatus(user.getUid(), true);
                        }
                        callback.onSuccess(user);
                        Log.d(TAG, "signInWithPhoneCredential:success");
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Phone verification failed";
                        callback.onFailure(error);
                        Log.e(TAG, "signInWithPhoneCredential:failure", task.getException());
                    }
                });
    }

    /**
     * Send email verification
     */
    public void sendEmailVerification(final VoidCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                            Log.d(TAG, "Email verification sent");
                        } else {
                            String error = task.getException() != null ?
                                    task.getException().getMessage() : "Failed to send verification email";
                            callback.onFailure(error);
                            Log.e(TAG, "sendEmailVerification:failure", task.getException());
                        }
                    });
        } else {
            callback.onFailure("No user logged in");
        }
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String email, final VoidCallback callback) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                        Log.d(TAG, "Password reset email sent");
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Failed to send reset email";
                        callback.onFailure(error);
                        Log.e(TAG, "sendPasswordResetEmail:failure", task.getException());
                    }
                });
    }

    /**
     * Change password
     */
    public void changePassword(String newPassword, final VoidCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                            Log.d(TAG, "Password changed successfully");
                        } else {
                            String error = task.getException() != null ?
                                    task.getException().getMessage() : "Failed to change password";
                            callback.onFailure(error);
                            Log.e(TAG, "changePassword:failure", task.getException());
                        }
                    });
        } else {
            callback.onFailure("No user logged in");
        }
    }

    /**
     * Logout current user
     */
    public void logout() {
        String userId = getCurrentUserId();
        if (userId != null) {
            updateUserOnlineStatus(userId, false);
        }

        // Clean up listeners
        for (ListenerRegistration registration : listenerRegistrations) {
            registration.remove();
        }
        listenerRegistrations.clear();

        // Sign out
        mAuth.signOut();
        Log.d(TAG, "User logged out");
    }

    /**
     * Delete user account
     */
    public void deleteAccount(final VoidCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // First delete user data from Firestore
            deleteUser(userId, new VoidCallback() {
                @Override
                public void onSuccess() {
                    // Then delete auth account
                    user.delete().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                            Log.d(TAG, "Account deleted successfully");
                        } else {
                            String error = task.getException() != null ?
                                    task.getException().getMessage() : "Failed to delete account";
                            callback.onFailure(error);
                            Log.e(TAG, "deleteAccount:failure", task.getException());
                        }
                    });
                }

                @Override
                public void onFailure(String error) {
                    callback.onFailure(error);
                }
            });
        } else {
            callback.onFailure("No user logged in");
        }
    }

    /**
     * Get current user ID
     */
    public String getCurrentUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Get current Firebase user
     */
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    /**
     * Check if email is verified
     */
    public boolean isEmailVerified() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null && user.isEmailVerified();
    }

    /**
     * Reload current user to get updated verification status
     */
    public void reloadUser(final VoidCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    String error = task.getException() != null ?
                            task.getException().getMessage() : "Failed to reload user";
                    callback.onFailure(error);
                }
            });
        } else {
            callback.onFailure("No user logged in");
        }
    }

    // ==================== USER CRUD OPERATIONS ====================

    /**
     * Create user in Firestore
     */
    public void createUser(User user, final VoidCallback callback) {
        if (user.getUserId() == null || user.getUserId().isEmpty()) {
            callback.onFailure("User ID is required");
            return;
        }

        db.collection(USERS_COLLECTION)
                .document(user.getUserId())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                    Log.d(TAG, "User created successfully: " + user.getUserId());
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error creating user", e);
                });
    }

    /**
     * Get user by ID
     */
    public void getUser(String userId, final DataCallback<User> callback) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        callback.onSuccess(user);
                        Log.d(TAG, "User retrieved: " + userId);
                    } else {
                        callback.onFailure("User not found");
                        Log.w(TAG, "User not found: " + userId);
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error getting user", e);
                });
    }

    /**
     * Update user
     */
    public void updateUser(String userId, Map<String, Object> updates, final VoidCallback callback) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                    Log.d(TAG, "User updated successfully: " + userId);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error updating user", e);
                });
    }

    /**
     * Update user online status
     */
    public void updateUserOnlineStatus(String userId, boolean isOnline) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isOnline", isOnline);
        updates.put("lastSeen", System.currentTimeMillis());

        db.collection(USERS_COLLECTION)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "User online status updated: " + userId + " - " + isOnline))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error updating online status", e));
    }

    /**
     * Delete user
     */
    public void deleteUser(String userId, final VoidCallback callback) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                    Log.d(TAG, "User deleted successfully: " + userId);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error deleting user", e);
                });
    }

    /**
     * Search users by name or email
     */
    public void searchUsers(String query, final ListCallback<User> callback) {
        db.collection(USERS_COLLECTION)
                .orderBy("userName")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        users.add(user);
                    }
                    callback.onSuccess(users);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error searching users", e);
                });
    }

    // ==================== JOB CRUD OPERATIONS ====================

    /**
     * Create job
     */
    public void createJob(Job job, final DataCallback<String> callback) {
        DocumentReference docRef = db.collection(JOBS_COLLECTION).document();
        job.setJobId(docRef.getId());
        job.setCreatedAt(System.currentTimeMillis());

        docRef.set(job)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(job.getJobId());
                    Log.d(TAG, "Job created successfully: " + job.getJobId());
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error creating job", e);
                });
    }

    /**
     * Get job by ID
     */
    public void getJob(String jobId, final DataCallback<Job> callback) {
        db.collection(JOBS_COLLECTION)
                .document(jobId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Job job = documentSnapshot.toObject(Job.class);
                        callback.onSuccess(job);
                        Log.d(TAG, "Job retrieved: " + jobId);
                    } else {
                        callback.onFailure("Job not found");
                        Log.w(TAG, "Job not found: " + jobId);
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error getting job", e);
                });
    }

    /**
     * Get all active jobs
     */
    public void getActiveJobs(final ListCallback<Job> callback) {
        db.collection(JOBS_COLLECTION)
                .whereEqualTo("jobActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Job> jobs = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Job job = document.toObject(Job.class);
                        jobs.add(job);
                    }
                    callback.onSuccess(jobs);
                    Log.d(TAG, "Retrieved " + jobs.size() + " active jobs");
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error getting active jobs", e);
                });
    }

    /**
     * Get jobs by type
     */
    public void getJobsByType(String jobType, final ListCallback<Job> callback) {
        db.collection(JOBS_COLLECTION)
                .whereEqualTo("jobActive", true)
                .whereEqualTo("jobType", jobType)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Job> jobs = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Job job = document.toObject(Job.class);
                        jobs.add(job);
                    }
                    callback.onSuccess(jobs);
                    Log.d(TAG, "Retrieved " + jobs.size() + " jobs of type: " + jobType);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error getting jobs by type", e);
                });
    }

    /**
     * Get jobs by employer
     */
    public void getJobsByEmployer(String employerId, final ListCallback<Job> callback) {
        db.collection(JOBS_COLLECTION)
                .whereEqualTo("jobEmployerId", employerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Job> jobs = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Job job = document.toObject(Job.class);
                        jobs.add(job);
                    }
                    callback.onSuccess(jobs);
                    Log.d(TAG, "Retrieved " + jobs.size() + " jobs for employer: " + employerId);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error getting jobs by employer", e);
                });
    }

    /**
     * Search jobs by title, company, or description
     */
    public void searchJobs(String query, final ListCallback<Job> callback) {
        // Note: Firestore doesn't support full-text search natively
        // This is a basic implementation - consider using Algolia or Elasticsearch for production
        db.collection(JOBS_COLLECTION)
                .whereEqualTo("jobActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Job> jobs = new ArrayList<>();
                    String lowerQuery = query.toLowerCase();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Job job = document.toObject(Job.class);
                        if (job.getJobTitle().toLowerCase().contains(lowerQuery) ||
                                job.getJobCompany().toLowerCase().contains(lowerQuery) ||
                                job.getJobDescription().toLowerCase().contains(lowerQuery)) {
                            jobs.add(job);
                        }
                    }
                    callback.onSuccess(jobs);
                    Log.d(TAG, "Search returned " + jobs.size() + " jobs for query: " + query);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error searching jobs", e);
                });
    }

    /**
     * Update job
     */
    public void updateJob(String jobId, Map<String, Object> updates, final VoidCallback callback) {
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection(JOBS_COLLECTION)
                .document(jobId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                    Log.d(TAG, "Job updated successfully: " + jobId);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error updating job", e);
                });
    }

    /**
     * Delete job
     */
    public void deleteJob(String jobId, final VoidCallback callback) {
        db.collection(JOBS_COLLECTION)
                .document(jobId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                    Log.d(TAG, "Job deleted successfully: " + jobId);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error deleting job", e);
                });
    }

    /**
     * Listen to active jobs in real-time
     */
    public ListenerRegistration listenToActiveJobs(final ListCallback<Job> callback) {
        ListenerRegistration registration = db.collection(JOBS_COLLECTION)
                .whereEqualTo("jobActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        callback.onFailure(e.getMessage());
                        Log.e(TAG, "Listen to jobs failed", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<Job> jobs = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Job job = document.toObject(Job.class);
                            jobs.add(job);
                        }
                        callback.onSuccess(jobs);
                        Log.d(TAG, "Jobs updated via listener: " + jobs.size());
                    }
                });

        listenerRegistrations.add(registration);
        return registration;
    }

    /**
     * Increment job view count
     */
    public void incrementJobViewCount(String jobId) {
        db.collection(JOBS_COLLECTION)
                .document(jobId)
                .update("viewCount", FieldValue.increment(1))
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "View count incremented for job: " + jobId))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error incrementing view count", e));
    }

    /**
     * Add applicant to job
     */
    public void addJobApplicant(String jobId, String userId, final VoidCallback callback) {
        db.collection(JOBS_COLLECTION)
                .document(jobId)
                .update("applicants", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                    Log.d(TAG, "Applicant added to job: " + jobId);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error adding applicant", e);
                });
    }

    // ==================== MESSAGE CRUD OPERATIONS ====================

    /**
     * Send message
     */
    public void sendMessage(Message message, final DataCallback<String> callback) {
        DocumentReference docRef = db.collection(MESSAGES_COLLECTION).document();
        message.setMessageId(docRef.getId());
        message.setMessageTimestamp(System.currentTimeMillis());

        docRef.set(message)
                .addOnSuccessListener(aVoid -> {
                    // Update conversation's last message
                    updateConversationLastMessage(message);
                    callback.onSuccess(message.getMessageId());
                    Log.d(TAG, "Message sent successfully: " + message.getMessageId());
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error sending message", e);
                });
    }

    /**
     * Get messages for a conversation
     */
    public void getMessages(String conversationId, final ListCallback<Message> callback) {
        db.collection(MESSAGES_COLLECTION)
                .whereEqualTo("conversationId", conversationId)
                .orderBy("messageTimestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Message> messages = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Message message = document.toObject(Message.class);
                        messages.add(message);
                    }
                    callback.onSuccess(messages);
                    Log.d(TAG, "Retrieved " + messages.size() + " messages for conversation: " + conversationId);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error getting messages", e);
                });
    }

    /**
     * Listen to messages in real-time
     */
    public ListenerRegistration listenToMessages(String conversationId, final ListCallback<Message> callback) {
        ListenerRegistration registration = db.collection(MESSAGES_COLLECTION)
                .whereEqualTo("conversationId", conversationId)
                .orderBy("messageTimestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        callback.onFailure(e.getMessage());
                        Log.e(TAG, "Listen to messages failed", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<Message> messages = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Message message = document.toObject(Message.class);
                            messages.add(message);
                        }
                        callback.onSuccess(messages);
                        Log.d(TAG, "Messages updated via listener: " + messages.size());
                    }
                });

        listenerRegistrations.add(registration);
        return registration;
    }

    /**
     * Mark message as read
     */
    public void markMessageAsRead(String messageId, final VoidCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("messageRead", true);

        db.collection(MESSAGES_COLLECTION)
                .document(messageId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                    Log.d(TAG, "Message marked as read: " + messageId);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error marking message as read", e);
                });
    }

    /**
     * Mark all messages in conversation as read
     */
    public void markAllMessagesAsRead(String conversationId, String userId, final VoidCallback callback) {
        db.collection(MESSAGES_COLLECTION)
                .whereEqualTo("conversationId", conversationId)
                .whereEqualTo("messageReceiverId", userId)
                .whereEqualTo("messageRead", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().update("messageRead", true);
                    }
                    callback.onSuccess();
                    Log.d(TAG, "All messages marked as read for conversation: " + conversationId);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error marking all messages as read", e);
                });
    }

    /**
     * Delete message
     */
    public void deleteMessage(String messageId, final VoidCallback callback) {
        db.collection(MESSAGES_COLLECTION)
                .document(messageId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                    Log.d(TAG, "Message deleted: " + messageId);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error deleting message", e);
                });
    }

    /**
     * Update conversation's last message
     */
    private void updateConversationLastMessage(Message message) {
        Map<String, Object> conversationData = new HashMap<>();
        conversationData.put("lastMessage", message.getMessageText());
        conversationData.put("lastMessageTime", message.getMessageTimestamp());
        conversationData.put("lastMessageSenderId", message.getMessageSenderId());
        conversationData.put("participants", Arrays.asList(
                message.getMessageSenderId(),
                message.getMessageReceiverId()
        ));

        db.collection(CONVERSATIONS_COLLECTION)
                .document(message.getConversationId())
                .set(conversationData)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Conversation updated: " + message.getConversationId()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error updating conversation", e));
    }

    /**
     * Get user conversations
     */
    public void getUserConversations(String userId, final ListCallback<Conversation> callback) {
        db.collection(CONVERSATIONS_COLLECTION)
                .whereArrayContains("participants", userId)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Conversation> conversations = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Conversation conversation = document.toObject(Conversation.class);
                        conversation.setConversationId(document.getId());
                        conversations.add(conversation);
                    }
                    callback.onSuccess(conversations);
                    Log.d(TAG, "Retrieved " + conversations.size() + " conversations for user: " + userId);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error getting conversations", e);
                });
    }

    /**
     * Listen to conversations in real-time
     */
    public ListenerRegistration listenToConversations(String userId, final ListCallback<Conversation> callback) {
        ListenerRegistration registration = db.collection(CONVERSATIONS_COLLECTION)
                .whereArrayContains("participants", userId)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        callback.onFailure(e.getMessage());
                        Log.e(TAG, "Listen to conversations failed", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<Conversation> conversations = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Conversation conversation = document.toObject(Conversation.class);
                            conversation.setConversationId(document.getId());
                            conversations.add(conversation);
                        }
                        callback.onSuccess(conversations);
                        Log.d(TAG, "Conversations updated via listener: " + conversations.size());
                    }
                });

        listenerRegistrations.add(registration);
        return registration;
    }

    // ==================== CALL OPERATIONS ====================

    /**
     * Create call record
     */
    public void createCall(Call call, final DataCallback<String> callback) {
        DocumentReference docRef = db.collection(CALLS_COLLECTION).document();
        call.setCallId(docRef.getId());
        call.setStartTime(System.currentTimeMillis());

        docRef.set(call)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(call.getCallId());
                    Log.d(TAG, "Call created successfully: " + call.getCallId());
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error creating call", e);
                });
    }

    /**
     * Update call status
     */
    public void updateCallStatus(String callId, String status, final VoidCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("callStatus", status);

        if ("ended".equals(status)) {
            updates.put("endTime", System.currentTimeMillis());
        } else if ("connected".equals(status)) {
            updates.put("startTime", System.currentTimeMillis());
        }

        db.collection(CALLS_COLLECTION)
                .document(callId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                    Log.d(TAG, "Call status updated: " + callId + " - " + status);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error updating call status", e);
                });
    }

    /**
     * Get call by ID
     */
    public void getCall(String callId, final DataCallback<Call> callback) {
        db.collection(CALLS_COLLECTION)
                .document(callId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Call call = documentSnapshot.toObject(Call.class);
                        callback.onSuccess(call);
                        Log.d(TAG, "Call retrieved: " + callId);
                    } else {
                        callback.onFailure("Call not found");
                        Log.w(TAG, "Call not found: " + callId);
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error getting call", e);
                });
    }

    /**
     * Get call history for user
     */
    public void getCallHistory(String userId, final ListCallback<Call> callback) {
        List<Call> allCalls = new ArrayList<>();

        // Get calls where user is caller
        db.collection(CALLS_COLLECTION)
                .whereEqualTo("callerId", userId)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Call call = document.toObject(Call.class);
                        allCalls.add(call);
                    }

                    // Get calls where user is receiver
                    db.collection(CALLS_COLLECTION)
                            .whereEqualTo("receiverId", userId)
                            .orderBy("startTime", Query.Direction.DESCENDING)
                            .limit(50)
                            .get()
                            .addOnSuccessListener(receivedCallsSnapshot -> {
                                for (QueryDocumentSnapshot document : receivedCallsSnapshot) {
                                    Call call = document.toObject(Call.class);
                                    allCalls.add(call);
                                }
                                callback.onSuccess(allCalls);
                                Log.d(TAG, "Retrieved " + allCalls.size() + " calls for user: " + userId);
                            })
                            .addOnFailureListener(e -> {
                                callback.onFailure(e.getMessage());
                                Log.e(TAG, "Error getting received calls", e);
                            });
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error getting call history", e);
                });
    }

    // ==================== STORAGE OPERATIONS ====================

    /**
     * Upload image to Firebase Storage
     */
    public void uploadImage(Uri imageUri, String path, final UploadCallback callback) {
        if (imageUri == null) {
            callback.onFailure("Image URI is null");
            return;
        }

        // Generate unique filename
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference fileRef = storage.getReference().child(path + "/" + fileName);

        UploadTask uploadTask = fileRef.putFile(imageUri);
        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            callback.onProgress((int) progress);
        }).addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                callback.onSuccess(uri.toString());
                Log.d(TAG, "Image uploaded successfully: " + uri.toString());
            }).addOnFailureListener(e -> {
                callback.onFailure(e.getMessage());
                Log.e(TAG, "Error getting download URL", e);
            });
        }).addOnFailureListener(e -> {
            callback.onFailure(e.getMessage());
            Log.e(TAG, "Error uploading image", e);
        });
    }

    /**
     * Upload document to Firebase Storage
     */
    public void uploadDocument(Uri documentUri, String path, final UploadCallback callback) {
        if (documentUri == null) {
            callback.onFailure("Document URI is null");
            return;
        }

        String fileName = UUID.randomUUID().toString();
        StorageReference fileRef = storage.getReference().child(path + "/" + fileName);

        UploadTask uploadTask = fileRef.putFile(documentUri);
        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            callback.onProgress((int) progress);
        }).addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                callback.onSuccess(uri.toString());
                Log.d(TAG, "Document uploaded successfully: " + uri.toString());
            }).addOnFailureListener(e -> {
                callback.onFailure(e.getMessage());
                Log.e(TAG, "Error getting download URL", e);
            });
        }).addOnFailureListener(e -> {
            callback.onFailure(e.getMessage());
            Log.e(TAG, "Error uploading document", e);
        });
    }

    /**
     * Delete file from Firebase Storage
     */
    public void deleteFile(String fileUrl, final VoidCallback callback) {
        try {
            StorageReference fileRef = storage.getReferenceFromUrl(fileUrl);
            fileRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        callback.onSuccess();
                        Log.d(TAG, "File deleted successfully");
                    })
                    .addOnFailureListener(e -> {
                        callback.onFailure(e.getMessage());
                        Log.e(TAG, "Error deleting file", e);
                    });
        } catch (IllegalArgumentException e) {
            callback.onFailure("Invalid file URL");
            Log.e(TAG, "Invalid file URL", e);
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Remove all listeners
     */
    public void removeAllListeners() {
        for (ListenerRegistration registration : listenerRegistrations) {
            registration.remove();
        }
        listenerRegistrations.clear();
        Log.d(TAG, "All listeners removed");
    }

    /**
     * Generate conversation ID from two user IDs
     * Always generates the same ID regardless of order
     */
    public static String generateConversationId(String userId1, String userId2) {
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    /**
     * Check if user has saved a job
     */
    public void hasUserSavedJob(String userId, String jobId, final DataCallback<Boolean> callback) {
        db.collection(SAVED_JOBS_COLLECTION)
                .document(userId + "_" + jobId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    callback.onSuccess(documentSnapshot.exists());
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error checking saved job", e);
                });
    }

    /**
     * Save job for user
     */
    public void saveJob(String userId, String jobId, final VoidCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("jobId", jobId);
        data.put("savedAt", System.currentTimeMillis());

        db.collection(SAVED_JOBS_COLLECTION)
                .document(userId + "_" + jobId)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                    Log.d(TAG, "Job saved: " + jobId);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error saving job", e);
                });
    }

    /**
     * Unsave job for user
     */
    public void unsaveJob(String userId, String jobId, final VoidCallback callback) {
        db.collection(SAVED_JOBS_COLLECTION)
                .document(userId + "_" + jobId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                    Log.d(TAG, "Job unsaved: " + jobId);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error unsaving job", e);
                });
    }

    /**
     * Get saved jobs for user
     */
    public void getSavedJobs(String userId, final ListCallback<String> callback) {
        db.collection(SAVED_JOBS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("savedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> jobIds = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String jobId = document.getString("jobId");
                        if (jobId != null) {
                            jobIds.add(jobId);
                        }
                    }
                    callback.onSuccess(jobIds);
                    Log.d(TAG, "Retrieved " + jobIds.size() + " saved jobs for user: " + userId);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                    Log.e(TAG, "Error getting saved jobs", e);
                });
    }
}