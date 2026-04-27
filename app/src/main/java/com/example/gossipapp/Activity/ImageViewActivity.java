package com.example.gossipapp.Activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gossipapp.R; // Assuming R.layout.activity_image_view exists

import java.io.File;

public class ImageViewActivity extends AppCompatActivity {

    private static final String TAG = "ImageViewActivity";
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view); // Make sure this layout exists and is correct

        imageView = findViewById(R.id.fullImageView); // Make sure you have an ImageView with this ID

        // Retrieve the image file path from the Intent
        String imagePath = getIntent().getStringExtra("image_path");

        if (imagePath == null) {
            // Handle the case where no path was passed (e.g., direct launch, or bad Intent)
            Log.e(TAG, "Image path is missing from the Intent extras.");
            Toast.makeText(this, "Error: Image data not found.", Toast.LENGTH_LONG).show();
            finish(); // Close the activity
            return;
        }

        // Attempt to load the image from the file path
        File imgFile = new File(imagePath);

        if (imgFile.exists()) {
            try {
                // Decode the file into a Bitmap
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                if (myBitmap != null) {
                    imageView.setImageBitmap(myBitmap);
                } else {
                    // Bitmap failed to decode (file corrupt, bad format, etc.)
                    Toast.makeText(this, "Failed to decode image file.", Toast.LENGTH_LONG).show();
                    finish();
                }

            } catch (Exception e) {
                Log.e(TAG, "Error loading image from file: " + imagePath, e);
                Toast.makeText(this, "An unexpected error occurred while loading the image.", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            // The file doesn't exist (e.g., cache cleared, process killed)
            Log.e(TAG, "Image file not found at path: " + imagePath);
            Toast.makeText(this, "Image file not accessible. Please try again.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // CLEANUP: Attempt to delete the temporary image file when the activity is destroyed
        // This helps manage the app's cache size.
        String imagePath = getIntent().getStringExtra("image_path");
        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                // imgFile.delete(); // NOTE: You may choose to comment this out if you need the image to persist in the cache temporarily.
            }
        }
    }
}