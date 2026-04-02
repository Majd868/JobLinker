package com.example.joblinker.adapters;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    private final Context       context;
    private final List<Message> messages;
    private final String        currentUserId;

    // Track current playing audio to stop it when another is tapped
    private MediaPlayer currentPlayer = null;
    private View        currentPlayingView = null;

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

    private void bindSent(SentHolder h, Message msg) {
        bindContent(h.tvMessage, h.ivImage, h.layoutMedia, h.tvMediaLabel, msg);
        h.tvTimestamp.setText(DateTimeHelper.formatTime(msg.getMessageTimestamp()));
        if (h.ivStatus != null) {
            h.ivStatus.setImageResource(msg.isMessageRead()
                ? R.drawable.ic_check_double : R.drawable.ic_check);
        }
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
                    Glide.with(context)
                        .load(msg.getImageUrl())
                        .placeholder(R.drawable.ic_photo)
                        .centerCrop()
                        .into(ivImage);
                    ivImage.setOnClickListener(v -> openInBrowser(msg.getImageUrl()));
                }
                break;

            case "audio":
                tvMessage.setVisibility(View.GONE);
                if (ivImage != null) ivImage.setVisibility(View.GONE);
                if (layoutMedia != null) {
                    layoutMedia.setVisibility(View.VISIBLE);
                    if (tvMediaLabel != null) {
                        tvMediaLabel.setText(msg.getMessageText() != null
                            ? msg.getMessageText() : "🎤 Voice message");
                    }
                    // Tap to play/stop audio inline with MediaPlayer
                    layoutMedia.setOnClickListener(v -> playAudio(msg.getImageUrl(), layoutMedia, tvMediaLabel));
                }
                break;

            case "document":
                tvMessage.setVisibility(View.GONE);
                if (ivImage != null) ivImage.setVisibility(View.GONE);
                if (layoutMedia != null) {
                    layoutMedia.setVisibility(View.VISIBLE);
                    if (tvMediaLabel != null) {
                        tvMediaLabel.setText(msg.getMessageText() != null
                            ? msg.getMessageText() : "📄 Document");
                    }
                    layoutMedia.setOnClickListener(v -> openInBrowser(msg.getImageUrl()));
                }
                break;

            case "location":
                tvMessage.setVisibility(View.VISIBLE);
                if (ivImage  != null) ivImage.setVisibility(View.GONE);
                if (layoutMedia != null) layoutMedia.setVisibility(View.GONE);
                tvMessage.setText(msg.getMessageText());
                tvMessage.setOnClickListener(v -> {
                    String text = msg.getMessageText();
                    if (text != null && text.contains("http"))
                        openInBrowser(text.substring(text.indexOf("http")));
                });
                break;

            default: // text
                tvMessage.setVisibility(View.VISIBLE);
                if (ivImage  != null) ivImage.setVisibility(View.GONE);
                if (layoutMedia != null) layoutMedia.setVisibility(View.GONE);
                tvMessage.setText(msg.getMessageText());
                tvMessage.setOnClickListener(null);
                break;
        }
    }

    // ── Audio playback ────────────────────────────
    private void playAudio(String url, View layoutView, TextView label) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(context, "Audio not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // If this row is already playing → stop it
        if (currentPlayingView == layoutView && currentPlayer != null) {
            stopCurrentPlayer();
            if (label != null) label.setText(
                label.getText().toString().replace("⏹ ", ""));
            return;
        }

        // Stop any other playing audio first
        stopCurrentPlayer();

        final String originalLabel = label != null
            ? label.getText().toString() : "🎤 Voice message";
        if (label != null) label.setText("⏳ Loading…");

        // Keep strong reference — prevents garbage collection during prepareAsync
        currentPlayer      = new MediaPlayer();
        currentPlayingView = layoutView;
        final MediaPlayer player = currentPlayer;

        try {
            player.setDataSource(url);

            // Use AudioAttributes instead of deprecated setAudioStreamType()
            player.setAudioAttributes(
                new android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .build()
            );

            player.setOnPreparedListener(mp -> {
                if (label != null) label.setText("⏹ " + originalLabel);
                mp.start();
            });

            player.setOnCompletionListener(mp -> {
                if (label != null) label.setText(originalLabel);
                currentPlayer      = null;
                currentPlayingView = null;
                mp.release();
            });

            player.setOnErrorListener((mp, what, extra) -> {
                if (label != null) label.setText(originalLabel);
                Toast.makeText(context,
                    "Cannot play audio (err " + what + ")", Toast.LENGTH_SHORT).show();
                currentPlayer      = null;
                currentPlayingView = null;
                mp.release();
                return true;
            });

            player.prepareAsync(); // starts loading from URL

        } catch (Exception e) {
            if (label != null) label.setText(originalLabel);
            Toast.makeText(context,
                "Cannot play audio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            currentPlayer      = null;
            currentPlayingView = null;
            try { player.release(); } catch (Exception ignored) {}
        }
    }

    private void stopCurrentPlayer() {
        if (currentPlayer != null) {
            try {
                if (currentPlayer.isPlaying()) currentPlayer.stop();
                currentPlayer.release();
            } catch (Exception ignored) {}
            currentPlayer      = null;
            currentPlayingView = null;
        }
    }

    // ── Open URL in browser ───────────────────────
    private void openInBrowser(String url) {
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
