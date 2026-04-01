package com.example.joblinker.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.joblinker.R;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.models.Call;
import com.example.joblinker.utils.ImageUtils;

import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

public class CallActivity extends AppCompatActivity {

    public static final String EXTRA_RECEIVER_ID     = "receiver_id";
    public static final String EXTRA_RECEIVER_NAME   = "receiver_name";
    public static final String EXTRA_RECEIVER_AVATAR = "receiver_avatar";
    public static final String EXTRA_CALL_TYPE       = "call_type";

    // ── Replace with your Agora App ID ────────────
    private static final String AGORA_APP_ID = "YOUR_AGORA_APP_ID";

    private static final int REQUEST_PERMISSIONS = 300;

    // ── Views ─────────────────────────────────────
    private ImageView ivUserAvatar;
    private TextView tvUserName, tvCallStatus, tvCallTimer;
    private View viewPulse;
    private FrameLayout remoteVideoContainer, localVideoContainer;
    private FloatingActionButton btnMute, btnSpeaker, btnHangUp, btnVideoToggle;

    // ── Agora ─────────────────────────────────────
    private RtcEngine rtcEngine;

    // ── Firebase ──────────────────────────────────
    private JobLinkerFirebaseManager firebaseManager;
    private String callId;

    // ── Intent data ───────────────────────────────
    private String receiverId, receiverName, receiverAvatar, callType;

    // ── State ─────────────────────────────────────
    private boolean isMuted     = false;
    private boolean isSpeakerOn = false;
    private boolean isVideoOn   = true;
    private long callStartTime  = 0;

    // ── Timer ─────────────────────────────────────
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    // ── Agora event handler ───────────────────────
    private final IRtcEngineEventHandler rtcEventHandler = new IRtcEngineEventHandler() {

        @Override
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(() -> {
                onCallConnected();
                if ("video".equals(callType)) setupRemoteVideo(uid);
            });
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            runOnUiThread(() -> endCall());
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            runOnUiThread(() -> tvCallStatus.setText(R.string.calling));
        }

        @Override
        public void onError(int err) {
            runOnUiThread(() -> Toast.makeText(CallActivity.this,
                "Call error: " + err, Toast.LENGTH_SHORT).show());
        }
    };

    // ─────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        firebaseManager = JobLinkerFirebaseManager.getInstance();
        getIntentData();
        initializeViews();
        setupUI();
        setupClickListeners();
        checkPermissionsAndStartCall();
    }

    private void getIntentData() {
        receiverId     = getIntent().getStringExtra(EXTRA_RECEIVER_ID);
        receiverName   = getIntent().getStringExtra(EXTRA_RECEIVER_NAME);
        receiverAvatar = getIntent().getStringExtra(EXTRA_RECEIVER_AVATAR);
        callType       = getIntent().getStringExtra(EXTRA_CALL_TYPE);
    }

    private void initializeViews() {
        ivUserAvatar          = findViewById(R.id.iv_user_avatar);
        tvUserName            = findViewById(R.id.tv_user_name);
        tvCallStatus          = findViewById(R.id.tv_call_status);
        tvCallTimer           = findViewById(R.id.tv_call_timer);
        viewPulse             = findViewById(R.id.view_pulse);
        remoteVideoContainer  = findViewById(R.id.remote_video_container);
        localVideoContainer   = findViewById(R.id.local_video_container);
        btnMute               = findViewById(R.id.btn_mute);
        btnSpeaker            = findViewById(R.id.btn_speaker);
        btnHangUp             = findViewById(R.id.btn_hang_up);
        btnVideoToggle        = findViewById(R.id.btn_video_toggle);
    }

    private void setupUI() {
        tvUserName.setText(receiverName);
        ImageUtils.loadCircularImage(this, receiverAvatar, ivUserAvatar);
        tvCallStatus.setText(R.string.calling);

        if ("video".equals(callType)) {
            btnVideoToggle.setVisibility(View.VISIBLE);
        } else {
            btnVideoToggle.setVisibility(View.GONE);
            remoteVideoContainer.setVisibility(View.GONE);
            localVideoContainer.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        btnMute.setOnClickListener(v -> toggleMute());
        btnSpeaker.setOnClickListener(v -> toggleSpeaker());
        btnVideoToggle.setOnClickListener(v -> toggleVideo());
        btnHangUp.setOnClickListener(v -> endCall());
    }

    // ── Permissions ───────────────────────────────
    private void checkPermissionsAndStartCall() {
        String[] perms = "video".equals(callType)
            ? new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA}
            : new String[]{Manifest.permission.RECORD_AUDIO};

        boolean allGranted = true;
        for (String p : perms) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        if (allGranted) {
            initAgoraAndJoin();
        } else {
            ActivityCompat.requestPermissions(this, perms, REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean granted = true;
        for (int r : grantResults) if (r != PackageManager.PERMISSION_GRANTED) { granted = false; break; }
        if (granted) {
            initAgoraAndJoin();
        } else {
            Toast.makeText(this, "Camera/Microphone permission required", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // ── Agora init & join ─────────────────────────
    private void initAgoraAndJoin() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext    = getApplicationContext();
            config.mAppId      = AGORA_APP_ID;
            config.mEventHandler = rtcEventHandler;
            rtcEngine = RtcEngine.create(config);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to init Agora: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if ("video".equals(callType)) {
            rtcEngine.enableVideo();
            setupLocalVideo();
        } else {
            rtcEngine.disableVideo();
        }

        // Create call record in Firebase to get the channel name
        Call call = new Call(firebaseManager.getCurrentUserId(), receiverId, callType);
        call.setCallerName(firebaseManager.getCurrentUserId());
        call.setReceiverName(receiverName);

        firebaseManager.createCall(call, new JobLinkerFirebaseManager.DataCallback<String>() {
            @Override
            public void onSuccess(String id) {
                callId = id;
                // Use callId as the Agora channel name (unique per call)
                rtcEngine.joinChannel(null, callId, 0, null);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(CallActivity.this, "Call failed: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // ── Local video preview ───────────────────────
    private void setupLocalVideo() {
        SurfaceView localSurface = new SurfaceView(this);
        localVideoContainer.addView(localSurface);
        rtcEngine.setupLocalVideo(new VideoCanvas(localSurface,
            VideoCanvas.RENDER_MODE_HIDDEN, 0));
        rtcEngine.startPreview();
        localVideoContainer.setVisibility(View.VISIBLE);
    }

    // ── Remote video ──────────────────────────────
    private void setupRemoteVideo(int uid) {
        SurfaceView remoteSurface = new SurfaceView(this);
        remoteVideoContainer.addView(remoteSurface);
        rtcEngine.setupRemoteVideo(new VideoCanvas(remoteSurface,
            VideoCanvas.RENDER_MODE_HIDDEN, uid));
        remoteVideoContainer.setVisibility(View.VISIBLE);
    }

    // ── On connected ──────────────────────────────
    private void onCallConnected() {
        tvCallStatus.setText("Connected");
        if (viewPulse != null) viewPulse.setVisibility(View.GONE);
        callStartTime = System.currentTimeMillis();

        if (callId != null) {
            firebaseManager.updateCallStatus(callId, "connected",
                new JobLinkerFirebaseManager.VoidCallback() {
                    @Override public void onSuccess() {}
                    @Override public void onFailure(String e) {}
                });
        }

        // Hide avatar for video calls
        if ("video".equals(callType)) {
            ivUserAvatar.setVisibility(View.GONE);
        }

        startCallTimer();
    }

    // ── Timer ─────────────────────────────────────
    private void startCallTimer() {
        tvCallTimer.setVisibility(View.VISIBLE);
        timerRunnable = new Runnable() {
            @Override public void run() {
                long elapsed  = System.currentTimeMillis() - callStartTime;
                int total     = (int)(elapsed / 1000);
                int minutes   = total / 60;
                int seconds   = total % 60;
                tvCallTimer.setText(String.format("%02d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    // ── Mute ──────────────────────────────────────
    private void toggleMute() {
        isMuted = !isMuted;
        rtcEngine.muteLocalAudioStream(isMuted);
        btnMute.setImageResource(isMuted
            ? R.drawable.ic_mic_off : R.drawable.ic_mic);
        btnMute.setBackgroundTintList(getResources().getColorStateList(
            isMuted ? R.color.error : R.color.secondary, null));
    }

    // ── Speaker ───────────────────────────────────
    private void toggleSpeaker() {
        isSpeakerOn = !isSpeakerOn;
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setSpeakerphoneOn(isSpeakerOn);
        }
        btnSpeaker.setImageResource(R.drawable.ic_volume_up);
        btnSpeaker.setBackgroundTintList(getResources().getColorStateList(
            isSpeakerOn ? R.color.primary : R.color.secondary, null));
    }

    // ── Video toggle ──────────────────────────────
    private void toggleVideo() {
        isVideoOn = !isVideoOn;
        rtcEngine.muteLocalVideoStream(!isVideoOn);
        localVideoContainer.setVisibility(isVideoOn ? View.VISIBLE : View.GONE);
        btnVideoToggle.setImageResource(isVideoOn
            ? R.drawable.ic_videocam : R.drawable.ic_videocam_off);
    }

    // ── End call ──────────────────────────────────
    private void endCall() {
        timerHandler.removeCallbacksAndMessages(null);

        if (rtcEngine != null) {
            rtcEngine.leaveChannel();
        }

        if (callId != null) {
            firebaseManager.updateCallStatus(callId, "ended",
                new JobLinkerFirebaseManager.VoidCallback() {
                    @Override public void onSuccess() {}
                    @Override public void onFailure(String e) {}
                });
        }
        finish();
    }

    // ── Lifecycle ─────────────────────────────────
    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacksAndMessages(null);
        if (rtcEngine != null) {
            rtcEngine.leaveChannel();
            RtcEngine.destroy();
            rtcEngine = null;
        }
    }

    @Override
    public void onBackPressed() {
        endCall();
    }
}
