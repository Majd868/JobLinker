package com.example.joblinker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.example.joblinker.models.Conversation;
import com.example.joblinker.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChatsFragment extends Fragment
        implements ConversationAdapter.OnConversationClickListener {

    private RecyclerView recyclerConversations;
    private LinearLayout layoutEmpty;
    private ProgressBar  progressBar;
    private EditText     etSearch;

    private ConversationAdapter conversationAdapter;
    private final List<Conversation> allConversations = new ArrayList<>();
    private final List<Conversation> filtered         = new ArrayList<>();

    private JobLinkerFirebaseManager firebaseManager;
    private ListenerRegistration     conversationsListener;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        firebaseManager = JobLinkerFirebaseManager.getInstance();

        recyclerConversations = view.findViewById(R.id.recycler_conversations);
        layoutEmpty           = view.findViewById(R.id.layout_empty);
        progressBar           = view.findViewById(R.id.progress_bar);
        etSearch              = view.findViewById(R.id.et_search_chats);

        setupRecyclerView();
        setupSearch();
        loadConversations();

        return view;
    }

    private void setupRecyclerView() {
        conversationAdapter = new ConversationAdapter(requireContext(), filtered, this);
        recyclerConversations.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerConversations.setAdapter(conversationAdapter);
    }

    private void setupSearch() {
        if (etSearch == null) return;
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int b, int count) {
                filterConversations(s.toString().trim());
            }
        });
    }

    private void filterConversations(String query) {
        filtered.clear();
        if (query.isEmpty()) {
            filtered.addAll(allConversations);
        } else {
            String lower = query.toLowerCase();
            for (Conversation c : allConversations) {
                String name = c.getOtherUserName();
                String last = c.getLastMessage();
                if ((name != null && name.toLowerCase().contains(lower))
                 || (last != null && last.toLowerCase().contains(lower))) {
                    filtered.add(c);
                }
            }
        }
        conversationAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void loadConversations() {
        String userId = firebaseManager.getCurrentUserId();
        if (userId == null) return;

        progressBar.setVisibility(View.VISIBLE);

        // Real-time listener — updates the list whenever Firestore changes
        conversationsListener = firebaseManager.listenToConversations(userId,
            new JobLinkerFirebaseManager.ListCallback<Conversation>() {
                @Override
                public void onSuccess(List<Conversation> list) {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);
                    allConversations.clear();
                    for (Conversation c : list) loadOtherUserDetails(c);
                }

                @Override
                public void onFailure(String error) {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(),
                        "Error loading conversations: " + error, Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void loadOtherUserDetails(Conversation conversation) {
        String currentUserId = firebaseManager.getCurrentUserId();
        String otherUserId   = null;
        for (String id : conversation.getParticipants()) {
            if (!id.equals(currentUserId)) { otherUserId = id; break; }
        }
        if (otherUserId == null) return;

        firebaseManager.getUser(otherUserId, new JobLinkerFirebaseManager.DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (!isAdded()) return;
                conversation.setOtherUserName(user.getUserName());
                conversation.setOtherUserAvatarUrl(user.getAvatarUrl());
                conversation.setOtherUserOnline(user.isOnline());

                if (!allConversations.contains(conversation)) {
                    allConversations.add(conversation);
                }
                // Refresh filtered list
                String query = etSearch != null
                    ? etSearch.getText().toString().trim() : "";
                filterConversations(query);
            }

            @Override public void onFailure(String error) {}
        });
    }

    private void updateEmptyState() {
        if (!isAdded()) return;
        boolean empty = filtered.isEmpty();
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerConversations.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onConversationClick(Conversation conversation) {
        String currentUserId = firebaseManager.getCurrentUserId();
        String otherUserId   = null;
        for (String id : conversation.getParticipants()) {
            if (!id.equals(currentUserId)) { otherUserId = id; break; }
        }
        if (otherUserId == null) return;

        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_USER_ID,         otherUserId);
        intent.putExtra(ChatActivity.EXTRA_USER_NAME,       conversation.getOtherUserName());
        intent.putExtra(ChatActivity.EXTRA_USER_AVATAR,     conversation.getOtherUserAvatarUrl());
        intent.putExtra(ChatActivity.EXTRA_CONVERSATION_ID, conversation.getConversationId());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (conversationsListener != null) conversationsListener.remove();
    }
}
