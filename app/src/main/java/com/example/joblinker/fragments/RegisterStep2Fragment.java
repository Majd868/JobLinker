package com.example.joblinker.fragments;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.example.joblinker.R;
import com.example.joblinker.activities.RegisterActivity;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.models.User;
import com.example.joblinker.utils.ValidationHelper;

public class RegisterStep2Fragment extends Fragment {

    // View references with correct IDs
    private TextView tvVerificationMessage;
    private TextView tvEmailDisplay;
    private TextInputLayout tilVerificationCode;
    private TextInputEditText etVerificationCode;
    private TextView tvResendCode;
    private TextView tvResendTimer;
    private MaterialButton btnVerify;
    private MaterialButton btnSkipVerification;

    private JobLinkerFirebaseManager firebaseManager;
    private CountDownTimer resendTimer;
    private boolean canResend = false;

    private static final int RESEND_TIMEOUT = 60; // seconds
    private boolean emailVerified = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register_step2, container, false);

        firebaseManager = JobLinkerFirebaseManager.getInstance();

        initializeViews(view);
        setupClickListeners();
        displayEmailInfo();
        sendEmailVerification();

        return view;
    }

    private void initializeViews(View view) {
        // Map to correct XML IDs
        tvVerificationMessage = view.findViewById(R.id.tv_verification_message);
        tvEmailDisplay = view.findViewById(R.id.tv_email_display);
        tilVerificationCode = view.findViewById(R.id.til_verification_code);
        etVerificationCode = view.findViewById(R.id.et_verification_code);
        tvResendCode = view.findViewById(R.id.tv_resend_code);
        tvResendTimer = view.findViewById(R.id.tv_resend_timer);
        btnVerify = view.findViewById(R.id.btn_verify);
        btnSkipVerification = view.findViewById(R.id.btn_skip_verification);
    }

    private void setupClickListeners() {
        btnVerify.setOnClickListener(v -> verifyEmailCode());

        tvResendCode.setOnClickListener(v -> {
            if (canResend) {
                sendEmailVerification();
            } else {
                Toast.makeText(requireContext(),
                        "Please wait before requesting a new code",
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnSkipVerification.setOnClickListener(v -> skipEmailVerification());
    }

    private void displayEmailInfo() {
        if (getActivity() instanceof RegisterActivity) {
            RegisterActivity activity = (RegisterActivity) getActivity();
            User user = activity.getRegistrationUser();

            if (user != null && user.getUserEmail() != null) {
                tvEmailDisplay.setText(user.getUserEmail());
                String message = String.format(
                        "We've sent a 6-digit verification code to %s. Please enter it below.",
                        user.getUserEmail()
                );
                tvVerificationMessage.setText(message);
            }
        }
    }

    private void sendEmailVerification() {
        firebaseManager.sendEmailVerification(new JobLinkerFirebaseManager.VoidCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(),
                        "Verification code sent to your email",
                        Toast.LENGTH_SHORT).show();
                startResendTimer();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(requireContext(),
                        "Error sending code: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyEmailCode() {
        String code = etVerificationCode.getText().toString().trim();

        if (ValidationHelper.isEmpty(code)) {
            tilVerificationCode.setError("Enter verification code");
            etVerificationCode.requestFocus();
            return;
        }

        if (code.length() != 6) {
            tilVerificationCode.setError("Code must be 6 digits");
            etVerificationCode.requestFocus();
            return;
        }

        tilVerificationCode.setError(null);

        // Disable button during verification
        btnVerify.setEnabled(false);
        btnVerify.setText("Verifying...");

        // Reload user to check Firebase email verification
        firebaseManager.reloadUser(new JobLinkerFirebaseManager.VoidCallback() {
            @Override
            public void onSuccess() {
                if (firebaseManager.isEmailVerified()) {
                    emailVerified = true;
                    Toast.makeText(requireContext(),
                            "Email verified successfully!",
                            Toast.LENGTH_SHORT).show();
                    moveToNextStep();
                } else {
                    btnVerify.setEnabled(true);
                    btnVerify.setText(R.string.verify_email);

                    // Show helpful message
                    Toast.makeText(requireContext(),
                            "Please check your email and click the verification link first",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(String error) {
                btnVerify.setEnabled(true);
                btnVerify.setText(R.string.verify_email);
                Toast.makeText(requireContext(),
                        "Verification failed: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void skipEmailVerification() {
        Toast.makeText(requireContext(),
                "You can verify your email later in settings",
                Toast.LENGTH_SHORT).show();
        moveToNextStep();
    }

    private void moveToNextStep() {
        if (getActivity() instanceof RegisterActivity) {
            ((RegisterActivity) getActivity()).moveToNextStep();
        }
    }

    private void startResendTimer() {
        canResend = false;
        tvResendCode.setEnabled(false);
        tvResendCode.setAlpha(0.5f);
        tvResendTimer.setVisibility(View.VISIBLE);

        if (resendTimer != null) {
            resendTimer.cancel();
        }

        resendTimer = new CountDownTimer(RESEND_TIMEOUT * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                if (tvResendTimer != null) {
                    tvResendTimer.setText(
                            String.format("Resend code in %ds", secondsRemaining)
                    );
                }
            }

            @Override
            public void onFinish() {
                canResend = true;
                if (tvResendCode != null) {
                    tvResendCode.setEnabled(true);
                    tvResendCode.setAlpha(1.0f);
                }
                if (tvResendTimer != null) {
                    tvResendTimer.setVisibility(View.GONE);
                }
            }
        };
        resendTimer.start();
    }

    public boolean validateAndSaveData() {
        // Email verification is optional - user can skip
        // Just return true to allow progression
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (resendTimer != null) {
            resendTimer.cancel();
            resendTimer = null;
        }
    }
}