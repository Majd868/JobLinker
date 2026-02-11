package com.example.joblinker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.joblinker.R;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.joblinker.models.Call;
import com.example.joblinker.utils.ImageUtils;

public class CallActivity extends AppCompatActivity {

    public static final String EXTRA_RECEIVER_ID = "receiver_id";
    public static final String EXTRA_RECEIVER_NAME = "receiver_name";
    public static final String EXTRA_RECEIVER_AVATAR = "receiver_avatar";
    public static final String EXTRA_CALL_TYPE = "call_type";

    private ImageView ivUserAvatar;
    private TextView tvUserName, tvCallStatus, tvCallTimer;
    private View viewPulse, remoteVideoContainer, localVideoContainer;
    private FloatingActionButton btnMute, btnSpeaker, btnHangUp, btnVideoToggle;

    private JobLinkerFirebaseManager firebaseManager;
    private String receiverId;
    private String receiverName;
    private String receiverAvatar;
    private String callType;
    private String callId;

    private boolean isMuted = false;
    private boolean isSpeakerOn = false;
    private boolean isVideoOn = true;

    private Handler timerHandler;
    private Runnable timerRunnable;
    private long callStartTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        firebaseManager = JobLinkerFirebaseManager.getInstance();

        getIntentData();
        initializeViews();
        setupUI();
        setupClickListeners();
        initiateCall();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        receiverId = intent.getStringExtra(EXTRA_RECEIVER_ID);
        receiverName = intent.getStringExtra(EXTRA_RECEIVER_NAME);
        receiverAvatar = intent.getStringExtra(EXTRA_RECEIVER_AVATAR);
        callType = intent.getStringExtra(EXTRA_CALL_TYPE);
    }

    private void initializeViews() {
        ivUserAvatar = findViewById(R.id.iv_user_avatar);
        tvUserName = findViewById(R.id.tv_user_name);
        tvCallStatus = findViewById(R.id.tv_call_status);
        tvCallTimer = findViewById(R.id.tv_call_timer);
        viewPulse = findViewById(R.id.view_pulse);
        remoteVideoContainer = findViewById(R.id.remote_video_container);
        localVideoContainer = findViewById(R.id.local_video_container);
        btnMute = findViewById(R.id.btn_mute);
        btnSpeaker = findViewById(R.id.btn_speaker);
        btnHangUp = findViewById(R.id.btn_hang_up);
        btnVideoToggle = findViewById(R.id.btn_video_toggle);
    }

    private void setupUI() {
        tvUserName.setText(receiverName);
        ImageUtils.loadCircularImage(this, receiverAvatar, ivUserAvatar);

        if ("video".equals(callType)) {
            btnVideoToggle.setVisibility(View.VISIBLE);
            // Show video containers when call connects
        } else {
            btnVideoToggle.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        btnMute.setOnClickListener(v -> toggleMute());

        btnSpeaker.setOnClickListener(v -> toggleSpeaker());

        btnVideoToggle.setOnClickListener(v -> toggleVideo());

        btnHangUp.setOnClickListener(v -> endCall());
    }

    private void initiateCall() {
        tvCallStatus.setText(R.string.calling);

        // Create call record in Firebase
        Call call = new Call(firebaseManager.getCurrentUserId(), receiverId, callType);
        call.setCallerName(receiverName);
        call.setReceiverName(receiverName);

        firebaseManager.createCall(call, new JobLinkerFirebaseManager.DataCallback<String>() {
            @Override
            public void onSuccess(String id) {
                callId = id;
                // TODO: Integrate with Agora SDK
                simulateCallConnection();
            }

            @Override
            public void onFailure(String error) {
                tvCallStatus.setText("Call failed");
                finish();
            }
        });
    }

    private void simulateCallConnection() {
        // Simulate call connection after 2 seconds
        new Handler().postDelayed(() -> {
            onCallConnected();
        }, 2000);
    }

    private void onCallConnected() {
        tvCallStatus.setText(R.string.connections);
        viewPulse.setVisibility(View.GONE);
        callStartTime = System.currentTimeMillis();

        // Update call status in Firebase
        firebaseManager.updateCallStatus(callId, "connected",
                new JobLinkerFirebaseManager.VoidCallback() {
                    @Override
                    public void onSuccess() {}

                    @Override
                    public void onFailure(String error) {}
                });

        // Start timer
        startCallTimer();

        // Show video for video calls
        if ("video".equals(callType)) {
            ivUserAvatar.setVisibility(View.GONE);
            remoteVideoContainer.setVisibility(View.VISIBLE);
            localVideoContainer.setVisibility(View.VISIBLE);
        }
    }

    private void startCallTimer() {
        tvCallTimer.setVisibility(View.VISIBLE);

        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - callStartTime;
                int seconds = (int) (elapsed / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                tvCallTimer.setText(String.format("%02d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void toggleMute() {
        isMuted = !isMuted;

        if (isMuted) {
            btnMute.setImageResource(R.drawable.ic_mic);
            btnMute.setBackgroundTintList(getResources().getColorStateList(R.color.error, null));
        } else {
            btnMute.setImageResource(R.drawable.ic_mic);
            btnMute.setBackgroundTintList(getResources().getColorStateList(R.color.secondary, null));
        }

        // TODO: Mute audio in Agora SDK
    }

    private void toggleSpeaker() {
        isSpeakerOn = !isSpeakerOn;

        if (isSpeakerOn) {
            btnSpeaker.setImageResource(R.drawable.ic_volume_up);
            btnSpeaker.setBackgroundTintList(getResources().getColorStateList(R.color.primary, null));
        } else {
            btnSpeaker.setImageResource(R.drawable.ic_volume_up);
            btnSpeaker.setBackgroundTintList(getResources().getColorStateList(R.color.secondary, null));
        }

        // TODO: Toggle speaker in Agora SDK
    }

    private void toggleVideo() {
        isVideoOn = !isVideoOn;

        if (isVideoOn) {
            btnVideoToggle.setImageResource(R.drawable.ic_videocam);
            localVideoContainer.setVisibility(View.VISIBLE);
        } else {
            btnVideoToggle.setImageResource(R.drawable.ic_videocam);
            localVideoContainer.setVisibility(View.GONE);
        }

        // TODO: Toggle video in Agora SDK
    }

    private void endCall() {
        // Stop timer
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }

        // Update call status
        if (callId != null) {
            firebaseManager.updateCallStatus(callId, "ended",
                    new JobLinkerFirebaseManager.VoidCallback() {
                        @Override
                        public void onSuccess() {}

                        @Override
                        public void onFailure(String error) {}
                    });
        }

        // TODO: End call in Agora SDK

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    @Override
    public void onBackPressed() {
        // Prevent back button during call
        endCall();
    }
}