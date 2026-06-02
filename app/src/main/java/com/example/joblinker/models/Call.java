package com.example.joblinker.models;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

public class Call implements Serializable {

    private String callId;
    private String callerId;
    private String receiverId;
    private String callType; // "voice" or "video"
    private String callStatus; // "initiated", "ringing", "connected", "ended", "rejected"
    private long startTime;
    private long endTime;
    private long duration; // in seconds
    private String callerName;
    private String callerAvatarUrl;
    private String receiverName;
    private String receiverAvatarUrl;
    private String agoraChannelName;
    private String agoraToken;

    // مُنشئ افتراضي يُهيئ وقت البدء وحالة المكالمة والمدة
    public Call() {
        this.startTime = System.currentTimeMillis();
        this.callStatus = "initiated";
        this.duration = 0;
    }

    // مُنشئ بالحقول الأساسية: معرف المتصل، المستقبل، ونوع المكالمة
    public Call(String callerId, String receiverId, String callType) {
        this();
        this.callerId = callerId;
        this.receiverId = receiverId;
        this.callType = callType;
    }

    // الدوال الجالبة والمحددة مع تعليقات PropertyName

    @PropertyName("callId")
    public String getCallId() {
        return callId;
    }

    @PropertyName("callId")
    public void setCallId(String callId) {
        this.callId = callId;
    }

    @PropertyName("callerId")
    public String getCallerId() {
        return callerId;
    }

    @PropertyName("callerId")
    public void setCallerId(String callerId) {
        this.callerId = callerId;
    }

    @PropertyName("receiverId")
    public String getReceiverId() {
        return receiverId;
    }

    @PropertyName("receiverId")
    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    @PropertyName("callType")
    public String getCallType() {
        return callType;
    }

    @PropertyName("callType")
    public void setCallType(String callType) {
        this.callType = callType;
    }

    @PropertyName("callStatus")
    public String getCallStatus() {
        return callStatus;
    }

    @PropertyName("callStatus")
    public void setCallStatus(String callStatus) {
        this.callStatus = callStatus;
    }

    @PropertyName("startTime")
    public long getStartTime() {
        return startTime;
    }

    @PropertyName("startTime")
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @PropertyName("endTime")
    public long getEndTime() {
        return endTime;
    }

    @PropertyName("endTime")
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    @PropertyName("duration")
    public long getDuration() {
        return duration;
    }

    @PropertyName("duration")
    public void setDuration(long duration) {
        this.duration = duration;
    }

    @PropertyName("callerName")
    public String getCallerName() {
        return callerName;
    }

    @PropertyName("callerName")
    public void setCallerName(String callerName) {
        this.callerName = callerName;
    }

    @PropertyName("callerAvatarUrl")
    public String getCallerAvatarUrl() {
        return callerAvatarUrl;
    }

    @PropertyName("callerAvatarUrl")
    public void setCallerAvatarUrl(String callerAvatarUrl) {
        this.callerAvatarUrl = callerAvatarUrl;
    }

    @PropertyName("receiverName")
    public String getReceiverName() {
        return receiverName;
    }

    @PropertyName("receiverName")
    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    @PropertyName("receiverAvatarUrl")
    public String getReceiverAvatarUrl() {
        return receiverAvatarUrl;
    }

    @PropertyName("receiverAvatarUrl")
    public void setReceiverAvatarUrl(String receiverAvatarUrl) {
        this.receiverAvatarUrl = receiverAvatarUrl;
    }

    @PropertyName("agoraChannelName")
    public String getAgoraChannelName() {
        return agoraChannelName;
    }

    @PropertyName("agoraChannelName")
    public void setAgoraChannelName(String agoraChannelName) {
        this.agoraChannelName = agoraChannelName;
    }

    @PropertyName("agoraToken")
    public String getAgoraToken() {
        return agoraToken;
    }

    @PropertyName("agoraToken")
    public void setAgoraToken(String agoraToken) {
        this.agoraToken = agoraToken;
    }

    // دوال مساعدة
    // تُرجع true إذا كانت المكالمة صوتية
    public boolean isVoiceCall() {
        return "voice".equals(callType);
    }

    // تُرجع true إذا كانت المكالمة مرئية
    public boolean isVideoCall() {
        return "video".equals(callType);
    }

    // تُرجع true إذا كانت المكالمة متصلة حالياً
    public boolean isConnected() {
        return "connected".equals(callStatus);
    }

    // تُرجع true إذا انتهت المكالمة
    public boolean isEnded() {
        return "ended".equals(callStatus);
    }

    // تُرجع true إذا رفض المستقبل المكالمة
    public boolean isRejected() {
        return "rejected".equals(callStatus);
    }

    // يحسب ويخزن مدة المكالمة بالثواني من أوقات البدء والانتهاء
    public void calculateDuration() {
        if (endTime > 0 && startTime > 0) {
            this.duration = (endTime - startTime) / 1000;
        }
    }

    // تُرجع المدة منسقةً بصيغة MM:SS أو HH:MM:SS
    public String getFormattedDuration() {
        long hours = duration / 3600;
        long minutes = (duration % 3600) / 60;
        long seconds = duration % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    @Override
    public String toString() {
        return "Call{" +
                "callId='" + callId + '\'' +
                ", callType='" + callType + '\'' +
                ", callStatus='" + callStatus + '\'' +
                ", duration=" + duration +
                '}';
    }
}