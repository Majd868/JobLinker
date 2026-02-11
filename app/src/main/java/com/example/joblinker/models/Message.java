package com.joblinker.models;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

public class Message implements Serializable {

    private String messageId;
    private String conversationId;
    private String messageSenderId;
    private String messageReceiverId;
    private String messageText;
    private String messageType; // "text", "image", "voice"
    private long messageTimestamp;
    private boolean messageRead;
    private String senderName;
    private String senderAvatarUrl;
    private String imageUrl;
    private String voiceUrl;

    public Message() {
        this.messageTimestamp = System.currentTimeMillis();
        this.messageRead = false;
        this.messageType = "text";
    }

    public Message(String conversationId, String senderId, String receiverId, String text) {
        this();
        this.conversationId = conversationId;
        this.messageSenderId = senderId;
        this.messageReceiverId = receiverId;
        this.messageText = text;
    }

    // Getters and Setters with PropertyName annotations

    @PropertyName("messageId")
    public String getMessageId() {
        return messageId;
    }

    @PropertyName("messageId")
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @PropertyName("conversationId")
    public String getConversationId() {
        return conversationId;
    }

    @PropertyName("conversationId")
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    @PropertyName("messageSenderId")
    public String getMessageSenderId() {
        return messageSenderId;
    }

    @PropertyName("messageSenderId")
    public void setMessageSenderId(String messageSenderId) {
        this.messageSenderId = messageSenderId;
    }

    @PropertyName("messageReceiverId")
    public String getMessageReceiverId() {
        return messageReceiverId;
    }

    @PropertyName("messageReceiverId")
    public void setMessageReceiverId(String messageReceiverId) {
        this.messageReceiverId = messageReceiverId;
    }

    @PropertyName("messageText")
    public String getMessageText() {
        return messageText;
    }

    @PropertyName("messageText")
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    @PropertyName("messageType")
    public String getMessageType() {
        return messageType;
    }

    @PropertyName("messageType")
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    @PropertyName("messageTimestamp")
    public long getMessageTimestamp() {
        return messageTimestamp;
    }

    @PropertyName("messageTimestamp")
    public void setMessageTimestamp(long messageTimestamp) {
        this.messageTimestamp = messageTimestamp;
    }

    @PropertyName("messageRead")
    public boolean isMessageRead() {
        return messageRead;
    }

    @PropertyName("messageRead")
    public void setMessageRead(boolean messageRead) {
        this.messageRead = messageRead;
    }

    @PropertyName("senderName")
    public String getSenderName() {
        return senderName;
    }

    @PropertyName("senderName")
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    @PropertyName("senderAvatarUrl")
    public String getSenderAvatarUrl() {
        return senderAvatarUrl;
    }

    @PropertyName("senderAvatarUrl")
    public void setSenderAvatarUrl(String senderAvatarUrl) {
        this.senderAvatarUrl = senderAvatarUrl;
    }

    @PropertyName("imageUrl")
    public String getImageUrl() {
        return imageUrl;
    }

    @PropertyName("imageUrl")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @PropertyName("voiceUrl")
    public String getVoiceUrl() {
        return voiceUrl;
    }

    @PropertyName("voiceUrl")
    public void setVoiceUrl(String voiceUrl) {
        this.voiceUrl = voiceUrl;
    }

    // Helper methods
    public boolean isTextMessage() {
        return "text".equals(messageType);
    }

    public boolean isImageMessage() {
        return "image".equals(messageType);
    }

    public boolean isVoiceMessage() {
        return "voice".equals(messageType);
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", messageType='" + messageType + '\'' +
                ", messageTimestamp=" + messageTimestamp +
                ", messageRead=" + messageRead +
                '}';
    }
}