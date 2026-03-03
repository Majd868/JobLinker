package com.example.joblinker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joblinker.R;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.example.joblinker.models.User;
import com.example.joblinker.utils.ImageUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.ListenerRegistration;

// NOTE: keep these imports as in your project (your code used com.joblinker.* for Message/MessageAdapter)
import com.joblinker.adapters.MessageAdapter;
import com.joblinker.models.Message;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_NAME = "user_name";
    public static final String EXTRA_USER_AVATAR = "user_avatar";
    public static final String EXTRA_CONVERSATION_ID = "conversation_id";

    private MaterialToolbar toolbar;
    private ImageView ivUserAvatar;
    private TextView tvUserName, tvStatus, tvTyping;
    private ImageButton btnVoiceCall, btnVideoCall, btnAttachment;
    private RecyclerView recyclerMessages;
    private TextInputEditText etMessage;
    private FloatingActionButton btnSend;

    private MessageAdapter messageAdapter;
    private final List<Message> messages = new ArrayList<>();
    private JobLinkerFirebaseManager firebaseManager;
    private ListenerRegistration messageListener;

    private String otherUserId;
    private String otherUserName;
    private String otherUserAvatar;
    private String conversationId;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        firebaseManager = JobLinkerFirebaseManager.getInstance();
        currentUserId = firebaseManager.getCurrentUserId();

        getIntentData();

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        setupMessageListener();
        loadUserStatus();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        otherUserId = intent.getStringExtra(EXTRA_USER_ID);
        otherUserName = intent.getStringExtra(EXTRA_USER_NAME);
        otherUserAvatar = intent.getStringExtra(EXTRA_USER_AVATAR);
        conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID);

        // Generate conversation ID if not provided
        if (conversationId == null) {
            conversationId = JobLinkerFirebaseManager.generateConversationId(currentUserId, otherUserId);
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        ivUserAvatar = findViewById(R.id.iv_user_avatar);
        tvUserName = findViewById(R.id.tv_user_name);
        tvStatus = findViewById(R.id.tv_status);
        tvTyping = findViewById(R.id.tv_typing);
        btnVoiceCall = findViewById(R.id.btn_voice_call);
        btnVideoCall = findViewById(R.id.btn_video_call);
        btnAttachment = findViewById(R.id.btn_attachment);
        recyclerMessages = findViewById(R.id.recycler_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);

        tvUserName.setText(otherUserName);
        ImageUtils.loadCircularImage(this, otherUserAvatar, ivUserAvatar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(this, messages, currentUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        recyclerMessages.setLayoutManager(layoutManager);
        recyclerMessages.setAdapter(messageAdapter);
    }

    private void setupClickListeners() {
        btnSend.setOnClickListener(v -> sendMessage());

        btnVoiceCall.setOnClickListener(v -> initiateCall("voice"));

        btnVideoCall.setOnClickListener(v -> initiateCall("video"));

        btnAttachment.setOnClickListener(v ->
                Toast.makeText(this, "Attachment feature coming soon", Toast.LENGTH_SHORT).show()
        );

        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO: Send typing indicator to other user
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupMessageListener() {
        messageListener = firebaseManager.listenToMessages(conversationId,
                new JobLinkerFirebaseManager.ListCallback<Message>() {
                    @Override
                    public void onSuccess(List<Message> messageList) {
                        messages.clear();
                        messages.addAll(messageList);
                        messageAdapter.notifyDataSetChanged();

                        if (!messages.isEmpty()) {
                            recyclerMessages.scrollToPosition(messages.size() - 1);
                        }

                        markMessagesAsRead();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(ChatActivity.this,
                                "Error loading messages: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserStatus() {
        firebaseManager.getUser(otherUserId, new JobLinkerFirebaseManager.DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user.isOnline()) {
                    tvStatus.setText(R.string.online);
                    tvStatus.setTextColor(getResources().getColor(R.color.success, null));
                } else {
                    tvStatus.setText(R.string.offline);
                    tvStatus.setTextColor(getResources().getColor(R.color.text_secondary, null));
                }
            }

            @Override
            public void onFailure(String error) {
                // Ignore / handle error if needed
            }
        });
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();

        if (messageText.isEmpty()) {
            return;
        }

        Message message = new Message(conversationId, currentUserId, otherUserId, messageText);
        message.setMessageType("text");

        firebaseManager.sendMessage(message, new JobLinkerFirebaseManager.DataCallback<String>() {
            @Override
            public void onSuccess(String messageId) {
                etMessage.setText("");

                // TEMP: show it immediately (remove later after fixing listener)
                message.setMessageId(messageId);
                messages.add(message);
                messageAdapter.notifyItemInserted(messages.size() - 1);
                recyclerMessages.scrollToPosition(messages.size() - 1);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ChatActivity.this,
                        "Failed to send message: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markMessagesAsRead() {
        for (Message message : messages) {
            if (!message.isMessageRead()
                    && message.getMessageReceiverId() != null
                    && message.getMessageReceiverId().equals(currentUserId)) {

                firebaseManager.markMessageAsRead(message.getMessageId(),
                        new JobLinkerFirebaseManager.VoidCallback() {
                            @Override
                            public void onSuccess() {
                                // Message marked as read
                            }

                            @Override
                            public void onFailure(String error) {
                                // Ignore / handle error if needed
                            }
                        });
            }
        }
    }

    private void initiateCall(String callType) {
        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra(CallActivity.EXTRA_RECEIVER_ID, otherUserId);
        intent.putExtra(CallActivity.EXTRA_RECEIVER_NAME, otherUserName);
        intent.putExtra(CallActivity.EXTRA_RECEIVER_AVATAR, otherUserAvatar);
        intent.putExtra(CallActivity.EXTRA_CALL_TYPE, callType);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageListener != null) {
            messageListener.remove();
        }
    }
}