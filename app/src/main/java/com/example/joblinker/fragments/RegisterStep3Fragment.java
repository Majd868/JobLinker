package com.example.joblinker.fragments;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import com.example.joblinker.R;
import com.example.joblinker.activities.RegisterActivity;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.models.User;
import com.example.joblinker.utils.SharedPreferencesManager;
import com.example.joblinker.utils.ValidationHelper;

public class RegisterStep3Fragment extends Fragment {

    private TextView tvSmsVerificationMessage;
    private TextView tvPhoneDisplay;
    private MaterialButton btnSendSmsCode;
    private TextInputLayout tilSmsVerificationCode;
    private TextInputEditText etSmsVerificationCode;
    private LinearLayout layoutResendSms;
    private TextView tvResendSmsCode;
    private TextView tvSmsResendTimer;
    private MaterialButton btnVerifySms;
    private MaterialButton btnSkipSmsVerification;
    private MaterialButton btnCompleteRegistration;

    private JobLinkerFirebaseManager firebaseManager;
    private SharedPreferencesManager prefsManager;
    private CountDownTimer smsResendTimer;
    private boolean canResendSms = false;
    private boolean smsVerified = false;

    private static final int RESEND_TIMEOUT = 60; // seconds
    private String verificationId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_step3, container, false);

        firebaseManager = JobLinkerFirebaseManager.getInstance();
        prefsManager = SharedPreferencesManager.getInstance(requireContext());

        initializeViews(view);
        setupClickListeners();
        displayPhoneInfo();

        return view;
    }

    private void initializeViews(View view) {
        tvSmsVerificationMessage = view.findViewById(R.id.tv_sms_verification_message);
        tvPhoneDisplay = view.findViewById(R.id.tv_phone_display);
        btnSendSmsCode = view.findViewById(R.id.btn_send_sms_code);
        tilSmsVerificationCode = view.findViewById(R.id.til_sms_verification_code);
        etSmsVerificationCode = view.findViewById(R.id.et_sms_verification_code);
        layoutResendSms = view.findViewById(R.id.layout_resend_sms);
        tvResendSmsCode = view.findViewById(R.id.tv_resend_sms_code);
        tvSmsResendTimer = view.findViewById(R.id.tv_sms_resend_timer);
        btnVerifySms = view.findViewById(R.id.btn_verify_sms);
        btnSkipSmsVerification = view.findViewById(R.id.btn_skip_sms_verification);
        btnCompleteRegistration = view.findViewById(R.id.btn_complete_registration);
    }

    private void setupClickListeners() {
        btnSendSmsCode.setOnClickListener(v -> sendSmsCode());

        btnVerifySms.setOnClickListener(v -> verifySmsCode());

        tvResendSmsCode.setOnClickListener(v -> {
            if (canResendSms) {
                sendSmsCode();
            } else {
                Toast.makeText(requireContext(),
                        "Please wait before requesting a new code",
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnSkipSmsVerification.setOnClickListener(v -> completeRegistration());

        btnCompleteRegistration.setOnClickListener(v -> completeRegistration());
    }

    private void displayPhoneInfo() {
        if (getActivity() instanceof RegisterActivity) {
            RegisterActivity activity = (RegisterActivity) getActivity();
            User user = activity.getRegistrationUser();

            if (user != null && user.getUserPhone() != null) {
                tvPhoneDisplay.setText(user.getUserPhone());
            }
        }
    }

    private void sendSmsCode() {
        btnSendSmsCode.setEnabled(false);
        btnSendSmsCode.setText("Sending...");

        // TODO: Implement Firebase Phone Authentication
        // For demonstration, we'll simulate sending SMS
        simulateSendSms();
    }

    private void simulateSendSms() {
        // Simulate delay
        new android.os.Handler().postDelayed(() -> {
            // Show verification input
            btnSendSmsCode.setVisibility(View.GONE);
            tilSmsVerificationCode.setVisibility(View.VISIBLE);
            layoutResendSms.setVisibility(View.VISIBLE);
            btnVerifySms.setVisibility(View.VISIBLE);
            btnCompleteRegistration.setVisibility(View.VISIBLE);

            Toast.makeText(requireContext(),
                    "Verification code sent via SMS",
                    Toast.LENGTH_SHORT).show();

            startSmsResendTimer();
        }, 1500);
    }

    private void verifySmsCode() {
        String code = etSmsVerificationCode.getText().toString().trim();

        if (ValidationHelper.isEmpty(code)) {
            tilSmsVerificationCode.setError("Enter verification code");
            return;
        }

        if (code.length() != 6) {
            tilSmsVerificationCode.setError("Code must be 6 digits");
            return;
        }

        tilSmsVerificationCode.setError(null);

        // TODO: Verify with Firebase Phone Auth
        // For now, just mark as verified
        btnVerifySms.setEnabled(false);
        btnVerifySms.setText("Verifying...");

        // Simulate verification
        new android.os.Handler().postDelayed(() -> {
            smsVerified = true;
            Toast.makeText(requireContext(),
                    "Phone number verified successfully!",
                    Toast.LENGTH_SHORT).show();

            // Auto-complete registration
            completeRegistration();
        }, 1000);
    }

    private void startSmsResendTimer() {
        canResendSms = false;
        tvResendSmsCode.setEnabled(false);
        tvResendSmsCode.setAlpha(0.5f);
        tvSmsResendTimer.setVisibility(View.VISIBLE);

        smsResendTimer = new CountDownTimer(RESEND_TIMEOUT * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                tvSmsResendTimer.setText(
                        String.format("Resend code in %ds", secondsRemaining)
                );
            }

            @Override
            public void onFinish() {
                canResendSms = true;
                tvResendSmsCode.setEnabled(true);
                tvResendSmsCode.setAlpha(1.0f);
                tvSmsResendTimer.setVisibility(View.GONE);
            }
        };
        smsResendTimer.start();
    }

    public void validateAndCompleteRegistration() {
        completeRegistration();
    }

    private void completeRegistration() {
        if (getActivity() instanceof RegisterActivity) {
            RegisterActivity activity = (RegisterActivity) getActivity();
            User user = activity.getRegistrationUser();
            String password = activity.getRegistrationPassword();

            if (user == null || password == null) {
                Toast.makeText(requireContext(),
                        "Registration data missing",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable buttons
            btnCompleteRegistration.setEnabled(false);
            btnCompleteRegistration.setText("Creating account...");

            // Register user with Firebase
            firebaseManager.registerWithEmail(user.getUserEmail(), password,
                    new JobLinkerFirebaseManager.AuthCallback() {
                        @Override
                        public void onSuccess(FirebaseUser firebaseUser) {
                            // Set user ID
                            user.setUserId(firebaseUser.getUid());
                            user.setEmailVerified(firebaseManager.isEmailVerified());
                            user.setPhoneVerified(smsVerified);

                            // Create user in Firestore
                            firebaseManager.createUser(user, new JobLinkerFirebaseManager.VoidCallback() {
                                @Override
                                public void onSuccess() {
                                    // Save to preferences
                                    prefsManager.setLoggedIn(true);
                                    prefsManager.setUserId(user.getUserId());
                                    prefsManager.setUserName(user.getUserName());
                                    prefsManager.setUserEmail(user.getUserEmail());
                                    prefsManager.setUserRole(user.getUserRole());

                                    Toast.makeText(requireContext(),
                                            "Registration successful!",
                                            Toast.LENGTH_SHORT).show();

                                    // Navigate to main activity
                                    activity.onRegistrationComplete();
                                }

                                @Override
                                public void onFailure(String error) {
                                    btnCompleteRegistration.setEnabled(true);
                                    btnCompleteRegistration.setText(R.string.complete_registration);
                                    Toast.makeText(requireContext(),
                                            "Error creating profile: " + error,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            btnCompleteRegistration.setEnabled(true);
                            btnCompleteRegistration.setText(R.string.complete_registration);
                            Toast.makeText(requireContext(),
                                    "Registration failed: " + error,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (smsResendTimer != null) {
            smsResendTimer.cancel();
        }
    }
}