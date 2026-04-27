package com.example.gossipapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final List<ChatModel> messageList;
    private final String currentUserEmail;

    public MessageAdapter(List<ChatModel> messageList, String currentUserEmail) {
        this.messageList = messageList;
        this.currentUserEmail = currentUserEmail;
    }

    @Override
    public int getItemViewType(int position) {
        ChatModel chat = messageList.get(position);
        if (chat.getSenderEmail() != null && chat.getSenderEmail().equals(currentUserEmail)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.group_item_message_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.group_item_message_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatModel chat = messageList.get(position);
        String text = chat.getMessageText();
        String senderName = chat.getSenderName();
        String timeText = "";
        if (chat.getTimestamp() > 0) {
            long millis = chat.getTimestamp();
            timeText = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(millis));
        }

        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).bind(text, timeText);
        } else if (holder instanceof ReceivedViewHolder) {
            ((ReceivedViewHolder) holder).bind(text, timeText, senderName);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;
        private final TextView messageTime;

        SentViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textViewSentMessage);
            messageTime = itemView.findViewById(R.id.textViewSentTime);
        }

        void bind(String text, String time) {
            messageText.setText(text);
            messageTime.setText(time);
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;
        private final TextView messageTime;
        private final TextView senderName;

        ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textViewReceivedMessage);
            messageTime = itemView.findViewById(R.id.textViewReceivedTime);
            senderName = itemView.findViewById(R.id.textViewSenderName);
        }

        void bind(String text, String time, String name) {
            messageText.setText(text);
            messageTime.setText(time);
            senderName.setText(name != null ? name : "");
        }
    }
}
