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
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import com.example.joblinker.R;
import com.example.joblinker.activities.LoginActivity;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.utils.ValidationHelper;

import java.util.concurrent.TimeUnit;

public class LoginPhoneFragment extends Fragment {

    private TextInputEditText etCountryCode, etPhone, etVerificationCode;
    private MaterialButton btnSendCode, btnVerify;
    private ProgressBar progressBar;

    private JobLinkerFirebaseManager firebaseManager;
    private FirebaseAuth firebaseAuth;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_phone, container, false);

        firebaseManager = JobLinkerFirebaseManager.getInstance();
        firebaseAuth    = FirebaseAuth.getInstance();

        initializeViews(view);
        setupClickListeners();

        return view;
    }

    // يبحث عن جميع المكوّنات في تخطيط تسجيل الدخول بالهاتف ويخزّنها
    private void initializeViews(View view) {
        etCountryCode       = view.findViewById(R.id.et_country_code);
        etPhone             = view.findViewById(R.id.et_phone);
        etVerificationCode  = view.findViewById(R.id.et_verification_code);
        btnSendCode         = view.findViewById(R.id.btn_send_code);
        btnVerify           = view.findViewById(R.id.btn_verify);
        progressBar         = view.findViewById(R.id.progress_bar);
    }

    // يربط مستمعي النقر بزرَّي إرسال الرمز والتحقق
    private void setupClickListeners() {
        btnSendCode.setOnClickListener(v -> sendVerificationCode());
        btnVerify.setOnClickListener(v -> verifyCode());
    }

    // يتحقق من صحة رقم الهاتف ويُرسل رمز OTP عبر Firebase للرقم الدولي الكامل
    private void sendVerificationCode() {
        String countryCode = etCountryCode.getText().toString().trim();
        String phone       = etPhone.getText().toString().trim();

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

        // Ensure country code starts with +
        if (!countryCode.startsWith("+")) countryCode = "+" + countryCode;
        String fullPhone = countryCode + phone;

        progressBar.setVisibility(View.VISIBLE);
        btnSendCode.setEnabled(false);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(fullPhone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    // Auto-verified (instant on same device / test numbers)
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        signInWithCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        btnSendCode.setEnabled(true);
                        Toast.makeText(requireContext(),
                            "Verification failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    }

                    // OTP sent — show the code input
                    @Override
                    public void onCodeSent(@NonNull String vId,
                                          @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        if (!isAdded()) return;
                        verificationId = vId;
                        resendToken    = token;

                        progressBar.setVisibility(View.GONE);
                        btnSendCode.setEnabled(true);

                        etVerificationCode.setVisibility(View.VISIBLE);
                        btnVerify.setVisibility(View.VISIBLE);

                        Toast.makeText(requireContext(),
                            getString(R.string.verification_code_sent),
                            Toast.LENGTH_SHORT).show();
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // يتحقق من صحة مدخل الرمز المكوّن من 6 أرقام ويُنشئ PhoneAuthCredential لتسجيل الدخول
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
        if (verificationId == null) {
            Toast.makeText(requireContext(),
                "Please request a code first", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnVerify.setEnabled(false);

        PhoneAuthCredential credential =
            PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    // يُسجّل الدخول ببيانات اعتماد الهاتف المُعطاة وينتقل للشاشة الرئيسية عند النجاح
    private void signInWithCredential(PhoneAuthCredential credential) {
        firebaseManager.signInWithPhoneCredential(credential,
            new JobLinkerFirebaseManager.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);
                    // Navigate to main
                    if (getActivity() instanceof LoginActivity) {
                        ((LoginActivity) getActivity()).onLoginSuccess(user);
                    }
                }

                @Override
                public void onFailure(String error) {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);
                    btnVerify.setEnabled(true);
                    Toast.makeText(requireContext(),
                        "Sign in failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
    }
}
