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

    // مُنشئ افتراضي يُهيئ قيم المستخدم الابتدائية
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

    // مُنشئ بالحقول الأساسية: المعرف، الاسم، البريد الإلكتروني، والدور
    public User(String userId, String userName, String userEmail, String userRole) {
        this();
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userRole = userRole;
    }

    // الدوال الجالبة والمحددة مع تعليقات PropertyName

    // تُرجع المعرف الفريد للمستخدم
    @PropertyName("userId")
    public String getUserId() {
        return userId;
    }

    // تُعيّن المعرف الفريد للمستخدم
    @PropertyName("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    // تُرجع الاسم المعروض للمستخدم
    @PropertyName("userName")
    public String getUserName() {
        return userName;
    }

    // تُعيّن الاسم المعروض للمستخدم
    @PropertyName("userName")
    public void setUserName(String userName) {
        this.userName = userName;
    }

    // تُرجع عنوان البريد الإلكتروني للمستخدم
    @PropertyName("userEmail")
    public String getUserEmail() {
        return userEmail;
    }

    // تُعيّن عنوان البريد الإلكتروني للمستخدم
    @PropertyName("userEmail")
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    // تُرجع رقم هاتف المستخدم
    @PropertyName("userPhone")
    public String getUserPhone() {
        return userPhone;
    }

    // تُعيّن رقم هاتف المستخدم
    @PropertyName("userPhone")
    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    // تُرجع دور المستخدم (باحث عن عمل أو صاحب عمل)
    @PropertyName("userRole")
    public String getUserRole() {
        return userRole;
    }

    // تُعيّن دور المستخدم (باحث عن عمل أو صاحب عمل)
    @PropertyName("userRole")
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    // تُرجع بلد المستخدم
    @PropertyName("userCountry")
    public String getUserCountry() {
        return userCountry;
    }

    // تُعيّن بلد المستخدم
    @PropertyName("userCountry")
    public void setUserCountry(String userCountry) {
        this.userCountry = userCountry;
    }

    // تُرجع مدينة المستخدم
    @PropertyName("userCity")
    public String getUserCity() {
        return userCity;
    }

    // تُعيّن مدينة المستخدم
    @PropertyName("userCity")
    public void setUserCity(String userCity) {
        this.userCity = userCity;
    }

    // تُرجع رمز اللغة المفضلة للمستخدم (مثلاً: "en"، "ar")
    @PropertyName("userLanguage")
    public String getUserLanguage() {
        return userLanguage;
    }

    // تُعيّن رمز اللغة المفضلة للمستخدم
    @PropertyName("userLanguage")
    public void setUserLanguage(String userLanguage) {
        this.userLanguage = userLanguage;
    }

    // تُرجع رمز العملة المفضلة للمستخدم (مثلاً: "USD")
    @PropertyName("userCurrency")
    public String getUserCurrency() {
        return userCurrency;
    }

    // تُعيّن رمز العملة المفضلة للمستخدم
    @PropertyName("userCurrency")
    public void setUserCurrency(String userCurrency) {
        this.userCurrency = userCurrency;
    }

    // تُرجع نص السيرة الذاتية للمستخدم
    @PropertyName("userBio")
    public String getUserBio() {
        return userBio;
    }

    // تُعيّن نص السيرة الذاتية للمستخدم
    @PropertyName("userBio")
    public void setUserBio(String userBio) {
        this.userBio = userBio;
    }

    // تُرجع قائمة المهارات المرتبطة بالمستخدم
    @PropertyName("userSkills")
    public List<String> getUserSkills() {
        return userSkills;
    }

    // تُعيّن قائمة المهارات المرتبطة بالمستخدم
    @PropertyName("userSkills")
    public void setUserSkills(List<String> userSkills) {
        this.userSkills = userSkills;
    }

    // تُرجع وصف خبرة العمل للمستخدم
    @PropertyName("userExperience")
    public String getUserExperience() {
        return userExperience;
    }

    // تُعيّن وصف خبرة العمل للمستخدم
    @PropertyName("userExperience")
    public void setUserExperience(String userExperience) {
        this.userExperience = userExperience;
    }

    // تُرجع اسم شركة صاحب العمل
    @PropertyName("companyName")
    public String getCompanyName() {
        return companyName;
    }

    // تُعيّن اسم شركة صاحب العمل
    @PropertyName("companyName")
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    // تُرجع رابط موقع شركة صاحب العمل
    @PropertyName("companyWebsite")
    public String getCompanyWebsite() {
        return companyWebsite;
    }

    // تُعيّن رابط موقع شركة صاحب العمل
    @PropertyName("companyWebsite")
    public void setCompanyWebsite(String companyWebsite) {
        this.companyWebsite = companyWebsite;
    }

    // تُرجع تاريخ ميلاد المستخدم كنص
    @PropertyName("dateOfBirth")
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    // تُعيّن تاريخ ميلاد المستخدم كنص
    @PropertyName("dateOfBirth")
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    // تُرجع رابط صورة الملف الشخصي للمستخدم
    @PropertyName("avatarUrl")
    public String getAvatarUrl() {
        return avatarUrl;
    }

    // تُعيّن رابط صورة الملف الشخصي للمستخدم
    @PropertyName("avatarUrl")
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    // تُرجع true إذا تم التحقق من عنوان البريد الإلكتروني للمستخدم
    @PropertyName("emailVerified")
    public boolean isEmailVerified() {
        return emailVerified;
    }

    // تُعيّن حالة التحقق من البريد الإلكتروني
    @PropertyName("emailVerified")
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    // تُرجع true إذا تم التحقق من رقم هاتف المستخدم
    @PropertyName("phoneVerified")
    public boolean isPhoneVerified() {
        return phoneVerified;
    }

    // تُعيّن حالة التحقق من رقم الهاتف
    @PropertyName("phoneVerified")
    public void setPhoneVerified(boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    // تُرجع true إذا كان المستخدم متصلاً حالياً
    @PropertyName("isOnline")
    public boolean isOnline() {
        return isOnline;
    }

    // تُعيّن حالة الاتصال للمستخدم
    @PropertyName("isOnline")
    public void setOnline(boolean online) {
        isOnline = online;
    }

    // تُرجع الطابع الزمني لآخر ظهور للمستخدم
    @PropertyName("lastSeen")
    public long getLastSeen() {
        return lastSeen;
    }

    // تُعيّن الطابع الزمني لآخر ظهور للمستخدم
    @PropertyName("lastSeen")
    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    // تُرجع الطابع الزمني لوقت إنشاء حساب المستخدم
    @PropertyName("createdAt")
    public long getCreatedAt() {
        return createdAt;
    }

    // تُعيّن الطابع الزمني لوقت إنشاء حساب المستخدم
    @PropertyName("createdAt")
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // دوال مساعدة
    // تُرجع true إذا كان دور المستخدم صاحب عمل
    public boolean isEmployer() {
        return "Employer".equals(userRole);
    }

    // تُرجع true إذا كان دور المستخدم باحثاً عن عمل
    public boolean isJobSeeker() {
        return "JobSeeker".equals(userRole);
    }

    // تُضيف مهارة إلى قائمة مهارات المستخدم إذا لم تكن موجودة مسبقاً
    public void addSkill(String skill) {
        if (userSkills == null) {
            userSkills = new ArrayList<>();
        }
        if (!userSkills.contains(skill)) {
            userSkills.add(skill);
        }
    }

    // تُزيل مهارة من قائمة مهارات المستخدم
    public void removeSkill(String skill) {
        if (userSkills != null) {
            userSkills.remove(skill);
        }
    }

    // تُرجع نص موقع منسق يجمع المدينة والبلد
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