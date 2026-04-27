package com.example.gossipapp;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.firebase.messaging.FirebaseMessaging;

public class NotesFragment extends Fragment {

    private FloatingActionButton AddNote_Btn;
    private RecyclerView recyclerView;
    private NotesAdapter notesAdapter;
    private List<NoteModel> notesList;
    private FirebaseFirestore db;
    TextView searchBtn;
    EditText searchEdit;
    private List<ContactsContract.CommonDataKinds.Note> noteList = new ArrayList<>(); // all notes
    private List<ContactsContract.CommonDataKinds.Note> filteredList = new ArrayList<>();

    private ActivityResultLauncher<Intent> pickFileLauncher;
    private UserModel currentUser;
    FirebaseUser user;
    ImageView ivMasterControl;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notes, container, false);
        AddNote_Btn = view.findViewById(R.id.btnAddNote);
        recyclerView = view.findViewById(R.id.recyclerNotes);

        notesList = new ArrayList<>();
        notesAdapter = new NotesAdapter(requireContext(), notesList, currentUser);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(notesAdapter);
        searchBtn = view.findViewById(R.id.search_btn);
        searchEdit = view.findViewById(R.id.search_user_edt);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        ivMasterControl = view.findViewById(R.id.ivMasterControl);
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && "markanaumang@gmail.com".equalsIgnoreCase(user.getEmail())) {
            ivMasterControl.setVisibility(View.VISIBLE); // make sure it's visible first
            ivMasterControl.setAlpha(0f); // start transparent
            ivMasterControl.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .start(); // fade in
        } else {
            ivMasterControl.setVisibility(View.GONE); // hide for other users
        }

        ivMasterControl.setOnClickListener(v -> {
            // open master control fragment
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.frame_full, new MasterControlFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        db = FirebaseFirestore.getInstance();
        FirebaseMessaging.getInstance().subscribeToTopic("all");
        FirebaseMessaging.getInstance().subscribeToTopic("all");

        // Initialize file picker launcher
        pickFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri uri = result.getData().getData();
                        if (uri != null)
                            showUploadDialog(uri);
                    }
                });

        AddNote_Btn.setOnClickListener(v -> openFilePicker());
        searchBtn.setOnClickListener(v -> {
            if (searchEdit.getVisibility() == View.GONE) {
                // Show with animation
                searchEdit.setVisibility(View.VISIBLE);
                searchEdit.setAlpha(0f);
                searchEdit.setTranslationX(100f);
                searchEdit.animate()
                        .alpha(1f)
                        .translationX(0f)
                        .setDuration(300)
                        .start();
            } else {
                // Hide with animation
                searchEdit.animate()
                        .alpha(0f)
                        .translationX(100f)
                        .setDuration(200)
                        .withEndAction(() -> searchEdit.setVisibility(View.GONE))
                        .start();
            }
        });
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        fetchNotesFromFirestore();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("Users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                currentUser = doc.toObject(UserModel.class);
                // now set adapter
                notesAdapter = new NotesAdapter(requireContext(), notesList, currentUser);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(notesAdapter);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchNotesFromFirestore();
        });

        return view;
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] { "application/pdf", "image/*" });
        pickFileLauncher.launch(intent);
    }

    private void showUploadDialog(Uri fileUri) {

        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_upload_note);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);

        TextView tvFileName = dialog.findViewById(R.id.tvFileName);
        ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
        TextView tvProgress = dialog.findViewById(R.id.tvProgress);
        TextView btnUpload = dialog.findViewById(R.id.btnUpload);
        TextView btnCancel = dialog.findViewById(R.id.btnCancel);

        tvFileName.setText(fileUri.getLastPathSegment());
        progressBar.setProgress(0);
        tvProgress.setText("0%");

        btnUpload.setOnClickListener(v -> {
            uploadFileWithProgress(fileUri, progressBar, tvProgress, dialog);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void uploadFileWithProgress(
            Uri uri,
            ProgressBar progressBar,
            TextView tvProgress,
            Dialog dialog) {
        new Thread(() -> {
            try {
                String mimeType = getContext().getContentResolver().getType(uri);
                String title = getFileName(uri);
                InputStream inputStream = getContext().getContentResolver().openInputStream(uri);

                // Date & Time
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                String uploadDate = dateFormat.format(new Date());
                String uploadTime = timeFormat.format(new Date());

                String ownerName = currentUser != null ? currentUser.getUsername() : "Unknown";

                byte[] fileBytes;
                boolean isPdf = mimeType != null && mimeType.equals("application/pdf");
                boolean isImage = mimeType != null && mimeType.startsWith("image");

                if (isPdf) {

                    // Create doc first (metadata only)
                    DocumentReference noteRef = db.collection("notes").document();

                    HashMap<String, Object> note = new HashMap<>();
                    note.put("title", title);
                    note.put("type", "pdf");
                    note.put("ownerId", currentUser != null ? currentUser.getUserId() : "user123");
                    note.put("ownerName", ownerName);
                    note.put("uploadDate", uploadDate);
                    note.put("uploadTime", uploadTime);
                    note.put("createdAt", System.currentTimeMillis());

                    noteRef.set(note);

                    // STREAM PDF chunks instead of loading whole file
                    InputStream input = getContext().getContentResolver().openInputStream(uri);

                    byte[] buffer = new byte[700 * 1024]; // 700KB chunk
                    int bytesRead;
                    int index = 0;

                    while ((bytesRead = input.read(buffer)) != -1) {

                        // Only real bytes
                        byte[] chunk = new byte[bytesRead];
                        System.arraycopy(buffer, 0, chunk, 0, bytesRead);

                        String base64 = Base64.encodeToString(chunk, Base64.NO_WRAP);

                        HashMap<String, Object> chunkMap = new HashMap<>();
                        chunkMap.put("index", index);
                        chunkMap.put("data", base64);

                        // Upload chunk
                        int finalIndex = index;
                        int finalIndex1 = index;
                        noteRef.collection("chunks")
                                .document("chunk_" + index)
                                .set(chunkMap)
                                .addOnSuccessListener(a -> Log.d("PDF_CHUNK", "Chunk " + finalIndex + " uploaded"))
                                .addOnFailureListener(
                                        e -> Log.e("PDF_CHUNK_ERROR", "Failed at chunk " + finalIndex1, e));

                        index++;

                        // Progress animation
                        int progress = (index * 100) / 50; // rough estimate
                        getActivity().runOnUiThread(() -> {
                            progressBar.setProgress(progress);
                            tvProgress.setText(progress + "%");
                        });

                        Thread.sleep(60); // prevent Firestore overload
                    }

                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "PDF uploaded!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        fetchNotesFromFirestore();
                    });

                    return; // IMPORTANT: do not run old code

                } else if (isImage) {
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    fileBytes = baos.toByteArray();

                } else
                    return;

                // ---- CHUNKING PDF (up to 20MB supported) ----
                int totalSize = fileBytes.length;
                int chunkSize = 700 * 1024; // 700KB (Firestore-safe)
                int totalChunks = (int) Math.ceil(totalSize / (double) chunkSize);
                List<String> chunks = new ArrayList<>();

                int start = 0, chunkCount = 0;
                while (start < totalSize) {
                    int end = Math.min(start + chunkSize, totalSize);
                    byte[] chunk = new byte[end - start];
                    System.arraycopy(fileBytes, start, chunk, 0, end - start);
                    chunks.add(Base64.encodeToString(chunk, Base64.DEFAULT));

                    start += chunkSize;
                    chunkCount++;

                    int progress = (int) ((chunkCount / (float) totalChunks) * 100);

                    getActivity().runOnUiThread(() -> {
                        ValueAnimator animator = ValueAnimator.ofInt(progressBar.getProgress(), progress);
                        animator.setDuration(500);
                        animator.addUpdateListener(animation -> {
                            progressBar.setProgress((int) animation.getAnimatedValue());
                            tvProgress.setText(progressBar.getProgress() + "%");
                        });
                        animator.start();
                    });

                    Thread.sleep(100);
                }

                // --- META DATA FIRST ---
                HashMap<String, Object> note = new HashMap<>();
                note.put("title", title);
                note.put("type", isImage ? "image" : "pdf");
                note.put("ownerId", currentUser != null ? currentUser.getUserId() : "user123");
                note.put("ownerName", ownerName);
                note.put("uploadDate", uploadDate);
                note.put("uploadTime", uploadTime);
                note.put("createdAt", System.currentTimeMillis());
                if (isImage)
                    note.put("imageBase64", chunks.get(0));

                getActivity().runOnUiThread(() -> db.collection("notes")
                        .add(note)
                        .addOnSuccessListener(doc -> {

                            if (isPdf) {

                                FirebaseFirestore db2 = FirebaseFirestore.getInstance();

                                for (int i = 0; i < chunks.size(); i++) {

                                    HashMap<String, Object> chunkMap = new HashMap<>();
                                    chunkMap.put("index", i);
                                    chunkMap.put("data", chunks.get(i));

                                    int finalI = i;

                                    db2.collection("notes")
                                            .document(doc.getId())
                                            .collection("chunks")
                                            .document("chunk_" + i)
                                            .set(chunkMap)
                                            .addOnSuccessListener(
                                                    a -> Log.d("PDF_CHUNK", "Chunk " + finalI + " uploaded"))
                                            .addOnFailureListener(e -> {
                                                Log.e("PDF_CHUNK_ERROR", "Failed at chunk " + finalI, e);
                                                Toast.makeText(getContext(),
                                                        "Chunk " + finalI + " failed: " + e.getMessage(),
                                                        Toast.LENGTH_LONG).show();
                                            });
                                }
                            }

                            Toast.makeText(getContext(), "File uploaded!", Toast.LENGTH_SHORT).show();

                            // Notification sent via Cloud Function automatically

                            fetchNotesFromFirestore();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(
                                e -> Toast.makeText(getContext(), "Upload failed!", Toast.LENGTH_SHORT).show()));
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast
                        .makeText(getContext(), "Upload Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void fetchNotesFromFirestore() {
        db.collection("notes").orderBy("createdAt")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notesList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        NoteModel note = new NoteModel();
                        note.setId(doc.getId());
                        note.setTitle(doc.getString("title"));
                        note.setType(doc.getString("type"));
                        note.setImageBase64(doc.getString("imageBase64"));

                        // ✅ ADD THESE 3 LINES
                        note.setUploadDate(doc.getString("uploadDate"));
                        note.setUploadTime(doc.getString("uploadTime"));
                        note.setOwnerName(doc.getString("ownerName"));

                        notesList.add(note);
                    }
                    notesAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    private String getFileName(Uri uri) {
        if (uri == null)
            return "unknown";

        String result = null;
        if ("content".equals(uri.getScheme())) {
            Cursor cursor = null;
            try {
                cursor = getContext().getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }

        if (result == null) {
            String last = uri.getLastPathSegment();
            result = (last == null) ? "unknown" : last;
        }
        return result;
    }

    private void filterNotes(String query) {
        List<NoteModel> filteredList = new ArrayList<>();
        for (NoteModel note : notesList) {
            String title = note.getTitle() != null ? note.getTitle() : "";
            String type = note.getType() != null ? note.getType() : "";
            String chunks = note.getChunks() != null ? note.getChunks().toString() : "";

            if (title.toLowerCase().contains(query.toLowerCase()) ||
                    type.toLowerCase().contains(query.toLowerCase()) ||
                    chunks.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(note);
            }
        }

        notesAdapter.updateList(filteredList);
    }

    // sendNotification removed - handled by Cloud Function

}
