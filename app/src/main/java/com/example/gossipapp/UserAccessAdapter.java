package com.example.gossipapp;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class UserAccessAdapter extends RecyclerView.Adapter<UserAccessAdapter.VH> {

    private final List<UserAccessModel> data = new ArrayList<>();
    private final OnClick listener;

    public interface OnClick {
        void onClick(UserAccessModel user);
    }

    public UserAccessAdapter(OnClick listener) {
        this.listener = listener;
    }

    public void setData(List<UserAccessModel> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_access_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        UserAccessModel user = data.get(position);
        Context context = holder.itemView.getContext();

        holder.tvUsername.setText(user.getUsername());
        holder.tvEmail.setText(user.getEmail());

        int resId = context.getResources()
                .getIdentifier(user.getAvatarResName(), "drawable", context.getPackageName());
        holder.ivAvatar.setImageResource(resId != 0 ? resId : R.drawable.default_avatar);

        //  Show expiry-based label
        String statusText = getAccessStatusText(user);
        holder.accessStatus.setText(statusText);
        holder.accessStatus.setTextColor(context.getResources().getColor(
                user.hasNotesAccess()
                        ? R.color.green
                        : R.color.red
        ));

        // Long press = open admin access dialog
        holder.itemView.setOnLongClickListener(v -> {
            showAccessDialog(context, user, holder.getBindingAdapterPosition());
            return true;
        });

        holder.itemView.setOnClickListener(v -> listener.onClick(user));
    }


    private String getAccessStatusText(UserAccessModel user) {
        long expiry = user.getNotesAccessExpiry();
        if (expiry == -1) return "Access: Permanent ";
        if (expiry == 0) return "Access: None ";
        if (expiry < System.currentTimeMillis()) return "Access: Expired ";

        // Format expiry time
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM hh:mm a");
        return "Valid till: " + sdf.format(new Date(expiry));
    }

    private void showAccessDialog(Context context, UserAccessModel user, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_access_control, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        LinearLayout btn1hr = view.findViewById(R.id.btnAccess1hr);
        LinearLayout btn24hr = view.findViewById(R.id.btnAccess24hr);
        LinearLayout btnPermanent = view.findViewById(R.id.btnAccessPermanent);
        LinearLayout btnRevoke = view.findViewById(R.id.btnAccessRevoke);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        btn1hr.setOnClickListener(v -> {
            giveAccess(db, user, 60 * 60 * 1000L, position, context, "1 hour");
            dialog.dismiss();
        });

        btn24hr.setOnClickListener(v -> {
            giveAccess(db, user, 24 * 60 * 60 * 1000L, position, context, "24 hours");
            dialog.dismiss();
        });

        btnPermanent.setOnClickListener(v -> {
            giveAccess(db, user, -1, position, context, "Permanent");
            dialog.dismiss();
        });

        btnRevoke.setOnClickListener(v -> {
            revokeAccess(db, user, position, context);
            dialog.dismiss();
        });
    }

    /**
     * ✅ Grant access and refresh only affected item
     */
    private void giveAccess(FirebaseFirestore db, UserAccessModel user, long durationMs,
                            int position, Context context, String label) {
        String userId = user.getUserId();
        if (userId == null || userId.trim().isEmpty()) {
            Toast.makeText(context, "Invalid user ID", Toast.LENGTH_SHORT).show();
            return;
        }

        long expiry = (durationMs == -1) ? -1 : System.currentTimeMillis() + durationMs;
        user.setNotesAccessExpiry(expiry);

        HashMap<String, Object> accessData = new HashMap<>();
        accessData.put("userId", userId);
        accessData.put("grantedAt", System.currentTimeMillis());
        accessData.put("duration", durationMs);
        accessData.put("notesAccessExpiry", expiry);

        db.collection("AccessControl")
                .document(userId)
                .set(accessData)
                .addOnSuccessListener(a -> db.collection("Users")
                        .document(userId)
                        .update("notesAccessExpiry", expiry)
                        .addOnSuccessListener(unused -> {
                            notifyItemChanged(position);
                            Toast.makeText(context, "Access granted (" + label + ")", Toast.LENGTH_SHORT).show();
                        })
                )
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void revokeAccess(FirebaseFirestore db, UserAccessModel user, int position, Context context) {
        String userId = user.getUserId();
        if (userId == null || userId.trim().isEmpty()) {
            Toast.makeText(context, "Invalid user ID", Toast.LENGTH_SHORT).show();
            return;
        }

        user.setNotesAccessExpiry(0);

        db.collection("AccessControl")
                .document(userId)
                .delete()
                .addOnSuccessListener(a -> db.collection("Users")
                        .document(userId)
                        .update("notesAccessExpiry", 0)
                        .addOnSuccessListener(unused -> {
                            notifyItemChanged(position);
                            Toast.makeText(context, "Access revoked", Toast.LENGTH_SHORT).show();
                        })
                )
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvUsername, tvEmail, accessStatus;
        ImageView ivAvatar;

        VH(View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.username_txt);
            tvEmail = itemView.findViewById(R.id.useremail_txt);
            ivAvatar = itemView.findViewById(R.id.pic_profile);
            accessStatus = itemView.findViewById(R.id.access_status);
        }
    }
}
