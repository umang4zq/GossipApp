package com.example.gossipapp.Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gossipapp.R;
import com.example.gossipapp.UserModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter
        extends RecyclerView.Adapter<UsersAdapter.VH> {

    public UsersAdapter(List<UserModel> userList, FirebaseFirestore db) {
    }

    public interface OnClick { void onClick(UserProfile user); }

    private List<UserProfile> data     = new ArrayList<>();
    private List<UserProfile> filtered = new ArrayList<>();
    private OnClick listener;

    public UsersAdapter(OnClick listener) {
        this.listener = listener;
    }

    public void setData(List<UserProfile> list) {
        data.clear();
        data.addAll(list);
        filter("");
    }

    public void filter(String q) {
        filtered.clear();
        for (UserProfile u : data) {
            if (u.username.toLowerCase()
                    .contains(q.toLowerCase())) {
                filtered.add(u);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p,int vt){
        View v = LayoutInflater.from(p.getContext())
                .inflate(R.layout.search_user_rec_row, p, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h,int pos){
        UserProfile u = filtered.get(pos);
        h.tvUsername.setText(u.username);
        h.tvEmail.setText(u.email);
        h.itemView.setOnClickListener(v -> listener.onClick(u));
    }

    @Override public int getItemCount(){
        return filtered.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvUsername, tvEmail;
        VH(View v) {
            super(v);
            tvUsername = v.findViewById(R.id.user_Name);
            tvEmail    = v.findViewById(R.id.user_Email);
        }
    }

}
