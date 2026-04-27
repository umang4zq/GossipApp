package com.example.gossipapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.firebase.firestore.EventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupChatActivity extends AppCompatActivity {

    private String chatDocumentId;         // “chatId” passed from CreateGroupActivity
    private RecyclerView recyclerViewChat;
    private EditText editTextMessage;
    private Button buttonSend;
    private ImageView imageViewGroupAvatar; // optional: display group avatar in toolbar/header
    private EditText textViewGroupName;     // optional: display group name in toolbar/header

    private final List<ChatModel> messageList = new ArrayList<>();
    private MessageAdapter messageAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Button imageviewAttachment;   // preview of attached image
    private String attachedImageBase64 = null;
    private View replyBar;                   // a layout containing reply preview
    private TextView replyBarMessage;        // TextView showing replied‐to message text
    private TextView replyBarUser;           // TextView showing replied‐to sender name
    private String replyingToId = null;      // messageId of the message being replied to


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        // 1) Get the passed “chatId” (which is the groupId)
        chatDocumentId = getIntent().getStringExtra("chatId");
        if (chatDocumentId == null) {
            Toast.makeText(this, "Missing chat ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2) Find views
        recyclerViewChat         = findViewById(R.id.recyclerViewChat);
        editTextMessage          = findViewById(R.id.editTextMessage);
        buttonSend               = findViewById(R.id.send_btn);
        imageViewGroupAvatar     = findViewById(R.id.imageViewGroupAvatar); // optional
        textViewGroupName        = findViewById(R.id.textViewGroupName);    // optional
        editTextMessage = findViewById(R.id.editTextMessage);
        imageviewAttachment=findViewById(R.id.imageviewattachment);

        // 2) Find your attachment preview (if you have one):

        // 3) Find your reply bar container and its child TextViews:
        replyBar         = findViewById(R.id.reply_bar);            // entire reply‐bar layout
        replyBarMessage  = findViewById(R.id.replybar_message);     // the TextView showing the replied-to message
        replyBarUser     = findViewById(R.id.replybar_username);

        // 3) Initialize Firebase
        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 4) RecyclerView setup
        String myEmail = auth.getCurrentUser().getEmail();
        messageAdapter = new MessageAdapter(messageList, myEmail);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(messageAdapter);

        // 5) (Optional) Load group metadata: name + avatarBase64
        loadGroupMetadata();

        // 6) Load and listen for chat messages
        loadAllChats();

        // 7) Send button listener
        buttonSend.setOnClickListener(v -> sendMessage());
    }

    /**
     * Optional: If you stored group name and/or avatarBase64 in “groups/{chatId}” document,
     * fetch them and display in the header (e.g. toolbar).
     */
    private void loadGroupMetadata() {
        db.collection("groups")
                .document(chatDocumentId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // 1) Set group name
                        String groupName = doc.getString("name");
                        if (groupName != null) {
                            textViewGroupName.setText(groupName);
                        }

                        // 2) Decode avatarBase64 (if present) and set ImageView
                        String avatarBase64 = doc.getString("avatarBase64");
                        if (avatarBase64 != null && !avatarBase64.isEmpty()) {
                            Bitmap avatarBitmap = decodeBase64ToBitmap(avatarBase64);
                            if (avatarBitmap != null) {
                                imageViewGroupAvatar.setImageBitmap(avatarBitmap);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // You can optionally show a placeholder if metadata fails to load
                });
    }

    /**
     * Reads real-time chat messages under “chatMessages/{chatId}/messages”,
     * ordered by “timestamp”, and updates the RecyclerView.
     */
    private void loadAllChats() {
        db.collection("chatMessages")
                .document(chatDocumentId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            // Listen failed
                            return;
                        }
                        messageList.clear();
                        if (snapshots != null) {
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                ChatModel chat = doc.toObject(ChatModel.class);
                                messageList.add(chat);
                            }
                            messageAdapter.notifyDataSetChanged();
                            recyclerViewChat.scrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }

    /**
     * Creates a new ChatModel from the text input and writes it under
     * “chatMessages/{chatId}/messages” in Firestore.
     */
    private void sendMessage() {
        String text = editTextMessage.getText().toString().trim();

        if (text.isEmpty() && (attachedImageBase64 == null || attachedImageBase64.isEmpty())) {
            return;
        }

        String messageId = db.collection("chatMessages")
                .document(chatDocumentId)
                .collection("messages")
                .document()
                .getId();

        String senderEmail = auth.getCurrentUser().getEmail();

        db.collection("Users").document(senderEmail)
                .get()
                .addOnSuccessListener(doc -> {
                    String senderName = doc.exists() ? doc.getString("name") : "Unknown";

                    ChatModel chat = new ChatModel();
                    chat.setId(messageId);
                    chat.setSenderEmail(senderEmail);
                    chat.setSenderName(senderName);
                    chat.setReceiverEmail(chatDocumentId);
                    chat.setMessageText(text.isEmpty() ? null : text);
                    chat.setTimestamp(System.currentTimeMillis());
                    chat.setStatus("sent");
                    chat.setReplyTo(replyingToId);
                    chat.setReplyPreviewText(replyBarMessage.getText().toString());
                    chat.setReplyPreviewSender(replyBarUser.getText().toString());

                    if (attachedImageBase64 != null && !attachedImageBase64.isEmpty()) {
                        chat.setImageBase64(attachedImageBase64);
                    } else {
                        chat.setImageBase64(null);
                    }

                    chat.setReaction(null);

                    db.collection("chatMessages")
                            .document(chatDocumentId)
                            .collection("messages")
                            .document(messageId)
                            .set(chat)
                            .addOnSuccessListener(aVoid -> {
                                editTextMessage.setText("");
                                attachedImageBase64 = null;
                                replyBar.setVisibility(View.GONE);
                                replyingToId = null;
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                Log.e("sendMessage", "Firestore write failed", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Unable to get user info", Toast.LENGTH_SHORT).show();
                    Log.e("sendMessage", "Failed to fetch sender name", e);
                });
    }

    /**
     * Decodes a Base64‐encoded PNG/JPEG string back into a Bitmap.
     * Returns null on failure.
     */
    private Bitmap decodeBase64ToBitmap(String base64) {
        try {
            byte[] decodedBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
