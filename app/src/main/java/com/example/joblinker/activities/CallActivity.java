package com.example.joblinker.activities;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.joblinker.R;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.models.Call;
import com.example.joblinker.utils.ImageUtils;

/**
 * Call screen — UI and Firebase call record management.
 * Voice/video transmission requires a real-time SDK (Agora/WebRTC).
 * To enable calls: add your Agora App ID in strings.xml as "agora_app_id"
 * and integrate the Agora SDK dependency.
 */
public class CallActivity extends AppCompatActivity {

    public static final String EXTRA_RECEIVER_ID     = "receiver_id";
    public static final String EXTRA_RECEIVER_NAME   = "receiver_name";
    public static final String EXTRA_RECEIVER_AVATAR = "receiver_avatar";
    public static final String EXTRA_CALL_TYPE       = "call_type";

    private ImageView  ivUserAvatar;
    private TextView   tvUserName, tvCallStatus, tvCallTimer;
    private View       viewPulse, remoteVideoContainer, localVideoContainer;
    private FloatingActionButton btnMute, btnSpeaker, btnHangUp, btnVideoToggle;

    private JobLinkerFirebaseManager firebaseManager;
    private String receiverId, receiverName, receiverAvatar, callType, callId;

    private boolean isMuted     = false;
    private boolean isSpeakerOn = false;
    private boolean isVideoOn   = true;
    private long    callStartTime = 0;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        firebaseManager = JobLinkerFirebaseManager.getInstance();
        getIntentData();
        initializeViews();
        setupUI();
        setupClickListeners();
        startCall();
    }

    // يستخرج معرّف المستقبل واسمه وصورته ونوع المكالمة من Intent المُطلِق
    private void getIntentData() {
        receiverId     = getIntent().getStringExtra(EXTRA_RECEIVER_ID);
        receiverName   = getIntent().getStringExtra(EXTRA_RECEIVER_NAME);
        receiverAvatar = getIntent().getStringExtra(EXTRA_RECEIVER_AVATAR);
        callType       = getIntent().getStringExtra(EXTRA_CALL_TYPE);
    }

    // يربط جميع عناصر شاشة المكالمة من التخطيط
    private void initializeViews() {
        ivUserAvatar         = findViewById(R.id.iv_user_avatar);
        tvUserName           = findViewById(R.id.tv_user_name);
        tvCallStatus         = findViewById(R.id.tv_call_status);
        tvCallTimer          = findViewById(R.id.tv_call_timer);
        viewPulse            = findViewById(R.id.view_pulse);
        remoteVideoContainer = findViewById(R.id.remote_video_container);
        localVideoContainer  = findViewById(R.id.local_video_container);
        btnMute              = findViewById(R.id.btn_mute);
        btnSpeaker           = findViewById(R.id.btn_speaker);
        btnHangUp            = findViewById(R.id.btn_hang_up);
        btnVideoToggle       = findViewById(R.id.btn_video_toggle);
    }

    // يضبط الحالة الأولية للواجهة بناءً على معلومات المستقبل ونوع المكالمة (صوت أو فيديو)
    private void setupUI() {
        if (tvUserName  != null) tvUserName.setText(receiverName);
        if (ivUserAvatar != null)
            ImageUtils.loadCircularImage(this, receiverAvatar, ivUserAvatar);
        if (tvCallStatus != null) tvCallStatus.setText(R.string.calling);

        boolean isVideo = "video".equals(callType);
        if (btnVideoToggle != null)
            btnVideoToggle.setVisibility(isVideo ? View.VISIBLE : View.GONE);
        if (remoteVideoContainer != null)
            remoteVideoContainer.setVisibility(View.GONE);
        if (localVideoContainer != null)
            localVideoContainer.setVisibility(View.GONE);
    }

    // يربط مستمعي النقر بأزرار كتم الصوت والسماعة وتبديل الفيديو وإنهاء المكالمة
    private void setupClickListeners() {
        if (btnMute      != null) btnMute.setOnClickListener(v -> toggleMute());
        if (btnSpeaker   != null) btnSpeaker.setOnClickListener(v -> toggleSpeaker());
        if (btnVideoToggle != null) btnVideoToggle.setOnClickListener(v -> toggleVideo());
        if (btnHangUp    != null) btnHangUp.setOnClickListener(v -> endCall());
    }

    // ينشئ سجل مكالمة في Firebase ويحاكي اتصالاً بعد تأخير ثانيتين
    private void startCall() {
        Call call = new Call(firebaseManager.getCurrentUserId(), receiverId, callType);
        // Use stored display name — fall back to UID only if name is blank
        String myName = com.example.joblinker.utils.SharedPreferencesManager
                .getInstance(this).getUserName();
        call.setCallerName(myName != null && !myName.isEmpty() ? myName
                : firebaseManager.getCurrentUserId());
        call.setReceiverName(receiverName);

        firebaseManager.createCall(call, new JobLinkerFirebaseManager.DataCallback<String>() {
            @Override
            public void onSuccess(String id) {
                callId = id;
                // Simulate connection after 2s (replace with real SDK connection)
                timerHandler.postDelayed(CallActivity.this::onCallConnected, 2000);
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(CallActivity.this,
                    "Call failed: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // يحدّث الواجهة إلى حالة الاتصال ويبدأ مؤقت المكالمة ويعرض حاويات الفيديو لمكالمات الفيديو
    private void onCallConnected() {
        if (tvCallStatus != null) tvCallStatus.setText("Connected");
        if (viewPulse    != null) viewPulse.setVisibility(View.GONE);
        callStartTime = System.currentTimeMillis();

        if (callId != null) {
            firebaseManager.updateCallStatus(callId, "connected",
                new JobLinkerFirebaseManager.VoidCallback() {
                    @Override public void onSuccess() {}
                    @Override public void onFailure(String e) {}
                });
        }

        startCallTimer();

        if ("video".equals(callType)) {
            if (ivUserAvatar        != null) ivUserAvatar.setVisibility(View.GONE);
            if (remoteVideoContainer != null) remoteVideoContainer.setVisibility(View.VISIBLE);
            if (localVideoContainer  != null) localVideoContainer.setVisibility(View.VISIBLE);
        }
    }

    // يبدأ مؤقتاً يحدّث حقل نص مدة المكالمة كل ثانية
    private void startCallTimer() {
        if (tvCallTimer != null) tvCallTimer.setVisibility(View.VISIBLE);
        timerRunnable = new Runnable() {
            @Override public void run() {
                long elapsed = System.currentTimeMillis() - callStartTime;
                int total = (int)(elapsed / 1000);
                if (tvCallTimer != null)
                    tvCallTimer.setText(String.format("%02d:%02d", total/60, total%60));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    // يبدّل حالة كتم الميكروفون ويحدّث أيقونة زر الكتم ولونه
    private void toggleMute() {
        isMuted = !isMuted;
        if (btnMute != null) {
            btnMute.setImageResource(isMuted ? R.drawable.ic_mic_off : R.drawable.ic_mic);
            btnMute.setBackgroundTintList(getResources().getColorStateList(
                isMuted ? R.color.error : R.color.secondary, null));
        }
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (am != null) am.setMicrophoneMute(isMuted);
    }

    // يبدّل السماعة الخارجية ويحدّث لون زر السماعة
    private void toggleSpeaker() {
        isSpeakerOn = !isSpeakerOn;
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (am != null) am.setSpeakerphoneOn(isSpeakerOn);
        if (btnSpeaker != null) {
            btnSpeaker.setImageResource(R.drawable.ic_volume_up);
            btnSpeaker.setBackgroundTintList(getResources().getColorStateList(
                isSpeakerOn ? R.color.primary : R.color.secondary, null));
        }
    }

    // يبدّل ظهور بث الفيديو المحلي ويحدّث أيقونة زر تبديل الفيديو
    private void toggleVideo() {
        isVideoOn = !isVideoOn;
        if (btnVideoToggle != null)
            btnVideoToggle.setImageResource(
                isVideoOn ? R.drawable.ic_videocam : R.drawable.ic_videocam_off);
        if (localVideoContainer != null)
            localVideoContainer.setVisibility(isVideoOn ? View.VISIBLE : View.GONE);
    }

    // يوقف مؤقت المكالمة ويضع علامة إنهاء عليها في Firebase وينهي النشاط
    private void endCall() {
        timerHandler.removeCallbacksAndMessages(null);
        if (callId != null) {
            firebaseManager.updateCallStatus(callId, "ended",
                new JobLinkerFirebaseManager.VoidCallback() {
                    @Override public void onSuccess() {}
                    @Override public void onFailure(String e) {}
                });
        }
        finish();
    }

    // يزيل جميع callbacks المعلّقة في Handler عند تدمير النشاط
    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacksAndMessages(null);
    }

    // ينهي المكالمة بدلاً من الرجوع للخلف عند الضغط على زر الرجوع
    @Override
    public void onBackPressed() {
        endCall();
    }
}
