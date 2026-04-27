package com.example.gossipapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Displays list of users with a CheckBox to select/deselect each one.
 */
public class SelectableUserAdapter extends RecyclerView.Adapter<SelectableUserAdapter.UserViewHolder> {

    private final List<UserModel> userList;
    private final Set<String> selectedUids = new HashSet<>();

    public SelectableUserAdapter(List<UserModel> userList) {
        this.userList = userList;
    }

    /** Returns a List of the UIDs the user has checked. */
    public List<String> getSelectedUserIds() {
        return new ArrayList<>(selectedUids);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_selectable, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull UserViewHolder holder, int position) {
        UserModel user = userList.get(position);

        // 1) Load avatar if present
        String avatarUrl = user.getProfilepic();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.default_avatar)
                    .into(holder.imageViewAvatar);
        } else {
            holder.imageViewAvatar.setImageResource(R.drawable.default_avatar);
        }

        // 2) Username
        holder.textViewName.setText(user.getUsername());

        // 3) CheckBox logic (to avoid recycling glitches)
        holder.checkBox.setOnCheckedChangeListener(null);
        boolean checked = selectedUids.contains(user.getUserId());
        holder.checkBox.setChecked(checked);

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedUids.add(user.getUserId());
            } else {
                selectedUids.remove(user.getUserId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewAvatar;
        TextView  textViewName;
        CheckBox  checkBox;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewUserAvatar);
            textViewName    = itemView.findViewById(R.id.textViewUserName);
            checkBox        = itemView.findViewById(R.id.checkBoxSelectUser);
        }
    }
}
