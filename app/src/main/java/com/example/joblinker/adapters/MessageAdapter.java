package com.example.joblinker.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.joblinker.R;
import com.example.joblinker.models.Message;
import com.example.joblinker.utils.DateTimeHelper;
import com.example.joblinker.utils.ImageUtils;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_SENT     = 1;
    private static final int VIEW_RECEIVED = 2;

    private final Context      context;
    private final List<Message> messages;
    private final String        currentUserId;

    public MessageAdapter(Context context, List<Message> messages, String currentUserId) {
        this.context       = context;
        this.messages      = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
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

    // ── Sent bubble ───────────────────────────────
    private void bindSent(SentHolder h, Message msg) {
        bindContent(h.tvMessage, h.ivImage, h.layoutMedia, h.tvMediaLabel, msg);
        h.tvTimestamp.setText(DateTimeHelper.formatTime(msg.getMessageTimestamp()));

        if (msg.isMessageRead()) {
            h.ivStatus.setImageResource(R.drawable.ic_check_double);
            h.ivStatus.setColorFilter(context.getResources().getColor(R.color.primary, null));
        } else {
            h.ivStatus.setImageResource(R.drawable.ic_check);
            h.ivStatus.setColorFilter(context.getResources().getColor(R.color.text_hint, null));
        }
    }

    // ── Received bubble ───────────────────────────
    private void bindReceived(ReceivedHolder h, Message msg) {
        ImageUtils.loadCircularImage(context, msg.getSenderAvatarUrl(), h.ivAvatar);
        bindContent(h.tvMessage, h.ivImage, h.layoutMedia, h.tvMediaLabel, msg);
        h.tvTimestamp.setText(DateTimeHelper.formatTime(msg.getMessageTimestamp()));
    }

    // ── Shared content binding ────────────────────
    private void bindContent(TextView tvMessage, ImageView ivImage,
                              View layoutMedia, TextView tvMediaLabel, Message msg) {

        String type = msg.getMessageType();
        if (type == null) type = "text";

        switch (type) {

            case "image":
                // Show inline image thumbnail
                tvMessage.setVisibility(View.GONE);
                if (ivImage != null) {
                    ivImage.setVisibility(View.VISIBLE);
                    Glide.with(context)
                        .load(msg.getImageUrl())
                        .placeholder(R.drawable.ic_photo)
                        .centerCrop()
                        .into(ivImage);
                    ivImage.setOnClickListener(v -> openUrl(msg.getImageUrl()));
                }
                if (layoutMedia != null) layoutMedia.setVisibility(View.GONE);
                break;

            case "audio":
                // Show audio player row
                tvMessage.setVisibility(View.GONE);
                if (ivImage != null) ivImage.setVisibility(View.GONE);
                if (layoutMedia != null) {
                    layoutMedia.setVisibility(View.VISIBLE);
                    if (tvMediaLabel != null) tvMediaLabel.setText(
                        msg.getMessageText() != null ? msg.getMessageText() : "🎤 Voice message");
                    layoutMedia.setOnClickListener(v -> openUrl(msg.getImageUrl()));
                }
                break;

            case "document":
                // Show document row
                tvMessage.setVisibility(View.GONE);
                if (ivImage != null) ivImage.setVisibility(View.GONE);
                if (layoutMedia != null) {
                    layoutMedia.setVisibility(View.VISIBLE);
                    if (tvMediaLabel != null) tvMediaLabel.setText(
                        msg.getMessageText() != null ? msg.getMessageText() : "📄 Document");
                    layoutMedia.setOnClickListener(v -> openUrl(msg.getImageUrl()));
                }
                break;

            case "location":
                tvMessage.setVisibility(View.VISIBLE);
                if (ivImage != null) ivImage.setVisibility(View.GONE);
                if (layoutMedia != null) layoutMedia.setVisibility(View.GONE);
                tvMessage.setText(msg.getMessageText());
                tvMessage.setOnClickListener(v -> {
                    // Extract URL from text and open Maps
                    String text = msg.getMessageText();
                    if (text != null && text.contains("http")) {
                        String url = text.substring(text.indexOf("http"));
                        openUrl(url);
                    }
                });
                break;

            default: // text, contact, etc.
                tvMessage.setVisibility(View.VISIBLE);
                if (ivImage != null) ivImage.setVisibility(View.GONE);
                if (layoutMedia != null) layoutMedia.setVisibility(View.GONE);
                tvMessage.setText(msg.getMessageText());
                tvMessage.setOnClickListener(null);
                break;
        }
    }

    private void openUrl(String url) {
        if (url == null || url.isEmpty()) return;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception ignored) {}
    }

    @Override public int getItemCount() { return messages.size(); }

    // ── ViewHolders ───────────────────────────────

    static class SentHolder extends RecyclerView.ViewHolder {
        TextView  tvMessage, tvTimestamp, tvMediaLabel;
        ImageView ivStatus, ivImage;
        View      layoutMedia;

        SentHolder(@NonNull View v) {
            super(v);
            tvMessage   = v.findViewById(R.id.tv_message);
            tvTimestamp = v.findViewById(R.id.tv_timestamp);
            ivStatus    = v.findViewById(R.id.iv_status);
            ivImage     = v.findViewById(R.id.iv_image);
            layoutMedia = v.findViewById(R.id.layout_media);
            tvMediaLabel = v.findViewById(R.id.tv_media_label);
        }
    }

    static class ReceivedHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar, ivImage;
        TextView  tvMessage, tvTimestamp, tvMediaLabel;
        View      layoutMedia;

        ReceivedHolder(@NonNull View v) {
            super(v);
            ivAvatar    = v.findViewById(R.id.iv_avatar);
            tvMessage   = v.findViewById(R.id.tv_message);
            tvTimestamp = v.findViewById(R.id.tv_timestamp);
            ivImage     = v.findViewById(R.id.iv_image);
            layoutMedia = v.findViewById(R.id.layout_media);
            tvMediaLabel = v.findViewById(R.id.tv_media_label);
        }
    }
}
