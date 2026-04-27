package com.example.gossipapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT     = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final Context            context;
    private final ArrayList<ChatModel> messageList;
    private final String             currentUserEmail;
    private final boolean            isOneToOne;
    private OnMessageLongClickListener longClickListener;

    public ChatAdapter(Context context,
                       ArrayList<ChatModel> messageList,
                       String currentUserEmail,
                       boolean isOneToOne) {
        this.context           = context;
        this.messageList       = messageList;
        this.currentUserEmail  = currentUserEmail;
        this.isOneToOne        = isOneToOne;
    }

    public void setOnMessageLongClickListener(OnMessageLongClickListener listener) {
        this.longClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        ChatModel model = messageList.get(position);
        return TextUtils.equals(currentUserEmail, model.getSenderEmail())
                ? VIEW_TYPE_SENT
                : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        int layoutRes = (viewType == VIEW_TYPE_SENT)
                ? R.layout.item_message_sent
                : R.layout.item_message_received;

        View view = LayoutInflater.from(context)
                .inflate(layoutRes, parent, false);

        return (viewType == VIEW_TYPE_SENT)
                ? new SentViewHolder(view)
                : new ReceivedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,
            int position
    ) {
        ChatModel model = messageList.get(position);
        String time = new SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
        ).format(new Date(model.getTimestamp()));

        if (holder instanceof SentViewHolder) {
            bindCommon((SentViewHolder) holder, model, time, position);
        } else {
            bindCommon((ReceivedViewHolder) holder, model, time, position);
        }
    }

    private void bindCommon(
            RecyclerView.ViewHolder vh,
            ChatModel model,
            String time,
            int position
    ) {
        View itemView       = vh.itemView;
        TextView msgTv      = itemView.findViewById(R.id.text_message);
        TextView timeTv     = itemView.findViewById(R.id.text_time);
        ImageView imgIv     = itemView.findViewById(R.id.image_message);
        TextView statusTv   = (vh instanceof SentViewHolder)
                ? itemView.findViewById(R.id.sentText)
                : null;
        TextView usernameTv = (vh instanceof ReceivedViewHolder)
                ? itemView.findViewById(R.id.usernameLeft)
                : null;
        TextView reactionTv = itemView.findViewById(R.id.reaction_view);

        // 1) Set text + time
        msgTv.setText(model.getMessageText());
        timeTv.setText(time);

        // 2) If this is a “sent” message, show status; otherwise hide
        if (statusTv != null) {
            setStatus(statusTv, model.getStatus());
        }

        // 3) If “received” in a group chat, show username
        if (usernameTv != null) {
            if (isOneToOne) {
                usernameTv.setVisibility(View.GONE);
            } else {
                usernameTv.setVisibility(View.VISIBLE);
                usernameTv.setText(
                        model.getSenderEmail().split("@")[0]
                );
            }
        }

        // 4) Image vs. text:
        if (!TextUtils.isEmpty(model.getImageBase64())) {
            imgIv.setVisibility(View.VISIBLE);
            msgTv.setVisibility(View.GONE);
            try {
                byte[] data = Base64.decode(
                        model.getImageBase64(),
                        Base64.DEFAULT
                );
                final Bitmap decodedBitmap = BitmapFactory
                        .decodeByteArray(data, 0, data.length);
                imgIv.setImageBitmap(decodedBitmap);

                // → Show full-screen on click
                imgIv.setOnClickListener(v ->
                        showImagePreview(decodedBitmap)
                );
            } catch (Exception e) {
                imgIv.setVisibility(View.GONE);
                msgTv.setVisibility(View.VISIBLE);
            }
        } else {
            imgIv.setVisibility(View.GONE);
            msgTv.setVisibility(View.VISIBLE);
        }

        // 5) Reaction emoji (if any)
        if (!TextUtils.isEmpty(model.getReaction())) {
            reactionTv.setText(model.getReaction());
            reactionTv.setVisibility(View.VISIBLE);
        } else {
            reactionTv.setVisibility(View.GONE);
        }

        // 6) Long-press → show reaction picker + delete:
        itemView.setOnLongClickListener(v -> {
            showReactionsWithDelete(model, position);
            return true;
        });

        // 7) Double-click → toggle “🫶🏼” reaction and refresh just this row:
        itemView.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onDoubleClick(View v) {
                // Animation
                TextView tapEmoji = itemView.findViewById(R.id.reaction_view);
                showTapAnimation(tapEmoji);

                String sender   = model.getSenderEmail().replace(".", "_");
                String receiver = model.getReceiverEmail().replace(".", "_");
                String chatId   = (sender.compareTo(receiver) < 0)
                        ? sender + "_" + receiver
                        : receiver + "_" + sender;

                DatabaseReference msgRef = FirebaseDatabase
                        .getInstance()
                        .getReference("chats")
                        .child(chatId)
                        .child("messages")
                        .child(model.getId());

                if ("🫶🏼".equals(model.getReaction())) {
                    // Remove reaction
                    msgRef.child("reaction").removeValue()
                            .addOnSuccessListener(unused -> {
                                model.setReaction(null);
                                // ONLY this item rebinds:
                                notifyItemChanged(position);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            context,
                                            "Failed to remove reaction",
                                            Toast.LENGTH_SHORT
                                    ).show()
                            );
                } else {
                    // Add reaction
                    msgRef.child("reaction").setValue("🫶🏼")
                            .addOnSuccessListener(unused -> {
                                model.setReaction("🫶🏼");
                                // ONLY this item rebinds:
                                notifyItemChanged(position);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            context,
                                            "Failed to save reaction",
                                            Toast.LENGTH_SHORT
                                    ).show()
                            );
                }
            }
        });
    }

    private void setStatus(TextView tv, String status) {
        if (TextUtils.isEmpty(status)) {
            tv.setVisibility(View.GONE);
            return;
        }
        tv.setVisibility(View.VISIBLE);
        switch (status) {
            case "sent":
                tv.setText("Sent");
                break;
            case "delivered":
                tv.setText("Delivered");
                break;
            case "seen":
                tv.setText("● Seen");
                break;
            default:
                tv.setVisibility(View.GONE);
        }
    }

    private void showReactionsWithDelete(ChatModel model, int position) {
        View reactLayout = LayoutInflater.from(context)
                .inflate(R.layout.dialog_reactions, null);
        View delLayout = LayoutInflater.from(context)
                .inflate(R.layout.dialog_message_options, null);
        ((ViewGroup) reactLayout).addView(delLayout);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(reactLayout)
                .setCancelable(true)
                .create();

        RecyclerView rv = reactLayout.findViewById(R.id.reactionRecyclerView);
        rv.setLayoutManager(
                new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        );

        List<String> emojis = Arrays.asList("👍", "❤️", "😂", "😮", "😢");
        new ReactionAdapter(emojis, emoji -> {
            // Save chosen emoji in Firebase:
            String sender   = model.getSenderEmail().replace(".", "_");
            String receiver = model.getReceiverEmail().replace(".", "_");
            String chatId   = (sender.compareTo(receiver) < 0)
                    ? sender + "_" + receiver
                    : receiver + "_" + sender;

            DatabaseReference msgRef = FirebaseDatabase
                    .getInstance()
                    .getReference("chats")
                    .child(chatId)
                    .child("messages")
                    .child(model.getId());

            msgRef.child("reaction").setValue(emoji)
                    .addOnSuccessListener(unused -> {
                        model.setReaction(emoji);
                        // Only refresh this item:
                        notifyItemChanged(position);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(
                                    context,
                                    "Failed to save reaction",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
            dialog.dismiss();
        }).attachTo(rv);

        delLayout.findViewById(R.id.deleteForMe).setOnClickListener(v -> {
            if (longClickListener != null) longClickListener.onDeleteForMe(model);
            dialog.dismiss();
        });

        delLayout.findViewById(R.id.deleteForEveryone).setOnClickListener(v -> {
            if (longClickListener != null) longClickListener.onDeleteForEveryone(model);
            dialog.dismiss();
        });

        dialog.show();
    }

    public void replaceAll(List<ChatModel> newList) {
        messageList.clear();
        messageList.addAll(newList);
        notifyDataSetChanged();
    }

    /**
     * If you want to update only the status or reaction of one message:
     *   adapter.onMessageUpdated(updatedModel);
     */
    public void onMessageUpdated(ChatModel updatedMsg) {
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).getId().equals(updatedMsg.getId())) {
                messageList.set(i, updatedMsg);
                notifyItemChanged(i);
                return;
            }
        }
    }

    public void updateMessageStatus(String messageId, String newStatus) {
        for (int i = 0; i < messageList.size(); i++) {
            ChatModel m = messageList.get(i);
            if (TextUtils.equals(m.getId(), messageId)) {
                m.setStatus(newStatus);
                notifyItemChanged(i);
                return;
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public interface OnMessageLongClickListener {
        void onDeleteForMe(ChatModel msg);
        void onDeleteForEveryone(ChatModel msg);
    }

    private class ReactionAdapter extends com.example.gossipapp.ReactionAdapter {
        public ReactionAdapter(List<String> list, OnReactionClickListener l) {
            super(list, l);
        }
        public void attachTo(RecyclerView rv) {
            rv.setAdapter(this);
        }
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        SentViewHolder(@NonNull View v) { super(v); }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        ReceivedViewHolder(@NonNull View v) { super(v); }
    }

    abstract class DoubleClickListener implements View.OnClickListener {
        private static final long DOUBLE_CLICK_TIME_DELTA = 300; // ms
        long lastClickTime = 0;

        @Override
        public void onClick(View v) {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                onDoubleClick(v);
            }
            lastClickTime = clickTime;
        }

        public abstract void onDoubleClick(View v);
    }

    private void showTapAnimation(TextView emojiView) {
        emojiView.setVisibility(View.VISIBLE);
        emojiView.setScaleX(0f);
        emojiView.setScaleY(0f);
        emojiView.setAlpha(0f);

        emojiView.animate()
                .scaleX(1.5f)
                .scaleY(1.5f)
                .alpha(1f)
                .setDuration(200)
                .withEndAction(() -> emojiView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(() -> emojiView.setVisibility(View.GONE))
                        .start())
                .start();
    }

    private void showImagePreview(Bitmap bitmap) {
        ImageView fullImageView = new ImageView(context);
        fullImageView.setImageBitmap(bitmap);
        fullImageView.setAdjustViewBounds(true);
        fullImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        AlertDialog.Builder builder = new AlertDialog.Builder(
                context,
                android.R.style.Theme_Black_NoTitleBar_Fullscreen
        );
        builder.setView(fullImageView);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

        fullImageView.setOnClickListener(v -> dialog.dismiss());
    }
}
