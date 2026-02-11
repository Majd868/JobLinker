package com.example.joblinker.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.Task;

public class AuthManager {
    private FirebaseAuth auth;

    public AuthManager() {
        this.auth = FirebaseAuth.getInstance();
    }

    // Register new user
    public Task<AuthResult> registerUser(String email, String password) {
        return auth.createUserWithEmailAndPassword(email, password);
    }

    // Login user
    public Task<AuthResult> loginUser(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    // Logout user
    public void logoutUser() {
        auth.signOut();
    }

    // Send password reset email
    public Task<Void> sendPasswordResetEmail(String email) {
        return auth.sendPasswordResetEmail(email);
    }

    // Get current user ID
    public String getCurrentUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }

    // Get current user email
    public String getCurrentUserEmail() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getEmail();
        }
        return null;
    }

    // Check if user is logged in
    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    // Delete current user account
    public Task<Void> deleteCurrentUser() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().delete();
        }
        return null;
    }
}