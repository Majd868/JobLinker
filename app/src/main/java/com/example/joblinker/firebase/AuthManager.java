package com.example.joblinker.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.Task;

public class AuthManager {
    private FirebaseAuth auth;

    // يُنشئ كائن AuthManager ويُهيئ مثيل FirebaseAuth
    public AuthManager() {
        this.auth = FirebaseAuth.getInstance();
    }

    // تسجيل مستخدم جديد
    public Task<AuthResult> registerUser(String email, String password) {
        return auth.createUserWithEmailAndPassword(email, password);
    }

    // تسجيل دخول المستخدم
    public Task<AuthResult> loginUser(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    // تسجيل خروج المستخدم
    public void logoutUser() {
        auth.signOut();
    }

    // إرسال بريد إلكتروني لإعادة تعيين كلمة المرور
    public Task<Void> sendPasswordResetEmail(String email) {
        return auth.sendPasswordResetEmail(email);
    }

    // الحصول على معرّف المستخدم الحالي
    public String getCurrentUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }

    // الحصول على البريد الإلكتروني للمستخدم الحالي
    public String getCurrentUserEmail() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getEmail();
        }
        return null;
    }

    // التحقق مما إذا كان المستخدم قد سجّل دخوله
    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    // حذف حساب المستخدم الحالي
    public Task<Void> deleteCurrentUser() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().delete();
        }
        return null;
    }
}