package com.example.joblinker.models;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.List;

public class Conversation implements Serializable {

    private String conversationId;
    private List<String> participants;
    private String lastMessage;
    private long lastMessageTime;
    private String lastMessageSenderId;
    private int unreadCount;
    private String otherUserName;
    private String otherUserAvatarUrl;
    private boolean otherUserOnline;

    // مُنشئ افتراضي يُهيئ وقت آخر رسالة وعدد الرسائل غير المقروءة وحالة الاتصال
    public Conversation() {
        this.lastMessageTime = System.currentTimeMillis();
        this.unreadCount = 0;
        this.otherUserOnline = false;
    }

    // تُرجع المعرف الفريد لهذه المحادثة
    @PropertyName("conversationId")
    public String getConversationId() {
        return conversationId;
    }

    // تُعيّن المعرف الفريد لهذه المحادثة
    @PropertyName("conversationId")
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    // تُرجع قائمة معرّفات المستخدمين المشاركين في هذه المحادثة
    @PropertyName("participants")
    public List<String> getParticipants() {
        return participants;
    }

    // تُعيّن قائمة معرّفات المستخدمين المشاركين في هذه المحادثة
    @PropertyName("participants")
    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    // تُرجع نص آخر رسالة في هذه المحادثة
    @PropertyName("lastMessage")
    public String getLastMessage() {
        return lastMessage;
    }

    // تُعيّن نص آخر رسالة في هذه المحادثة
    @PropertyName("lastMessage")
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    // تُرجع الطابع الزمني لآخر رسالة
    @PropertyName("lastMessageTime")
    public long getLastMessageTime() {
        return lastMessageTime;
    }

    // تُعيّن الطابع الزمني لآخر رسالة
    @PropertyName("lastMessageTime")
    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    // تُرجع معرف المستخدم الذي أرسل آخر رسالة
    @PropertyName("lastMessageSenderId")
    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    // تُعيّن معرف المستخدم الذي أرسل آخر رسالة
    @PropertyName("lastMessageSenderId")
    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    // تُرجع عدد الرسائل غير المقروءة في هذه المحادثة
    @PropertyName("unreadCount")
    public int getUnreadCount() {
        return unreadCount;
    }

    // تُعيّن عدد الرسائل غير المقروءة في هذه المحادثة
    @PropertyName("unreadCount")
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    // تُرجع الاسم المعروض للمشارك الآخر
    @PropertyName("otherUserName")
    public String getOtherUserName() {
        return otherUserName;
    }

    // تُعيّن الاسم المعروض للمشارك الآخر
    @PropertyName("otherUserName")
    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    // تُرجع رابط صورة الملف الشخصي للمشارك الآخر
    @PropertyName("otherUserAvatarUrl")
    public String getOtherUserAvatarUrl() {
        return otherUserAvatarUrl;
    }

    // تُعيّن رابط صورة الملف الشخصي للمشارك الآخر
    @PropertyName("otherUserAvatarUrl")
    public void setOtherUserAvatarUrl(String otherUserAvatarUrl) {
        this.otherUserAvatarUrl = otherUserAvatarUrl;
    }

    // تُرجع true إذا كان المشارك الآخر متصلاً حالياً
    @PropertyName("otherUserOnline")
    public boolean isOtherUserOnline() {
        return otherUserOnline;
    }

    // تُعيّن حالة اتصال المشارك الآخر
    @PropertyName("otherUserOnline")
    public void setOtherUserOnline(boolean otherUserOnline) {
        this.otherUserOnline = otherUserOnline;
    }

    // تُرجع true إذا كانت هناك رسائل غير مقروءة في هذه المحادثة
    public boolean hasUnreadMessages() {
        return unreadCount > 0;
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "conversationId='" + conversationId + '\'' +
                ", lastMessage='" + lastMessage + '\'' +
                ", unreadCount=" + unreadCount +
                '}';
    }
}