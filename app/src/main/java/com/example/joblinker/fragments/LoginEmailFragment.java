package com.joblinker.fragments;

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

import com.example.joblinker.R;
import com.example.joblinker.activities.LoginActivity;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.utils.ValidationHelper;

public class LoginEmailFragment extends Fragment {

    private TextInputEditText etEmail, etPassword;
    private TextView tvForgotPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;

    private JobLinkerFirebaseManager firebaseManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_email, container, false);

        firebaseManager = JobLinkerFirebaseManager.getInstance();

        initializeViews(view);
        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        tvForgotPassword = view.findViewById(R.id.tv_forgot_password);
        btnLogin = view.findViewById(R.id.btn_login);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> loginWithEmail());

        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (ValidationHelper.isEmpty(email)) {
                etEmail.setError(getString(R.string.error_empty_field));
                etEmail.requestFocus();
                return;
            }

            if (!ValidationHelper.isValidEmail(email)) {
                etEmail.setError(getString(R.string.error_invalid_email));
                etEmail.requestFocus();
                return;
            }

            sendPasswordResetEmail(email);
        });
    }

    private void loginWithEmail() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (ValidationHelper.isEmpty(email)) {
            etEmail.setError(getString(R.string.error_empty_field));
            etEmail.requestFocus();
            return;
        }

        if (!ValidationHelper.isValidEmail(email)) {
            etEmail.setError(getString(R.string.error_invalid_email));
            etEmail.requestFocus();
            return;
        }

        if (ValidationHelper.isEmpty(password)) {
            etPassword.setError(getString(R.string.error_empty_field));
            etPassword.requestFocus();
            return;
        }

        if (!ValidationHelper.isValidPassword(password)) {
            etPassword.setError(getString(R.string.error_short_password));
            etPassword.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // Login
        firebaseManager.loginWithEmail(email, password, new JobLinkerFirebaseManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                progressBar.setVisibility(View.GONE);

                if (getActivity() instanceof LoginActivity) {
                    ((LoginActivity) getActivity()).onLoginSuccess(user);
                }
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendPasswordResetEmail(String email) {
        progressBar.setVisibility(View.VISIBLE);

        firebaseManager.sendPasswordResetEmail(email, new JobLinkerFirebaseManager.VoidCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(),
                        "Password reset email sent to " + email, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(),
                        "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}