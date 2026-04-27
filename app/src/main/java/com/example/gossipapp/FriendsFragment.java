package com.example.gossipapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class FriendsFragment extends Fragment {

    private RecyclerView recyclerView;
    private GroupsAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) Find RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewGroups);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 2) Firebase
        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 3) Adapter with click launching GroupChatActivity
        adapter = new GroupsAdapter(
                new ArrayList<>(),
                group -> {
                    Intent i = new Intent(requireContext(), GroupChatActivity.class);
                    i.putExtra("chatId", group.getId()); // or group.getId()
                    startActivity(i);
                }
        );
        recyclerView.setAdapter(adapter);

        // 4) Load groups
        loadMyGroups();
    }

    private void loadMyGroups() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("groups")
                .whereArrayContains("members", uid)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(requireContext(),
                                "Error loading groups: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<GroupModel> list = new ArrayList<>();
                    for (DocumentSnapshot doc: snapshots.getDocuments()) {
                        GroupModel g = doc.toObject(GroupModel.class);
                        // if your model uses 'groupId' vs. doc.getId(), set it:
                        g.setId(doc.getId());
                        list.add(g);
                    }
                    adapter.updateList(list);
                });
    }
}
