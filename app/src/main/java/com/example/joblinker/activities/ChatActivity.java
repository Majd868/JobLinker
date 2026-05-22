package com.example.joblinker.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joblinker.R;
import com.example.joblinker.adapters.MessageAdapter;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.models.Message;
import com.example.joblinker.models.User;
import com.example.joblinker.utils.ImageUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.ListenerRegistration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID         = "user_id";
    public static final String EXTRA_USER_NAME       = "user_name";
    public static final String EXTRA_USER_AVATAR     = "user_avatar";
    public static final String EXTRA_CONVERSATION_ID = "conversation_id";

    private static final int REQUEST_RECORD_AUDIO = 201;
    private static final int REQUEST_CAMERA       = 202;

    private MaterialToolbar toolbar;
    private ImageView ivUserAvatar;
    private TextView tvUserName, tvStatus, tvTyping, tvRecordingTime;
    private ImageButton btnVoiceCall, btnVideoCall, btnAttachment, btnEmoji,
            btnCamera, btnCancelRecording;
    private LinearLayout layoutAttachmentTray, layoutRecordingBar;
    private LinearLayout trayCamera, trayGallery, trayDocument, trayLocation, trayContact;
    private View viewRecordingDot;
    private RecyclerView recyclerMessages;
    private TextInputEditText etMessage;
    private FloatingActionButton btnSend;

    private MessageAdapter messageAdapter;
    private final List<Message> messages = new ArrayList<>();
    private JobLinkerFirebaseManager firebaseManager;

    // ── Audio playback ────────────────────────────
    private MediaPlayer audioPlayer = null;
    private String      playingUrl  = null;

    // ── Audio focus ───────────────────────────────
    private android.media.AudioManager      audioManager;
    private android.media.AudioFocusRequest audioFocusRequest;

    private ListenerRegistration messageListener;
    private String otherUserId, otherUserName, otherUserAvatar, conversationId, currentUserId;

    // ── Voice recording ───────────────────────────
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private boolean isRecording = false;
    private final Handler recordingHandler = new Handler(Looper.getMainLooper());
    private Runnable recordingTimerRunnable;
    private int recordingSeconds = 0;

    private Uri cameraImageUri;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) sendImageMessage(uri);
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && cameraImageUri != null)
                    sendImageMessage(cameraImageUri);
            });

    private final ActivityResultLauncher<Intent> documentLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) sendDocumentMessage(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        firebaseManager = JobLinkerFirebaseManager.getInstance();
        currentUserId   = firebaseManager.getCurrentUserId();

        getIntentData();
        if (isFinishing()) return;

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        setupMessageListener();
        loadUserStatus();
    }

    private void getIntentData() {
        Intent i        = getIntent();
        otherUserId     = i.getStringExtra(EXTRA_USER_ID);
        otherUserName   = i.getStringExtra(EXTRA_USER_NAME);
        otherUserAvatar = i.getStringExtra(EXTRA_USER_AVATAR);
        conversationId  = i.getStringExtra(EXTRA_CONVERSATION_ID);

        if (otherUserId == null || otherUserId.isEmpty()) {
            Toast.makeText(this, "Could not open chat — employer info missing",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (conversationId == null)
            conversationId = JobLinkerFirebaseManager.generateConversationId(currentUserId, otherUserId);
    }

    private void initializeViews() {
        toolbar              = findViewById(R.id.toolbar);
        ivUserAvatar         = findViewById(R.id.iv_user_avatar);
        tvUserName           = findViewById(R.id.tv_user_name);
        tvStatus             = findViewById(R.id.tv_status);
        tvTyping             = findViewById(R.id.tv_typing);
        btnVoiceCall         = findViewById(R.id.btn_voice_call);
        btnVideoCall         = findViewById(R.id.btn_video_call);
        btnAttachment        = findViewById(R.id.btn_attachment);
        btnEmoji             = findViewById(R.id.btn_emoji);
        btnCamera            = findViewById(R.id.btn_camera);
        btnCancelRecording   = findViewById(R.id.btn_cancel_recording);
        layoutAttachmentTray = findViewById(R.id.layout_attachment_tray);
        layoutRecordingBar   = findViewById(R.id.layout_recording_bar);
        viewRecordingDot     = findViewById(R.id.view_recording_dot);
        tvRecordingTime      = findViewById(R.id.tv_recording_time);
        trayCamera           = findViewById(R.id.tray_camera);
        trayGallery          = findViewById(R.id.tray_gallery);
        trayDocument         = findViewById(R.id.tray_document);
        trayLocation         = findViewById(R.id.tray_location);
        trayContact          = findViewById(R.id.tray_contact);
        recyclerMessages     = findViewById(R.id.recycler_messages);
        etMessage            = findViewById(R.id.et_message);
        btnSend              = findViewById(R.id.btn_send);

        tvUserName.setText(otherUserName);
        ImageUtils.loadCircularImage(this, otherUserAvatar, ivUserAvatar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(this, messages, currentUserId);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recyclerMessages.setLayoutManager(lm);
        recyclerMessages.setAdapter(messageAdapter);
    }

    private void setupClickListeners() {
        btnVoiceCall.setOnClickListener(v -> initiateCall("voice"));
        btnVideoCall.setOnClickListener(v -> initiateCall("video"));

        btnEmoji.setOnClickListener(v -> {
            etMessage.requestFocus();
            if (etMessage.isShown()) {
                etMessage.setInputType(
                        android.text.InputType.TYPE_CLASS_TEXT |
                                android.text.InputType.TYPE_TEXT_VARIATION_NORMAL);
            }
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(etMessage, 0);
        });

        btnCamera.setOnClickListener(v -> openCamera());
        btnAttachment.setOnClickListener(v -> toggleAttachmentTray());

        trayCamera.setOnClickListener(v   -> { closeTray(); openCamera();    });
        trayGallery.setOnClickListener(v  -> { closeTray(); openGallery();   });
        trayDocument.setOnClickListener(v -> { closeTray(); openDocument();  });
        trayLocation.setOnClickListener(v -> { closeTray(); shareLocation(); });
        trayContact.setOnClickListener(v  -> { closeTray(); shareContact();  });

        btnCancelRecording.setOnClickListener(v -> cancelRecording());

        btnSend.setOnClickListener(v -> {
            if (isRecording) { stopRecordingAndSend(); return; }
            String text = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
            if (!text.isEmpty()) sendTextMessage(text);
            else                 startVoiceRecording();
        });

        btnSend.setOnLongClickListener(v -> { startVoiceRecording(); return true; });

        etMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSend.setImageResource(s.length() > 0 ? R.drawable.ic_send : R.drawable.ic_mic);
            }
        });
    }

    private void toggleAttachmentTray() {
        if (layoutAttachmentTray.getVisibility() == View.VISIBLE) closeTray();
        else {
            layoutAttachmentTray.setVisibility(View.VISIBLE);
            btnAttachment.setImageResource(R.drawable.ic_close);
        }
    }

    private void closeTray() {
        layoutAttachmentTray.setVisibility(View.GONE);
        btnAttachment.setImageResource(R.drawable.ic_attachment);
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            return;
        }
        try {
            File photoFile = createTempImageFile();
            cameraImageUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", photoFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            cameraLauncher.launch(intent);
        } catch (IOException e) {
            Toast.makeText(this, "Could not open camera", Toast.LENGTH_SHORT).show();
        }
    }

    private File createTempImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir  = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        return File.createTempFile("IMG_" + timeStamp, ".jpg", storageDir);
    }

    private void openGallery() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        else
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void openDocument() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        documentLauncher.launch(Intent.createChooser(intent, "Select Document"));
    }

    private void shareLocation() {
        android.location.LocationManager lm =
                (android.location.LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            android.location.Location loc = null;
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                loc = lm.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
                if (loc == null)
                    loc = lm.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
            }
            if (loc != null) {
                String mapsUrl = "https://maps.google.com/?q=" + loc.getLatitude() + "," + loc.getLongitude();
                Message msg = new Message(conversationId, currentUserId, otherUserId,
                        "📍 Location: " + mapsUrl);
                msg.setMessageType("location");
                sendMessageObject(msg);
            } else {
                Toast.makeText(this, "Could not get location. Enable GPS and try again.",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 203);
        }
    }

    private void shareContact() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, 204);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 204 && resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            if (contactUri != null) {
                android.database.Cursor cursor = getContentResolver().query(
                        contactUri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIdx = cursor.getColumnIndex(
                            android.provider.ContactsContract.Contacts.DISPLAY_NAME);
                    String name = nameIdx >= 0 ? cursor.getString(nameIdx) : "Contact";
                    cursor.close();
                    Message msg = new Message(conversationId, currentUserId, otherUserId,
                            "👤 Contact: " + name);
                    msg.setMessageType("contact");
                    sendMessageObject(msg);
                }
            }
        }
    }

    // ══════════════════════════════════════════════
    // VOICE RECORDING
    // ══════════════════════════════════════════════
    private void startVoiceRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
            return;
        }

        isRecording = true;
        recordingSeconds = 0;
        layoutRecordingBar.setVisibility(View.VISIBLE);
        btnSend.setImageResource(R.drawable.ic_stop);
        tvRecordingTime.setText("0:00");

        recordingTimerRunnable = new Runnable() {
            @Override public void run() {
                if (!isRecording) return;
                recordingSeconds++;
                int m = recordingSeconds / 60, s = recordingSeconds % 60;
                tvRecordingTime.setText(String.format(Locale.getDefault(), "%d:%02d", m, s));
                recordingHandler.postDelayed(this, 1000);
            }
        };
        recordingHandler.postDelayed(recordingTimerRunnable, 1000);
        startRecordingDotAnimation();

        audioFilePath = getExternalCacheDir().getAbsolutePath()
                + "/voice_" + System.currentTimeMillis() + ".3gp";
        final String filePath = audioFilePath;

        new Thread(() -> {
            try {
                MediaRecorder recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                recorder.setOutputFile(filePath);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                recorder.prepare();
                recorder.start();
                // ✅ FIX: check isRecording in case user cancelled before runOnUiThread fired
                runOnUiThread(() -> {
                    if (!isRecording) {
                        try { recorder.stop(); } catch (Exception ignored) {}
                        recorder.release();
                        new File(filePath).delete();
                    } else {
                        mediaRecorder = recorder;
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    isRecording = false;
                    recordingHandler.removeCallbacks(recordingTimerRunnable);
                    layoutRecordingBar.setVisibility(View.GONE);
                    btnSend.setImageResource(R.drawable.ic_mic);
                    Toast.makeText(this, "Failed to start recording: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void stopRecordingAndSend() {
        if (!isRecording) return;
        stopRecording();

        String duration = tvRecordingTime != null ? tvRecordingTime.getText().toString() : "0:00";
        try {
            java.io.File audioFile = new java.io.File(audioFilePath);
            if (!audioFile.exists() || audioFile.length() == 0) {
                Toast.makeText(this, "Recording failed", Toast.LENGTH_SHORT).show();
                return;
            }
            // ✅ FIX: readFully guarantees ALL bytes are read — read() may return early
            byte[] audioBytes = new byte[(int) audioFile.length()];
            try (java.io.DataInputStream dis = new java.io.DataInputStream(
                    new java.io.FileInputStream(audioFile))) {
                dis.readFully(audioBytes);
            }

            String uploadPath = "chat_audio/" + conversationId + "/" + System.currentTimeMillis() + ".3gp";
            firebaseManager.uploadRawBytes(audioBytes, uploadPath, "audio/3gpp",
                    new JobLinkerFirebaseManager.UploadCallback() {
                        @Override public void onSuccess(String downloadUrl) {
                            audioFile.delete();
                            Message msg = new Message(conversationId, currentUserId, otherUserId,
                                    "🎤 Voice message (" + duration + ")");
                            msg.setMessageType("audio");
                            msg.setImageUrl(downloadUrl);
                            sendMessageObject(msg);
                        }
                        @Override public void onProgress(int progress) {}
                        @Override public void onFailure(String error) {
                            audioFile.delete();
                            Toast.makeText(ChatActivity.this,
                                    "Failed to send voice message", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Could not send voice message: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelRecording() {
        if (!isRecording) return;
        stopRecording();
        File f = new File(audioFilePath);
        if (f.exists()) f.delete();
        Toast.makeText(this, "Recording cancelled", Toast.LENGTH_SHORT).show();
    }

    private void stopRecording() {
        isRecording = false;
        recordingHandler.removeCallbacks(recordingTimerRunnable);
        layoutRecordingBar.setVisibility(View.GONE);
        if (viewRecordingDot != null) viewRecordingDot.setAlpha(1f);
        btnSend.setImageResource(R.drawable.ic_mic);
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            }
        } catch (Exception e) {
            mediaRecorder = null;
        }
    }

    private void startRecordingDotAnimation() {
        Runnable blink = new Runnable() {
            @Override public void run() {
                if (!isRecording) return;
                viewRecordingDot.animate().alpha(0f).setDuration(500)
                        .withEndAction(() -> viewRecordingDot.animate().alpha(1f).setDuration(500)
                                .withEndAction(this).start()).start();
            }
        };
        blink.run();
    }

    // ══════════════════════════════════════════════
    // SEND MESSAGES
    // ══════════════════════════════════════════════
    private void sendTextMessage(String text) {
        Message message = new Message(conversationId, currentUserId, otherUserId, text);
        message.setMessageType("text");
        sendMessageObject(message);
        etMessage.setText("");
    }

    private void sendImageMessage(Uri imageUri) {
        byte[] bytes = readUriBytes(imageUri);
        if (bytes == null || bytes.length == 0) {
            Toast.makeText(this, "Could not read image", Toast.LENGTH_SHORT).show();
            return;
        }
        String uploadPath = "chat_images/" + conversationId + "/" + System.currentTimeMillis() + ".jpg";
        firebaseManager.uploadBytes(bytes, uploadPath,
                new JobLinkerFirebaseManager.UploadCallback() {
                    @Override public void onSuccess(String downloadUrl) {
                        Message msg = new Message(conversationId, currentUserId, otherUserId, "📷 Photo");
                        msg.setMessageType("image");
                        msg.setImageUrl(downloadUrl);
                        sendMessageObject(msg);
                    }
                    @Override public void onProgress(int p) {}
                    @Override public void onFailure(String error) {
                        Toast.makeText(ChatActivity.this,
                                "Failed to send image: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private byte[] readUriBytes(Uri uri) {
        try {
            java.io.InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) return null;
            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            byte[] chunk = new byte[8192]; int read;
            while ((read = is.read(chunk)) != -1) buffer.write(chunk, 0, read);
            is.close();
            return buffer.toByteArray();
        } catch (Exception e) {
            Log.e("ChatActivity", "readUriBytes failed: " + e.getMessage());
            return null;
        }
    }

    private void sendDocumentMessage(Uri documentUri) {
        String fileName = "document";
        try {
            android.database.Cursor cursor = getContentResolver().query(
                    documentUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) fileName = cursor.getString(idx);
                cursor.close();
            }
        } catch (Exception ignored) {}

        byte[] bytes = readUriBytes(documentUri);
        if (bytes == null || bytes.length == 0) {
            Toast.makeText(this, "Could not read document", Toast.LENGTH_SHORT).show();
            return;
        }

        final String finalName = fileName;
        String uploadPath = "chat_documents/" + conversationId + "/" + System.currentTimeMillis();
        firebaseManager.uploadBytes(bytes, uploadPath,
                new JobLinkerFirebaseManager.UploadCallback() {
                    @Override public void onSuccess(String downloadUrl) {
                        Message msg = new Message(conversationId, currentUserId, otherUserId,
                                "📄 " + finalName);
                        msg.setMessageType("document");
                        msg.setImageUrl(downloadUrl);
                        sendMessageObject(msg);
                    }
                    @Override public void onProgress(int p) {}
                    @Override public void onFailure(String error) {
                        Toast.makeText(ChatActivity.this,
                                "Failed to upload document", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendMessageObject(Message message) {
        firebaseManager.sendMessage(message, new JobLinkerFirebaseManager.DataCallback<String>() {
            @Override public void onSuccess(String id) {
                message.setMessageId(id);
                messages.add(message);
                messageAdapter.notifyItemInserted(messages.size() - 1);
                recyclerMessages.scrollToPosition(messages.size() - 1);
            }
            @Override public void onFailure(String error) {
                Toast.makeText(ChatActivity.this,
                        "Failed to send: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ══════════════════════════════════════════════
    // REAL-TIME LISTENER
    // ══════════════════════════════════════════════
    private void setupMessageListener() {
        messageListener = firebaseManager.listenToMessages(conversationId,
                new JobLinkerFirebaseManager.ListCallback<Message>() {
                    @Override public void onSuccess(List<Message> list) {
                        messages.clear();
                        messages.addAll(list);
                        messageAdapter.notifyDataSetChanged();
                        if (!messages.isEmpty())
                            recyclerMessages.scrollToPosition(messages.size() - 1);
                        markMessagesAsRead();
                    }
                    @Override public void onFailure(String error) {
                        Toast.makeText(ChatActivity.this,
                                "Error loading messages: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserStatus() {
        firebaseManager.getUser(otherUserId, new JobLinkerFirebaseManager.DataCallback<User>() {
            @Override public void onSuccess(User user) {
                if (user.isOnline()) {
                    tvStatus.setText(R.string.online);
                    tvStatus.setTextColor(getResources().getColor(R.color.success, null));
                } else {
                    tvStatus.setText(R.string.offline);
                    tvStatus.setTextColor(getResources().getColor(R.color.text_secondary, null));
                }
            }
            @Override public void onFailure(String error) {}
        });
    }

    private void markMessagesAsRead() {
        for (Message m : messages) {
            if (!m.isMessageRead() && currentUserId.equals(m.getMessageReceiverId())) {
                firebaseManager.markMessageAsRead(m.getMessageId(),
                        new JobLinkerFirebaseManager.VoidCallback() {
                            @Override public void onSuccess() {}
                            @Override public void onFailure(String e) {}
                        });
            }
        }
    }

    private void initiateCall(String callType) {
        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra(CallActivity.EXTRA_RECEIVER_ID,     otherUserId);
        intent.putExtra(CallActivity.EXTRA_RECEIVER_NAME,   otherUserName);
        intent.putExtra(CallActivity.EXTRA_RECEIVER_AVATAR, otherUserAvatar);
        intent.putExtra(CallActivity.EXTRA_CALL_TYPE,       callType);
        startActivity(intent);
    }

    // ══════════════════════════════════════════════
    // PERMISSIONS
    // ══════════════════════════════════════════════
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_RECORD_AUDIO) startVoiceRecording();
            else if (requestCode == REQUEST_CAMERA)  openCamera();
        } else {
            Toast.makeText(this,
                    requestCode == REQUEST_RECORD_AUDIO
                            ? "Microphone permission required for voice messages"
                            : "Camera permission required",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // ══════════════════════════════════════════════
    // AUDIO PLAYBACK  (called by MessageAdapter)
    // ══════════════════════════════════════════════
    public void playVoiceMessage(String url, Runnable onStart, Runnable onStop) {
        // ✅ FIX #5: guard null URL before calling .equals()
        if (url == null || url.isEmpty()) {
            if (onStop != null) onStop.run();
            return;
        }

        // Toggle: tap same message while playing → stop
        if (url.equals(playingUrl) && audioPlayer != null) {
            stopAudioPlayer();
            if (onStop != null) onStop.run();
            return;
        }

        stopAudioPlayer();

        audioPlayer = new MediaPlayer();
        playingUrl  = url;
        final MediaPlayer player = audioPlayer; // local ref guards against race condition

        try {
            // ✅ FIX #3: AudioAttributes MUST be set before setDataSource
            audioPlayer.setAudioAttributes(
                    new android.media.AudioAttributes.Builder()
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                            .build());

            audioPlayer.setDataSource(url);

            audioPlayer.setOnPreparedListener(mp -> {
                // ✅ FIX #4: stale-player guard — player may have been released already
                if (mp != player || audioPlayer != player) {
                    try { mp.release(); } catch (Exception ignored) {}
                    return;
                }
                // ✅ FIX #2: request audio focus — without this the OS silences playback
                if (!requestAudioFocus()) {
                    if (onStop != null) onStop.run();
                    stopAudioPlayer();
                    return;
                }
                if (onStart != null) onStart.run();
                mp.start();
            });

            audioPlayer.setOnCompletionListener(mp -> {
                abandonAudioFocus();
                if (onStop != null) onStop.run();
                stopAudioPlayer();
            });

            audioPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("ChatActivity", "MediaPlayer error what=" + what + " extra=" + extra);
                Toast.makeText(this, "Cannot play audio", Toast.LENGTH_SHORT).show();
                abandonAudioFocus();
                if (onStop != null) onStop.run();
                stopAudioPlayer();
                return true;
            });

            audioPlayer.prepareAsync();

        } catch (Exception e) {
            Log.e("ChatActivity", "playVoiceMessage failed: " + e.getMessage(), e);
            Toast.makeText(this, "Cannot play audio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            if (onStop != null) onStop.run();
            stopAudioPlayer();
        }
    }

    private boolean requestAudioFocus() {
        if (audioManager == null)
            audioManager = (android.media.AudioManager) getSystemService(AUDIO_SERVICE);

        audioFocusRequest = new android.media.AudioFocusRequest.Builder(
                android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(new android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build())
                .setOnAudioFocusChangeListener(focusChange -> {
                    if (focusChange == android.media.AudioManager.AUDIOFOCUS_LOSS ||
                            focusChange == android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        runOnUiThread(this::stopAudioPlayer);
                    }
                })
                .build();

        return audioManager.requestAudioFocus(audioFocusRequest)
                == android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void abandonAudioFocus() {
        if (audioManager != null && audioFocusRequest != null) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
            audioFocusRequest = null;
        }
    }

    public void stopAudioPlayer() {
        // ✅ FIX #4: capture then null — prevents double-release on re-entrant calls
        MediaPlayer p = audioPlayer;
        audioPlayer   = null;
        playingUrl    = null;
        if (p != null) {
            try { p.stop();    } catch (Exception ignored) {}
            try { p.release(); } catch (Exception ignored) {}
        }
        abandonAudioFocus();
    }

    // ══════════════════════════════════════════════
    // LIFECYCLE
    // ══════════════════════════════════════════════
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudioPlayer();
        if (messageListener != null) messageListener.remove();
        if (isRecording) stopRecording();
        recordingHandler.removeCallbacksAndMessages(null);
    }
}