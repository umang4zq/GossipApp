package com.example.gossipapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gossipapp.Activity.ImageViewActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private static final String TAG = "NotesAdapter";
    private Context context;
    private List<NoteModel> notesList;
    private UserModel currentUser; // reference to logged-in user

    public NotesAdapter(Context context, List<NoteModel> notesList, UserModel currentUser) {
        this.context = context;
        this.notesList = notesList;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note_card, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteModel note = notesList.get(position);
        holder.tvTitle.setText(note.getTitle());

        // Set owner name safely
        if (holder.tvOwner != null) {
            String ownerName = currentUser != null ? currentUser.getUsername() : "Unknown";
            holder.tvOwner.setText("By: " + ownerName);
        }

        if ("image".equals(note.getType()) && note.getImageBase64() != null) {
            // 1. Decode for the thumbnail (Keep this part to show the small preview)
            byte[] decodedBytes = Base64.decode(note.getImageBase64(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            holder.imgIcon.setImageBitmap(bitmap);

            // 2. On Click: Save to file instead of passing the string
            holder.itemView.setOnClickListener(v -> {
                try {
                    // Create a temporary file in the cache directory
                    File imageFile = new File(context.getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");

                    // Write the decoded bytes directly to the file
                    FileOutputStream fos = new FileOutputStream(imageFile);
                    fos.write(decodedBytes);
                    fos.flush();
                    fos.close();

                    // 3. Pass the FILE PATH, not the image data
                    Intent intent = new Intent(context, ImageViewActivity.class);
                    intent.putExtra("image_path", imageFile.getAbsolutePath()); // Pass path, not Base64
                    context.startActivity(intent);

                } catch (Exception e) {
                    Log.e(TAG, "Error saving image to cache", e);
                    Toast.makeText(context, "Error opening image", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if ("pdf".equals(note.getType())) {

            holder.imgIcon.setImageResource(R.drawable.folder_icon);

            holder.itemView.setOnClickListener(v -> {

                // 🔥 SHOW LOADING OVERLAY
                Dialog loadingDialog = new Dialog(context);
                loadingDialog.setContentView(R.layout.pdf_loading_dialog);
                loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                loadingDialog.setCancelable(false);
                loadingDialog.show();

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("notes")
                        .document(note.getId())
                        .collection("chunks")
                        .orderBy("index")
                        .get()
                        .addOnSuccessListener(query -> {

                            if (query.isEmpty()) {
                                loadingDialog.dismiss();
                                Toast.makeText(context, "PDF not found", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            try {
                                File pdfFile = new File(
                                        context.getCacheDir(),
                                        "pdf_" + System.currentTimeMillis() + ".pdf"
                                );

                                FileOutputStream fos = new FileOutputStream(pdfFile);

                                // 🔥 STREAM chunks to file safely
                                for (DocumentSnapshot snap : query.getDocuments()) {
                                    String base64 = snap.getString("data");

                                    if (base64 == null || base64.isEmpty()) continue;

                                    fos.write(Base64.decode(base64, Base64.DEFAULT));
                                }

                                fos.flush();
                                fos.close();

                                loadingDialog.dismiss();

                                // OPEN PDF VIEWER
                                Intent intent = new Intent(context, PdfViewerActivity.class);
                                intent.putExtra("pdf_path", pdfFile.getAbsolutePath());
                                context.startActivity(intent);

                            } catch (Exception e) {
                                loadingDialog.dismiss();
                                Toast.makeText(context, "Failed to open PDF", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }

                        })
                        .addOnFailureListener(e -> {
                            loadingDialog.dismiss();
                            Toast.makeText(context, "Error loading PDF", Toast.LENGTH_SHORT).show();
                        });
            });
        }
        // Set date + time
        if (holder.tvDate != null) {
            String date = note.getUploadDate() != null ? note.getUploadDate() : "--";
            String time = note.getUploadTime() != null ? note.getUploadTime() : "--";
            holder.tvDate.setText(date + " • " + time);
        }

    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView tvTitle;
        TextView tvOwner;
        TextView tvDate;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgNoteIcon);
            tvTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvOwner = itemView.findViewById(R.id.tvNoteAddedBy);
            tvDate = itemView.findViewById(R.id.tvNoteDate);

        }
    }

    public void updateList(List<NoteModel> newList) {
        if (newList == null) return;
        this.notesList.clear();
        this.notesList.addAll(newList);
        notifyDataSetChanged();
    }

}
