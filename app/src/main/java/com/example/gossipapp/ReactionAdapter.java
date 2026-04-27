package com.example.gossipapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReactionAdapter extends RecyclerView.Adapter<ReactionAdapter.ReactionViewHolder> {
    private final List<String> reactions;
    private final OnReactionClickListener listener;

    public interface OnReactionClickListener {
        void onReactionClick(String emoji);
    }

    public ReactionAdapter(List<String> reactions, OnReactionClickListener listener) {
        this.reactions = reactions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reaction_emoji, parent, false);
        return new ReactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReactionViewHolder holder, int position) {
        String emoji = reactions.get(position);
        holder.reactionEmoji.setText(emoji);
        holder.reactionEmoji.setOnClickListener(v -> listener.onReactionClick(emoji));
    }

    @Override
    public int getItemCount() {
        return reactions.size();
    }

    static class ReactionViewHolder extends RecyclerView.ViewHolder {
        TextView reactionEmoji;

        public ReactionViewHolder(@NonNull View itemView) {
            super(itemView);
            reactionEmoji = itemView.findViewById(R.id.reactionEmoji);
        }
    }
}

