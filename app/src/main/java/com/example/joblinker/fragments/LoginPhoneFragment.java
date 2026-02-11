package com.example.joblinker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import com.example.joblinker.R;
import com.example.joblinker.activities.LoginActivity;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.utils.ValidationHelper;

public class LoginPhoneFragment extends Fragment {

    private TextInputEditText etCountryCode, etPhone, etVerificationCode;
    private MaterialButton btnSendCode, btnVerify;
    private ProgressBar progressBar;

    private JobLinkerFirebaseManager firebaseManager;
    private String verificationId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_phone, container, false);

        firebaseManager = JobLinkerFirebaseManager.getInstance();

        initializeViews(view);
        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        etCountryCode = view.findViewById(R.id.et_country_code);
        etPhone = view.findViewById(R.id.et_phone);
        etVerificationCode = view.findViewById(R.id.et_verification_code);
        btnSendCode = view.findViewById(R.id.btn_send_code);
        btnVerify = view.findViewById(R.id.btn_verify);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupClickListeners() {
        btnSendCode.setOnClickListener(v -> sendVerificationCode());
        btnVerify.setOnClickListener(v -> verifyCode());
    }

    private void sendVerificationCode() {
        String countryCode = etCountryCode.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Validation
        if (ValidationHelper.isEmpty(countryCode)) {
            etCountryCode.setError(getString(R.string.error_empty_field));
            etCountryCode.requestFocus();
            return;
        }

        if (ValidationHelper.isEmpty(phone)) {
            etPhone.setError(getString(R.string.error_empty_field));
            etPhone.requestFocus();
            return;
        }

        if (!ValidationHelper.isValidPhone(phone)) {
            etPhone.setError(getString(R.string.error_invalid_phone));
            etPhone.requestFocus();
            return;
        }

        String fullPhoneNumber = countryCode + phone;

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnSendCode.setEnabled(false);

        // TODO: Implement Firebase Phone Authentication
        // For now, simulate sending code
        simulateSendCode();
    }

    private void simulateSendCode() {
        // Simulate delay
        new android.os.Handler().postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            btnSendCode.setEnabled(true);

            // Show verification code input
            etVerificationCode.setVisibility(View.VISIBLE);
            btnVerify.setVisibility(View.VISIBLE);

            Toast.makeText(requireContext(),
                    getString(R.string.verification_code_sent), Toast.LENGTH_SHORT).show();
        }, 1500);
    }

    private void verifyCode() {
        String code = etVerificationCode.getText().toString().trim();

        if (ValidationHelper.isEmpty(code)) {
            etVerificationCode.setError(getString(R.string.error_empty_field));
            etVerificationCode.requestFocus();
            return;
        }

        if (code.length() != 6) {
            etVerificationCode.setError("Code must be 6 digits");
            etVerificationCode.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnVerify.setEnabled(false);

        // TODO: Implement actual verification with Firebase
        // For now, show error message
        progressBar.setVisibility(View.GONE);
        btnVerify.setEnabled(true);
        Toast.makeText(requireContext(),
                "Phone authentication not yet implemented. Please use email login.",
                Toast.LENGTH_LONG).show();
    }
}