package com.example.joblinker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.example.joblinker.R;
import com.example.joblinker.activities.LoginActivity;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.utils.ValidationHelper;

import java.util.concurrent.Executors;

public class LoginEmailFragment extends Fragment {

    // ⚠️ Replace with your Web Client ID from Firebase Console → Authentication → Google → Web SDK Config
    private static final String WEB_CLIENT_ID = "943441972422-p7rs8rbnq9k28tl7rpre25t327mroape.apps.googleusercontent.com";

    private TextInputEditText etEmail, etPassword;
    private TextView          tvForgotPassword;
    private MaterialButton    btnLogin, btnGoogleLogin;
    private ProgressBar       progressBar;
    private JobLinkerFirebaseManager firebaseManager;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_email, container, false);
        firebaseManager = JobLinkerFirebaseManager.getInstance();
        initViews(view);
        setupListeners();
        return view;
    }

    private void initViews(View v) {
        etEmail          = v.findViewById(R.id.et_email);
        etPassword       = v.findViewById(R.id.et_password);
        tvForgotPassword = v.findViewById(R.id.tv_forgot_password);
        btnLogin         = v.findViewById(R.id.btn_login);
        btnGoogleLogin   = v.findViewById(R.id.btn_google_login);
        progressBar      = v.findViewById(R.id.progress_bar);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> loginWithEmail());

        if (btnGoogleLogin != null)
            btnGoogleLogin.setOnClickListener(v -> loginWithGoogle());

        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (ValidationHelper.isEmpty(email)) {
                etEmail.setError(getString(R.string.error_empty_field));
                etEmail.requestFocus(); return;
            }
            if (!ValidationHelper.isValidEmail(email)) {
                etEmail.setError(getString(R.string.error_invalid_email));
                etEmail.requestFocus(); return;
            }
            resetPassword(email);
        });
    }

    // ── Email login ───────────────────────────────
    private void loginWithEmail() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (ValidationHelper.isEmpty(email)) {
            etEmail.setError(getString(R.string.error_empty_field));
            etEmail.requestFocus(); return;
        }
        if (!ValidationHelper.isValidEmail(email)) {
            etEmail.setError(getString(R.string.error_invalid_email));
            etEmail.requestFocus(); return;
        }
        if (ValidationHelper.isEmpty(password)) {
            etPassword.setError(getString(R.string.error_empty_field));
            etPassword.requestFocus(); return;
        }
        if (!ValidationHelper.isValidPassword(password)) {
            etPassword.setError(getString(R.string.error_short_password));
            etPassword.requestFocus(); return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        firebaseManager.loginWithEmail(email, password,
            new JobLinkerFirebaseManager.AuthCallback() {
                @Override public void onSuccess(FirebaseUser user) {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);
                    if (getActivity() instanceof LoginActivity)
                        ((LoginActivity) getActivity()).onLoginSuccess(user);
                }
                @Override public void onFailure(String error) {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
    }

    // ── Google login ──────────────────────────────
    private void loginWithGoogle() {
        progressBar.setVisibility(View.VISIBLE);
        if (btnGoogleLogin != null) btnGoogleLogin.setEnabled(false);

        try {
            GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(true)
                .build();

            GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

            CredentialManager credentialManager =
                CredentialManager.create(requireContext());

            credentialManager.getCredentialAsync(
                requireContext(),
                request,
                null,
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        requireActivity().runOnUiThread(() ->
                            handleGoogleCredential(result));
                    }
                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        requireActivity().runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            if (btnGoogleLogin != null) btnGoogleLogin.setEnabled(true);
                            Toast.makeText(requireContext(),
                                "Google sign-in failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            );
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            if (btnGoogleLogin != null) btnGoogleLogin.setEnabled(true);
            Toast.makeText(requireContext(),
                "Google sign-in unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleGoogleCredential(GetCredentialResponse result) {
        try {
            GoogleIdTokenCredential googleCred =
                GoogleIdTokenCredential.createFrom(result.getCredential().getData());
            String idToken = googleCred.getIdToken();

            com.google.firebase.auth.AuthCredential firebaseCred =
                GoogleAuthProvider.getCredential(idToken, null);

            com.google.firebase.auth.FirebaseAuth.getInstance()
                .signInWithCredential(firebaseCred)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (btnGoogleLogin != null) btnGoogleLogin.setEnabled(true);
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        FirebaseUser user = task.getResult().getUser();
                        // Create user profile in Firestore if first login
                        createUserIfNew(user);
                    } else {
                        Toast.makeText(requireContext(),
                            "Google sign-in failed", Toast.LENGTH_SHORT).show();
                    }
                });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            if (btnGoogleLogin != null) btnGoogleLogin.setEnabled(true);
            Toast.makeText(requireContext(),
                "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void createUserIfNew(FirebaseUser firebaseUser) {
        firebaseManager.getUser(firebaseUser.getUid(),
            new JobLinkerFirebaseManager.DataCallback<com.example.joblinker.models.User>() {
                @Override
                public void onSuccess(com.example.joblinker.models.User user) {
                    // User already exists — go to main
                    if (getActivity() instanceof LoginActivity)
                        ((LoginActivity) getActivity()).onLoginSuccess(firebaseUser);
                }
                @Override
                public void onFailure(String error) {
                    // New user — create profile
                    com.example.joblinker.models.User newUser =
                        new com.example.joblinker.models.User(
                            firebaseUser.getUid(),
                            firebaseUser.getDisplayName() != null
                                ? firebaseUser.getDisplayName() : "User",
                            firebaseUser.getEmail(),
                            "JobSeeker");
                    if (firebaseUser.getPhotoUrl() != null)
                        newUser.setAvatarUrl(firebaseUser.getPhotoUrl().toString());

                    firebaseManager.createUser(newUser,
                        new JobLinkerFirebaseManager.VoidCallback() {
                            @Override public void onSuccess() {
                                if (getActivity() instanceof LoginActivity)
                                    ((LoginActivity) getActivity()).onLoginSuccess(firebaseUser);
                            }
                            @Override public void onFailure(String e) {
                                if (getActivity() instanceof LoginActivity)
                                    ((LoginActivity) getActivity()).onLoginSuccess(firebaseUser);
                            }
                        });
                }
            });
    }

    // ── Password reset ────────────────────────────
    private void resetPassword(String email) {
        progressBar.setVisibility(View.VISIBLE);
        firebaseManager.sendPasswordResetEmail(email,
            new JobLinkerFirebaseManager.VoidCallback() {
                @Override public void onSuccess() {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),
                        "Password reset email sent to " + email, Toast.LENGTH_LONG).show();
                }
                @Override public void onFailure(String error) {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),
                        "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
    }
}
