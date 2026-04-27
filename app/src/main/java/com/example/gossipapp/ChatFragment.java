package com.example.gossipapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ChatFragment extends Fragment {

    private EditText search_Edt;
    private TextView tvSearchResults;
    private RecyclerView rec_Users;
    private SearchUserAdapter searchUserAdapter;
    private FirebaseFirestore db;
    private ImageView userAvatar;
    private Button createGroupBtn;
    private CardView cardAnnouncement;
    private TextView tvAnnouncementTitle, tvAnnouncementLink;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        db = FirebaseFirestore.getInstance();
        rec_Users = view.findViewById(R.id.search_user_rec);
        search_Edt = view.findViewById(R.id.search_user_edt);
        tvSearchResults = view.findViewById(R.id.tv_search_results);
        userAvatar = view.findViewById(R.id.avatar);
        createGroupBtn = view.findViewById(R.id.buttonOpenCreateGroup);
        cardAnnouncement = view.findViewById(R.id.cardAnnouncement);
        tvAnnouncementTitle = view.findViewById(R.id.tvAnnouncementTitle);
        tvAnnouncementLink = view.findViewById(R.id.tvAnnouncementLink);

        loadCurrentUserAvatar();

        // Set up RecyclerView
        rec_Users.setLayoutManager(new LinearLayoutManager(getContext()));
        setupRecyclerView(""); // initial load

        // Handle search bar text change
        search_Edt.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String queryStr = s.toString().trim().toLowerCase();
                if (!queryStr.isEmpty()) {
                    tvSearchResults.setText("Results for \"" + queryStr + "\"");
                    tvSearchResults.setVisibility(View.VISIBLE);
                } else {
                    tvSearchResults.setVisibility(View.GONE);
                }
                setupRecyclerView(queryStr);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Open group creation activity
        createGroupBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), CreateGroupActivity.class);
            startActivity(intent);
        });

        // ✅ Firebase Realtime Database announcement listener

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("announcements");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) return; // safe context
                Context ctx = getContext();

                if (snapshot.exists()) {
                    DataSnapshot latest = null;
                    long latestTime = 0;

                    // find the latest visible announcement
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Boolean visible = ds.child("visible").getValue(Boolean.class);
                        if (visible == null || !visible) continue;

                        Long ts = ds.child("timestamp").getValue(Long.class);
                        if (ts != null && ts > latestTime) {
                            latestTime = ts;
                            latest = ds;
                        }
                    }

                    if (latest == null) {
                        cardAnnouncement.setVisibility(View.GONE);
                        return;
                    }

                    String title = latest.child("title").getValue(String.class);
                    String message = latest.child("message").getValue(String.class);
                    String link = latest.child("link").getValue(String.class);

                    if (title == null || message == null) return;

                    // optional: check if already seen
                    if (hasSeenAnnouncement(ctx, title)) {
                        cardAnnouncement.setVisibility(View.GONE);
                        return;
                    }

                    tvAnnouncementTitle.setText(title);
                    tvAnnouncementLink.setText(message);

                    // fade-in
                    if (cardAnnouncement.getVisibility() != View.VISIBLE) {
                        cardAnnouncement.setAlpha(0f);
                        cardAnnouncement.setVisibility(View.VISIBLE);
                        cardAnnouncement.animate()
                                .alpha(1f)
                                .setDuration(500)
                                .setListener(null);
                    }

                    // open link
                    tvAnnouncementLink.setOnClickListener(v -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                        startActivity(intent);
                        markAnnouncementSeen(ctx, title);
                    });

                    // close button
                    ImageView closeBtn = view.findViewById(R.id.btnCloseAnnouncement);
                    if (closeBtn != null) {
                        closeBtn.setOnClickListener(v -> {
                            cardAnnouncement.animate()
                                    .alpha(0f)
                                    .setDuration(300)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            cardAnnouncement.setVisibility(View.GONE);
                                            markAnnouncementSeen(ctx, title);
                                        }
                                    });
                        });
                    }

                } else {
                    // hide if no announcements
                    if (cardAnnouncement.getVisibility() == View.VISIBLE) {
                        cardAnnouncement.animate()
                                .alpha(0f)
                                .setDuration(400)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        cardAnnouncement.setVisibility(View.GONE);
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        ImageView closeBtn = view.findViewById(R.id.btnCloseAnnouncement);
        closeBtn.setOnClickListener(v -> {
            cardAnnouncement.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            cardAnnouncement.setVisibility(View.GONE);
                        }
                    });
        });

        return view;
    }


    private void markAnnouncementSeen(Context context, String id) {
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("lastSeenAnnouncementId", id)
                .apply();
    }

    private boolean hasSeenAnnouncement(Context context, String id) {
        String seen = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .getString("lastSeenAnnouncementId", "");
        return seen.equals(id);
    }




    private void loadCurrentUserAvatar() {
        // grab context and view references one time up–front
        final Context ctx = getContext();
        final ImageView avatarView = userAvatar;
        if (ctx == null || avatarView == null) return;

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("Users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    // make sure fragment is still added and view still around
                    if (!isAdded() || avatarView == null) return;

                    String base64 = snapshot.getString("profilepic");
                    if (base64 != null && !base64.isEmpty()) {
                        byte[] data = Base64.decode(base64, Base64.DEFAULT);
                        Glide.with(ctx)
                                .asBitmap()
                                .load(data)
                                .circleCrop()
                                .into(avatarView);
                        return;
                    }

                    String resName = snapshot.getString("avatarResName");
                    if (resName != null && !resName.isEmpty()) {
                        int resId = ctx.getResources()
                                .getIdentifier(resName, "drawable", ctx.getPackageName());
                        if (resId != 0) {
                            avatarView.setImageResource(resId);
                            return;
                        }
                    }

                    // final fallback
                    avatarView.setImageResource(R.drawable.default_avatar);
                });
    }

    private void setupRecyclerView(String searchTerm) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Users").document(currentUserId)
                .get()
                .addOnSuccessListener(userSnap -> {

                    boolean isAdmin = userSnap.getBoolean("isAdmin") != null &&
                            userSnap.getBoolean("isAdmin");

                    Query query;

                    if (isAdmin) {
                        query = db.collection("Users")
                                .whereNotEqualTo(FieldPath.documentId(), currentUserId)
                                .orderBy("username")
                                .startAt(searchTerm)
                                .endAt(searchTerm + "\uf8ff");

                    } else {
                        query = db.collection("Users")
                                .whereNotEqualTo(FieldPath.documentId(), currentUserId)
                                .whereEqualTo("isHidden", false)
                                .orderBy("username")
                                .startAt(searchTerm)
                                .endAt(searchTerm + "\uf8ff");
                    }

                    FirestoreRecyclerOptions<UserModel> options =
                            new FirestoreRecyclerOptions.Builder<UserModel>()
                                    .setQuery(query, UserModel.class)
                                    .build();

                    if (searchUserAdapter != null) {
                        searchUserAdapter.updateOptions(options);
                    } else {
                        searchUserAdapter = new SearchUserAdapter(options, getContext());
                        rec_Users.setAdapter(searchUserAdapter);
                    }

                    searchUserAdapter.startListening();
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (searchUserAdapter != null) searchUserAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (searchUserAdapter != null) searchUserAdapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (searchUserAdapter != null) searchUserAdapter.startListening();
    }
    public void playAccessDeniedAnimation() {
        LottieAnimationView animView = getView().findViewById(R.id.accessDeniedAnim);
        if (animView == null) return;

        animView.setVisibility(View.VISIBLE);
        animView.playAnimation();

        animView.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animView.setVisibility(View.GONE);
            }
        });
    }


}
