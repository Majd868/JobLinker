package com.joblinker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joblinker.R;
import com.joblinker.models.Message;
import com.example.joblinker.utils.DateTimeHelper;
import com.example.joblinker.utils.ImageUtils;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private Context context;
    private List<Message> messages;
    private String currentUserId;

    public MessageAdapter(Context context, List<Message> messages, String currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.getMessageSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder instanceof SentMessageViewHolder) {
            SentMessageViewHolder sentHolder = (SentMessageViewHolder) holder;
            sentHolder.tvMessage.setText(message.getMessageText());
            sentHolder.tvTimestamp.setText(DateTimeHelper.formatTime(message.getMessageTimestamp()));

            // Set status icon
            if (message.isMessageRead()) {
                sentHolder.ivStatus.setImageResource(R.drawable.ic_check_double);
                sentHolder.ivStatus.setColorFilter(context.getResources().getColor(R.color.primary, null));
            } else {
                sentHolder.ivStatus.setImageResource(R.drawable.ic_check);
                sentHolder.ivStatus.setColorFilter(context.getResources().getColor(R.color.text_hint, null));
            }

        } else if (holder instanceof ReceivedMessageViewHolder) {
            ReceivedMessageViewHolder receivedHolder = (ReceivedMessageViewHolder) holder;
            receivedHolder.tvMessage.setText(message.getMessageText());
            receivedHolder.tvTimestamp.setText(DateTimeHelper.formatTime(message.getMessageTimestamp()));

            // Load sender avatar
            ImageUtils.loadCircularImage(context, message.getSenderAvatarUrl(), receivedHolder.ivAvatar);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTimestamp;
        ImageView ivStatus;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            ivStatus = itemView.findViewById(R.id.iv_status);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvMessage, tvTimestamp;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
        }
    }
}