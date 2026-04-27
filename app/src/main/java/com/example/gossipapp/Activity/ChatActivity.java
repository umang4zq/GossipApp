package com.example.gossipapp.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gossipapp.ChatAdapter;
import com.example.gossipapp.ChatModel;
import com.example.gossipapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_EMAIL = "email";

    private TextView usernameText, typingStatusText, activeStatusTxt, replyBarMessage, replyBarUser;
    private EditText etMessage;
    private Button sendBtn, backBtn;
    LinearLayout replyBar;
    private RecyclerView chatRecyclerView;

    private String senderEmail;
    private String receiverEmail;
    private String receiverUsername;

    private DatabaseReference chatMessagesRef;
    private DatabaseReference receiverStatusRef;
    private ChildEventListener messageListener;
    private ValueEventListener otherTypingListener;
    private ValueEventListener receiverStatusListener;
    private DatabaseReference myTypingStatusRef;
    private DatabaseReference otherTypingStatusRef;

    private Handler typingHandler = new Handler();
    private Runnable stopTypingRunnable;

    private ChatAdapter chatAdapter;
    private ArrayList<ChatModel> messageList = new ArrayList<>();
    private String attachedImageBase64 = null; // holds your picked image

    private String replyingToId = null;
    private static final int PICK_FILE_REQUEST = 1001;
    private ImageView imageView;
    private SharedPreferences prefs;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        usernameText = findViewById(R.id.other_username);
        typingStatusText = findViewById(R.id.typing_status);
        etMessage = findViewById(R.id.etMessage);
        sendBtn = findViewById(R.id.btn_send);
        backBtn = findViewById(R.id.back_btn);
        chatRecyclerView = findViewById(R.id.msg_recyclerview);
        activeStatusTxt = findViewById(R.id.statusText);
        replyBar = findViewById(R.id.replybar);
        replyBarUser = findViewById(R.id.replyBarUsername);
        replyBarMessage = findViewById(R.id.replyBarMessage);

        ImageButton replyBarClose = findViewById(R.id.replyBarClose);
        imageView = findViewById(R.id.prewImageView);

        prefs = getSharedPreferences("my_app_prefs", MODE_PRIVATE);
        loadImageFromPrefs();

        if (FirebaseAuth.getInstance().getCurrentUser() != null &&
                FirebaseAuth.getInstance().getCurrentUser().getEmail() != null) {
            senderEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        } else {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            senderEmail = prefs.getString(KEY_EMAIL, "markanaumang@gmail.com");
        }

        receiverEmail = getIntent().getStringExtra("receiverEmail");
        receiverUsername = getIntent().getStringExtra("receiverUsername");

        usernameText.setText(!TextUtils.isEmpty(receiverUsername) ? receiverUsername : receiverEmail);

        String chatId = makeChatId(senderEmail, receiverEmail);
        chatMessagesRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId).child("messages");
        myTypingStatusRef = FirebaseDatabase.getInstance().getReference("typingStatus").child(chatId).child("users")
                .child(keyFromEmail(senderEmail));
        otherTypingStatusRef = FirebaseDatabase.getInstance().getReference("typingStatus").child(chatId).child("users")
                .child(keyFromEmail(receiverEmail));

        otherTypingListener = otherTypingStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isTyping = snapshot.child("isTyping").getValue(Boolean.class);
                if (Boolean.TRUE.equals(isTyping)) {
                    typingStatusText.setText(receiverUsername + " typing...");
                    typingStatusText.setVisibility(TextView.VISIBLE);
                } else {
                    typingStatusText.setVisibility(TextView.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(lm);
        chatAdapter = new ChatAdapter(this, messageList, senderEmail, true);
        chatRecyclerView.setAdapter(chatAdapter);

        loadMessages();
        markMessagesAsDelivered();
        markMessagesAsSeen();

        stopTypingRunnable = () -> myTypingStatusRef.child("isTyping").setValue(false);
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {
            }

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                myTypingStatusRef.child("isTyping").setValue(true);
                typingHandler.removeCallbacks(stopTypingRunnable);
                typingHandler.postDelayed(stopTypingRunnable, 2000);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        sendBtn.setOnClickListener(v -> sendMessage());
        backBtn.setOnClickListener(v -> navigateToMainActivity());
        replyBarClose.setOnClickListener(v -> {
            replyingToId = null;
            replyBar.setVisibility(View.GONE);
        });

        chatAdapter.setOnMessageLongClickListener(new ChatAdapter.OnMessageLongClickListener() {
            @Override
            public void onDeleteForMe(ChatModel msg) {
                deleteForMe(msg.getId());
            }

            @Override
            public void onDeleteForEveryone(ChatModel msg) {
                deleteForEveryone(msg.getId());
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder v,
                    @NonNull RecyclerView.ViewHolder t) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getAdapterPosition();
                ChatModel msg = messageList.get(pos);
                replyingToId = msg.getId();
                replyBarUser.setText(msg.getSenderEmail().split("@")[0]);
                replyBarMessage.setText(msg.getMessageText());
                replyBar.setVisibility(View.VISIBLE);
                chatAdapter.notifyItemChanged(pos);
            }
        }).attachToRecyclerView(chatRecyclerView);

        receiverStatusRef = FirebaseDatabase.getInstance().getReference("Users").child(keyFromEmail(receiverEmail));
        receiverStatusListener = receiverStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isOnline = snapshot.child("isOnline").getValue(Boolean.class);
                if (isOnline != null && isOnline) {
                    activeStatusTxt.setText("Online");
                    activeStatusTxt.setTextColor(Color.GREEN);
                } else {
                    activeStatusTxt.setText("Offline");
                    activeStatusTxt.setTextColor(Color.RED);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadMessages() {
        messageListener = chatMessagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatModel model = snapshot.getValue(ChatModel.class);
                if (model != null) {
                    messageList.add(model);
                    Collections.sort(messageList, Comparator.comparingLong(ChatModel::getTimestamp));
                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatModel model = snapshot.getValue(ChatModel.class);
                if (model != null) {
                    for (int i = 0; i < messageList.size(); i++) {
                        if (messageList.get(i).getId().equals(model.getId())) {
                            messageList.set(i, model);
                            break;
                        }
                    }
                    chatAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                ChatModel model = snapshot.getValue(ChatModel.class);
                if (model != null) {
                    for (int i = 0; i < messageList.size(); i++) {
                        if (messageList.get(i).getId().equals(model.getId())) {
                            messageList.remove(i);
                            break;
                        }
                    }
                    chatAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot s, @Nullable String p) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty() && attachedImageBase64 == null)
            return;

        DatabaseReference newMsgRef = chatMessagesRef.push();
        String id = newMsgRef.getKey();
        long timestamp = System.currentTimeMillis();

        ChatModel msg = new ChatModel(id, senderEmail, receiverEmail, text, timestamp, "sent", replyingToId,
                replyBarMessage.getText().toString(), replyBarUser.getText().toString());
        if (attachedImageBase64 != null)
            msg.setImageBase64(attachedImageBase64);

        newMsgRef.setValue(msg).addOnSuccessListener(a -> {
            etMessage.setText("");
            attachedImageBase64 = null;
            imageView.setVisibility(View.GONE);
            replyBar.setVisibility(View.GONE);
            replyingToId = null;
        });
    }

    private void deleteForMe(String id) {
        chatMessagesRef.child(id).removeValue();
    }

    private void deleteForEveryone(String id) {
        chatMessagesRef.child(id).removeValue();
    }

    private void markMessagesAsDelivered() {
        chatMessagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ChatModel model = ds.getValue(ChatModel.class);
                    if (model != null && "sent".equals(model.getStatus())
                            && senderEmail.equals(model.getReceiverEmail())) {
                        ds.getRef().child("status").setValue("delivered");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
    }

    private void markMessagesAsSeen() {
        chatMessagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ChatModel model = ds.getValue(ChatModel.class);
                    if (model != null && "delivered".equals(model.getStatus())
                            && senderEmail.equals(model.getReceiverEmail())) {
                        ds.getRef().child("status").setValue("seen");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
    }

    private String makeChatId(String a, String b) {
        String x = a.replace(".", "_");
        String y = b.replace(".", "_");
        return (x.compareTo(y) < 0) ? x + "_" + y : y + "_" + x;
    }

    private String keyFromEmail(String email) {
        return email.replace(".", "_").replace("#", "_").replace("$", "_").replace("[", "_").replace("]", "_");
    }

    private void navigateToMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private void loadImageFromPrefs() {
        String base64 = prefs.getString("saved_image_base64", null);
        if (base64 != null) {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    public void openFilePicker(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK && data != null
                && data.getData() != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                attachedImageBase64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseDatabase.getInstance().getReference("Users").child(keyFromEmail(senderEmail)).child("isOnline")
                .setValue(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseDatabase.getInstance().getReference("Users").child(keyFromEmail(senderEmail)).child("isOnline")
                .setValue(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageListener != null)
            chatMessagesRef.removeEventListener(messageListener);
        if (otherTypingListener != null)
            otherTypingStatusRef.removeEventListener(otherTypingListener);
        if (receiverStatusListener != null)
            receiverStatusRef.removeEventListener(receiverStatusListener);
    }
}
