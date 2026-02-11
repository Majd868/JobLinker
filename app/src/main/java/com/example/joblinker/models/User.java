package com.example.joblinker.models;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {

    private String userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String userRole; // "JobSeeker" or "Employer"
    private String userCountry;
    private String userCity;
    private String userLanguage; // "en", "ar", "he"
    private String userCurrency; // "USD", "EUR", "ILS", etc.
    private String userBio;
    private List<String> userSkills;
    private String userExperience;
    private String companyName;
    private String companyWebsite;
    private String dateOfBirth;
    private String avatarUrl;
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean isOnline;
    private long lastSeen;
    private long createdAt;

    public User() {
        this.userSkills = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.lastSeen = System.currentTimeMillis();
        this.emailVerified = false;
        this.phoneVerified = false;
        this.isOnline = false;
        this.userRole = "JobSeeker";
        this.userLanguage = "en";
        this.userCurrency = "USD";
    }

    public User(String userId, String userName, String userEmail, String userRole) {
        this();
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userRole = userRole;
    }

    // Getters and Setters with PropertyName annotations

    @PropertyName("userId")
    public String getUserId() {
        return userId;
    }

    @PropertyName("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @PropertyName("userName")
    public String getUserName() {
        return userName;
    }

    @PropertyName("userName")
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @PropertyName("userEmail")
    public String getUserEmail() {
        return userEmail;
    }

    @PropertyName("userEmail")
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    @PropertyName("userPhone")
    public String getUserPhone() {
        return userPhone;
    }

    @PropertyName("userPhone")
    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    @PropertyName("userRole")
    public String getUserRole() {
        return userRole;
    }

    @PropertyName("userRole")
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    @PropertyName("userCountry")
    public String getUserCountry() {
        return userCountry;
    }

    @PropertyName("userCountry")
    public void setUserCountry(String userCountry) {
        this.userCountry = userCountry;
    }

    @PropertyName("userCity")
    public String getUserCity() {
        return userCity;
    }

    @PropertyName("userCity")
    public void setUserCity(String userCity) {
        this.userCity = userCity;
    }

    @PropertyName("userLanguage")
    public String getUserLanguage() {
        return userLanguage;
    }

    @PropertyName("userLanguage")
    public void setUserLanguage(String userLanguage) {
        this.userLanguage = userLanguage;
    }

    @PropertyName("userCurrency")
    public String getUserCurrency() {
        return userCurrency;
    }

    @PropertyName("userCurrency")
    public void setUserCurrency(String userCurrency) {
        this.userCurrency = userCurrency;
    }

    @PropertyName("userBio")
    public String getUserBio() {
        return userBio;
    }

    @PropertyName("userBio")
    public void setUserBio(String userBio) {
        this.userBio = userBio;
    }

    @PropertyName("userSkills")
    public List<String> getUserSkills() {
        return userSkills;
    }

    @PropertyName("userSkills")
    public void setUserSkills(List<String> userSkills) {
        this.userSkills = userSkills;
    }

    @PropertyName("userExperience")
    public String getUserExperience() {
        return userExperience;
    }

    @PropertyName("userExperience")
    public void setUserExperience(String userExperience) {
        this.userExperience = userExperience;
    }

    @PropertyName("companyName")
    public String getCompanyName() {
        return companyName;
    }

    @PropertyName("companyName")
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @PropertyName("companyWebsite")
    public String getCompanyWebsite() {
        return companyWebsite;
    }

    @PropertyName("companyWebsite")
    public void setCompanyWebsite(String companyWebsite) {
        this.companyWebsite = companyWebsite;
    }

    @PropertyName("dateOfBirth")
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    @PropertyName("dateOfBirth")
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @PropertyName("avatarUrl")
    public String getAvatarUrl() {
        return avatarUrl;
    }

    @PropertyName("avatarUrl")
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @PropertyName("emailVerified")
    public boolean isEmailVerified() {
        return emailVerified;
    }

    @PropertyName("emailVerified")
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    @PropertyName("phoneVerified")
    public boolean isPhoneVerified() {
        return phoneVerified;
    }

    @PropertyName("phoneVerified")
    public void setPhoneVerified(boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    @PropertyName("isOnline")
    public boolean isOnline() {
        return isOnline;
    }

    @PropertyName("isOnline")
    public void setOnline(boolean online) {
        isOnline = online;
    }

    @PropertyName("lastSeen")
    public long getLastSeen() {
        return lastSeen;
    }

    @PropertyName("lastSeen")
    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    @PropertyName("createdAt")
    public long getCreatedAt() {
        return createdAt;
    }

    @PropertyName("createdAt")
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // Helper methods
    public boolean isEmployer() {
        return "Employer".equals(userRole);
    }

    public boolean isJobSeeker() {
        return "JobSeeker".equals(userRole);
    }

    public void addSkill(String skill) {
        if (userSkills == null) {
            userSkills = new ArrayList<>();
        }
        if (!userSkills.contains(skill)) {
            userSkills.add(skill);
        }
    }

    public void removeSkill(String skill) {
        if (userSkills != null) {
            userSkills.remove(skill);
        }
    }

    public String getLocation() {
        if (userCity != null && userCountry != null) {
            return userCity + ", " + userCountry;
        } else if (userCountry != null) {
            return userCountry;
        } else if (userCity != null) {
            return userCity;
        }
        return "";
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userRole='" + userRole + '\'' +
                '}';
    }
}