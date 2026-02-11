package com.example.joblinker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joblinker.R;
import com.joblinker.models.Conversation;
import com.example.joblinker.utils.DateTimeHelper;
import com.example.joblinker.utils.ImageUtils;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private Context context;
    private List<Conversation> conversations;
    private OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    public ConversationAdapter(Context context, List<Conversation> conversations,
                               OnConversationClickListener listener) {
        this.context = context;
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);

        // Avatar
        ImageUtils.loadCircularImage(context, conversation.getOtherUserAvatarUrl(), holder.ivAvatar);

        // User info
        holder.tvName.setText(conversation.getOtherUserName());
        holder.tvLastMessage.setText(conversation.getLastMessage());
        holder.tvTimestamp.setText(DateTimeHelper.getMessageTimestamp(conversation.getLastMessageTime()));

        // Online status
        holder.viewOnline.setVisibility(conversation.isOtherUserOnline() ? View.VISIBLE : View.GONE);

        // Unread count
        if (conversation.hasUnreadMessages()) {
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
            holder.tvUnreadCount.setText(String.valueOf(conversation.getUnreadCount()));
        } else {
            holder.tvUnreadCount.setVisibility(View.GONE);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConversationClick(conversation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        View viewOnline;
        TextView tvName, tvLastMessage, tvTimestamp, tvUnreadCount;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            viewOnline = itemView.findViewById(R.id.view_online);
            tvName = itemView.findViewById(R.id.tv_name);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            tvUnreadCount = itemView.findViewById(R.id.tv_unread_count);
        }
    }
}