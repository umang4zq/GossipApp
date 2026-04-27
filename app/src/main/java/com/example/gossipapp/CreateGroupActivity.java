    package com.example.gossipapp;

    import android.content.Intent;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.net.Uri;
    import android.os.Bundle;
    import android.provider.MediaStore;
    import android.util.Base64;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.ImageView;
    import android.widget.Toast;

    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import com.google.firebase.Timestamp;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.firestore.FieldValue;
    import com.google.firebase.firestore.FirebaseFirestore;

    import java.io.ByteArrayOutputStream;
    import java.io.InputStream;
    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.UUID;


    public class CreateGroupActivity extends AppCompatActivity {

        private static final int PICK_IMAGE_REQUEST = 1;

        private ImageView groupAvatarImageView;
        private EditText groupNameEditText;
        private RecyclerView userRecyclerView;
        private Button createGroupButton;

        // Holds URI of selected avatar (if any)
        private Uri selectedImageUri = null;

        // List of all users (to display in RecyclerView)
        private final List<UserModel> userList = new ArrayList<>();
        private SelectableUserAdapter userAdapter;

        private FirebaseFirestore db;
        private FirebaseAuth auth;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_create_group);

            groupAvatarImageView = findViewById(R.id.groupAvatarImageView);
            groupNameEditText    = findViewById(R.id.groupNameEditText);
            userRecyclerView     = findViewById(R.id.userRecyclerView);
            createGroupButton    = findViewById(R.id.createGroupButton);

            db   = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();

            userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            userAdapter = new SelectableUserAdapter(userList);
            userRecyclerView.setAdapter(userAdapter);

            loadAllUsers();

            groupAvatarImageView.setOnClickListener(v -> openImagePicker());

            createGroupButton.setOnClickListener(v -> createGroup());
        }

        private void openImagePicker() {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        }


        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == PICK_IMAGE_REQUEST
                    && resultCode == RESULT_OK
                    && data != null
                    && data.getData() != null) {
                selectedImageUri = data.getData();
                groupAvatarImageView.setImageURI(selectedImageUri);
            }
        }


        private void loadAllUsers() {
            String currentUid = auth.getCurrentUser().getUid();
            db.collection("Users")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        userList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            UserModel user = doc.toObject(UserModel.class);
                            user.setUserId(doc.getId());

                            if (!user.getUserId().equals(currentUid)) {
                                userList.add(user);
                            }
                        }
                        userAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CreateGroupActivity.this,
                                "Failed to load users.",
                                Toast.LENGTH_SHORT).show();
                    });
        }


        private void createGroup() {
            String groupName = groupNameEditText.getText().toString().trim();
            List<String> selectedUids = userAdapter.getSelectedUserIds();

            if (groupName.isEmpty()) {
                groupNameEditText.setError("Enter a group name");
                return;
            }
            if (selectedUids.isEmpty()) {
                Toast.makeText(this, "Select at least one member", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentUid = auth.getCurrentUser().getUid();
            if (!selectedUids.contains(currentUid)) {
                selectedUids.add(currentUid);
            }

            String avatarBase64 = null;
            if (selectedImageUri != null) {
                avatarBase64 = encodeImageToBase64(selectedImageUri);
                if (avatarBase64 == null) {
                    Toast.makeText(this, "Failed to encode avatar", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            String groupId = UUID.randomUUID().toString();
            Map<String, Object> groupData = new HashMap<>();
            groupData.put("groupId", groupId);
            groupData.put("name", groupName);
            groupData.put("members", selectedUids);
            groupData.put("avatarBase64", avatarBase64);
            groupData.put("createdBy", currentUid);
            groupData.put("createdAt", FieldValue.serverTimestamp());

            db.collection("groups")
                    .document(groupId)
                    .set(groupData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CreateGroupActivity.this,
                                "Group created successfully",
                                Toast.LENGTH_SHORT).show();

                        // Launch GroupChatActivity, passing “chatId” = groupId
                        Intent intent = new Intent(CreateGroupActivity.this, GroupChatActivity.class);
                        intent.putExtra("chatId", groupId);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CreateGroupActivity.this,
                                "Error creating group: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }


        private String encodeImageToBase64(Uri imageUri) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                if (inputStream == null) return null;

                Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();



                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                originalBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] imageBytes = baos.toByteArray();

                return Base64.encodeToString(imageBytes, Base64.DEFAULT);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
