package com.example.gossipapp;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gossipapp.Activity.ChatActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

public class SearchUserAdapter extends FirestoreRecyclerAdapter<UserModel, SearchUserAdapter.UserModelViewHolder> {

    private final Context context;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Map<UserModelViewHolder, ListenerRegistration> listenerMap = new HashMap<>();

    public SearchUserAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull UserModelViewHolder holder, int position, @NonNull UserModel model) {
        holder.userNametxt.setText(model.getUsername());
        holder.userEmailtxt.setText(model.getEmail());

        // Load profile picture (Base64 or avatarResName or fallback)
        if (model.getProfilepic() != null && !model.getProfilepic().isEmpty()) {
            byte[] imageBytes = Base64.decode(model.getProfilepic(), Base64.DEFAULT);
            Glide.with(context)
                    .asBitmap()
                    .load(imageBytes)
                    .circleCrop()
                    .into(holder.profilePic);
        } else if (model.getAvatarResName() != null && !model.getAvatarResName().isEmpty()) {
            int resId = context.getResources().getIdentifier(
                    model.getAvatarResName(), "drawable", context.getPackageName());
            if (resId != 0) {
                holder.profilePic.setImageResource(resId);
            } else {
                holder.profilePic.setImageResource(R.drawable.default_avatar); // fallback
            }
        } else {
            holder.profilePic.setImageResource(R.drawable.default_avatar); // fallback
        }

        // Clear previous animation
        holder.statusDot.clearAnimation();

        // Remove previous listener if recycled
        if (listenerMap.containsKey(holder)) {
            listenerMap.get(holder).remove();
        }

        // Real-time listener for "online" field
        String docId = getSnapshots().getSnapshot(position).getId();
        DocumentReference userRef = db.collection("Users").document(docId);

        ListenerRegistration listener = userRef.addSnapshotListener((snapshot, error) -> {
            if (error != null || snapshot == null || !snapshot.exists())
                return;

            Boolean online = snapshot.getBoolean("isOnline"); // use "online" field
            if (online != null && online) {
                holder.statusDot.setBackgroundResource(R.drawable.status_circle_green);
                holder.statusDot.startAnimation(AnimationUtils.loadAnimation(context, R.anim.pulse));
            } else {
                holder.statusDot.setBackgroundResource(R.drawable.status_circle_gray);
                holder.statusDot.clearAnimation();
            }
        });

        listenerMap.put(holder, listener);

        // Open chat
        holder.card.setOnClickListener(v -> {
            Intent chatIntent = new Intent(context, ChatActivity.class);
            chatIntent.putExtra("receiverEmail", model.getEmail());
            chatIntent.putExtra("receiverUsername", model.getUsername());
            context.startActivity(chatIntent);
        });
    }

    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_user_rec_row, parent, false);
        return new UserModelViewHolder(view);
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        notifyDataSetChanged();
    }

    @Override
    public void onViewRecycled(@NonNull UserModelViewHolder holder) {
        super.onViewRecycled(holder);
        if (listenerMap.containsKey(holder)) {
            listenerMap.get(holder).remove();
            listenerMap.remove(holder);
        }
    }

    static class UserModelViewHolder extends RecyclerView.ViewHolder {
        TextView userNametxt, userEmailtxt;
        ImageView profilePic;
        View statusDot;
        CardView card;

        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            userNametxt = itemView.findViewById(R.id.user_Name);
            userEmailtxt = itemView.findViewById(R.id.user_Email);
            profilePic = itemView.findViewById(R.id.profilepic);
            statusDot = itemView.findViewById(R.id.status_dot);
            card = itemView.findViewById(R.id.card);
        }
    }
}
