package com.example.gossipapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.VH> {

    public interface ClickListener {
        void onClick(GroupModel group);
    }

    private List<GroupModel> data;
    private final ClickListener listener;

    public GroupsAdapter(List<GroupModel> data, ClickListener l) {
        this.data = data;
        this.listener = l;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        GroupModel g = data.get(position);
        holder.name.setText(g.getName());

        if (g.getAvatarBase64() != null && !g.getAvatarBase64().isEmpty()) {
            try {
                byte[] bytes = Base64.decode(g.getAvatarBase64(), Base64.DEFAULT);
                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.avatar.setImageBitmap(bm);
            } catch (Exception e) {
                e.printStackTrace(); // Optional: log decoding failure
            }
        } else {
            holder.avatar.setImageResource(R.drawable.default_avatar); // fallback
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(g));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void updateList(List<GroupModel> newList) {
        this.data = newList;
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView name;

        VH(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.imageViewGroupAvatar);
            name = itemView.findViewById(R.id.textViewGroupName);
        }
    }
}
