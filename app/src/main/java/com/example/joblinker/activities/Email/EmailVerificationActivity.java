package com.example.joblinker.activities.Email;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.example.joblinker.R;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;

public class EmailVerificationActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvVerificationMessage;
    private TextInputEditText etVerificationCode;
    private TextView tvResendCode;
    private TextView tvResendTimer;
    private MaterialButton btnVerify;
    private MaterialButton btnSkipVerification;

    private JobLinkerFirebaseManager firebaseManager;
    private CountDownTimer resendTimer;
    private boolean canResend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        firebaseManager = JobLinkerFirebaseManager.getInstance();

        initializeViews();
        setupToolbar();
        setupClickListeners();
        startResendTimer();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvVerificationMessage = findViewById(R.id.tv_verification_message);
        etVerificationCode = findViewById(R.id.et_verification_code);
        tvResendCode = findViewById(R.id.tv_resend_code);
        tvResendTimer = findViewById(R.id.tv_resend_timer);
        btnVerify = findViewById(R.id.btn_verify);
        btnSkipVerification = findViewById(R.id.btn_skip_verification);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupClickListeners() {
        btnVerify.setOnClickListener(v -> verifyCode());

        tvResendCode.setOnClickListener(v -> {
            if (canResend) {
                resendCode();
            }
        });

        btnSkipVerification.setOnClickListener(v -> finish());
    }

    private void verifyCode() {
        String code = etVerificationCode.getText().toString().trim();

        if (code.isEmpty() || code.length() != 6) {
            etVerificationCode.setError("Enter 6-digit code");
            return;
        }

        // TODO: Implement verification logic
        Toast.makeText(this, "Verification successful!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void resendCode() {
        firebaseManager.sendEmailVerification(new JobLinkerFirebaseManager.VoidCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(EmailVerificationActivity.this,
                        "Verification code sent", Toast.LENGTH_SHORT).show();
                startResendTimer();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(EmailVerificationActivity.this,
                        "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startResendTimer() {
        canResend = false;
        tvResendCode.setEnabled(false);
        tvResendCode.setAlpha(0.5f);
        tvResendTimer.setVisibility(View.VISIBLE);

        resendTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                tvResendTimer.setText(String.format("Resend code in %ds", seconds));
            }

            @Override
            public void onFinish() {
                canResend = true;
                tvResendCode.setEnabled(true);
                tvResendCode.setAlpha(1.0f);
                tvResendTimer.setVisibility(View.GONE);
            }
        };
        resendTimer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resendTimer != null) {
            resendTimer.cancel();
        }
    }
}