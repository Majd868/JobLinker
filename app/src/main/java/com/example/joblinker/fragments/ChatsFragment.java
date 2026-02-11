package com.example.joblinker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.ListenerRegistration;

import com.example.joblinker.R;
import com.example.joblinker.activities.ChatActivity;
import com.example.joblinker.adapters.ConversationAdapter;
import com.example.joblinker.firebase.JobLinkerFirebaseManager;
import com.joblinker.models.Conversation;
import com.example.joblinker.models.User;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment implements ConversationAdapter.OnConversationClickListener {

    private RecyclerView recyclerConversations;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;

    private ConversationAdapter conversationAdapter;
    private List<Conversation> conversations;
    private JobLinkerFirebaseManager firebaseManager;
    private ListenerRegistration conversationsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        firebaseManager = JobLinkerFirebaseManager.getInstance();
        conversations = new ArrayList<>();

        initializeViews(view);
        setupRecyclerView();
        loadConversations();

        return view;
    }

    private void initializeViews(View view) {
        recyclerConversations = view.findViewById(R.id.recycler_conversations);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupRecyclerView() {
        conversationAdapter = new ConversationAdapter(requireContext(), conversations, this);
        recyclerConversations.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerConversations.setAdapter(conversationAdapter);
    }

    private void loadConversations() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        String userId = firebaseManager.getCurrentUserId();

        conversationsListener = firebaseManager.listenToConversations(userId,
                new JobLinkerFirebaseManager.ListCallback<Conversation>() {
                    @Override
                    public void onSuccess(List<Conversation> conversationList) {
                        progressBar.setVisibility(View.GONE);
                        conversations.clear();

                        // Load other user details for each conversation
                        for (Conversation conversation : conversationList) {
                            loadOtherUserDetails(conversation);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        progressBar.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                        Toast.makeText(requireContext(),
                                "Error loading conversations: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadOtherUserDetails(Conversation conversation) {
        String currentUserId = firebaseManager.getCurrentUserId();
        String otherUserId = null;

        // Find the other user ID
        for (String participantId : conversation.getParticipants()) {
            if (!participantId.equals(currentUserId)) {
                otherUserId = participantId;
                break;
            }
        }

        if (otherUserId != null) {
            String finalOtherUserId = otherUserId;
            firebaseManager.getUser(otherUserId, new JobLinkerFirebaseManager.DataCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    conversation.setOtherUserName(user.getUserName());
                    conversation.setOtherUserAvatarUrl(user.getAvatarUrl());
                    conversation.setOtherUserOnline(user.isOnline());

                    // Add to list and update UI
                    if (!conversations.contains(conversation)) {
                        conversations.add(conversation);
                        conversationAdapter.notifyDataSetChanged();
                    }

                    updateEmptyState();
                }

                @Override
                public void onFailure(String error) {
                    // Handle error
                }
            });
        }
    }

    private void updateEmptyState() {
        if (conversations.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerConversations.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerConversations.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onConversationClick(Conversation conversation) {
        // Get other user ID
        String currentUserId = firebaseManager.getCurrentUserId();
        String otherUserId = null;

        for (String participantId : conversation.getParticipants()) {
            if (!participantId.equals(currentUserId)) {
                otherUserId = participantId;
                break;
            }
        }

        if (otherUserId != null) {
            Intent intent = new Intent(requireContext(), ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_USER_ID, otherUserId);
            intent.putExtra(ChatActivity.EXTRA_USER_NAME, conversation.getOtherUserName());
            intent.putExtra(ChatActivity.EXTRA_USER_AVATAR, conversation.getOtherUserAvatarUrl());
            intent.putExtra(ChatActivity.EXTRA_CONVERSATION_ID, conversation.getConversationId());
            startActivity(intent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (conversationsListener != null) {
            conversationsListener.remove();
        }
    }
}