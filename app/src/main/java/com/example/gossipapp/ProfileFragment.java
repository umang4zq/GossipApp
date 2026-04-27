package com.example.gossipapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.gossipapp.Activity.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private ImageView profileImage, changePasswordIcon;
    private TextView usernameTextView, emailTextView, passwordTextView,Add_profile;
    private Button logoutBtn;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, parent, false);
        profileImage=view.findViewById(R.id.avatar_image);
        Add_profile       = view.findViewById(R.id.add_profile);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        passwordTextView = view.findViewById(R.id.passwordTextView);
        logoutBtn          = view.findViewById(R.id.logout_btn);

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        Add_profile.setOnClickListener(v -> showImageChoice());
//        changePasswordIcon.setOnClickListener(v -> showPasswordDialog());
        logoutBtn.setOnClickListener(v -> performLogout());

        loadUserImage();
        loadUserData();


        return view;
    }

    private void performLogout() {
        auth.signOut();
        requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit().clear().apply();
        startActivity(new Intent(requireContext(), LoginActivity.class));
        requireActivity().finish();
    }

    private void showPasswordDialog() {
        TextView input = new TextView(requireContext());
        input.setHint("New Password");

        new AlertDialog.Builder(requireContext())
                .setTitle("Change Password")
                .setView(input)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newPassword = input.getText().toString().trim();
                    if (newPassword.length() >= 6) {
                        auth.getCurrentUser().updatePassword(newPassword)
                                .addOnSuccessListener(aVoid -> showToast("Password updated"))
                                .addOnFailureListener(e -> showToast("Failed: " + e.getMessage()));
                    } else {
                        showToast("Password must be at least 6 characters");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

//    private void displayUserData() {
//        String uid = auth.getCurrentUser().getUid();
//        db.collection("Users").document(uid).get()
//                .addOnSuccessListener(snapshot -> {
//                    nameView.setText(snapshot.getString("username"));
//                    emailView.setText(auth.getCurrentUser().getEmail());
//                    passwordView.setText(snapshot.getString("password"));
//                })
//                .addOnFailureListener(e -> Log.w("ProfileFragment", "Failed to load user data", e));
//    }

    private void showImageChoice() {
        View avatarLayout = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_select_avatar, null);
        GridView avatarGrid = avatarLayout.findViewById(R.id.avatarGrid);
        int[] avatarRes = getAvatarIds();
        AvatarAdapter adapter = new AvatarAdapter(requireContext(), avatarRes);
        avatarGrid.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(avatarLayout).create();
        avatarLayout.findViewById(R.id.dialogCloseTxt).setOnClickListener(v -> dialog.dismiss());

        avatarGrid.setOnItemClickListener((parent, view, pos, id) -> {
            int selectedRes = avatarRes[pos];
            Add_profile.setText(getResources().getResourceEntryName(selectedRes));
            updateAvatarResource(getResources().getResourceEntryName(selectedRes));
            dialog.dismiss();
        });

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }
    }

    private int[] getAvatarIds() {
        ArrayList<Integer> ids = new ArrayList<>();
        String pkg = requireContext().getPackageName();
        for (int i = 1; i <= 100; i++) {
            int resId = getResources().getIdentifier("avatar" + i, "drawable", pkg);
            if (resId != 0) ids.add(resId);
        }
        int[] arr = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) arr[i] = ids.get(i);
        return arr;
    }

    private void updateAvatarResource(String resName) {
        String uid = auth.getCurrentUser().getUid();
        db.collection("Users").document(uid)
                .update("avatarResName", resName, "profilepic", null)
                .addOnSuccessListener(aVoid -> showToast("Avatar updated"))
                .addOnFailureListener(e -> showToast("Update failed"));
    }

    private void loadUserImage() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("Users").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded() || profileImage == null) return; // ✅ fixed variable

                    String base64 = snapshot.getString("profilepic");
                    if (base64 != null && !base64.isEmpty()) {
                        // 🟢 Load Base64 image
                        byte[] data = Base64.decode(base64, Base64.DEFAULT);
                        Glide.with(this)
                                .asBitmap()
                                .load(data)
                                .circleCrop()
                                .into(profileImage);
                    } else {
                        // 🟢 Load default avatar image from drawable
                        String resName = snapshot.getString("avatarResName");
                        if (resName != null) {
                            int resId = getResources().getIdentifier(
                                    resName, "drawable", requireContext().getPackageName());
                            if (resId != 0) profileImage.setImageResource(resId);
                            else profileImage.setImageResource(R.drawable.default_avatar);
                        } else {
                            profileImage.setImageResource(R.drawable.default_avatar);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Log.w("ProfileFragment", "loadUserImage failed", e));
    }

    private void showToast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
    private void loadUserData() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("Users").document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String username = snapshot.getString("username");
                        String email = snapshot.getString("email");
                        String password = snapshot.getString("password");

                        usernameTextView.setText(username != null ? username : "Unknown");
                        emailTextView.setText(email != null ? email : "No Email");
                        passwordTextView.setText(password != null ? password : "No Password");
                    }
                })
                .addOnFailureListener(e -> Log.w("ProfileFragment", "Failed to load user data", e));
    }
}
