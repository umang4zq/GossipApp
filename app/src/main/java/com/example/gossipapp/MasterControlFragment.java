package com.example.gossipapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasterControlFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private UserAccessAdapter adapter;
    private final List<UserAccessModel> userList = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_master_control, container, false);

        recyclerView = view.findViewById(R.id.recyclerUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();

        adapter = new UserAccessAdapter(user -> {
            // 🔧 Extend permission by 7 days (admin action)
            long newExpiry = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000);
            updateUserAccess(user.getUserId(), newExpiry);
        });
        recyclerView.setAdapter(adapter);

        checkIfAdminAndLoadUsers();

        // ✅ Button to trigger announcement
        ImageView btnTriggerUpdate = view.findViewById(R.id.btnTriggerUpdate);
        btnTriggerUpdate.setOnClickListener(v -> sendAnnouncement());

        return view;
    }


    private void sendAnnouncement() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("announcements");
        String announcementId = ref.push().getKey(); // unique key

        AnnouncementModel announcement = new AnnouncementModel(
                "Update GossipApp " + System.currentTimeMillis(),
                "Tap here to see what’s new 🎉",
                "https://github.com/umang4zq/GossipApp",
                System.currentTimeMillis()
        );

        if (announcementId != null) {
            ref.child(announcementId).setValue(announcement)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(getContext(), "Announcement sent!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * ✅ Check if current user is admin, then load all users
     */
    private void checkIfAdminAndLoadUsers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = currentUser.getUid();

        db.collection("Users").document(currentUserId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String role = snapshot.getString("role");
                        Log.d("CheckRole", "Fetched role: " + role);

                        if ("admin".equalsIgnoreCase(role)) {
                            loadAllUsers();
                        } else {
                            Toast.makeText(requireContext(), "Access denied. Admins only.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "User document not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error checking role", e);
                    Toast.makeText(requireContext(), "Failed to check role", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * ✅ Load all users (for admin)
     */
    private void loadAllUsers() {
        db.collection("Users")
                .get()
                .addOnSuccessListener(query -> {
                    userList.clear();

                    for (QueryDocumentSnapshot doc : query) {
                        if (!doc.exists()) continue;

                        String userId = doc.getId();
                        String username = safeString(doc.getString("username"), "Unknown");
                        String email = safeString(doc.getString("email"), "No email");
                        String avatar = safeString(doc.getString("avatarResName"), "default_avatar");

                        long expiry = 0;
                        Object expiryObj = doc.get("notesAccessExpiry");
                        if (expiryObj instanceof Long) {
                            expiry = (Long) expiryObj;
                        } else if (expiryObj instanceof Double) {
                            expiry = ((Double) expiryObj).longValue();
                        } else if (expiryObj == null) {
                            addNotesAccessField(db.collection("Users").document(userId), 0);
                        }

                        UserAccessModel user = new UserAccessModel(
                                userId, username, email, avatar, expiry
                        );
                        userList.add(user);
                    }

                    adapter.setData(userList);
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error loading users", e));
    }

    /**
     * ✅ Admin updates another user's permission
     */
    private void updateUserAccess(String userId, long newExpiry) {
        Map<String, Object> update = new HashMap<>();
        update.put("notesAccessExpiry", newExpiry);

        db.collection("Users").document(userId)
                .update(update)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Access updated for " + userId, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating access", e);
                    Toast.makeText(getContext(), "Failed to update access", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * ✅ Adds missing `notesAccessExpiry` field
     */
    private void addNotesAccessField(DocumentReference docRef, long defaultValue) {
        Map<String, Object> update = new HashMap<>();
        update.put("notesAccessExpiry", defaultValue);

        docRef.update(update)
                .addOnSuccessListener(aVoid ->
                        Log.d("Firestore", "Added missing notesAccessExpiry for " + docRef.getId()))
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Error adding notesAccessExpiry", e));
    }

    private String safeString(String value, String fallback) {
        return (value != null && !value.trim().isEmpty()) ? value : fallback;
    }
}
