package com.example.joblinker.models;

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

    // مُنشئ افتراضي يُهيئ الطابع الزمني وحالة القراءة ونوع الرسالة
    public Message() {
        this.messageTimestamp = System.currentTimeMillis();
        this.messageRead = false;
        this.messageType = "text";
    }

    // مُنشئ بالحقول الأساسية: معرف المحادثة، المرسِل، المستقبِل، والنص
    public Message(String conversationId, String senderId, String receiverId, String text) {
        this();
        this.conversationId = conversationId;
        this.messageSenderId = senderId;
        this.messageReceiverId = receiverId;
        this.messageText = text;
    }

    // الدوال الجالبة والمحددة مع تعليقات PropertyName

    // تُرجع المعرف الفريد للرسالة
    @PropertyName("messageId")
    public String getMessageId() {
        return messageId;
    }

    // تُعيّن المعرف الفريد للرسالة
    @PropertyName("messageId")
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    // تُرجع معرف المحادثة التي تنتمي إليها هذه الرسالة
    @PropertyName("conversationId")
    public String getConversationId() {
        return conversationId;
    }

    // تُعيّن معرف المحادثة التي تنتمي إليها هذه الرسالة
    @PropertyName("conversationId")
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    // تُرجع معرف المستخدم المرسِل للرسالة
    @PropertyName("messageSenderId")
    public String getMessageSenderId() {
        return messageSenderId;
    }

    // تُعيّن معرف المستخدم المرسِل للرسالة
    @PropertyName("messageSenderId")
    public void setMessageSenderId(String messageSenderId) {
        this.messageSenderId = messageSenderId;
    }

    // تُرجع معرف المستخدم المستقبِل للرسالة
    @PropertyName("messageReceiverId")
    public String getMessageReceiverId() {
        return messageReceiverId;
    }

    // تُعيّن معرف المستخدم المستقبِل للرسالة
    @PropertyName("messageReceiverId")
    public void setMessageReceiverId(String messageReceiverId) {
        this.messageReceiverId = messageReceiverId;
    }

    // تُرجع محتوى نص الرسالة
    @PropertyName("messageText")
    public String getMessageText() {
        return messageText;
    }

    // تُعيّن محتوى نص الرسالة
    @PropertyName("messageText")
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    // تُرجع نوع الرسالة (نص، صورة، أو صوت)
    @PropertyName("messageType")
    public String getMessageType() {
        return messageType;
    }

    // تُعيّن نوع الرسالة (نص، صورة، أو صوت)
    @PropertyName("messageType")
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    // تُرجع الطابع الزمني لوقت إرسال الرسالة
    @PropertyName("messageTimestamp")
    public long getMessageTimestamp() {
        return messageTimestamp;
    }

    // تُعيّن الطابع الزمني لوقت إرسال الرسالة
    @PropertyName("messageTimestamp")
    public void setMessageTimestamp(long messageTimestamp) {
        this.messageTimestamp = messageTimestamp;
    }

    // تُرجع true إذا قرأ المستلم الرسالة
    @PropertyName("messageRead")
    public boolean isMessageRead() {
        return messageRead;
    }

    // تُعيّن حالة قراءة الرسالة
    @PropertyName("messageRead")
    public void setMessageRead(boolean messageRead) {
        this.messageRead = messageRead;
    }

    // تُرجع الاسم المعروض للمرسِل
    @PropertyName("senderName")
    public String getSenderName() {
        return senderName;
    }

    // تُعيّن الاسم المعروض للمرسِل
    @PropertyName("senderName")
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    // تُرجع رابط صورة الملف الشخصي للمرسِل
    @PropertyName("senderAvatarUrl")
    public String getSenderAvatarUrl() {
        return senderAvatarUrl;
    }

    // تُعيّن رابط صورة الملف الشخصي للمرسِل
    @PropertyName("senderAvatarUrl")
    public void setSenderAvatarUrl(String senderAvatarUrl) {
        this.senderAvatarUrl = senderAvatarUrl;
    }

    // تُرجع رابط الصورة المرفقة بهذه الرسالة
    @PropertyName("imageUrl")
    public String getImageUrl() {
        return imageUrl;
    }

    // تُعيّن رابط الصورة المرفقة بهذه الرسالة
    @PropertyName("imageUrl")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // تُرجع رابط التسجيل الصوتي المرفق بهذه الرسالة
    @PropertyName("voiceUrl")
    public String getVoiceUrl() {
        return voiceUrl;
    }

    // تُعيّن رابط التسجيل الصوتي المرفق بهذه الرسالة
    @PropertyName("voiceUrl")
    public void setVoiceUrl(String voiceUrl) {
        this.voiceUrl = voiceUrl;
    }

    // دوال مساعدة
    // تُرجع true إذا كانت الرسالة نصاً عادياً
    public boolean isTextMessage() {
        return "text".equals(messageType);
    }

    // تُرجع true إذا كانت الرسالة تحتوي على صورة
    public boolean isImageMessage() {
        return "image".equals(messageType);
    }

    // تُرجع true إذا كانت الرسالة تحتوي على تسجيل صوتي
    public boolean isVoiceMessage() {
        return "audio".equals(messageType) || "voice".equals(messageType);
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