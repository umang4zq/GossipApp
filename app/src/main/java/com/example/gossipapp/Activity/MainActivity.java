package com.example.gossipapp.Activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.gossipapp.ChatFragment;
import com.example.gossipapp.FriendsFragment;
import com.example.gossipapp.NotesFragment;
import com.example.gossipapp.ProfileFragment;
import com.example.gossipapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;
    FirebaseFirestore db;
    FirebaseAuth auth;
    private long lastNotesClickTime = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadFragment(new ChatFragment(), "ChatFragment");

        bottomNav = findViewById(R.id.bottomnav);
        bottomNav.setSelectedItemId(R.id.chat_nav);

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.chat_nav) {
                loadFragment(new ChatFragment(), "ChatFragment");
                return true;
            } else if (id == R.id.friends_nav) {
                loadFragment(new FriendsFragment(), "FriendsFragment");
                return true;
            } else if (id == R.id.profile_nav) {
                loadFragment(new ProfileFragment(), "ProfileFragment");
                return true;
            } else if (id == R.id.notes_nav) {
                // ✅ Prevent double-tap crash
                if (System.currentTimeMillis() - lastNotesClickTime < 1000) {
                    return true; // Ignore if clicked again within 1 second
                }
                lastNotesClickTime = System.currentTimeMillis();

                checkNotesAccess(); // your existing method
                return false; // stop immediate navigation
            }

            return false;
        });

    }

    private void checkNotesAccess() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Users").document(auth.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Long expiry = document.getLong("notesAccessExpiry");
                        boolean hasAccess = false;

                        if (expiry != null) {
                            if (expiry == -1) {
                                hasAccess = true; // permanent access
                            } else if (expiry > System.currentTimeMillis()) {
                                hasAccess = true; // still valid
                            }
                        }

                        if (hasAccess) {
                            loadFragment(new NotesFragment(), "NotesFragment");
                        } else {
                            bottomNav.setSelectedItemId(R.id.chat_nav);
                            Toast.makeText(getApplicationContext(), "Access denied", Toast.LENGTH_LONG).show();

                            Fragment current = getSupportFragmentManager().findFragmentByTag("ChatFragment");
                            if (current instanceof ChatFragment) {
                                ((ChatFragment) current).playAccessDeniedAnimation();
                            }

                        }
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to check access", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ✅ Set isOnline to true
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getUid() != null) {
            FirebaseDatabase.getInstance().getReference("Users").child(user.getUid())
                    .child("isOnline").setValue(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // ✅ Set isOnline to false
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getUid() != null) {
            FirebaseDatabase.getInstance().getReference("Users").child(user.getUid())
                    .child("isOnline").setValue(false);
        }
    }

    private void loadFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_full, fragment, tag);
        fragmentTransaction.commit();
    }

}
