package com.joblinker.models;

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

    public Conversation() {
        this.lastMessageTime = System.currentTimeMillis();
        this.unreadCount = 0;
        this.otherUserOnline = false;
    }

    @PropertyName("conversationId")
    public String getConversationId() {
        return conversationId;
    }

    @PropertyName("conversationId")
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    @PropertyName("participants")
    public List<String> getParticipants() {
        return participants;
    }

    @PropertyName("participants")
    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    @PropertyName("lastMessage")
    public String getLastMessage() {
        return lastMessage;
    }

    @PropertyName("lastMessage")
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    @PropertyName("lastMessageTime")
    public long getLastMessageTime() {
        return lastMessageTime;
    }

    @PropertyName("lastMessageTime")
    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    @PropertyName("lastMessageSenderId")
    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    @PropertyName("lastMessageSenderId")
    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    @PropertyName("unreadCount")
    public int getUnreadCount() {
        return unreadCount;
    }

    @PropertyName("unreadCount")
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    @PropertyName("otherUserName")
    public String getOtherUserName() {
        return otherUserName;
    }

    @PropertyName("otherUserName")
    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    @PropertyName("otherUserAvatarUrl")
    public String getOtherUserAvatarUrl() {
        return otherUserAvatarUrl;
    }

    @PropertyName("otherUserAvatarUrl")
    public void setOtherUserAvatarUrl(String otherUserAvatarUrl) {
        this.otherUserAvatarUrl = otherUserAvatarUrl;
    }

    @PropertyName("otherUserOnline")
    public boolean isOtherUserOnline() {
        return otherUserOnline;
    }

    @PropertyName("otherUserOnline")
    public void setOtherUserOnline(boolean otherUserOnline) {
        this.otherUserOnline = otherUserOnline;
    }

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