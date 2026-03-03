package com.example.joblinker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import com.example.joblinker.R;
import com.example.joblinker.activities.RegisterActivity;
import com.example.joblinker.models.User;
import com.example.joblinker.utils.ValidationHelper;

public class RegisterStep1Fragment extends Fragment {

    private TextInputEditText etFullName, etEmail, etPhone, etPassword;
    private MaterialButtonToggleGroup toggleRole;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_step1, container, false);

        initializeViews(view);

        return view;
    }

    private void initializeViews(View view) {
        etFullName = view.findViewById(R.id.et_full_name);
        etEmail = view.findViewById(R.id.et_email);
        etPhone = view.findViewById(R.id.et_phone);
        etPassword = view.findViewById(R.id.et_password);
        toggleRole = view.findViewById(R.id.toggle_role);
    }

    public boolean validateAndSaveData() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (ValidationHelper.isEmpty(fullName)) {
            etFullName.setError(getString(R.string.error_empty_field));
            etFullName.requestFocus();
            return false;
        }

        if (!ValidationHelper.isValidName(fullName)) {
            etFullName.setError("Please enter a valid name");
            etFullName.requestFocus();
            return false;
        }

        if (ValidationHelper.isEmpty(email)) {
            etEmail.setError(getString(R.string.error_empty_field));
            etEmail.requestFocus();
            return false;
        }

        if (!ValidationHelper.isValidEmail(email)) {
            etEmail.setError(getString(R.string.error_invalid_email));
            etEmail.requestFocus();
            return false;
        }

        if (ValidationHelper.isEmpty(phone)) {
            etPhone.setError(getString(R.string.error_empty_field));
            etPhone.requestFocus();
            return false;
        }

        if (!ValidationHelper.isValidPhone(phone)) {
            etPhone.setError(getString(R.string.error_invalid_phone));
            etPhone.requestFocus();
            return false;
        }

        if (ValidationHelper.isEmpty(password)) {
            etPassword.setError(getString(R.string.error_empty_field));
            etPassword.requestFocus();
            return false;
        }

        if (!ValidationHelper.isValidPassword(password)) {
            etPassword.setError(getString(R.string.error_short_password));
            etPassword.requestFocus();
            return false;
        }

        // Determine role
        String role = "JobSeeker";
        if (toggleRole.getCheckedButtonId() == R.id.btn_employer) {
            role = "Employer";
        }

        // Save to RegisterActivity
        if (getActivity() instanceof RegisterActivity) {
            RegisterActivity activity = (RegisterActivity) getActivity();

            // 1. Get the existing User object and update it
            User user = activity.getRegistrationUser();
            if (user == null) {
                user = new User(); // Fallback just in case it's null
            }

            user.setUserName(fullName);
            user.setUserEmail(email);
            user.setUserPhone(phone);
            user.setUserRole(role);

            // 2. Save the User object back to Activity
            activity.setRegistrationUser(user);

            // 3. THIS WAS MISSING: Save the password to the Activity!
            activity.setRegistrationPassword(password);
        }

        return true; // Proceed to next step
    }
}