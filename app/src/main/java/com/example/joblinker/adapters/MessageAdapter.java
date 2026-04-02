package com.example.joblinker.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.joblinker.R;
import com.example.joblinker.activities.ChatActivity;
import com.example.joblinker.models.Message;
import com.example.joblinker.utils.DateTimeHelper;
import com.example.joblinker.utils.ImageUtils;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_SENT     = 1;
    private static final int VIEW_RECEIVED = 2;

    private final Context       context;
    private final List<Message> messages;
    private final String        currentUserId;

    // Track which URL is currently "playing" to show correct icon
    private String currentlyPlayingUrl = null;

    public MessageAdapter(Context context, List<Message> messages, String currentUserId) {
        this.context       = context;
        this.messages      = messages;
        this.currentUserId = currentUserId;
    }

    @Override public int getItemViewType(int position) {
        return messages.get(position).getMessageSenderId().equals(currentUserId)
            ? VIEW_SENT : VIEW_RECEIVED;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(context);
        if (viewType == VIEW_SENT)
            return new SentHolder(inf.inflate(R.layout.item_message_sent, parent, false));
        return new ReceivedHolder(inf.inflate(R.layout.item_message_received, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);
        if (holder instanceof SentHolder)     bindSent    ((SentHolder)     holder, msg);
        else                                   bindReceived((ReceivedHolder) holder, msg);
    }

    private void bindSent(SentHolder h, Message msg) {
        bindContent(h.tvMessage, h.ivImage, h.layoutMedia, h.tvMediaLabel, msg);
        h.tvTimestamp.setText(DateTimeHelper.formatTime(msg.getMessageTimestamp()));
        if (h.ivStatus != null)
            h.ivStatus.setImageResource(msg.isMessageRead()
                ? R.drawable.ic_check_double : R.drawable.ic_check);
    }

    private void bindReceived(ReceivedHolder h, Message msg) {
        ImageUtils.loadCircularImageCached(context, msg.getSenderAvatarUrl(), h.ivAvatar);
        bindContent(h.tvMessage, h.ivImage, h.layoutMedia, h.tvMediaLabel, msg);
        h.tvTimestamp.setText(DateTimeHelper.formatTime(msg.getMessageTimestamp()));
    }

    private void bindContent(TextView tvMessage, ImageView ivImage,
                              View layoutMedia, TextView tvMediaLabel, Message msg) {
        String type = msg.getMessageType();
        if (type == null) type = "text";

        switch (type) {

            case "image":
                tvMessage.setVisibility(View.GONE);
                if (layoutMedia != null) layoutMedia.setVisibility(View.GONE);
                if (ivImage != null) {
                    ivImage.setVisibility(View.VISIBLE);
                    Glide.with(context).load(msg.getImageUrl())
                        .placeholder(R.drawable.ic_photo).centerCrop().into(ivImage);
                    ivImage.setOnClickListener(v -> openUrl(msg.getImageUrl()));
                }
                break;

            case "audio":
                tvMessage.setVisibility(View.GONE);
                if (ivImage != null) ivImage.setVisibility(View.GONE);
                if (layoutMedia != null) {
                    layoutMedia.setVisibility(View.VISIBLE);
                    String baseLabel = msg.getMessageText() != null
                        ? msg.getMessageText() : "🎤 Voice message";

                    // Show play/stop state
                    if (tvMediaLabel != null) {
                        boolean isPlaying = baseLabel.equals(currentlyPlayingUrl)
                            || (msg.getImageUrl() != null
                                && msg.getImageUrl().equals(currentlyPlayingUrl));
                        tvMediaLabel.setText(isPlaying ? "⏹ " + baseLabel : "▶ " + baseLabel);
                    }

                    layoutMedia.setOnClickListener(v -> {
                        if (!(context instanceof ChatActivity)) return;
                        ChatActivity activity = (ChatActivity) context;
                        final String audioUrl = msg.getImageUrl();
                        final String label    = msg.getMessageText() != null
                            ? msg.getMessageText() : "🎤 Voice message";

                        if (tvMediaLabel != null) tvMediaLabel.setText("⏳ Loading…");

                        activity.playVoiceMessage(audioUrl,
                            () -> { // onStart
                                currentlyPlayingUrl = audioUrl;
                                if (tvMediaLabel != null)
                                    tvMediaLabel.setText("⏹ " + label);
                            },
                            () -> { // onStop
                                currentlyPlayingUrl = null;
                                if (tvMediaLabel != null)
                                    tvMediaLabel.setText("▶ " + label);
                            }
                        );
                    });
                }
                break;

            case "document":
                tvMessage.setVisibility(View.GONE);
                if (ivImage != null) ivImage.setVisibility(View.GONE);
                if (layoutMedia != null) {
                    layoutMedia.setVisibility(View.VISIBLE);
                    if (tvMediaLabel != null)
                        tvMediaLabel.setText(msg.getMessageText() != null
                            ? msg.getMessageText() : "📄 Document");
                    layoutMedia.setOnClickListener(v -> openUrl(msg.getImageUrl()));
                }
                break;

            case "location":
                tvMessage.setVisibility(View.VISIBLE);
                if (ivImage     != null) ivImage.setVisibility(View.GONE);
                if (layoutMedia != null) layoutMedia.setVisibility(View.GONE);
                tvMessage.setText(msg.getMessageText());
                tvMessage.setOnClickListener(v -> {
                    String text = msg.getMessageText();
                    if (text != null && text.contains("http"))
                        openUrl(text.substring(text.indexOf("http")));
                });
                break;

            default:
                tvMessage.setVisibility(View.VISIBLE);
                if (ivImage     != null) ivImage.setVisibility(View.GONE);
                if (layoutMedia != null) layoutMedia.setVisibility(View.GONE);
                tvMessage.setText(msg.getMessageText());
                tvMessage.setOnClickListener(null);
                break;
        }
    }

    private void openUrl(String url) {
        if (url == null || url.isEmpty()) return;
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception ignored) {}
    }

    @Override public int getItemCount() { return messages.size(); }

    static class SentHolder extends RecyclerView.ViewHolder {
        TextView  tvMessage, tvTimestamp, tvMediaLabel;
        ImageView ivStatus, ivImage;
        View      layoutMedia;
        SentHolder(@NonNull View v) {
            super(v);
            tvMessage    = v.findViewById(R.id.tv_message);
            tvTimestamp  = v.findViewById(R.id.tv_timestamp);
            ivStatus     = v.findViewById(R.id.iv_status);
            ivImage      = v.findViewById(R.id.iv_image);
            layoutMedia  = v.findViewById(R.id.layout_media);
            tvMediaLabel = v.findViewById(R.id.tv_media_label);
        }
    }

    static class ReceivedHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar, ivImage;
        TextView  tvMessage, tvTimestamp, tvMediaLabel;
        View      layoutMedia;
        ReceivedHolder(@NonNull View v) {
            super(v);
            ivAvatar     = v.findViewById(R.id.iv_avatar);
            tvMessage    = v.findViewById(R.id.tv_message);
            tvTimestamp  = v.findViewById(R.id.tv_timestamp);
            ivImage      = v.findViewById(R.id.iv_image);
            layoutMedia  = v.findViewById(R.id.layout_media);
            tvMediaLabel = v.findViewById(R.id.tv_media_label);
        }
    }
}
